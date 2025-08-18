package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.models.entities.*
import com.pucetec.expensemate.repositories.CreditCardRepository
import com.pucetec.expensemate.repositories.PaymentReminderRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

class ReminderServiceTest {

    private lateinit var creditCardRepository: CreditCardRepository
    private lateinit var reminderRepository: PaymentReminderRepository
    private lateinit var service: ReminderService

    @BeforeEach
    fun setUp() {
        creditCardRepository = mock(CreditCardRepository::class.java)
        reminderRepository = mock(PaymentReminderRepository::class.java)
        service = ReminderService(creditCardRepository, reminderRepository)
    }

    private fun user(id: Long = 1L, name: String = "Alex", email: String = "alex@example.com"): User =
        User(name = name, email = email).also { setId(it, id) }

    private fun creditCard(
        id: Long = 10L,
        owner: User = user(),
        name: String = "Visa",
        last4: String = "1234",
        cutDay: Int = 5,
        dueDay: Int = 20
    ): CreditCard =
        CreditCard(
            name = name,
            lastFourDigits = last4,
            courtDate = cutDay,
            maximumPaymentDate = dueDay,
            user = owner
        ).also { setId(it, id) }

    private fun pr(
        u: User,
        cc: CreditCard,
        cycle: String,
        type: ReminderType,
        status: ReminderStatus,
        date: LocalDate
    ): PaymentReminder =
        PaymentReminder(
            user = u,
            creditCard = cc,
            cycleYyyyMm = cycle,
            type = type,
            scheduledDate = date,
            status = status
        )

    private fun setId(target: Any, id: Long) {
        var c: Class<*>? = target.javaClass
        var f = c?.declaredFields?.find { it.name == "id" }
        while (f == null && c != null) {
            c = c.superclass
            f = c?.declaredFields?.find { it.name == "id" }
        }
        requireNotNull(f) { "Field id not found" }
        f.isAccessible = true
        f.set(target, id)
    }

    @Test
    fun should_create_on_due_and_escalate_previous_pending() {
        val u = user(1L)
        val cc = creditCard(id = 10L, owner = u, cutDay = 5, dueDay = 20)
        `when`(creditCardRepository.findAll()).thenReturn(listOf(cc))

        val date = LocalDate.of(2025, 8, 20)
        val cycle = YearMonth.from(date).toString()

        `when`(
            reminderRepository.existsByUserIdAndCreditCardIdAndCycleYyyyMmAndStatus(
                1L, 10L, cycle, ReminderStatus.CLEARED
            )
        ).thenReturn(false)

        val beforeDue = pr(u, cc, cycle, ReminderType.BEFORE_DUE, ReminderStatus.PENDING, date.minusDays(3))
        `when`(
            reminderRepository.findByUserIdAndCreditCardIdAndCycleYyyyMm(1L, 10L, cycle)
        ).thenReturn(mutableListOf(beforeDue))

        `when`(
            reminderRepository.existsByUserIdAndCreditCardIdAndCycleYyyyMmAndType(
                1L, 10L, cycle, ReminderType.ON_DUE
            )
        ).thenReturn(false)

        val captor = ArgumentCaptor.forClass(PaymentReminder::class.java)
        `when`(reminderRepository.save(any(PaymentReminder::class.java))).thenAnswer { it.arguments[0] }

        service.generateFor(date)

        assertEquals(ReminderStatus.SENT, beforeDue.status)

        verify(reminderRepository).saveAll(anyList())

        verify(reminderRepository).save(captor.capture())
        val saved = captor.value
        assertEquals(ReminderType.ON_DUE, saved.type)
        assertEquals(ReminderStatus.PENDING, saved.status)
        assertEquals(date, saved.scheduledDate)
        assertEquals(cycle, saved.cycleYyyyMm)

        verify(creditCardRepository).findAll()
        verify(reminderRepository).existsByUserIdAndCreditCardIdAndCycleYyyyMmAndStatus(1L, 10L, cycle, ReminderStatus.CLEARED)
        verify(reminderRepository).existsByUserIdAndCreditCardIdAndCycleYyyyMmAndType(1L, 10L, cycle, ReminderType.ON_DUE)
        verifyNoMoreInteractions(creditCardRepository)

    }

    @Test
    fun should_not_create_when_cycle_already_cleared() {
        val u = user(1L)
        val cc = creditCard(id = 10L, owner = u, cutDay = 5, dueDay = 20)
        `when`(creditCardRepository.findAll()).thenReturn(listOf(cc))

        val date = LocalDate.of(2025, 8, 20)
        val cycle = YearMonth.from(date).toString()

        `when`(
            reminderRepository.existsByUserIdAndCreditCardIdAndCycleYyyyMmAndStatus(
                1L, 10L, cycle, ReminderStatus.CLEARED
            )
        ).thenReturn(true)

        service.generateFor(date)

        verify(creditCardRepository).findAll()
        verify(reminderRepository).existsByUserIdAndCreditCardIdAndCycleYyyyMmAndStatus(1L, 10L, cycle, ReminderStatus.CLEARED)
        verifyNoMoreInteractions(reminderRepository, creditCardRepository)
    }

    @Test
    fun should_not_duplicate_if_type_already_exists() {
        val u = user(1L)
        val cc = creditCard(id = 10L, owner = u, cutDay = 5, dueDay = 20)
        `when`(creditCardRepository.findAll()).thenReturn(listOf(cc))

        val date = LocalDate.of(2025, 8, 6)
        val cycle = YearMonth.from(date).toString()

        `when`(
            reminderRepository.existsByUserIdAndCreditCardIdAndCycleYyyyMmAndStatus(
                1L, 10L, cycle, ReminderStatus.CLEARED
            )
        ).thenReturn(false)

        `when`(
            reminderRepository.findByUserIdAndCreditCardIdAndCycleYyyyMm(1L, 10L, cycle)
        ).thenReturn(mutableListOf())

        `when`(
            reminderRepository.existsByUserIdAndCreditCardIdAndCycleYyyyMmAndType(
                1L, 10L, cycle, ReminderType.AFTER_CUT
            )
        ).thenReturn(true)

        service.generateFor(date)

        verify(reminderRepository, never()).save(any(PaymentReminder::class.java))
        verify(reminderRepository, never()).saveAll(anyList())
    }

    @Test
    fun should_create_before_due_on_coerced_safe_date_end_of_february() {
        val u = user(1L)
        val cc = creditCard(id = 11L, owner = u, cutDay = 10, dueDay = 31)
        `when`(creditCardRepository.findAll()).thenReturn(listOf(cc))

        val ym = YearMonth.of(2025, 2)
        val dueCoerced = ym.atEndOfMonth()
        val date = dueCoerced.minusDays(3)

        `when`(
            reminderRepository.existsByUserIdAndCreditCardIdAndCycleYyyyMmAndStatus(
                1L, 11L, ym.toString(), ReminderStatus.CLEARED
            )
        ).thenReturn(false)

        `when`(
            reminderRepository.findByUserIdAndCreditCardIdAndCycleYyyyMm(1L, 11L, ym.toString())
        ).thenReturn(mutableListOf())

        `when`(
            reminderRepository.existsByUserIdAndCreditCardIdAndCycleYyyyMmAndType(
                1L, 11L, ym.toString(), ReminderType.BEFORE_DUE
            )
        ).thenReturn(false)

        val cap = ArgumentCaptor.forClass(PaymentReminder::class.java)
        `when`(reminderRepository.save(any(PaymentReminder::class.java))).thenAnswer { it.arguments[0] }

        service.generateFor(date)

        verify(reminderRepository).save(cap.capture())
        val saved = cap.value
        assertEquals(ReminderType.BEFORE_DUE, saved.type)
        assertEquals(date, saved.scheduledDate)
    }

    @Test
    fun should_delegate_generateDailyReminders() {
        `when`(creditCardRepository.findAll()).thenReturn(emptyList())
        service.generateDailyReminders()
        verify(creditCardRepository).findAll()
    }

    @Test
    fun should_get_pending_by_user() {
        val uId = 7L
        val rows = listOf<PaymentReminder>()
        `when`(reminderRepository.findByUserIdAndStatus(uId, ReminderStatus.PENDING)).thenReturn(rows)

        val result = service.getPendingByUser(uId)

        assertSame(rows, result)
        verify(reminderRepository).findByUserIdAndStatus(uId, ReminderStatus.PENDING)
        verifyNoMoreInteractions(reminderRepository)
    }

    @Test
    fun should_mark_cycle_paid_creating_cleared_when_empty() {
        val u = user(1L)
        val cc = creditCard(id = 22L, owner = u, cutDay = 5, dueDay = 20)
        val cycle = "2025-08"

        `when`(
            reminderRepository.findByUserIdAndCreditCardIdAndCycleYyyyMm(1L, 22L, cycle)
        ).thenReturn(emptyList())

        `when`(creditCardRepository.findById(22L)).thenReturn(Optional.of(cc))

        val cap = ArgumentCaptor.forClass(PaymentReminder::class.java)
        `when`(reminderRepository.save(any(PaymentReminder::class.java))).thenAnswer { it.arguments[0] }

        service.markCyclePaid(1L, 22L, cycle)

        verify(reminderRepository).save(cap.capture())
        val saved = cap.value
        assertEquals(ReminderStatus.CLEARED, saved.status)
        assertEquals(ReminderType.AFTER_CUT, saved.type)
        assertEquals(YearMonth.parse(cycle).atEndOfMonth(), saved.scheduledDate)
    }

    @Test
    fun should_throw_when_mark_cycle_paid_and_card_not_found() {
        val cycle = "2025-08"
        `when`(
            reminderRepository.findByUserIdAndCreditCardIdAndCycleYyyyMm(1L, 999L, cycle)
        ).thenReturn(emptyList())
        `when`(creditCardRepository.findById(999L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            service.markCyclePaid(1L, 999L, cycle)
        }

        verify(creditCardRepository).findById(999L)
        verify(reminderRepository, never()).save(any(PaymentReminder::class.java))
    }

    @Test
    fun should_mark_cycle_paid_setting_all_rows_to_cleared() {
        val u = user(1L)
        val cc = creditCard(id = 33L, owner = u)
        val cycle = "2025-08"

        val r1 = pr(u, cc, cycle, ReminderType.AFTER_CUT, ReminderStatus.PENDING, LocalDate.of(2025,8,6))
        val r2 = pr(u, cc, cycle, ReminderType.BEFORE_DUE, ReminderStatus.SENT, LocalDate.of(2025,8,17))

        val rows = mutableListOf(r1, r2)
        `when`(reminderRepository.findByUserIdAndCreditCardIdAndCycleYyyyMm(1L, 33L, cycle))
            .thenReturn(rows)

        service.markCyclePaid(1L, 33L, cycle)

        assertEquals(ReminderStatus.CLEARED, r1.status)
        assertEquals(ReminderStatus.CLEARED, r2.status)
        verify(reminderRepository).saveAll(rows)
        verify(reminderRepository).findByUserIdAndCreditCardIdAndCycleYyyyMm(1L, 33L, cycle)
        verify(reminderRepository).saveAll(rows)
    }

    @Test
    fun should_not_escalate_when_existing_is_not_pending() {

        val u = user(1L)
        val cc = creditCard(id = 44L, owner = u, cutDay = 5, dueDay = 20)
        `when`(creditCardRepository.findAll()).thenReturn(listOf(cc))

        val date = LocalDate.of(2025, 8, 20)
        val cycle = YearMonth.from(date).toString()

        `when`(
            reminderRepository.existsByUserIdAndCreditCardIdAndCycleYyyyMmAndStatus(
                1L, 44L, cycle, ReminderStatus.CLEARED
            )
        ).thenReturn(false)

        val existing = pr(u, cc, cycle, ReminderType.BEFORE_DUE, ReminderStatus.SENT, date.minusDays(3))
        val rows = mutableListOf(existing)
        `when`(reminderRepository.findByUserIdAndCreditCardIdAndCycleYyyyMm(1L, 44L, cycle)).thenReturn(rows)

        `when`(
            reminderRepository.existsByUserIdAndCreditCardIdAndCycleYyyyMmAndType(
                1L, 44L, cycle, ReminderType.ON_DUE
            )
        ).thenReturn(false)

        val cap = ArgumentCaptor.forClass(PaymentReminder::class.java)
        `when`(reminderRepository.save(any(PaymentReminder::class.java))).thenAnswer { it.arguments[0] }

        service.generateFor(date)

        assertEquals(ReminderStatus.SENT, existing.status)

        verify(reminderRepository).saveAll(rows)

        verify(reminderRepository).save(cap.capture())
        assertEquals(ReminderType.ON_DUE, cap.value.type)
    }

    @Test
    fun should_not_escalate_when_existing_has_higher_or_equal_order() {

        val u = user(1L)
        val cc = creditCard(id = 55L, owner = u, cutDay = 5, dueDay = 20)
        `when`(creditCardRepository.findAll()).thenReturn(listOf(cc))

        val ym = YearMonth.of(2025, 8)
        val due = LocalDate.of(2025, 8, 20)
        val beforeDueDate = due.minusDays(3)
        val cycle = ym.toString()

        `when`(
            reminderRepository.existsByUserIdAndCreditCardIdAndCycleYyyyMmAndStatus(
                1L, 55L, cycle, ReminderStatus.CLEARED
            )
        ).thenReturn(false)

        val existingOnDue = pr(u, cc, cycle, ReminderType.ON_DUE, ReminderStatus.PENDING, due)
        val rows = mutableListOf(existingOnDue)
        `when`(reminderRepository.findByUserIdAndCreditCardIdAndCycleYyyyMm(1L, 55L, cycle)).thenReturn(rows)

        `when`(
            reminderRepository.existsByUserIdAndCreditCardIdAndCycleYyyyMmAndType(
                1L, 55L, cycle, ReminderType.BEFORE_DUE
            )
        ).thenReturn(false)

        val cap = ArgumentCaptor.forClass(PaymentReminder::class.java)
        `when`(reminderRepository.save(any(PaymentReminder::class.java))).thenAnswer { it.arguments[0] }

        service.generateFor(beforeDueDate)

        assertEquals(ReminderStatus.PENDING, existingOnDue.status)

        verify(reminderRepository).saveAll(rows)
        verify(reminderRepository).save(cap.capture())
        assertEquals(ReminderType.BEFORE_DUE, cap.value.type)
        assertEquals(beforeDueDate, cap.value.scheduledDate)
    }
}

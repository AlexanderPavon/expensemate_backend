package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.models.entities.*
import com.pucetec.expensemate.repositories.CreditCardRepository
import com.pucetec.expensemate.repositories.PaymentReminderRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class ReminderService(
    private val creditCardRepository: CreditCardRepository,
    private val reminderRepository: PaymentReminderRepository
) {
    private val order = mapOf(
        ReminderType.AFTER_CUT to 1,
        ReminderType.BEFORE_DUE to 2,
        ReminderType.ON_DUE to 3
    )

    fun generateFor(date: LocalDate) {
        val cards = creditCardRepository.findAll()

        cards.forEach { card ->
            val userId = card.user.id ?: return@forEach

            val cutDay = card.courtDate
            val dueDay = card.maximumPaymentDate

            val ym = YearMonth.of(date.year, date.month)
            val cutDate = safeDate(ym, cutDay)
            val dueDate = safeDate(ym, dueDay)
            val cycle = ym.toString()

            if (reminderRepository.existsByUserIdAndCreditCardIdAndCycleYyyyMmAndStatus(
                    userId, card.id!!, cycle, ReminderStatus.CLEARED
                )
            ) return@forEach

            listOf(
                ReminderType.AFTER_CUT to cutDate.plusDays(1),
                ReminderType.BEFORE_DUE to dueDate.minusDays(3),
                ReminderType.ON_DUE to dueDate
            ).forEach { (type, scheduled) ->
                if (scheduled == date) {
                    escalatePending(userId, card.id!!, cycle, type)

                    if (!reminderRepository.existsByUserIdAndCreditCardIdAndCycleYyyyMmAndType(
                            userId, card.id!!, cycle, type
                        )
                    ) {
                        reminderRepository.save(
                            PaymentReminder(
                                user = card.user,
                                creditCard = card,
                                cycleYyyyMm = cycle,
                                type = type,
                                scheduledDate = scheduled,
                                status = ReminderStatus.PENDING
                            )
                        )
                    }
                }
            }
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    fun generateDailyReminders() = generateFor(LocalDate.now())

    private fun escalatePending(userId: Long, ccId: Long, cycle: String, newType: ReminderType) {
        val rows = reminderRepository.findByUserIdAndCreditCardIdAndCycleYyyyMm(userId, ccId, cycle)
        rows.filter { it.status == ReminderStatus.PENDING && order[it.type]!! < order[newType]!! }
            .forEach { it.status = ReminderStatus.SENT }
        if (rows.isNotEmpty()) reminderRepository.saveAll(rows)
    }

    fun getPendingByUser(userId: Long) =
        reminderRepository.findByUserIdAndStatus(userId, ReminderStatus.PENDING)

    fun markCyclePaid(userId: Long, ccId: Long, cycle: String) {
        val rows = reminderRepository.findByUserIdAndCreditCardIdAndCycleYyyyMm(userId, ccId, cycle)
        if (rows.isEmpty()) {
            val card = creditCardRepository.findById(ccId)
                .orElseThrow { ResourceNotFoundException("Credit card not found") }
            reminderRepository.save(
                PaymentReminder(
                    user = card.user,
                    creditCard = card,
                    cycleYyyyMm = cycle,
                    type = ReminderType.AFTER_CUT,
                    scheduledDate = YearMonth.parse(cycle).atEndOfMonth(),
                    status = ReminderStatus.CLEARED
                )
            )
            return
        }
        rows.forEach { it.status = ReminderStatus.CLEARED }
        reminderRepository.saveAll(rows)
    }

    private fun safeDate(ym: YearMonth, day: Int): LocalDate =
        ym.atDay(day.coerceIn(1, ym.lengthOfMonth()))
}

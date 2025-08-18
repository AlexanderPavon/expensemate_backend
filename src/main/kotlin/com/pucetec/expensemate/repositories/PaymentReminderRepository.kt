package com.pucetec.expensemate.repositories

import com.pucetec.expensemate.models.entities.PaymentReminder
import com.pucetec.expensemate.models.entities.ReminderStatus
import com.pucetec.expensemate.models.entities.ReminderType
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface PaymentReminderRepository : JpaRepository<PaymentReminder, Long> {
    fun findByUserIdAndStatus(userId: Long, status: ReminderStatus): List<PaymentReminder>
    fun findByUserIdAndCreditCardIdAndCycleYyyyMm(userId: Long, ccId: Long, cycle: String): List<PaymentReminder>

    fun existsByUserIdAndCreditCardIdAndCycleYyyyMmAndType(
        userId: Long, ccId: Long, cycle: String, type: ReminderType
    ): Boolean

    fun existsByUserIdAndCreditCardIdAndCycleYyyyMmAndStatus(
        userId: Long, ccId: Long, cycle: String, status: ReminderStatus
    ): Boolean

    fun findByScheduledDateAndStatus(date: LocalDate, status: ReminderStatus): List<PaymentReminder>
}

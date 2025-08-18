package com.pucetec.expensemate.models.entities

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "payment_reminders",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_reminder_unique",
            columnNames = ["user_id", "credit_card_id", "cycle_yyyy_mm", "type"]
        )
    ]
)
data class PaymentReminder(
    @ManyToOne @JoinColumn(name = "user_id") val user: User,
    @ManyToOne @JoinColumn(name = "credit_card_id") val creditCard: CreditCard,

    @Column(name = "cycle_yyyy_mm") val cycleYyyyMm: String,
    @Enumerated(EnumType.STRING) val type: ReminderType,

    @Column(name = "scheduled_date") val scheduledDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "status") var status: ReminderStatus = ReminderStatus.PENDING
) : BaseEntity()

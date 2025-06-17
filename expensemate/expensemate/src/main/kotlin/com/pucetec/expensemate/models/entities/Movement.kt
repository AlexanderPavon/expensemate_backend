package com.pucetec.expensemate.models.entities

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "movements")
data class Movement(
    var type: String, // "ingreso" o "egreso"
    var amount: Double,
    var date: LocalDate,
    var note: String? = null, // opcional

    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne
    @JoinColumn(name = "category_id")
    var category: Category,

    @ManyToOne
    @JoinColumn(name = "credit_card_id")
    var creditCard: CreditCard? = null, // opcional

    @ManyToOne
    @JoinColumn(name = "account_id")
    var account: Account? = null // opcional
): BaseEntity()

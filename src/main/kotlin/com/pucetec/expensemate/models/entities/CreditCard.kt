package com.pucetec.expensemate.models.entities

import jakarta.persistence.*

@Entity
@Table(name = "credit_cards")
data class CreditCard(
    var name: String,

    @Column(name = "last_four_digits")
    var lastFourDigits: String,

    @Column(name = "court_date")
    var courtDate: Int,

    @Column(name = "maximum_payment_date")
    var maximumPaymentDate: Int,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User
) : BaseEntity()

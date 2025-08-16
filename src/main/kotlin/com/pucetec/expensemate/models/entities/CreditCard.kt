package com.pucetec.expensemate.models.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import jakarta.persistence.ManyToOne

@Entity
@Table(name = "credit_cards")
data class CreditCard(
    var name: String,

    @Column(name = "last_four_digits")
    var lastFourDigits: String,

    @Column(name = "court_date")
    var courtDate: String,

    @Column(name = "maximum_payment_date")
    var maximumPaymentDate: String,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User
): BaseEntity()

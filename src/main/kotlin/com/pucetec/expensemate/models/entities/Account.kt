package com.pucetec.expensemate.models.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.ManyToOne
import jakarta.persistence.JoinColumn
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.FetchType

@Entity
@Table(name = "accounts")
data class Account(

    @Column(nullable = false)
    var bank: String,

    @Column(name = "account_number", nullable = false, unique = true)
    var accountNumber: String,

    @Column(nullable = false)
    var balance: Double = 0.0,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User

) : BaseEntity() {

    @PrePersist
    @PreUpdate
    fun normalize() {
        bank = bank.trim()
        accountNumber = accountNumber.trim()
        accountNumber = accountNumber.replace(Regex("[\\s-]"), "")
    }
}

package com.pucetec.expensemate.models.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.ManyToOne

@Entity
@Table(name = "accounts")
data class Account(
    var bank: String,

    @Column(name = "account_number")
    var accountNumber: String,

    @ManyToOne
    val user: User
): BaseEntity()

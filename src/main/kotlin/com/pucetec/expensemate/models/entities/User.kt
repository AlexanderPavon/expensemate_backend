package com.pucetec.expensemate.models.entities

import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "users")
data class User(
    var name: String,
    var email: String,

    @OneToMany(mappedBy = "user")
    val movements: List<Movement> = emptyList(),

    @OneToMany(mappedBy = "user")
    val creditCards: List<CreditCard> = emptyList(),

    @OneToMany(mappedBy = "user")
    val accounts: List<Account> = emptyList()
): BaseEntity()
package com.pucetec.expensemate.models.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table

@Entity
@Table(name = "users")
data class User(

    @Column(nullable = false)
    var name: String,
    @Column(nullable = false, unique = true)
    var email: String,

    @OneToMany(mappedBy = "user")
    val movements: List<Movement> = emptyList(),

    @OneToMany(mappedBy = "user")
    val creditCards: List<CreditCard> = emptyList(),

    @OneToMany(mappedBy = "user")
    val accounts: List<Account> = emptyList()
): BaseEntity() {
    @PrePersist
    @PreUpdate
    fun normalize() {
        name = name.trim()
        email = email.trim().lowercase()
    }
}
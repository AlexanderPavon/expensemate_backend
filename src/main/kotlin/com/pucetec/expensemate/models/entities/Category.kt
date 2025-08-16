package com.pucetec.expensemate.models.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table

@Entity
@Table(name = "categories")
data class Category(

    @Column(nullable = false, unique = true)
    var name: String

) : BaseEntity() {

    @PrePersist
    @PreUpdate
    fun normalize() {
        name = name.trim().uppercase()
    }
}

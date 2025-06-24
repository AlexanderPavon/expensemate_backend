package com.pucetec.expensemate.models.entities

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set

    @Column(nullable = true)
    var deletedAt: LocalDateTime? = null
        protected set

    @PreUpdate
    protected fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun markAsDeleted() {
        deletedAt = LocalDateTime.now()
    }

    fun isDeleted(): Boolean {
        return deletedAt != null
    }
}
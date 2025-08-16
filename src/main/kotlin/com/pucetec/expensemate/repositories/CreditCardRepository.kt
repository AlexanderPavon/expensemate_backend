package com.pucetec.expensemate.repositories

import com.pucetec.expensemate.models.entities.CreditCard
import org.springframework.data.jpa.repository.JpaRepository

interface CreditCardRepository : JpaRepository<CreditCard, Long> {
    fun findAllByUserId(userId: Long): List<CreditCard>
}

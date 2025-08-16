package com.pucetec.expensemate.repositories

import com.pucetec.expensemate.models.entities.Movement
import org.springframework.data.jpa.repository.JpaRepository

interface MovementRepository : JpaRepository<Movement, Long> {
    fun findAllByUserId(userId: Long): List<Movement>
    fun findAllByUserIdAndCategoryId(userId: Long, categoryId: Long): List<Movement>
}

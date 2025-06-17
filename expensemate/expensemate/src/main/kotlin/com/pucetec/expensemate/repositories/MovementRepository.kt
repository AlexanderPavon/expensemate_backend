package com.pucetec.expensemate.repositories

import com.pucetec.expensemate.models.entities.Movement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MovementRepository: JpaRepository<Movement, Long>
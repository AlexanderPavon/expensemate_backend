package com.pucetec.expensemate.repositories

import com.pucetec.expensemate.models.entities.CreditCard
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CreditCardRepository: JpaRepository<CreditCard, Long>
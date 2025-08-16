package com.pucetec.expensemate.repositories

import com.pucetec.expensemate.models.entities.Account
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<Account, Long> {
    fun existsByAccountNumber(accountNumber: String): Boolean
    fun findByAccountNumber(accountNumber: String): Account?
    fun findAllByUserId(userId: Long): List<Account>
}

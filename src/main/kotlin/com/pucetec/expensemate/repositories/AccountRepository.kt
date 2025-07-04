package com.pucetec.expensemate.repositories

import com.pucetec.expensemate.models.entities.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository: JpaRepository<Account, Long>
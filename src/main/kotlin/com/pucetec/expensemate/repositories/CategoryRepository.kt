package com.pucetec.expensemate.repositories

import com.pucetec.expensemate.models.entities.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository: JpaRepository<Category, Long>
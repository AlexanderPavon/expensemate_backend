package com.pucetec.expensemate.models.entities

import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "categories")
data class Category(
    var name: String
): BaseEntity()

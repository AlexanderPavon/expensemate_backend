package com.pucetec.expensemate.models.requests

import com.pucetec.expensemate.models.entities.Category

data class CreateCategoryRequest(
    val name: String
){
    fun toEntity(): Category {
        return Category(
            name = this.name
        )
    }
}

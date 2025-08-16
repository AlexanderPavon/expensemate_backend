package com.pucetec.expensemate.models.requests

import com.pucetec.expensemate.models.entities.Category
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.PropertyNamingStrategies

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateCategoryRequest(
    val name: String
){
    fun toEntity(): Category {
        return Category(
            name = this.name
        )
    }
}

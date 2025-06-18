package com.pucetec.expensemate.mappers

import com.pucetec.expensemate.models.entities.Category
import com.pucetec.expensemate.models.responses.CategoryResponse
import org.springframework.stereotype.Component

@Component
class CategoryMapper {
    fun toResponse(category: Category): CategoryResponse {
        return CategoryResponse(
            id = category.id,
            name = category.name
        )
    }
}

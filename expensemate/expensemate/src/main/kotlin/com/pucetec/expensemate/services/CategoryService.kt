package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.mappers.CategoryMapper
import com.pucetec.expensemate.models.requests.CreateCategoryRequest
import com.pucetec.expensemate.models.responses.CategoryResponse
import com.pucetec.expensemate.repositories.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val categoryMapper: CategoryMapper
) {
    fun createCategory(request: CreateCategoryRequest): CategoryResponse {
        val category = categoryRepository.save(request.toEntity())
        return categoryMapper.toResponse(category)
    }

    fun getAllCategories(): List<CategoryResponse> =
        categoryRepository.findAll().map { categoryMapper.toResponse(it) }

    fun getCategoryById(id: Long): CategoryResponse =
        categoryMapper.toResponse(
            categoryRepository.findById(id).orElseThrow {
                ResourceNotFoundException("Category with ID $id not found")
            }
        )

    fun updateCategory(id: Long, request: CreateCategoryRequest): CategoryResponse {
        val category = categoryRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Category with ID $id not found")
        }
        category.name = request.name
        return categoryMapper.toResponse(categoryRepository.save(category))
    }

    fun deleteCategory(id: Long) {
        val category = categoryRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Category with ID $id not found")
        }
        categoryRepository.delete(category)
    }
}
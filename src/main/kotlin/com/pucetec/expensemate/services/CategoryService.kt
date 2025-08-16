package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.DuplicateResourceException
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
        val name = request.name.trim()

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw DuplicateResourceException("The category '$name' already exists")
        }

        val entity = request.toEntity().apply { this.name = name }

        val saved = categoryRepository.save(entity)
        return categoryMapper.toResponse(saved)
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

        val name = request.name.trim()

        val existing = categoryRepository.findByNameIgnoreCase(name)
        if (existing != null && existing.id != category.id) {
            throw DuplicateResourceException("The category '$name' already exists")
        }

        category.name = name
        val saved = categoryRepository.save(category)
        return categoryMapper.toResponse(saved)
    }

    fun deleteCategory(id: Long) {
        val category = categoryRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Category with ID $id not found")
        }
        categoryRepository.delete(category)
    }
}

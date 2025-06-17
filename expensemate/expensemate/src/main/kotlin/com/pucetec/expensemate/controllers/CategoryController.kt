package com.pucetec.expensemate.controllers

import com.pucetec.expensemate.models.requests.CreateCategoryRequest
import com.pucetec.expensemate.models.responses.CategoryResponse
import com.pucetec.expensemate.routes.Routes
import com.pucetec.expensemate.services.CategoryService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Routes.CATEGORIES)
class CategoryController(
    private val categoryService: CategoryService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCategory(@RequestBody request: CreateCategoryRequest): CategoryResponse =
        categoryService.createCategory(request)

    @GetMapping
    fun getAllCategories(): List<CategoryResponse> = categoryService.getAllCategories()

    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: Long): CategoryResponse =
        categoryService.getCategoryById(id)

    @PutMapping("/{id}")
    fun updateCategory(@PathVariable id: Long, @RequestBody request: CreateCategoryRequest): CategoryResponse =
        categoryService.updateCategory(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCategory(@PathVariable id: Long) = categoryService.deleteCategory(id)
}
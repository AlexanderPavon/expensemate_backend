package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.mappers.CategoryMapper
import com.pucetec.expensemate.models.entities.Category
import com.pucetec.expensemate.models.requests.CreateCategoryRequest
import com.pucetec.expensemate.models.responses.CategoryResponse
import com.pucetec.expensemate.repositories.CategoryRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.*

class CategoryServiceTest {

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var categoryMapper: CategoryMapper
    private lateinit var categoryService: CategoryService

    @BeforeEach
    fun setUp() {
        categoryRepository = mock(CategoryRepository::class.java)
        categoryMapper = mock(CategoryMapper::class.java)
        categoryService = CategoryService(categoryRepository, categoryMapper)
    }

    @Test
    fun should_create_a_new_category() {
        val request = CreateCategoryRequest("Hogar")
        val category = Category(name = request.name)
        val response = CategoryResponse(1L, "Hogar")

        `when`(categoryRepository.save(any(Category::class.java))).thenReturn(category)
        `when`(categoryMapper.toResponse(category)).thenReturn(response)

        val result = categoryService.createCategory(request)

        assertEquals("Hogar", result.name)
    }

    @Test
    fun should_return_all_categories() {
        val category = Category(name = "Salud")
        val response = CategoryResponse(1L, "Salud")

        `when`(categoryRepository.findAll()).thenReturn(listOf(category))
        `when`(categoryMapper.toResponse(category)).thenReturn(response)

        val result = categoryService.getAllCategories()

        assertEquals(1, result.size)
        assertEquals("Salud", result[0].name)
    }

    @Test
    fun should_return_category_by_id() {
        val category = Category(name = "Educación")
        val response = CategoryResponse(1L, "Educación")

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category))
        `when`(categoryMapper.toResponse(category)).thenReturn(response)

        val result = categoryService.getCategoryById(1L)

        assertEquals("Educación", result.name)
    }

    @Test
    fun should_throw_exception_when_category_by_id_not_found() {
        `when`(categoryRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            categoryService.getCategoryById(1L)
        }
    }

    @Test
    fun should_update_category() {
        val existingCategory = Category(name = "Viejo")
        val request = CreateCategoryRequest("Nuevo")
        val updatedCategory = Category(name = "Nuevo")
        val response = CategoryResponse(1L, "Nuevo")

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory))
        `when`(categoryRepository.save(existingCategory)).thenReturn(updatedCategory)
        `when`(categoryMapper.toResponse(updatedCategory)).thenReturn(response)

        val result = categoryService.updateCategory(1L, request)

        assertEquals("Nuevo", result.name)
    }

    @Test
    fun should_throw_exception_when_updating_non_existent_category() {
        val request = CreateCategoryRequest("Nueva")

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            categoryService.updateCategory(1L, request)
        }
    }

    @Test
    fun should_delete_category() {
        val category = Category(name = "Eliminar")

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category))

        categoryService.deleteCategory(1L)

        verify(categoryRepository).delete(category)
    }

    @Test
    fun should_throw_exception_when_deleting_non_existent_category() {
        `when`(categoryRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            categoryService.deleteCategory(1L)
        }
    }
}

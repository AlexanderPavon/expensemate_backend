package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.DuplicateResourceException
import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.mappers.CategoryMapper
import com.pucetec.expensemate.models.entities.Category
import com.pucetec.expensemate.models.requests.CreateCategoryRequest
import com.pucetec.expensemate.models.responses.CategoryResponse
import com.pucetec.expensemate.repositories.CategoryRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
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
    fun should_create_a_new_category_trimming_name() {
        val request = CreateCategoryRequest("  Hogar  ")
        val saved = Category(name = "Hogar").also { setId(it, 1L) }
        val response = CategoryResponse(1L, "Hogar")

        `when`(categoryRepository.existsByNameIgnoreCase("Hogar")).thenReturn(false)
        `when`(categoryRepository.save(any(Category::class.java))).thenReturn(saved)
        `when`(categoryMapper.toResponse(saved)).thenReturn(response)

        val result = categoryService.createCategory(request)

        assertEquals(1L, result.id)
        assertEquals("Hogar", result.name)

        val captor = ArgumentCaptor.forClass(Category::class.java)
        verify(categoryRepository).save(captor.capture())
        assertEquals("Hogar", captor.value.name)

        verify(categoryRepository).existsByNameIgnoreCase("Hogar")
        verify(categoryMapper).toResponse(saved)
        verifyNoMoreInteractions(categoryRepository, categoryMapper)
    }

    @Test
    fun should_throw_duplicate_on_create_when_name_already_exists_case_insensitive() {
        val request = CreateCategoryRequest("hogar")

        `when`(categoryRepository.existsByNameIgnoreCase("hogar".trim())).thenReturn(true)

        assertThrows<DuplicateResourceException> {
            categoryService.createCategory(request)
        }

        verify(categoryRepository).existsByNameIgnoreCase("hogar")
        verifyNoMoreInteractions(categoryRepository)
        verifyNoInteractions(categoryMapper)
    }

    @Test
    fun should_return_all_categories() {
        val c1 = Category(name = "Salud").also { setId(it, 1L) }
        val r1 = CategoryResponse(1L, "Salud")

        `when`(categoryRepository.findAll()).thenReturn(listOf(c1))
        `when`(categoryMapper.toResponse(c1)).thenReturn(r1)

        val result = categoryService.getAllCategories()

        assertEquals(1, result.size)
        assertEquals("Salud", result[0].name)

        verify(categoryRepository).findAll()
        verify(categoryMapper).toResponse(c1)
        verifyNoMoreInteractions(categoryRepository, categoryMapper)
    }

    @Test
    fun should_return_category_by_id() {
        val c = Category(name = "Educación").also { setId(it, 7L) }
        val r = CategoryResponse(7L, "Educación")

        `when`(categoryRepository.findById(7L)).thenReturn(Optional.of(c))
        `when`(categoryMapper.toResponse(c)).thenReturn(r)

        val result = categoryService.getCategoryById(7L)

        assertEquals(7L, result.id)
        assertEquals("Educación", result.name)

        verify(categoryRepository).findById(7L)
        verify(categoryMapper).toResponse(c)
        verify(categoryRepository).findById(7L)
        verify(categoryMapper).toResponse(c)
        verifyNoMoreInteractions(categoryRepository, categoryMapper)
    }

    @Test
    fun should_throw_exception_when_category_by_id_not_found() {
        `when`(categoryRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            categoryService.getCategoryById(1L)
        }

        verify(categoryRepository).findById(1L)
        verifyNoInteractions(categoryMapper)
    }

    @Test
    fun should_update_category_trimming_and_without_duplicate_conflict() {
        val existing = Category(name = "Viejo").also { setId(it, 10L) }
        val request = CreateCategoryRequest("  Nuevo  ")
        val saved = Category(name = "Nuevo").also { setId(it, 10L) }
        val response = CategoryResponse(10L, "Nuevo")

        `when`(categoryRepository.findById(10L)).thenReturn(Optional.of(existing))
        `when`(categoryRepository.findByNameIgnoreCase("Nuevo")).thenReturn(null)
        `when`(categoryRepository.save(existing)).thenReturn(saved)
        `when`(categoryMapper.toResponse(saved)).thenReturn(response)

        val result = categoryService.updateCategory(10L, request)

        assertEquals(10L, result.id)
        assertEquals("Nuevo", result.name)

        verify(categoryRepository).findById(10L)
        verify(categoryRepository).findByNameIgnoreCase("Nuevo")
        verify(categoryRepository).save(existing)
        verify(categoryMapper).toResponse(saved)
        verifyNoMoreInteractions(categoryRepository, categoryMapper)
    }

    @Test
    fun should_throw_duplicate_on_update_when_name_belongs_to_another_category() {
        val current = Category(name = "Actual").also { setId(it, 1L) }
        val request = CreateCategoryRequest("Hogar")

        val other = Category(name = "Hogar").also { setId(it, 2L) }

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(current))
        `when`(categoryRepository.findByNameIgnoreCase("Hogar")).thenReturn(other)

        assertThrows<DuplicateResourceException> {
            categoryService.updateCategory(1L, request)
        }

        verify(categoryRepository).findById(1L)
        verify(categoryRepository).findByNameIgnoreCase("Hogar")
        verifyNoMoreInteractions(categoryRepository)
        verifyNoInteractions(categoryMapper)
    }

    @Test
    fun should_throw_exception_when_updating_non_existent_category() {
        val request = CreateCategoryRequest("Nueva")

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            categoryService.updateCategory(1L, request)
        }

        verify(categoryRepository).findById(1L)
        verifyNoMoreInteractions(categoryRepository)
        verifyNoInteractions(categoryMapper)
    }

    @Test
    fun should_update_category_when_name_belongs_to_same_category() {
        val existing = Category(name = "Viejo").also { setId(it, 10L) }
        val request = CreateCategoryRequest("  nuevo  ")

        val same = Category(name = "nuevo").also { setId(it, 10L) }

        val saved = Category(name = "nuevo").also { setId(it, 10L) }
        val response = CategoryResponse(10L, "nuevo")

        `when`(categoryRepository.findById(10L)).thenReturn(Optional.of(existing))
        `when`(categoryRepository.findByNameIgnoreCase("nuevo")).thenReturn(same)
        `when`(categoryRepository.save(existing)).thenReturn(saved)
        `when`(categoryMapper.toResponse(saved)).thenReturn(response)

        val result = categoryService.updateCategory(10L, request)

        assertEquals(10L, result.id)
        assertEquals("nuevo", result.name)

        verify(categoryRepository).findById(10L)
        verify(categoryRepository).findByNameIgnoreCase("nuevo")
        verify(categoryRepository).save(existing)
        verify(categoryMapper).toResponse(saved)
        verifyNoMoreInteractions(categoryRepository, categoryMapper)
    }

    @Test
    fun should_delete_category() {
        val c = Category(name = "Eliminar").also { setId(it, 5L) }

        `when`(categoryRepository.findById(5L)).thenReturn(Optional.of(c))

        categoryService.deleteCategory(5L)

        verify(categoryRepository).findById(5L)
        verify(categoryRepository).delete(c)
        verifyNoMoreInteractions(categoryRepository)
        verifyNoInteractions(categoryMapper)
    }

    @Test
    fun should_throw_exception_when_deleting_non_existent_category() {
        `when`(categoryRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            categoryService.deleteCategory(1L)
        }

        verify(categoryRepository).findById(1L)
        verifyNoMoreInteractions(categoryRepository)
        verifyNoInteractions(categoryMapper)
    }

    private fun setId(target: Any, id: Long) {
        var clazz: Class<*>? = target.javaClass
        var field = clazz?.declaredFields?.find { it.name == "id" }
        while (field == null && clazz != null) {
            clazz = clazz.superclass
            field = clazz?.declaredFields?.find { it.name == "id" }
        }
        requireNotNull(field) { "No se encontró el campo 'id' en ${target.javaClass.name}" }
        field.isAccessible = true
        field.set(target, id)
    }
}

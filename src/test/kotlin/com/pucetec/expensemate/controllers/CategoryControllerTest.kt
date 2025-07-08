package com.pucetec.expensemate.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.models.requests.CreateCategoryRequest
import com.pucetec.expensemate.models.responses.CategoryResponse
import com.pucetec.expensemate.routes.Routes
import com.pucetec.expensemate.services.CategoryService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
import kotlin.test.assertEquals

@WebMvcTest(CategoryController::class)
@Import(CategoryMockConfig::class)
class CategoryControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var categoryService: CategoryService

    private lateinit var objectMapper: ObjectMapper

    private val baseUrl = Routes.CATEGORIES

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    @Test
    fun should_create_category_when_post() {
        val request = CreateCategoryRequest("Electrónica")
        val response = CategoryResponse(1L, "Electrónica")

        `when`(categoryService.createCategory(request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.name") { value("Electrónica") }
        }.andReturn()

        assertEquals(201, result.response.status)
    }

    @Test
    fun should_return_all_categories_when_get_all() {
        val categories = listOf(
            CategoryResponse(1L, "Electrónica"),
            CategoryResponse(2L, "Alimentos")
        )

        `when`(categoryService.getAllCategories()).thenReturn(categories)

        val result = mockMvc.get(baseUrl)
            .andExpect {
                status { isOk() }
                jsonPath("$.size()") { value(2) }
                jsonPath("$[0].name") { value("Electrónica") }
                jsonPath("$[1].name") { value("Alimentos") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_category_when_get_by_id() {
        val response = CategoryResponse(1L, "Electrónica")

        `when`(categoryService.getCategoryById(1L)).thenReturn(response)

        val result = mockMvc.get("$baseUrl/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.name") { value("Electrónica") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_category_not_found() {
        `when`(categoryService.getCategoryById(99L))
            .thenThrow(ResourceNotFoundException("Category not found"))

        val result = mockMvc.get("$baseUrl/99")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_update_category_when_put() {
        val request = CreateCategoryRequest("Transporte")
        val response = CategoryResponse(1L, "Transporte")

        `when`(categoryService.updateCategory(1L, request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$baseUrl/1") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.name") { value("Transporte") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_updating_nonexistent_category() {
        val request = CreateCategoryRequest("Nueva")

        `when`(categoryService.updateCategory(999L, request))
            .thenThrow(ResourceNotFoundException("Category not found"))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$baseUrl/999") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isNotFound() }
        }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_delete_category_when_delete() {
        val result = mockMvc.delete("$baseUrl/1")
            .andExpect {
                status { isNoContent() }
            }.andReturn()

        assertEquals(204, result.response.status)
    }

    @Test
    fun should_return_404_when_deleting_nonexistent_category() {
        `when`(categoryService.deleteCategory(999L))
            .thenThrow(ResourceNotFoundException("Category not found"))

        val result = mockMvc.delete("$baseUrl/999")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }
}

@TestConfiguration
class CategoryMockConfig {
    @Bean
    fun categoryService(): CategoryService = mock(CategoryService::class.java)
}

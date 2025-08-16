package com.pucetec.expensemate.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.pucetec.expensemate.exceptions.exceptions.InvalidRequestException
import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.exceptions.handlers.GlobalExceptionHandler
import com.pucetec.expensemate.models.entities.MovementType
import com.pucetec.expensemate.models.requests.CreateMovementRequest
import com.pucetec.expensemate.models.responses.*
import com.pucetec.expensemate.routes.Routes
import com.pucetec.expensemate.services.MovementService
import org.hamcrest.Matchers
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDateTime
import kotlin.test.assertEquals

@WebMvcTest(MovementController::class)
@Import(MovementMockConfig::class, GlobalExceptionHandler::class)
class MovementControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var movementService: MovementService

    private lateinit var objectMapper: ObjectMapper
    private val baseUrl = Routes.MOVEMENTS

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    @Test
    fun should_create_movement_when_post() {
        val request = CreateMovementRequest(
            type = MovementType.INCOME,
            amount = 100.0,
            note = "Pago recibido",
            userId = 1L,
            categoryId = 1L,
            creditCardId = null,
            accountId = 1L
        )

        val ts = LocalDateTime.of(2025, 7, 3, 10, 0, 0)

        val response = MovementResponse(
            id = 1L,
            type = "income",
            amount = request.amount,
            date = ts,
            note = request.note,
            category = CategoryResponse(1L, "Salario"),
            creditCard = null,
            account = AccountSummaryResponse(1L, "Pichincha", "12345678", balance = 1500.0),
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", totalBalance = 5000.0)
        )

        `when`(movementService.createMovement(request)).thenReturn(response)

        val result = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.type") { value("income") }
            jsonPath("$.amount") { value(100.0) }
            jsonPath("$.date") { value(Matchers.matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")) }
            jsonPath("$.account.bank") { value("Pichincha") }
            jsonPath("$.account.account_number") { value("12345678") }
            jsonPath("$.account.balance") { value(1500.0) }
            jsonPath("$.user.total_balance") { value(5000.0) }
        }.andReturn()

        assertEquals(201, result.response.status)
    }

    @Test
    fun should_return_all_movements_when_get_all() {
        val t1 = LocalDateTime.of(2025, 7, 3, 9, 0, 0)
        val t2 = LocalDateTime.of(2025, 7, 4, 18, 30, 0)

        val responseList = listOf(
            MovementResponse(
                id = 1L,
                type = "income",
                amount = 150.0,
                date = t1,
                note = "Pago 1",
                category = CategoryResponse(1L, "Salario"),
                creditCard = null,
                account = AccountSummaryResponse(1L, "Pichincha", "1234", balance = 1200.0),
                user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
            ),
            MovementResponse(
                id = 2L,
                type = "expense",
                amount = 50.0,
                date = t2,
                note = "Gasto comida",
                category = CategoryResponse(2L, "Alimentación"),
                creditCard = CreditCardSummaryResponse(1L, "Visa", "5678", "2025-07-05", "2025-07-20"),
                account = null,
                user = UserSummaryResponse(2L, "Katherine Iza", "kmiza@puce.edu.ec", 6000.0)
            )
        )

        `when`(movementService.getAllMovements()).thenReturn(responseList)

        val result = mockMvc.get(baseUrl)
            .andExpect {
                status { isOk() }
                jsonPath("$.size()") { value(2) }
                jsonPath("$[0].type") { value("income") }
                jsonPath("$[0].date") { value(Matchers.matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*")) }
                jsonPath("$[1].type") { value("expense") }
                jsonPath("$[1].credit_card.name") { value("Visa") }
                jsonPath("$[1].credit_card.last_four_digits") { value("5678") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_movement_when_get_by_id() {
        val ts = LocalDateTime.of(2025, 7, 3, 10, 0, 0)
        val response = MovementResponse(
            id = 1L,
            type = "income",
            amount = 100.0,
            date = ts,
            note = "Pago recibido",
            category = CategoryResponse(1L, "Salario"),
            creditCard = null,
            account = AccountSummaryResponse(1L, "Pichincha", "12345678", balance = 1500.0),
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
        )

        `when`(movementService.getMovementById(1L)).thenReturn(response)

        val result = mockMvc.get("$baseUrl/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.type") { value("income") }
                jsonPath("$.date") { value(Matchers.matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*")) }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_movement_not_found() {
        `when`(movementService.getMovementById(99L))
            .thenThrow(ResourceNotFoundException("Movement with ID 99 not found"))

        val result = mockMvc.get("$baseUrl/99")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Movement with ID 99 not found") }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_return_movements_by_user_when_get_by_user() {
        val userId = 1L
        val t = LocalDateTime.of(2025, 7, 10, 8, 0, 0)
        val list = listOf(
            MovementResponse(
                id = 10L,
                type = "income",
                amount = 300.0,
                date = t,
                note = "Pago freelance",
                category = CategoryResponse(1L, "Ingresos"),
                creditCard = null,
                account = AccountSummaryResponse(1L, "Pichincha", "1111", balance = 1800.0),
                user = UserSummaryResponse(userId, "Alexander Pavón", "afpavon@puce.edu.ec", 5200.0)
            ),
            MovementResponse(
                id = 11L,
                type = "expense",
                amount = 40.0,
                date = t.plusHours(2),
                note = "Transporte",
                category = CategoryResponse(3L, "Transporte"),
                creditCard = null,
                account = AccountSummaryResponse(1L, "Pichincha", "1111", balance = 1760.0),
                user = UserSummaryResponse(userId, "Alexander Pavón", "afpavon@puce.edu.ec", 5160.0)
            )
        )

        `when`(movementService.getMovementsByUser(userId)).thenReturn(list)

        val result = mockMvc.get("$baseUrl/by-user/$userId")
            .andExpect {
                status { isOk() }
                jsonPath("$.size()") { value(2) }
                jsonPath("$[0].user.id") { value(userId.toInt()) }
                jsonPath("$[1].user.id") { value(userId.toInt()) }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_user_not_found_in_get_by_user() {
        val userId = 999L
        `when`(movementService.getMovementsByUser(userId))
            .thenThrow(ResourceNotFoundException("User with ID $userId not found"))

        val result = mockMvc.get("$baseUrl/by-user/$userId")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("User with ID 999 not found") }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_return_movements_by_user_and_category_when_get() {
        val userId = 1L
        val categoryId = 2L
        val t = LocalDateTime.of(2025, 7, 12, 12, 0, 0)

        val list = listOf(
            MovementResponse(
                id = 20L,
                type = "expense",
                amount = 25.0,
                date = t,
                note = "Almuerzo",
                category = CategoryResponse(categoryId, "Alimentación"),
                creditCard = null,
                account = AccountSummaryResponse(1L, "Pichincha", "2222", balance = 900.0),
                user = UserSummaryResponse(userId, "Alexander Pavón", "afpavon@puce.edu.ec", 4000.0)
            )
        )

        `when`(movementService.getMovementsByUserAndCategory(userId, categoryId)).thenReturn(list)

        val result = mockMvc.get("$baseUrl/by-user/$userId/by-category/$categoryId")
            .andExpect {
                status { isOk() }
                jsonPath("$.size()") { value(1) }
                jsonPath("$[0].user.id") { value(userId.toInt()) }
                jsonPath("$[0].category.id") { value(categoryId.toInt()) }
                jsonPath("$[0].category.name") { value("Alimentación") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_user_or_category_not_found_in_get_by_user_and_category() {
        val userId = 999L
        val categoryId = 888L

        `when`(movementService.getMovementsByUserAndCategory(userId, categoryId))
            .thenThrow(ResourceNotFoundException("User $userId or Category $categoryId not found"))

        val result = mockMvc.get("$baseUrl/by-user/$userId/by-category/$categoryId")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("User 999 or Category 888 not found") }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_update_movement_when_put() {
        val request = CreateMovementRequest(
            type = MovementType.EXPENSE,
            amount = 200.0,
            note = "Pago servicio",
            userId = 1L,
            categoryId = 1L,
            creditCardId = null,
            accountId = 1L
        )

        val ts = LocalDateTime.of(2025, 7, 5, 11, 0, 0)
        val response = MovementResponse(
            id = 1L,
            type = "expense",
            amount = request.amount,
            date = ts,
            note = request.note,
            category = CategoryResponse(1L, "Servicios"),
            creditCard = null,
            account = AccountSummaryResponse(1L, "Pichincha", "12345678", balance = 1300.0),
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5200.0)
        )

        `when`(movementService.updateMovement(1L, request)).thenReturn(response)

        val result = mockMvc.put("$baseUrl/1") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.type") { value("expense") }
            jsonPath("$.account.balance") { value(1300.0) }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_updating_nonexistent_movement() {
        val request = CreateMovementRequest(
            type = MovementType.EXPENSE,
            amount = 200.0,
            note = "Pago servicio",
            userId = 1L,
            categoryId = 1L,
            creditCardId = null,
            accountId = 1L
        )

        `when`(movementService.updateMovement(99L, request))
            .thenThrow(ResourceNotFoundException("Movement with ID 99 not found"))

        val result = mockMvc.put("$baseUrl/99") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error") { value("Movement with ID 99 not found") }
        }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_return_400_when_invalid_request_from_service() {
        val request = CreateMovementRequest(
            type = MovementType.EXPENSE,
            amount = 9999.0,
            note = "Intento con saldo insuficiente",
            userId = 1L,
            categoryId = 1L,
            creditCardId = null,
            accountId = 1L
        )

        `when`(movementService.createMovement(request))
            .thenThrow(InvalidRequestException("Insufficient balance in account"))

        val result = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Insufficient balance in account") }
        }.andReturn()

        assertEquals(400, result.response.status)
    }

    @Test
    fun should_delete_movement_when_delete() {
        val result = mockMvc.delete("$baseUrl/1")
            .andExpect {
                status { isNoContent() }
            }.andReturn()

        assertEquals(204, result.response.status)
    }

    @Test
    fun should_return_404_when_deleting_nonexistent_movement() {
        `when`(movementService.deleteMovement(99L))
            .thenThrow(ResourceNotFoundException("Movement with ID 99 not found"))

        val result = mockMvc.delete("$baseUrl/99")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Movement with ID 99 not found") }
            }.andReturn()

        assertEquals(404, result.response.status)
    }
}

@TestConfiguration
class MovementMockConfig {
    @Bean
    fun movementService(): MovementService = mock(MovementService::class.java)
}

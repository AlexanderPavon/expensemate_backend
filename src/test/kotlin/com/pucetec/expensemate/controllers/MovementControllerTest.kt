package com.pucetec.expensemate.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.models.requests.CreateMovementRequest
import com.pucetec.expensemate.models.responses.*
import com.pucetec.expensemate.routes.Routes
import com.pucetec.expensemate.services.MovementService
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
import java.time.LocalDate
import kotlin.test.assertEquals

@WebMvcTest(MovementController::class)
@Import(MovementMockConfig::class)
class MovementControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var movementService: MovementService

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
            type = "ingreso",
            amount = 100.0,
            date = LocalDate.parse("2025-07-03"),
            note = "Pago recibido",
            userId = 1L,
            categoryId = 1L,
            creditCardId = null,
            accountId = 1L
        )

        val response = MovementResponse(
            id = 1L,
            type = request.type,
            amount = request.amount,
            date = request.date,
            note = request.note,
            category = CategoryResponse(1L, "Salario"),
            creditCard = null,
            account = AccountSummaryResponse(1L, "Pichincha", "12345678"),
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        )

        `when`(movementService.createMovement(request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.type") { value("ingreso") }
            jsonPath("$.amount") { value(100.0) }
        }.andReturn()

        assertEquals(201, result.response.status)
    }

    @Test
    fun should_return_all_movements_when_get_all() {
        val responseList = listOf(
            MovementResponse(
                id = 1L,
                type = "ingreso",
                amount = 150.0,
                date = LocalDate.parse("2025-07-03"),
                note = "Pago 1",
                category = CategoryResponse(1L, "Salario"),
                creditCard = null,
                account = AccountSummaryResponse(1L, "Pichincha", "1234"),
                user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
            ),
            MovementResponse(
                id = 2L,
                type = "egreso",
                amount = 50.0,
                date = LocalDate.parse("2025-07-04"),
                note = "Gasto comida",
                category = CategoryResponse(2L, "Alimentación"),
                creditCard = null,
                account = AccountSummaryResponse(2L, "Guayaquil", "5678"),
                user = UserSummaryResponse(2L, "Katherine Iza", "kmiza@puce.edu.ec")
            )
        )

        `when`(movementService.getAllMovements()).thenReturn(responseList)

        val result = mockMvc.get(baseUrl)
            .andExpect {
                status { isOk() }
                jsonPath("$.size()") { value(2) }
                jsonPath("$[0].type") { value("ingreso") }
                jsonPath("$[1].type") { value("egreso") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_movement_when_get_by_id() {
        val response = MovementResponse(
            id = 1L,
            type = "ingreso",
            amount = 100.0,
            date = LocalDate.parse("2025-07-03"),
            note = "Pago recibido",
            category = CategoryResponse(1L, "Salario"),
            creditCard = null,
            account = AccountSummaryResponse(1L, "Pichincha", "12345678"),
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        )

        `when`(movementService.getMovementById(1L)).thenReturn(response)

        val result = mockMvc.get("$baseUrl/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.type") { value("ingreso") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_movement_not_found() {
        `when`(movementService.getMovementById(99L))
            .thenThrow(ResourceNotFoundException("Movement not found"))

        val result = mockMvc.get("$baseUrl/99")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_update_movement_when_put() {
        val request = CreateMovementRequest(
            type = "egreso",
            amount = 200.0,
            date = LocalDate.parse("2025-07-05"),
            note = "Pago servicio",
            userId = 1L,
            categoryId = 1L,
            creditCardId = null,
            accountId = 1L
        )

        val response = MovementResponse(
            id = 1L,
            type = request.type,
            amount = request.amount,
            date = request.date,
            note = request.note,
            category = CategoryResponse(1L, "Servicios"),
            creditCard = null,
            account = AccountSummaryResponse(1L, "Pichincha", "12345678"),
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        )

        `when`(movementService.updateMovement(1L, request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$baseUrl/1") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.type") { value("egreso") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_updating_nonexistent_movement() {
        val request = CreateMovementRequest(
            type = "egreso",
            amount = 200.0,
            date = LocalDate.parse("2025-07-05"),
            note = "Pago servicio",
            userId = 1L,
            categoryId = 1L,
            creditCardId = null,
            accountId = 1L
        )

        `when`(movementService.updateMovement(99L, request))
            .thenThrow(ResourceNotFoundException("Movement not found"))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$baseUrl/99") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isNotFound() }
        }.andReturn()

        assertEquals(404, result.response.status)
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
            .thenThrow(ResourceNotFoundException("Movement not found"))

        val result = mockMvc.delete("$baseUrl/99")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }
}

@TestConfiguration
class MovementMockConfig {
    @Bean
    fun movementService(): MovementService = mock(MovementService::class.java)
}

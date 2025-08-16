package com.pucetec.expensemate.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.exceptions.handlers.GlobalExceptionHandler
import com.pucetec.expensemate.models.requests.CreateCreditCardRequest
import com.pucetec.expensemate.models.responses.CreditCardResponse
import com.pucetec.expensemate.models.responses.UserSummaryResponse
import com.pucetec.expensemate.routes.Routes
import com.pucetec.expensemate.services.CreditCardService
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
import kotlin.test.assertEquals

@WebMvcTest(CreditCardController::class)
@Import(CreditCardMockConfig::class, GlobalExceptionHandler::class)
class CreditCardControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var creditCardService: CreditCardService

    private lateinit var objectMapper: ObjectMapper
    private val baseUrl = Routes.CREDIT_CARDS

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    @Test
    fun should_create_credit_card_when_post() {
        val request = CreateCreditCardRequest(
            name = "Visa",
            lastFourDigits = "1234",
            courtDate = "15",
            maximumPaymentDate = "30",
            userId = 1L
        )

        val response = CreditCardResponse(
            id = 1L,
            name = request.name,
            lastFourDigits = request.lastFourDigits,
            courtDate = request.courtDate,
            maximumPaymentDate = request.maximumPaymentDate,
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", totalBalance = 5000.0)
        )

        `when`(creditCardService.createCard(request)).thenReturn(response)

        val result = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.name") { value("Visa") }
            jsonPath("$.last_four_digits") { value("1234") }
            jsonPath("$.court_date") { value("15") }
            jsonPath("$.maximum_payment_date") { value("30") }
            jsonPath("$.user.name") { value("Alexander Pavón") }
            jsonPath("$.user.total_balance") { value(5000.0) }
        }.andReturn()

        assertEquals(201, result.response.status)
    }

    @Test
    fun should_return_all_cards_when_get_all() {
        val cards = listOf(
            CreditCardResponse(
                1L, "Visa", "1234", "15", "30",
                UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
            ),
            CreditCardResponse(
                2L, "MasterCard", "5678", "15", "30",
                UserSummaryResponse(2L, "Elizabeth Grant", "lanadelrey@puce.edu.ec", 6000.0)
            )
        )

        `when`(creditCardService.getAllCards()).thenReturn(cards)

        val result = mockMvc.get(baseUrl)
            .andExpect {
                status { isOk() }
                jsonPath("$.size()") { value(2) }
                jsonPath("$[0].name") { value("Visa") }
                jsonPath("$[0].last_four_digits") { value("1234") }
                jsonPath("$[1].name") { value("MasterCard") }
                jsonPath("$[1].last_four_digits") { value("5678") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_card_when_get_by_id() {
        val response = CreditCardResponse(
            1L, "Visa", "1234", "15", "30",
            UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
        )

        `when`(creditCardService.getCardById(1L)).thenReturn(response)

        val result = mockMvc.get("$baseUrl/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.name") { value("Visa") }
                jsonPath("$.last_four_digits") { value("1234") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_card_not_found() {
        `when`(creditCardService.getCardById(99L))
            .thenThrow(ResourceNotFoundException("Credit card with ID 99 not found"))

        val result = mockMvc.get("$baseUrl/99")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Credit card with ID 99 not found") }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_return_cards_by_user_when_get_by_user() {
        val userId = 1L
        val cards = listOf(
            CreditCardResponse(
                10L, "Visa", "1111", "10", "25",
                UserSummaryResponse(userId, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
            ),
            CreditCardResponse(
                11L, "MasterCard", "2222", "12", "27",
                UserSummaryResponse(userId, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
            )
        )

        `when`(creditCardService.getCardsByUser(userId)).thenReturn(cards)

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

        `when`(creditCardService.getCardsByUser(userId))
            .thenThrow(ResourceNotFoundException("User with ID $userId not found"))

        val result = mockMvc.get("$baseUrl/by-user/$userId")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("User with ID 999 not found") }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_update_card_when_put() {
        val request = CreateCreditCardRequest(
            name = "MasterCard",
            lastFourDigits = "5678",
            courtDate = "15",
            maximumPaymentDate = "30",
            userId = 1L
        )

        val response = CreditCardResponse(
            id = 1L,
            name = request.name,
            lastFourDigits = request.lastFourDigits,
            courtDate = request.courtDate,
            maximumPaymentDate = request.maximumPaymentDate,
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
        )

        `when`(creditCardService.updateCard(1L, request)).thenReturn(response)

        val result = mockMvc.put("$baseUrl/1") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.name") { value("MasterCard") }
            jsonPath("$.last_four_digits") { value("5678") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_updating_nonexistent_card() {
        val request = CreateCreditCardRequest(
            name = "MasterCard",
            lastFourDigits = "5678",
            courtDate = "15",
            maximumPaymentDate = "30",
            userId = 1L
        )

        `when`(creditCardService.updateCard(99L, request))
            .thenThrow(ResourceNotFoundException("Credit card with ID 99 not found"))

        val result = mockMvc.put("$baseUrl/99") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error") { value("Credit card with ID 99 not found") }
        }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_delete_card_when_delete() {
        val result = mockMvc.delete("$baseUrl/1")
            .andExpect {
                status { isNoContent() }
            }.andReturn()

        assertEquals(204, result.response.status)
    }

    @Test
    fun should_return_404_when_deleting_nonexistent_card() {
        `when`(creditCardService.deleteCard(99L))
            .thenThrow(ResourceNotFoundException("Credit card with ID 99 not found"))

        val result = mockMvc.delete("$baseUrl/99")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Credit card with ID 99 not found") }
            }.andReturn()

        assertEquals(404, result.response.status)
    }
}

@TestConfiguration
class CreditCardMockConfig {
    @Bean
    fun creditCardService(): CreditCardService = mock(CreditCardService::class.java)
}

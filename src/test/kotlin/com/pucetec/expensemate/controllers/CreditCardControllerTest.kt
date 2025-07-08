package com.pucetec.expensemate.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
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
import org.springframework.test.web.servlet.*
import kotlin.test.assertEquals

@WebMvcTest(CreditCardController::class)
@Import(CreditCardMockConfig::class)
class CreditCardControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var creditCardService: CreditCardService

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
            courtDate = "2025-07-05",
            maximumPaymentDate = "2025-07-20",
            userId = 1L
        )

        val response = CreditCardResponse(
            id = 1L,
            name = request.name,
            lastFourDigits = request.lastFourDigits,
            courtDate = request.courtDate,
            maximumPaymentDate = request.maximumPaymentDate,
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        )

        `when`(creditCardService.createCard(request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.name") { value("Visa") }
            jsonPath("$.lastFourDigits") { value("1234") }
        }.andReturn()

        assertEquals(201, result.response.status)
    }

    @Test
    fun should_return_all_cards_when_get_all() {
        val cards = listOf(
            CreditCardResponse(
                1L, "Visa", "1234", "2025-07-05", "2025-07-20",
                UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
            ),
            CreditCardResponse(
                2L, "MasterCard", "5678", "2025-07-10", "2025-07-25",
                UserSummaryResponse(2L, "Elizabeth Grant", "lanadelrey@puce.edu.ec")
            )
        )

        `when`(creditCardService.getAllCards()).thenReturn(cards)

        val result = mockMvc.get(baseUrl)
            .andExpect {
                status { isOk() }
                jsonPath("$.size()") { value(2) }
                jsonPath("$[0].name") { value("Visa") }
                jsonPath("$[1].name") { value("MasterCard") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_card_when_get_by_id() {
        val response = CreditCardResponse(
            1L, "Visa", "1234", "2025-07-05", "2025-07-20",
            UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        )

        `when`(creditCardService.getCardById(1L)).thenReturn(response)

        val result = mockMvc.get("$baseUrl/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.name") { value("Visa") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_card_not_found() {
        `when`(creditCardService.getCardById(99L))
            .thenThrow(ResourceNotFoundException("Card not found"))

        val result = mockMvc.get("$baseUrl/99")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_update_card_when_put() {
        val request = CreateCreditCardRequest(
            name = "MasterCard",
            lastFourDigits = "5678",
            courtDate = "2025-07-10",
            maximumPaymentDate = "2025-07-25",
            userId = 1L
        )

        val response = CreditCardResponse(
            id = 1L,
            name = request.name,
            lastFourDigits = request.lastFourDigits,
            courtDate = request.courtDate,
            maximumPaymentDate = request.maximumPaymentDate,
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        )

        `when`(creditCardService.updateCard(1L, request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$baseUrl/1") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.name") { value("MasterCard") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_updating_nonexistent_card() {
        val request = CreateCreditCardRequest(
            name = "MasterCard",
            lastFourDigits = "5678",
            courtDate = "2025-07-10",
            maximumPaymentDate = "2025-07-25",
            userId = 1L
        )

        `when`(creditCardService.updateCard(99L, request))
            .thenThrow(ResourceNotFoundException("Card not found"))

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
            .thenThrow(ResourceNotFoundException("Card not found"))

        val result = mockMvc.delete("$baseUrl/99")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }
}

@TestConfiguration
class CreditCardMockConfig {
    @Bean
    fun creditCardService(): CreditCardService = mock(CreditCardService::class.java)
}

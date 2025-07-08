package com.pucetec.expensemate.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.models.requests.CreateAccountRequest
import com.pucetec.expensemate.models.responses.AccountResponse
import com.pucetec.expensemate.models.responses.UserSummaryResponse
import com.pucetec.expensemate.routes.Routes
import com.pucetec.expensemate.services.AccountService
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

@WebMvcTest(AccountController::class)
@Import(AccountMockConfig::class)
class AccountControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var accountService: AccountService

    private lateinit var objectMapper: ObjectMapper

    private val baseUrl = Routes.ACCOUNTS

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    @Test
    fun should_create_account_when_post() {
        val request = CreateAccountRequest("Banco Pichincha", "12345678", 1L)
        val response = AccountResponse(
            id = 1L,
            bank = request.bank,
            accountNumber = request.accountNumber,
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        )

        `when`(accountService.createAccount(request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.bank") { value("Banco Pichincha") }
            jsonPath("$.accountNumber") { value("12345678") }
            jsonPath("$.user.name") { value("Alexander Pavón") }
        }.andReturn()

        assertEquals(201, result.response.status)
    }

    @Test
    fun should_return_all_accounts_when_get_all() {
        val accounts = listOf(
            AccountResponse(1L, "Banco Pichincha", "12345678", UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")),
            AccountResponse(2L, "Banco Guayaquil", "87654321", UserSummaryResponse(2L, "Kenia Osuna", "kos@puce.edu.ec"))
        )

        `when`(accountService.getAllAccounts()).thenReturn(accounts)

        val result = mockMvc.get(baseUrl)
            .andExpect {
                status { isOk() }
                jsonPath("$.size()") { value(2) }
                jsonPath("$[0].bank") { value("Banco Pichincha") }
                jsonPath("$[1].bank") { value("Banco Guayaquil") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_account_when_get_by_id() {
        val response = AccountResponse(1L, "Banco Pichincha", "12345678", UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec"))

        `when`(accountService.getAccountById(1L)).thenReturn(response)

        val result = mockMvc.get("$baseUrl/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.bank") { value("Banco Pichincha") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_account_not_found() {
        `when`(accountService.getAccountById(99L))
            .thenThrow(ResourceNotFoundException("Account not found"))

        val result = mockMvc.get("$baseUrl/99")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_update_account_when_put() {
        val request = CreateAccountRequest("Banco Guayaquil", "99999999", 1L)
        val response = AccountResponse(1L, request.bank, request.accountNumber, UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec"))

        `when`(accountService.updateAccount(1L, request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$baseUrl/1") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.bank") { value("Banco Guayaquil") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_updating_nonexistent_account() {
        val request = CreateAccountRequest("Banco Guayaquil", "99999999", 1L)

        `when`(accountService.updateAccount(99L, request))
            .thenThrow(ResourceNotFoundException("Account not found"))

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
    fun should_delete_account_when_delete() {
        val result = mockMvc.delete("$baseUrl/1")
            .andExpect {
                status { isNoContent() }
            }.andReturn()

        assertEquals(204, result.response.status)
    }

    @Test
    fun should_return_404_when_deleting_nonexistent_account() {
        `when`(accountService.deleteAccount(99L))
            .thenThrow(ResourceNotFoundException("Account not found"))

        val result = mockMvc.delete("$baseUrl/99")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }
}

@TestConfiguration
class AccountMockConfig {
    @Bean
    fun accountService(): AccountService = mock(AccountService::class.java)
}

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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
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
        val request = CreateAccountRequest(
            bank = "Banco Pichincha",
            accountNumber = "12345678",
            userId = 1L
        )
        val response = AccountResponse(
            id = 1L,
            bank = request.bank,
            accountNumber = request.accountNumber,
            balance = 1500.50,
            user = UserSummaryResponse(
                1L, "Alexander Pavón", "afpavon@puce.edu.ec", totalBalance = 5000.0
            )
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
            jsonPath("$.account_number") { value("12345678") }
            jsonPath("$.balance") { value(1500.50) }
            jsonPath("$.user.name") { value("Alexander Pavón") }
            jsonPath("$.user.total_balance") { value(5000.0) }
        }.andReturn()

        assertEquals(201, result.response.status)
    }

    @Test
    fun should_return_all_accounts_when_get_all() {
        val accounts = listOf(
            AccountResponse(
                1L, "Banco Pichincha", "12345678", 1500.0,
                UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
            ),
            AccountResponse(
                2L, "Banco Guayaquil", "87654321", 2000.0,
                UserSummaryResponse(2L, "Kenia Osuna", "kos@puce.edu.ec", 6000.0)
            )
        )

        `when`(accountService.getAllAccounts()).thenReturn(accounts)

        val result = mockMvc.get(baseUrl)
            .andExpect {
                status { isOk() }
                jsonPath("$.size()") { value(2) }
                jsonPath("$[0].bank") { value("Banco Pichincha") }
                jsonPath("$[0].balance") { value(1500.0) }
                jsonPath("$[1].bank") { value("Banco Guayaquil") }
                jsonPath("$[1].balance") { value(2000.0) }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_account_when_get_by_id() {
        val response = AccountResponse(
            1L, "Banco Pichincha", "12345678", 1500.0,
            UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
        )

        `when`(accountService.getAccountById(1L)).thenReturn(response)

        val result = mockMvc.get("$baseUrl/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.bank") { value("Banco Pichincha") }
                jsonPath("$.balance") { value(1500.0) }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_account_not_found() {
        `when`(accountService.getAccountById(99L))
            .thenThrow(ResourceNotFoundException("Account with ID 99 not found"))

        val result = mockMvc.get("$baseUrl/99")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Account with ID 99 not found") }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_return_accounts_by_user_when_get_by_user() {
        val userId = 1L
        val accounts = listOf(
            AccountResponse(
                10L, "Banco Pichincha", "1111", 1200.0,
                UserSummaryResponse(userId, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
            ),
            AccountResponse(
                11L, "Banco Guayaquil", "2222", 800.0,
                UserSummaryResponse(userId, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
            )
        )

        `when`(accountService.getAccountsByUser(userId)).thenReturn(accounts)

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
        `when`(accountService.getAccountsByUser(userId))
            .thenThrow(ResourceNotFoundException("User with ID $userId not found"))

        val result = mockMvc.get("$baseUrl/by-user/$userId")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("User with ID 999 not found") }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_update_account_when_put() {
        val request = CreateAccountRequest("Banco Guayaquil", "99999999", 1L)
        val response = AccountResponse(
            1L, request.bank, request.accountNumber, 2000.0,
            UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
        )

        `when`(accountService.updateAccount(1L, request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$baseUrl/1") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.bank") { value("Banco Guayaquil") }
            jsonPath("$.balance") { value(2000.0) }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_updating_nonexistent_account() {
        val request = CreateAccountRequest("Banco Guayaquil", "99999999", 1L)

        `when`(accountService.updateAccount(99L, request))
            .thenThrow(ResourceNotFoundException("Account with ID 99 not found"))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$baseUrl/99") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error") { value("Account with ID 99 not found") }
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
            .thenThrow(ResourceNotFoundException("Account with ID 99 not found"))

        val result = mockMvc.delete("$baseUrl/99")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Account with ID 99 not found") }
            }.andReturn()

        assertEquals(404, result.response.status)
    }
}

@TestConfiguration
class AccountMockConfig {
    @Bean
    fun accountService(): AccountService = mock(AccountService::class.java)
}

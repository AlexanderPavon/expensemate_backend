package com.pucetec.expensemate.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.models.requests.CreateUserRequest
import com.pucetec.expensemate.models.responses.*
import com.pucetec.expensemate.routes.Routes
import com.pucetec.expensemate.services.UserService
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

@WebMvcTest(UserController::class)
@Import(UserMockConfig::class)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userService: UserService

    private lateinit var objectMapper: ObjectMapper

    private val BASE_URL = Routes.USERS

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    @Test
    fun should_return_user_when_get_by_id() {
        val response = UserResponse(
            id = 1L,
            name = "Alexander Pavón",
            email = "afpavon@puce.edu.ec",
            movements = listOf(),
            creditCards = listOf(),
            accounts = listOf()
        )

        `when`(userService.getUserById(1L)).thenReturn(response)

        val result = mockMvc.get("$BASE_URL/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Alexander Pavón") }
                jsonPath("$.email") { value("afpavon@puce.edu.ec") }
                jsonPath("$.movements") { isArray() }
                jsonPath("$.creditCards") { isArray() }
                jsonPath("$.accounts") { isArray() }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_user_not_found() {
        val id = 100L

        `when`(userService.getUserById(id)).thenThrow(ResourceNotFoundException("User not found"))

        val result = mockMvc.get("$BASE_URL/$id")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_return_all_users_when_get_all() {
        val users = listOf(
            UserResponse(
                id = 1L,
                name = "Alexander Pavón",
                email = "afpavon@puce.edu.ec",
                movements = listOf(),
                creditCards = listOf(),
                accounts = listOf()
            ),
            UserResponse(
                id = 2L,
                name = "Katherine Iza",
                email = "kmiza@puce.edu.ec",
                movements = listOf(),
                creditCards = listOf(),
                accounts = listOf()
            )
        )

        `when`(userService.getAllUsers()).thenReturn(users)

        val result = mockMvc.get(BASE_URL)
            .andExpect {
                status { isOk() }
                jsonPath("$.size()") { value(2) }
                jsonPath("$[0].name") { value("Alexander Pavón") }
                jsonPath("$[1].name") { value("Katherine Iza") }
            }
            .andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_create_user_when_post() {
        val request = CreateUserRequest(name = "Katherine Iza", email = "kmiza@puce.edu.ec")

        val response = UserResponse(
            id = 2L,
            name = request.name,
            email = request.email,
            movements = listOf(),
            creditCards = listOf(),
            accounts = listOf()
        )

        `when`(userService.createUser(request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isCreated() }
            jsonPath("$.name") { value("Katherine Iza") }
            jsonPath("$.email") { value("kmiza@puce.edu.ec") }
        }.andReturn()

        assertEquals(201, result.response.status)
    }

    @Test
    fun should_update_user_when_put() {
        val request = CreateUserRequest("Alexander Pavón Updated", "afpavon_updated@puce.edu.ec")

        val response = UserResponse(
            id = 1L,
            name = request.name,
            email = request.email,
            movements = listOf(),
            creditCards = listOf(),
            accounts = listOf()
        )

        `when`(userService.updateUser(1L, request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Alexander Pavón Updated") }
            jsonPath("$.email") { value("afpavon_updated@puce.edu.ec") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_updating_nonexistent_user() {
        val request = CreateUserRequest("Ghost", "ghost@puce.edu.ec")

        `when`(userService.updateUser(999L, request))
            .thenThrow(ResourceNotFoundException("User not found"))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/999") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isNotFound() }
        }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_delete_user_when_delete() {
        val result = mockMvc.delete("$BASE_URL/1")
            .andExpect {
                status { isNoContent() }
            }.andReturn()

        assertEquals(204, result.response.status)
    }

    @Test
    fun should_return_404_when_deleting_nonexistent_user() {
        `when`(userService.deleteUser(999L))
            .thenThrow(ResourceNotFoundException("User not found"))

        val result = mockMvc.delete("$BASE_URL/999")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }
}

@TestConfiguration
class UserMockConfig {
    @Bean
    fun userService(): UserService = mock(UserService::class.java)
}

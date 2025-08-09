package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.mappers.UserMapper
import com.pucetec.expensemate.models.entities.User
import com.pucetec.expensemate.models.requests.CreateUserRequest
import com.pucetec.expensemate.models.responses.UserResponse
import com.pucetec.expensemate.models.responses.UserSummaryResponse
import com.pucetec.expensemate.repositories.UserRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.*

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userMapper: UserMapper
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        userMapper = mock(UserMapper::class.java)
        userService = UserService(userRepository, userMapper)
    }

    @Test
    fun should_create_a_new_user() {
        val request = CreateUserRequest("Alexander Pavón", "afpavon@puce.edu.ec")
        val user = User(name = request.name, email = request.email)
        val response = UserResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", listOf(), listOf(), listOf())

        `when`(userRepository.save(any(User::class.java))).thenReturn(user)
        `when`(userMapper.toResponse(user)).thenReturn(response)

        val result = userService.createUser(request)

        assertEquals("Alexander Pavón", result.name)
        assertEquals("afpavon@puce.edu.ec", result.email)
    }

    @Test
    fun should_return_all_users() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val response = UserResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", listOf(), listOf(), listOf())

        `when`(userRepository.findAll()).thenReturn(listOf(user))
        `when`(userMapper.toResponse(user)).thenReturn(response)

        val result = userService.getAllUsers()

        assertEquals(1, result.size)
        assertEquals("Alexander Pavón", result[0].name)
    }

    @Test
    fun should_return_user_by_id() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val response = UserResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", listOf(), listOf(), listOf())

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(userMapper.toResponse(user)).thenReturn(response)

        val result = userService.getUserById(1L)

        assertEquals("Alexander Pavón", result.name)
    }

    @Test
    fun should_throw_exception_when_user_by_id_not_found() {
        `when`(userRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            userService.getUserById(1L)
        }
    }

    @Test
    fun should_return_user_summary_by_email() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val summary = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)

        `when`(userRepository.findByEmail("afpavon@puce.edu.ec")).thenReturn(user)
        `when`(userMapper.toSummary(user)).thenReturn(summary)

        val result = userService.getUserByEmail("afpavon@puce.edu.ec")

        assertEquals(1L, result.id)
        assertEquals("Alexander Pavón", result.name)
        assertEquals("afpavon@puce.edu.ec", result.email)
        assertEquals(5000.0, result.totalBalance)
    }

    @Test
    fun should_throw_exception_when_user_by_email_not_found() {
        `when`(userRepository.findByEmail("missing@puce.edu.ec")).thenReturn(null)

        assertThrows<ResourceNotFoundException> {
            userService.getUserByEmail("missing@puce.edu.ec")
        }
    }

    @Test
    fun should_return_user_summary_by_id() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val summary = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5200.0)

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(userMapper.toSummary(user)).thenReturn(summary)

        val result = userService.getUserSummary(1L)

        assertEquals(1L, result.id)
        assertEquals("Alexander Pavón", result.name)
        assertEquals("afpavon@puce.edu.ec", result.email)
        assertEquals(5200.0, result.totalBalance)
    }

    @Test
    fun should_throw_exception_when_user_summary_id_not_found() {
        `when`(userRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            userService.getUserSummary(99L)
        }
    }

    @Test
    fun should_update_user() {
        val existingUser = User(name = "Old", email = "old@puce.edu.ec")
        val request = CreateUserRequest("New", "new@puce.edu.ec")
        val updatedUser = User(name = "New", email = "new@puce.edu.ec")
        val response = UserResponse(1L, "New", "new@puce.edu.ec", listOf(), listOf(), listOf())

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(existingUser))
        `when`(userRepository.save(existingUser)).thenReturn(updatedUser)
        `when`(userMapper.toResponse(updatedUser)).thenReturn(response)

        val result = userService.updateUser(1L, request)

        assertEquals("New", result.name)
        assertEquals("new@puce.edu.ec", result.email)
    }

    @Test
    fun should_throw_exception_when_updating_non_existent_user() {
        val request = CreateUserRequest("Test", "test@puce.edu.ec")

        `when`(userRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            userService.updateUser(1L, request)
        }
    }

    @Test
    fun should_delete_user() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))

        userService.deleteUser(1L)

        verify(userRepository).delete(user)
    }

    @Test
    fun should_throw_exception_when_deleting_non_existent_user() {
        `when`(userRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            userService.deleteUser(1L)
        }
    }
}
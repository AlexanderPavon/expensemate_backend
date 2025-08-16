package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.DuplicateResourceException
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
import org.mockito.ArgumentCaptor
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
    fun should_create_a_new_user_trimming_and_lowercasing_email() {
        val request = CreateUserRequest("  Alexander Pavón  ", "  AfPaVon@puce.edu.ec  ")

        val saved = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec").also { setId(it, 1L) }
        val response = UserResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", listOf(), listOf(), listOf())

        `when`(userRepository.findByEmail("afpavon@puce.edu.ec")).thenReturn(null)
        `when`(userRepository.save(any(User::class.java))).thenReturn(saved)
        `when`(userMapper.toResponse(saved)).thenReturn(response)

        val result = userService.createUser(request)

        assertEquals(1L, result.id)
        assertEquals("Alexander Pavón", result.name)
        assertEquals("afpavon@puce.edu.ec", result.email)

        val captor = ArgumentCaptor.forClass(User::class.java)
        verify(userRepository).save(captor.capture())
        assertEquals("Alexander Pavón", captor.value.name)
        assertEquals("afpavon@puce.edu.ec", captor.value.email)

        verify(userRepository).findByEmail("afpavon@puce.edu.ec")
        verify(userMapper).toResponse(saved)
        verifyNoMoreInteractions(userRepository, userMapper)
    }

    @Test
    fun should_throw_duplicate_on_create_when_email_already_exists() {
        val request = CreateUserRequest("Alex", "alex@puce.edu.ec")
        val existing = User(name = "Otro", email = "alex@puce.edu.ec").also { setId(it, 99L) }

        `when`(userRepository.findByEmail("alex@puce.edu.ec")).thenReturn(existing)

        assertThrows<DuplicateResourceException> {
            userService.createUser(request)
        }

        verify(userRepository).findByEmail("alex@puce.edu.ec")
        verifyNoMoreInteractions(userRepository)
        verifyNoInteractions(userMapper)
    }

    @Test
    fun should_return_all_users() {
        val u = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec").also { setId(it, 1L) }
        val r = UserResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", listOf(), listOf(), listOf())

        `when`(userRepository.findAll()).thenReturn(listOf(u))
        `when`(userMapper.toResponse(u)).thenReturn(r)

        val result = userService.getAllUsers()

        assertEquals(1, result.size)
        assertEquals("Alexander Pavón", result[0].name)

        verify(userRepository).findAll()
        verify(userMapper).toResponse(u)
        verifyNoMoreInteractions(userRepository, userMapper)
    }

    @Test
    fun should_return_user_by_id() {
        val u = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec").also { setId(it, 7L) }
        val r = UserResponse(7L, "Alexander Pavón", "afpavon@puce.edu.ec", listOf(), listOf(), listOf())

        `when`(userRepository.findById(7L)).thenReturn(Optional.of(u))
        `when`(userMapper.toResponse(u)).thenReturn(r)

        val result = userService.getUserById(7L)

        assertEquals(7L, result.id)
        assertEquals("Alexander Pavón", result.name)

        verify(userRepository).findById(7L)
        verify(userMapper).toResponse(u)
        verifyNoMoreInteractions(userRepository, userMapper)
    }

    @Test
    fun should_throw_exception_when_user_by_id_not_found() {
        `when`(userRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            userService.getUserById(1L)
        }

        verify(userRepository).findById(1L)
        verifyNoInteractions(userMapper)
    }

    @Test
    fun should_return_user_summary_by_email_with_trim_and_lowercase() {
        val u = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec").also { setId(it, 1L) }
        val s = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)

        `when`(userRepository.findByEmail("afpavon@puce.edu.ec")).thenReturn(u)
        `when`(userMapper.toSummary(u)).thenReturn(s)

        val result = userService.getUserByEmail("  AfPaVoN@puce.edu.ec ")

        assertEquals(1L, result.id)
        assertEquals("afpavon@puce.edu.ec", result.email)
        assertEquals(5000.0, result.totalBalance)

        verify(userRepository).findByEmail("afpavon@puce.edu.ec")
        verify(userMapper).toSummary(u)
    }

    @Test
    fun should_throw_exception_when_user_by_email_not_found() {
        `when`(userRepository.findByEmail("missing@puce.edu.ec")).thenReturn(null)

        assertThrows<ResourceNotFoundException> {
            userService.getUserByEmail(" missing@puce.edu.ec ")
        }

        verify(userRepository).findByEmail("missing@puce.edu.ec")
        verifyNoInteractions(userMapper)
    }

    @Test
    fun should_return_user_summary_by_id() {
        val u = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec").also { setId(it, 1L) }
        val s = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5200.0)

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(u))
        `when`(userMapper.toSummary(u)).thenReturn(s)

        val result = userService.getUserSummary(1L)

        assertEquals(1L, result.id)
        assertEquals("Alexander Pavón", result.name)
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
    fun should_update_user_with_trim_and_lowercase_when_no_duplicate_conflict() {
        val existing = User(name = "Old", email = "old@puce.edu.ec").also { setId(it, 1L) }
        val request = CreateUserRequest("  New  ", "  NEW@puce.edu.ec ")
        val saved = User(name = "New", email = "new@puce.edu.ec").also { setId(it, 1L) }
        val response = UserResponse(1L, "New", "new@puce.edu.ec", listOf(), listOf(), listOf())

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(existing))
        `when`(userRepository.findByEmail("new@puce.edu.ec")).thenReturn(null)
        `when`(userRepository.save(existing)).thenReturn(saved)
        `when`(userMapper.toResponse(saved)).thenReturn(response)

        val result = userService.updateUser(1L, request)

        assertEquals(1L, result.id)
        assertEquals("New", result.name)
        assertEquals("new@puce.edu.ec", result.email)

        verify(userRepository).findByEmail("new@puce.edu.ec")
        verify(userRepository).save(existing)
        verify(userMapper).toResponse(saved)
    }

    @Test
    fun should_throw_duplicate_on_update_when_email_belongs_to_another_user() {
        val current = User(name = "Actual", email = "actual@puce.edu.ec").also { setId(it, 1L) }
        val request = CreateUserRequest("Actual", "nuevo@puce.edu.ec")

        val other = User(name = "Otro", email = "nuevo@puce.edu.ec").also { setId(it, 2L) }

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(current))
        `when`(userRepository.findByEmail("nuevo@puce.edu.ec")).thenReturn(other)

        assertThrows<DuplicateResourceException> {
            userService.updateUser(1L, request)
        }

        verify(userRepository).findById(1L)
        verify(userRepository).findByEmail("nuevo@puce.edu.ec")
        verifyNoMoreInteractions(userRepository)
        verifyNoInteractions(userMapper)
    }

    @Test
    fun should_throw_exception_when_updating_non_existent_user() {
        val request = CreateUserRequest("Test", "test@puce.edu.ec")

        `when`(userRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            userService.updateUser(1L, request)
        }

        verify(userRepository).findById(1L)
        verifyNoMoreInteractions(userRepository)
        verifyNoInteractions(userMapper)
    }

    @Test
    fun should_update_user_when_email_belongs_to_same_user() {
        val existing = User(name = "Old", email = "old@puce.edu.ec").also { setId(it, 1L) }

        val request = CreateUserRequest("  New  ", "  OLD@puce.edu.ec ")

        val same = User(name = "Old", email = "old@puce.edu.ec").also { setId(it, 1L) }

        val saved = User(name = "New", email = "old@puce.edu.ec").also { setId(it, 1L) }
        val response = UserResponse(1L, "New", "old@puce.edu.ec", listOf(), listOf(), listOf())

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(existing))
        `when`(userRepository.findByEmail("old@puce.edu.ec")).thenReturn(same)
        `when`(userRepository.save(existing)).thenReturn(saved)
        `when`(userMapper.toResponse(saved)).thenReturn(response)

        val result = userService.updateUser(1L, request)

        assertEquals(1L, result.id)
        assertEquals("New", result.name)
        assertEquals("old@puce.edu.ec", result.email)

        verify(userRepository).findById(1L)
        verify(userRepository).findByEmail("old@puce.edu.ec")
        verify(userRepository).save(existing)
        verify(userMapper).toResponse(saved)
        verifyNoMoreInteractions(userRepository, userMapper)
    }

    @Test
    fun should_delete_user() {
        val u = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec").also { setId(it, 1L) }

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(u))

        userService.deleteUser(1L)

        verify(userRepository).findById(1L)
        verify(userRepository).delete(u)
        verifyNoMoreInteractions(userRepository)
        verifyNoInteractions(userMapper)
    }

    @Test
    fun should_throw_exception_when_deleting_non_existent_user() {
        `when`(userRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            userService.deleteUser(1L)
        }

        verify(userRepository).findById(1L)
        verifyNoMoreInteractions(userRepository)
        verifyNoInteractions(userMapper)
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

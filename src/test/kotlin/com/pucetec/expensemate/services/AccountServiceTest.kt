package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.DuplicateResourceException
import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.mappers.AccountMapper
import com.pucetec.expensemate.models.entities.Account
import com.pucetec.expensemate.models.entities.User
import com.pucetec.expensemate.models.requests.CreateAccountRequest
import com.pucetec.expensemate.models.responses.AccountResponse
import com.pucetec.expensemate.models.responses.UserSummaryResponse
import com.pucetec.expensemate.repositories.AccountRepository
import com.pucetec.expensemate.repositories.UserRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import java.util.*

class AccountServiceTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var userRepository: UserRepository
    private lateinit var accountMapper: AccountMapper
    private lateinit var accountService: AccountService

    @BeforeEach
    fun setUp() {
        accountRepository = mock(AccountRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        accountMapper = mock(AccountMapper::class.java)
        accountService = AccountService(accountRepository, userRepository, accountMapper)
    }

    @Test
    fun should_create_new_account_and_normalize_number() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        setId(user, 1L)
        val request = CreateAccountRequest("  Banco Pichincha  ", "1234 567-890 ", 1L)

        val saved = Account("Banco Pichincha", "1234567890", 1500.5, user).also { setId(it, 10L) }

        val response = AccountResponse(
            id = 10L, bank = "Banco Pichincha", accountNumber = "1234567890", balance = 1500.5,
            user = UserSummaryResponse(1L, user.name, user.email, 5000.0)
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(accountRepository.existsByAccountNumber("1234567890")).thenReturn(false)
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(saved)
        `when`(accountMapper.toResponse(saved)).thenReturn(response)

        val result = accountService.createAccount(request)

        assertEquals(10L, result.id)
        assertEquals("Banco Pichincha", result.bank)
        assertEquals("1234567890", result.accountNumber)

        verify(accountRepository).existsByAccountNumber("1234567890")

        val captor = ArgumentCaptor.forClass(Account::class.java)
        verify(accountRepository).save(captor.capture())
        assertEquals("Banco Pichincha", captor.value.bank)
        assertEquals("1234567890", captor.value.accountNumber)

        verify(userRepository).findById(1L)
        verify(accountMapper).toResponse(saved)
        verifyNoMoreInteractions(accountRepository, userRepository, accountMapper)
    }

    @Test
    fun should_throw_duplicate_on_create_when_account_number_exists() {
        val request = CreateAccountRequest("Banco", "00 0011-11", 1L)

        `when`(accountRepository.existsByAccountNumber("00001111")).thenReturn(true)

        assertThrows<DuplicateResourceException> {
            `when`(userRepository.findById(1L)).thenReturn(Optional.of(User(name="X", email="x@x.com")))
            accountService.createAccount(request)
        }

        verify(accountRepository).existsByAccountNumber("00001111")
        verifyNoMoreInteractions(accountRepository)
    }

    @Test
    fun should_throw_when_user_not_found_on_create() {
        val request = CreateAccountRequest("Banco", "00001111", 99L)
        `when`(accountRepository.existsByAccountNumber("00001111")).thenReturn(false)
        `when`(userRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            accountService.createAccount(request)
        }

        verify(accountRepository).existsByAccountNumber("00001111")
        verify(userRepository).findById(99L)
        verifyNoMoreInteractions(accountRepository, userRepository)
        verifyNoInteractions(accountMapper)
    }

    @Test
    fun should_return_all_accounts() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec").also { setId(it, 1L) }
        val acc1 = Account("Banco Guayaquil", "0987654321", 100.0, user).also { setId(it, 11L) }
        val acc2 = Account("Banco Pichincha", "1234567890", 200.0, user).also { setId(it, 12L) }

        val resp1 = AccountResponse(11L, "Banco Guayaquil", "0987654321", 100.0,
            UserSummaryResponse(1L, user.name, user.email, 5000.0))
        val resp2 = AccountResponse(12L, "Banco Pichincha", "1234567890", 200.0,
            UserSummaryResponse(1L, user.name, user.email, 5000.0))

        `when`(accountRepository.findAll()).thenReturn(listOf(acc1, acc2))
        `when`(accountMapper.toResponse(acc1)).thenReturn(resp1)
        `when`(accountMapper.toResponse(acc2)).thenReturn(resp2)

        val result = accountService.getAllAccounts()

        assertEquals(2, result.size)
        assertEquals("Banco Guayaquil", result[0].bank)
        assertEquals(200.0, result[1].balance)

        verify(accountRepository).findAll()
        verify(accountMapper).toResponse(acc1)
        verify(accountMapper).toResponse(acc2)
        verifyNoInteractions(userRepository)
    }

    @Test
    fun should_return_account_by_id() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec").also { setId(it, 1L) }
        val account = Account("Banco Loja", "0000111122", 999.99, user).also { setId(it, 21L) }

        val response = AccountResponse(21L, "Banco Loja", "0000111122", 999.99,
            UserSummaryResponse(1L, user.name, user.email, 7000.0))

        `when`(accountRepository.findById(21L)).thenReturn(Optional.of(account))
        `when`(accountMapper.toResponse(account)).thenReturn(response)

        val result = accountService.getAccountById(21L)

        assertEquals("Banco Loja", result.bank)
        assertEquals("0000111122", result.accountNumber)
        assertEquals(999.99, result.balance)

        verify(accountRepository).findById(21L)
        verify(accountMapper).toResponse(account)
        verifyNoInteractions(userRepository)
    }

    @Test
    fun should_throw_exception_when_account_by_id_not_found() {
        `when`(accountRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            accountService.getAccountById(1L)
        }

        verify(accountRepository).findById(1L)
        verifyNoInteractions(accountMapper, userRepository)
    }

    @Test
    fun should_return_accounts_by_user() {
        val userId = 1L
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec").also { setId(it, userId) }

        val acc = Account("Banco Pichincha", "1111222233", 250.0, user).also { setId(it, 31L) }
        val resp = AccountResponse(31L, "Banco Pichincha", "1111222233", 250.0,
            UserSummaryResponse(userId, user.name, user.email, 3333.0))

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(accountRepository.findAllByUserId(userId)).thenReturn(listOf(acc))
        `when`(accountMapper.toResponse(acc)).thenReturn(resp)

        val result = accountService.getAccountsByUser(userId)

        assertEquals(1, result.size)
        assertEquals("Banco Pichincha", result[0].bank)
        assertEquals("1111222233", result[0].accountNumber)

        verify(userRepository).findById(userId)
        verify(accountRepository).findAllByUserId(userId)
        verify(accountMapper).toResponse(acc)
        verifyNoMoreInteractions(accountRepository, userRepository, accountMapper)
    }

    @Test
    fun should_throw_when_user_not_found_in_get_accounts_by_user() {
        val userId = 999L
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            accountService.getAccountsByUser(userId)
        }

        verify(userRepository).findById(userId)
        verifyNoInteractions(accountRepository, accountMapper)
    }

    @Test
    fun should_update_account_and_normalize_number() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec").also { setId(it, 1L) }
        val existing = Account("Banco Viejo", "9999999999", 10.0, user).also { setId(it, 41L) }

        val request = CreateAccountRequest(
            bank = "  Banco Actualizado ",
            accountNumber = "11 11-222233",
            userId = 1L
        )

        val afterSave = Account("Banco Actualizado", "1111222233", 321.0, user).also { setId(it, 41L) }
        val response = AccountResponse(41L, "Banco Actualizado", "1111222233", 321.0,
            UserSummaryResponse(1L, user.name, user.email, 7777.0))

        `when`(accountRepository.findById(41L)).thenReturn(Optional.of(existing))
        `when`(accountRepository.findByAccountNumber("1111222233")).thenReturn(null)
        `when`(accountRepository.save(existing)).thenReturn(afterSave)
        `when`(accountMapper.toResponse(afterSave)).thenReturn(response)

        val result = accountService.updateAccount(41L, request)

        assertEquals("Banco Actualizado", result.bank)
        assertEquals("1111222233", result.accountNumber)
        verify(accountRepository).findByAccountNumber("1111222233")
    }

    @Test
    fun should_throw_duplicate_on_update_when_number_belongs_to_another_account() {
        val user = User(name = "Alex", email = "a@a.com").also { setId(it, 1L) }
        val current = Account("Banco A", "0000", 1.0, user).also { setId(it, 51L) }

        val other = Account("Banco B", "1111", 5.0, user).also { setId(it, 99L) }
        val request = CreateAccountRequest("Banco A", " 11-11 ", 1L)

        `when`(accountRepository.findById(51L)).thenReturn(Optional.of(current))
        `when`(accountRepository.findByAccountNumber("1111")).thenReturn(other)

        assertThrows<DuplicateResourceException> {
            accountService.updateAccount(51L, request)
        }

        verify(accountRepository).findById(51L)
        verify(accountRepository).findByAccountNumber("1111")
        verifyNoMoreInteractions(accountRepository)
        verifyNoInteractions(accountMapper, userRepository)
    }

    @Test
    fun should_throw_exception_when_updating_non_existent_account() {
        val request = CreateAccountRequest("Banco", "00001111", 1L)
        `when`(accountRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            accountService.updateAccount(1L, request)
        }

        verify(accountRepository).findById(1L)
        verifyNoMoreInteractions(accountRepository)
        verifyNoInteractions(accountMapper, userRepository)
    }

    @Test
    fun should_update_account_when_number_belongs_to_same_account() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec").also { setId(it, 1L) }
        val existing = Account("Banco Viejo", "1111222233", 10.0, user).also { setId(it, 41L) }

        val request = CreateAccountRequest(
            bank = "  Banco Actualizado ",
            accountNumber = "11 11-222233",
            userId = 1L
        )

        val afterSave = Account("Banco Actualizado", "1111222233", 321.0, user).also { setId(it, 41L) }
        val response = AccountResponse(
            41L, "Banco Actualizado", "1111222233", 321.0,
            UserSummaryResponse(1L, user.name, user.email, 7777.0)
        )

        `when`(accountRepository.findById(41L)).thenReturn(Optional.of(existing))
        `when`(accountRepository.findByAccountNumber("1111222233")).thenReturn(existing)
        `when`(accountRepository.save(existing)).thenReturn(afterSave)
        `when`(accountMapper.toResponse(afterSave)).thenReturn(response)

        val result = accountService.updateAccount(41L, request)

        assertEquals(41L, result.id)
        assertEquals("Banco Actualizado", result.bank)
        assertEquals("1111222233", result.accountNumber)

        verify(accountRepository).findById(41L)
        verify(accountRepository).findByAccountNumber("1111222233")
        verify(accountRepository).save(existing)
        verify(accountMapper).toResponse(afterSave)
        verifyNoMoreInteractions(accountRepository, accountMapper)
        verifyNoInteractions(userRepository)
    }

    @Test
    fun should_delete_account() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec").also { setId(it, 1L) }
        val account = Account("Banco Internacional", "4444555566", 0.0, user).also { setId(it, 61L) }

        `when`(accountRepository.findById(61L)).thenReturn(Optional.of(account))

        accountService.deleteAccount(61L)

        verify(accountRepository).findById(61L)
        verify(accountRepository).delete(account)
        verifyNoMoreInteractions(accountRepository)
        verifyNoInteractions(userRepository, accountMapper)
    }

    @Test
    fun should_throw_exception_when_deleting_non_existent_account() {
        `when`(accountRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            accountService.deleteAccount(1L)
        }

        verify(accountRepository).findById(1L)
        verifyNoMoreInteractions(accountRepository)
        verifyNoInteractions(userRepository, accountMapper)
    }

    private fun setId(target: Any, id: Long) {
        var clazz: Class<*>? = target.javaClass
        var field = clazz?.declaredFields?.find { it.name == "id" }
        while (field == null && clazz != null) {
            clazz = clazz.superclass
            field = clazz?.declaredFields?.find { it.name == "id" }
        }
        requireNotNull(field) { "No se encontró el campo 'id' en la jerarquía de ${target.javaClass.name}" }
        field.isAccessible = true
        field.set(target, id)
    }
}

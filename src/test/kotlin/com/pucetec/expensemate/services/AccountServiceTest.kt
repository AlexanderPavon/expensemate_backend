package com.pucetec.expensemate.services

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
    fun should_create_new_account() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val request = CreateAccountRequest("Banco Pichincha", "1234567890", 1L)
        val savedAccount = Account("Banco Pichincha", "1234567890", 1500.5, user)

        val userSummary = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
        val response = AccountResponse(1L, "Banco Pichincha", "1234567890", 1500.5, userSummary)

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(savedAccount)
        `when`(accountMapper.toResponse(savedAccount)).thenReturn(response)

        val result = accountService.createAccount(request)

        assertEquals("Banco Pichincha", result.bank)
        assertEquals("1234567890", result.accountNumber)
        assertEquals(1500.5, result.balance)
        assertEquals("Alexander Pavón", result.user.name)
        assertEquals(5000.0, result.user.totalBalance)

        verify(userRepository).findById(1L)
        verify(accountRepository).save(any(Account::class.java))
        verify(accountMapper).toResponse(savedAccount)
        verifyNoMoreInteractions(accountRepository, userRepository, accountMapper)
    }

    @Test
    fun should_throw_when_user_not_found_on_create() {
        val request = CreateAccountRequest("Banco", "00001111", 99L)
        `when`(userRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            accountService.createAccount(request)
        }

        verify(userRepository).findById(99L)
        verifyNoInteractions(accountRepository, accountMapper)
    }

    @Test
    fun should_return_all_accounts() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val acc1 = Account("Banco Guayaquil", "0987654321", 100.0, user)
        val acc2 = Account("Banco Pichincha", "1234567890", 200.0, user)

        val resp1 = AccountResponse(1L, "Banco Guayaquil", "0987654321", 100.0,
            UserSummaryResponse(1L, user.name, user.email, 5000.0))
        val resp2 = AccountResponse(2L, "Banco Pichincha", "1234567890", 200.0,
            UserSummaryResponse(1L, user.name, user.email, 5000.0))

        `when`(accountRepository.findAll()).thenReturn(listOf(acc1, acc2))
        `when`(accountMapper.toResponse(acc1)).thenReturn(resp1)
        `when`(accountMapper.toResponse(acc2)).thenReturn(resp2)

        val result = accountService.getAllAccounts()

        assertEquals(2, result.size)
        assertEquals("Banco Guayaquil", result[0].bank)
        assertEquals(100.0, result[0].balance)
        assertEquals("Banco Pichincha", result[1].bank)
        assertEquals(200.0, result[1].balance)

        verify(accountRepository).findAll()
        verify(accountMapper).toResponse(acc1)
        verify(accountMapper).toResponse(acc2)
        verifyNoMoreInteractions(accountRepository, accountMapper)
        verifyNoInteractions(userRepository)
    }

    @Test
    fun should_return_account_by_id() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val account = Account("Banco Loja", "0000111122", 999.99, user)
        val response = AccountResponse(1L, "Banco Loja", "0000111122", 999.99,
            UserSummaryResponse(1L, user.name, user.email, 7000.0))

        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(account))
        `when`(accountMapper.toResponse(account)).thenReturn(response)

        val result = accountService.getAccountById(1L)

        assertEquals("Banco Loja", result.bank)
        assertEquals("0000111122", result.accountNumber)
        assertEquals(999.99, result.balance)
        assertEquals("Alexander Pavón", result.user.name)
        assertEquals(7000.0, result.user.totalBalance)

        verify(accountRepository).findById(1L)
        verify(accountMapper).toResponse(account)
        verifyNoMoreInteractions(accountRepository, accountMapper)
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
    fun should_update_account() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val existingAccount = Account("Banco Viejo", "9999999999", 10.0, user)
        val request = CreateAccountRequest("Banco Actualizado", "1111222233", 1L)
        val updatedAccount = Account("Banco Actualizado", "1111222233", 321.0, user)

        val response = AccountResponse(1L, "Banco Actualizado", "1111222233", 321.0,
            UserSummaryResponse(1L, user.name, user.email, 7777.0))

        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(existingAccount))
        `when`(accountRepository.save(existingAccount)).thenReturn(updatedAccount)
        `when`(accountMapper.toResponse(updatedAccount)).thenReturn(response)

        val result = accountService.updateAccount(1L, request)

        assertEquals("Banco Actualizado", result.bank)
        assertEquals("1111222233", result.accountNumber)
        assertEquals(321.0, result.balance)
        assertEquals("Alexander Pavón", result.user.name)
        assertEquals(7777.0, result.user.totalBalance)

        verify(accountRepository).findById(1L)
        verify(accountRepository).save(existingAccount)
        verify(accountMapper).toResponse(updatedAccount)
        verifyNoMoreInteractions(accountRepository, accountMapper)
        verifyNoInteractions(userRepository)
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
    fun should_delete_account() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val account = Account("Banco Internacional", "4444555566", 0.0, user)

        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(account))

        accountService.deleteAccount(1L)

        verify(accountRepository).findById(1L)
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
}

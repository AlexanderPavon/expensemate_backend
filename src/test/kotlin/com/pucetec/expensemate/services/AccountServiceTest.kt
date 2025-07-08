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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
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
        val request = CreateAccountRequest(
            bank = "Banco Pichincha",
            accountNumber = "1234567890",
            userId = 1L
        )
        val account = Account("Banco Pichincha", "1234567890", user)
        val userSummary = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        val response = AccountResponse(1L, "Banco Pichincha", "1234567890", userSummary)

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(account)
        `when`(accountMapper.toResponse(account)).thenReturn(response)

        val result = accountService.createAccount(request)

        assertEquals("Banco Pichincha", result.bank)
        assertEquals("1234567890", result.accountNumber)
        assertEquals("Alexander Pavón", result.user.name)
    }

    @Test
    fun should_return_all_accounts() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val account = Account("Banco Guayaquil", "0987654321", user)
        val userSummary = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        val response = AccountResponse(1L, "Banco Guayaquil", "0987654321", userSummary)

        `when`(accountRepository.findAll()).thenReturn(listOf(account))
        `when`(accountMapper.toResponse(account)).thenReturn(response)

        val result = accountService.getAllAccounts()

        assertEquals(1, result.size)
        assertEquals("Banco Guayaquil", result[0].bank)
    }

    @Test
    fun should_return_account_by_id() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val account = Account("Banco Loja", "0000111122", user)
        val userSummary = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        val response = AccountResponse(1L, "Banco Loja", "0000111122", userSummary)

        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(account))
        `when`(accountMapper.toResponse(account)).thenReturn(response)

        val result = accountService.getAccountById(1L)

        assertEquals("Banco Loja", result.bank)
        assertEquals("0000111122", result.accountNumber)
        assertEquals("Alexander Pavón", result.user.name)
    }

    @Test
    fun should_throw_exception_when_account_by_id_not_found() {
        `when`(accountRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            accountService.getAccountById(1L)
        }
    }

    @Test
    fun should_update_account() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val existingAccount = Account("Banco Viejo", "9999999999", user)
        val request = CreateAccountRequest("Banco Actualizado", "1111222233", 1L)
        val updatedAccount = Account("Banco Actualizado", "1111222233", user)
        val userSummary = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        val response = AccountResponse(1L, "Banco Actualizado", "1111222233", userSummary)

        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(existingAccount))
        `when`(accountRepository.save(existingAccount)).thenReturn(updatedAccount)
        `when`(accountMapper.toResponse(updatedAccount)).thenReturn(response)

        val result = accountService.updateAccount(1L, request)

        assertEquals("Banco Actualizado", result.bank)
        assertEquals("1111222233", result.accountNumber)
        assertEquals("Alexander Pavón", result.user.name)
    }

    @Test
    fun should_throw_exception_when_updating_non_existent_account() {
        val request = CreateAccountRequest("Banco", "00001111", 1L)

        `when`(accountRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            accountService.updateAccount(1L, request)
        }
    }

    @Test
    fun should_delete_account() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val account = Account("Banco Internacional", "4444555566", user)

        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(account))

        accountService.deleteAccount(1L)

        verify(accountRepository).delete(account)
    }

    @Test
    fun should_throw_exception_when_deleting_non_existent_account() {
        `when`(accountRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            accountService.deleteAccount(1L)
        }
    }
}

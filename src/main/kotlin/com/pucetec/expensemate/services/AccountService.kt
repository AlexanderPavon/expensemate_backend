package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.DuplicateResourceException
import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.mappers.AccountMapper
import com.pucetec.expensemate.models.requests.CreateAccountRequest
import com.pucetec.expensemate.models.responses.AccountResponse
import com.pucetec.expensemate.repositories.AccountRepository
import com.pucetec.expensemate.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val accountMapper: AccountMapper
) {

    fun createAccount(request: CreateAccountRequest): AccountResponse {
        val bank = request.bank.trim()
        val normalizedAccountNumber = request.accountNumber.trim().replace(Regex("[\\s-]"), "")

        if (accountRepository.existsByAccountNumber(normalizedAccountNumber)) {
            throw DuplicateResourceException("Account number already exists: ${request.accountNumber}")
        }

        val user = userRepository.findById(request.userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val entity = request.toEntity(user).apply {
            this.bank = bank
            this.accountNumber = normalizedAccountNumber
        }

        val saved = accountRepository.save(entity)
        return accountMapper.toResponse(saved)
    }

    fun getAllAccounts(): List<AccountResponse> =
        accountRepository.findAll().map { accountMapper.toResponse(it) }

    fun getAccountById(id: Long): AccountResponse =
        accountMapper.toResponse(
            accountRepository.findById(id).orElseThrow {
                ResourceNotFoundException("Account with ID $id not found")
            }
        )

    fun getAccountsByUser(userId: Long): List<AccountResponse> {
        userRepository.findById(userId).orElseThrow {
            ResourceNotFoundException("User with ID $userId not found")
        }
        return accountRepository.findAllByUserId(userId)
            .map { accountMapper.toResponse(it) }
    }

    fun updateAccount(id: Long, request: CreateAccountRequest): AccountResponse {
        val account = accountRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Account with ID $id not found")
        }

        val bank = request.bank.trim()
        val normalizedAccountNumber = request.accountNumber.trim().replace(Regex("[\\s-]"), "")

        val existing = accountRepository.findByAccountNumber(normalizedAccountNumber)
        if (existing != null && existing.id != account.id) {
            throw DuplicateResourceException("Account number already exists: ${request.accountNumber}")
        }

        account.bank = bank
        account.accountNumber = normalizedAccountNumber

        val saved = accountRepository.save(account)
        return accountMapper.toResponse(saved)
    }

    fun deleteAccount(id: Long) {
        val account = accountRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Account with ID $id not found")
        }
        accountRepository.delete(account)
    }
}

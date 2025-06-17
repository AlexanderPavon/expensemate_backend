package com.pucetec.expensemate.services

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
        val user = userRepository.findById(request.userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val account = request.toEntity(user)
        return accountMapper.toResponse(accountRepository.save(account))
    }

    fun getAllAccounts(): List<AccountResponse> =
        accountRepository.findAll().map { accountMapper.toResponse(it) }

    fun getAccountById(id: Long): AccountResponse =
        accountMapper.toResponse(
            accountRepository.findById(id).orElseThrow {
                ResourceNotFoundException("Account with ID $id not found")
            }
        )

    fun updateAccount(id: Long, request: CreateAccountRequest): AccountResponse {
        val account = accountRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Account with ID $id not found")
        }
        account.bank = request.bank
        account.accountNumber = request.accountNumber
        return accountMapper.toResponse(accountRepository.save(account))
    }

    fun deleteAccount(id: Long) {
        val account = accountRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Account with ID $id not found")
        }
        accountRepository.delete(account)
    }
}

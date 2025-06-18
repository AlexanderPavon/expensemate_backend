package com.pucetec.expensemate.controllers

import com.pucetec.expensemate.models.requests.CreateAccountRequest
import com.pucetec.expensemate.models.responses.AccountResponse
import com.pucetec.expensemate.routes.Routes
import com.pucetec.expensemate.services.AccountService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Routes.ACCOUNTS)
class AccountController(
    private val accountService: AccountService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAccount(@RequestBody request: CreateAccountRequest): AccountResponse =
        accountService.createAccount(request)

    @GetMapping
    fun getAllAccounts(): List<AccountResponse> = accountService.getAllAccounts()

    @GetMapping("/{id}")
    fun getAccountById(@PathVariable id: Long): AccountResponse =
        accountService.getAccountById(id)

    @PutMapping("/{id}")
    fun updateAccount(@PathVariable id: Long, @RequestBody request: CreateAccountRequest): AccountResponse =
        accountService.updateAccount(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAccount(@PathVariable id: Long) = accountService.deleteAccount(id)
}

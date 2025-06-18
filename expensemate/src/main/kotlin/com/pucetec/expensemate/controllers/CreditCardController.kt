package com.pucetec.expensemate.controllers

import com.pucetec.expensemate.models.requests.CreateCreditCardRequest
import com.pucetec.expensemate.models.responses.CreditCardResponse
import com.pucetec.expensemate.routes.Routes
import com.pucetec.expensemate.services.CreditCardService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Routes.CREDIT_CARDS)
class CreditCardController(
    private val creditCardService: CreditCardService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCreditCard(@RequestBody request: CreateCreditCardRequest): CreditCardResponse =
        creditCardService.createCard(request)

    @GetMapping
    fun getAllCards(): List<CreditCardResponse> = creditCardService.getAllCards()

    @GetMapping("/{id}")
    fun getCardById(@PathVariable id: Long): CreditCardResponse =
        creditCardService.getCardById(id)

    @PutMapping("/{id}")
    fun updateCard(@PathVariable id: Long, @RequestBody request: CreateCreditCardRequest): CreditCardResponse =
        creditCardService.updateCard(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCard(@PathVariable id: Long) = creditCardService.deleteCard(id)
}
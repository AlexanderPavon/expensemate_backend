package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.mappers.CreditCardMapper
import com.pucetec.expensemate.models.requests.CreateCreditCardRequest
import com.pucetec.expensemate.models.responses.CreditCardResponse
import com.pucetec.expensemate.repositories.CreditCardRepository
import com.pucetec.expensemate.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class CreditCardService(
    private val creditCardRepository: CreditCardRepository,
    private val userRepository: UserRepository,
    private val creditCardMapper: CreditCardMapper
) {
    fun createCard(request: CreateCreditCardRequest): CreditCardResponse {
        val user = userRepository.findById(request.userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val creditCard = request.toEntity(user)
        return creditCardMapper.toResponse(creditCardRepository.save(creditCard))
    }

    fun getAllCards(): List<CreditCardResponse> =
        creditCardRepository.findAll().map { creditCardMapper.toResponse(it) }

    fun getCardById(id: Long): CreditCardResponse =
        creditCardMapper.toResponse(
            creditCardRepository.findById(id).orElseThrow {
                ResourceNotFoundException("Credit card with ID $id not found")
            }
        )

    fun getCardsByUser(userId: Long): List<CreditCardResponse> {
        userRepository.findById(userId).orElseThrow {
            ResourceNotFoundException("User with ID $userId not found")
        }
        return creditCardRepository.findAllByUserId(userId)
            .map { creditCardMapper.toResponse(it) }
    }

    fun updateCard(id: Long, request: CreateCreditCardRequest): CreditCardResponse {
        val creditCard = creditCardRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Credit card with ID $id not found")
        }
        creditCard.name = request.name
        creditCard.lastFourDigits = request.lastFourDigits
        creditCard.courtDate = request.courtDate
        creditCard.maximumPaymentDate = request.maximumPaymentDate
        return creditCardMapper.toResponse(creditCardRepository.save(creditCard))
    }

    fun deleteCard(id: Long) {
        val card = creditCardRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Credit card with ID $id not found")
        }
        creditCardRepository.delete(card)
    }
}

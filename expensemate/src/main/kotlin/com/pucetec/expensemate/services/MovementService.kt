package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.mappers.MovementMapper
import com.pucetec.expensemate.models.requests.CreateMovementRequest
import com.pucetec.expensemate.models.responses.MovementResponse
import com.pucetec.expensemate.repositories.*
import org.springframework.stereotype.Service

@Service
class MovementService(
    private val movementRepository: MovementRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val creditCardRepository: CreditCardRepository,
    private val accountRepository: AccountRepository,
    private val movementMapper: MovementMapper
) {
    fun createMovement(request: CreateMovementRequest): MovementResponse {
        val user = userRepository.findById(request.userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { ResourceNotFoundException("Category not found") }
        val creditCard = request.creditCardId?.let {
            creditCardRepository.findById(it).orElseThrow { ResourceNotFoundException("CreditCard not found") }
        }
        val account = request.accountId?.let {
            accountRepository.findById(it).orElseThrow { ResourceNotFoundException("Account not found") }
        }
        val movement = request.toEntity(user, category, creditCard, account)
        return movementMapper.toResponse(movementRepository.save(movement))
    }

    fun getAllMovements(): List<MovementResponse> =
        movementRepository.findAll().map { movementMapper.toResponse(it) }

    fun getMovementById(id: Long): MovementResponse =
        movementMapper.toResponse(
            movementRepository.findById(id).orElseThrow {
                ResourceNotFoundException("Movement with ID $id not found")
            }
        )

    fun updateMovement(id: Long, request: CreateMovementRequest): MovementResponse {
        val movement = movementRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Movement with ID $id not found")
        }
        val user = userRepository.findById(request.userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { ResourceNotFoundException("Category not found") }
        val creditCard = request.creditCardId?.let {
            creditCardRepository.findById(it).orElseThrow { ResourceNotFoundException("CreditCard not found") }
        }
        val account = request.accountId?.let {
            accountRepository.findById(it).orElseThrow { ResourceNotFoundException("Account not found") }
        }

        movement.type = request.type
        movement.amount = request.amount
        movement.date = request.date
        movement.note = request.note
        movement.user = user
        movement.category = category
        movement.creditCard = creditCard
        movement.account = account

        return movementMapper.toResponse(movementRepository.save(movement))
    }

    fun deleteMovement(id: Long) {
        val movement = movementRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Movement with ID $id not found")
        }
        movementRepository.delete(movement)
    }
}

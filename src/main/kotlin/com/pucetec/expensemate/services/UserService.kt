package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.DuplicateResourceException
import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.mappers.UserMapper
import com.pucetec.expensemate.models.requests.CreateUserRequest
import com.pucetec.expensemate.models.responses.UserResponse
import com.pucetec.expensemate.models.responses.UserSummaryResponse
import com.pucetec.expensemate.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) {
    fun createUser(request: CreateUserRequest): UserResponse {
        val normalizedName = request.name.trim()
        val normalizedEmail = request.email.trim().lowercase()

        userRepository.findByEmail(normalizedEmail)?.let {
            throw DuplicateResourceException("Email already registered: $normalizedEmail")
        }

        val entity = request.toEntity().apply {
            name = normalizedName
            email = normalizedEmail
        }

        val saved = userRepository.save(entity)
        return userMapper.toResponse(saved)
    }

    fun getAllUsers(): List<UserResponse> =
        userRepository.findAll().map { userMapper.toResponse(it) }

    fun getUserById(id: Long): UserResponse =
        userMapper.toResponse(
            userRepository.findById(id).orElseThrow {
                ResourceNotFoundException("User with ID $id not found")
            }
        )

    fun getUserByEmail(email: String): UserSummaryResponse {
        val normalizedEmail = email.trim().lowercase()
        val user = userRepository.findByEmail(normalizedEmail)
            ?: throw ResourceNotFoundException("User with email $normalizedEmail not found")
        return userMapper.toSummary(user)
    }

    fun getUserSummary(id: Long): UserSummaryResponse {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with ID $id not found") }

        return userMapper.toSummary(user)
    }

    fun updateUser(id: Long, request: CreateUserRequest): UserResponse {
        val user = userRepository.findById(id).orElseThrow {
            ResourceNotFoundException("User with ID $id not found")
        }

        val normalizedName = request.name.trim()
        val normalizedEmail = request.email.trim().lowercase()

        val existing = userRepository.findByEmail(normalizedEmail)
        if (existing != null && existing.id != user.id) {
            throw DuplicateResourceException("Email already registered: $normalizedEmail")
        }

        user.name = normalizedName
        user.email = normalizedEmail

        return userMapper.toResponse(userRepository.save(user))
    }

    fun deleteUser(id: Long) {
        val user = userRepository.findById(id).orElseThrow {
            ResourceNotFoundException("User with ID $id not found")
        }
        userRepository.delete(user)
    }
}

package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.mappers.UserMapper
import com.pucetec.expensemate.models.requests.CreateUserRequest
import com.pucetec.expensemate.models.responses.UserResponse
import com.pucetec.expensemate.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) {
    fun createUser(request: CreateUserRequest): UserResponse {
        val user = userRepository.save(request.toEntity())
        return userMapper.toResponse(user)
    }

    fun getAllUsers(): List<UserResponse> =
        userRepository.findAll().map { userMapper.toResponse(it) }

    fun getUserById(id: Long): UserResponse =
        userMapper.toResponse(
            userRepository.findById(id).orElseThrow {
                ResourceNotFoundException("User with ID $id not found")
            }
        )

    fun updateUser(id: Long, request: CreateUserRequest): UserResponse {
        val user = userRepository.findById(id).orElseThrow {
            ResourceNotFoundException("User with ID $id not found")
        }
        user.name = request.name
        user.email = request.email
        return userMapper.toResponse(userRepository.save(user))
    }

    fun deleteUser(id: Long) {
        val user = userRepository.findById(id).orElseThrow {
            ResourceNotFoundException("User with ID $id not found")
        }
        userRepository.delete(user)
    }
}

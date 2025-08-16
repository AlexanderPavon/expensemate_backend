package com.pucetec.expensemate.models.requests

import com.pucetec.expensemate.models.entities.User
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.PropertyNamingStrategies

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateUserRequest(
    val name: String,
    val email: String
){
    fun toEntity(): User {
        return User(
            name = this.name,
            email = this.email
        )
    }
}

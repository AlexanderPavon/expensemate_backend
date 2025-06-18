package com.pucetec.expensemate.models.requests

import com.pucetec.expensemate.models.entities.User

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

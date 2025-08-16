package com.pucetec.expensemate.models.requests

import com.pucetec.expensemate.models.entities.Account
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.PropertyNamingStrategies

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateAccountRequest(
    val bank: String,
    val accountNumber: String,
    val userId: Long
){
    fun toEntity(user: com.pucetec.expensemate.models.entities.User): Account {
        return Account(
            bank = this.bank,
            accountNumber = this.accountNumber,
            user = user
        )
    }
}

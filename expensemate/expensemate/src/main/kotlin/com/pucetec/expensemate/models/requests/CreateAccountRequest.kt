package com.pucetec.expensemate.models.requests

import com.fasterxml.jackson.annotation.JsonProperty
import com.pucetec.expensemate.models.entities.Account

data class CreateAccountRequest(
    val bank: String,
    @JsonProperty("account_number")
    val accountNumber: String,
    @JsonProperty("user_id")
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

package com.pucetec.expensemate.models.requests

import com.pucetec.expensemate.models.entities.CreditCard
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.PropertyNamingStrategies

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateCreditCardRequest(
    val name: String,
    val lastFourDigits: String,
    val courtDate: String,
    val maximumPaymentDate: String,
    val userId: Long
){
    fun toEntity(user: com.pucetec.expensemate.models.entities.User): CreditCard {
        return CreditCard(
            name = this.name,
            lastFourDigits = this.lastFourDigits,
            courtDate = this.courtDate,
            maximumPaymentDate = this.maximumPaymentDate,
            user = user
        )
    }
}

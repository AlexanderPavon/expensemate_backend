package com.pucetec.expensemate.models.requests

import com.fasterxml.jackson.annotation.JsonProperty
import com.pucetec.expensemate.models.entities.Category
import com.pucetec.expensemate.models.entities.CreditCard

data class CreateCreditCardRequest(
    val name: String,
    @JsonProperty("last_four_digits")
    val lastFourDigits: String,
    @JsonProperty("court_date")
    val courtDate: String,
    @JsonProperty("maximum_payment_date")
    val maximumPaymentDate: String,
    @JsonProperty("user_id")
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

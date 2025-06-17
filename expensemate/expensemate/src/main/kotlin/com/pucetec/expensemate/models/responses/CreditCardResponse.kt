package com.pucetec.expensemate.models.responses

data class CreditCardResponse(
    val id: Long,
    val name: String,
    val lastFourDigits: String,
    val courtDate: String,
    val maximumPaymentDate: String,
    val user: UserSummaryResponse
)
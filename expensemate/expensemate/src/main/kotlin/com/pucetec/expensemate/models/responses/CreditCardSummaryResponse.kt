package com.pucetec.expensemate.models.responses

data class CreditCardSummaryResponse(
    val id: Long,
    val name: String,
    val lastFourDigits: String,
    val courtDate: String,
    val maximumPaymentDate: String
)
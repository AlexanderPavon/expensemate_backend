package com.pucetec.expensemate.models.responses

import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.PropertyNamingStrategies

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreditCardSummaryResponse(
    val id: Long,
    val name: String,
    val lastFourDigits: String,
    val courtDate: String,
    val maximumPaymentDate: String
)
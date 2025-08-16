package com.pucetec.expensemate.models.responses

import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.PropertyNamingStrategies

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val movements: List<MovementSummaryResponse>,
    val creditCards: List<CreditCardSummaryResponse>,
    val accounts: List<AccountSummaryResponse>
)

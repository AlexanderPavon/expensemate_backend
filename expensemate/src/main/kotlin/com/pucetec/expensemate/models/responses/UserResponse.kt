package com.pucetec.expensemate.models.responses

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val movements: List<MovementSummaryResponse>,
    val creditCards: List<CreditCardSummaryResponse>,
    val accounts: List<AccountSummaryResponse>
)

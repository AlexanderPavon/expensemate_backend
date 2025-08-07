package com.pucetec.expensemate.models.responses

data class UserSummaryResponse(
    val id: Long,
    val name: String,
    val email: String,
    val totalBalance: Double
)

package com.pucetec.expensemate.models.responses

data class AccountResponse(
    val id: Long,
    val bank: String,
    val accountNumber: String,
    val user: UserSummaryResponse
)
package com.pucetec.expensemate.models.responses

data class AccountSummaryResponse(
    val id: Long,
    val bank: String,
    val accountNumber: String,
    val balance: Double,
)

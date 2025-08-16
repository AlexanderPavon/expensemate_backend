package com.pucetec.expensemate.models.responses

import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.PropertyNamingStrategies

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AccountSummaryResponse(
    val id: Long,
    val bank: String,
    val accountNumber: String,
    val balance: Double,
)

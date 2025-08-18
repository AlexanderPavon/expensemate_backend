package com.pucetec.expensemate.models.requests

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MarkPaidRequest(
    val creditCardId: Long,
    val cycle: String
)

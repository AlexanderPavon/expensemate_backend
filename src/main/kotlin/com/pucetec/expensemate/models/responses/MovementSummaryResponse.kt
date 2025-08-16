package com.pucetec.expensemate.models.responses

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.PropertyNamingStrategies

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MovementSummaryResponse(
    val id: Long,
    val type: String, // "ingreso" o "egreso"
    val amount: Double,
    val date: LocalDateTime,
    val note: String?, // opcional
    val category: CategoryResponse,
    val creditCard: CreditCardResponse?, // opcional
    val account: AccountResponse? // opcional
)

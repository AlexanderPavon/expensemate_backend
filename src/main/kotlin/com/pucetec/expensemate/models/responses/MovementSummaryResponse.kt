package com.pucetec.expensemate.models.responses

import java.time.LocalDateTime

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

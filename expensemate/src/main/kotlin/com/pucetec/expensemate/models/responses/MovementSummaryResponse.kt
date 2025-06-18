package com.pucetec.expensemate.models.responses

import java.time.LocalDate

data class MovementSummaryResponse(
    val id: Long,
    val type: String, // "ingreso" o "egreso"
    val amount: Double,
    val date: LocalDate,
    val note: String?, // opcional
    val category: CategoryResponse,
    val creditCard: CreditCardResponse?, // opcional
    val account: AccountResponse? // opcional
)

package com.pucetec.expensemate.models.responses

import java.time.LocalDate

data class MovementResponse(
    val id: Long,
    val type: String, // "ingreso" o "egreso"
    val amount: Double,
    val date: LocalDate,
    val note: String?, // opcional
    val category: CategoryResponse,
    val creditCard: CreditCardSummaryResponse?, // opcional
    val account: AccountSummaryResponse?, // opcional
    val user: UserSummaryResponse
)
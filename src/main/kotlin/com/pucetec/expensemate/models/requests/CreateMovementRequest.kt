package com.pucetec.expensemate.models.requests

import com.pucetec.expensemate.models.entities.Movement
import com.pucetec.expensemate.models.entities.MovementType
import java.time.LocalDateTime
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.PropertyNamingStrategies

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateMovementRequest(
    val type: MovementType,// "ingreso" o "egreso"
    val amount: Double,
    val note: String?, // opcional
    val userId: Long,
    val categoryId: Long,
    val creditCardId: Long?, // opcional
    val accountId: Long?     // opcional
){
    fun toEntity(
        user: com.pucetec.expensemate.models.entities.User,
        category: com.pucetec.expensemate.models.entities.Category,
        creditCard: com.pucetec.expensemate.models.entities.CreditCard?,
        account: com.pucetec.expensemate.models.entities.Account?
    ): Movement {
        return Movement(
            type = this.type,
            amount = this.amount,
            date = LocalDateTime.now(),
            note = this.note,
            user = user,
            category = category,
            creditCard = creditCard,
            account = account,
        )
    }
}

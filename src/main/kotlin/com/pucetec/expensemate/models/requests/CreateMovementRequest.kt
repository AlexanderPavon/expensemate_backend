package com.pucetec.expensemate.models.requests

import com.fasterxml.jackson.annotation.JsonProperty
import com.pucetec.expensemate.models.entities.Movement
import com.pucetec.expensemate.models.entities.MovementType
import java.time.LocalDate

data class CreateMovementRequest(
    val type: MovementType,// "ingreso" o "egreso"
    val amount: Double,
    val date: LocalDate,
    val note: String?, // opcional
    @JsonProperty("user_id")
    val userId: Long,
    @JsonProperty("category_id")
    val categoryId: Long,
    @JsonProperty("credit_card_id")
    val creditCardId: Long?, // opcional
    @JsonProperty("account_id")
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
            date = this.date,
            note = this.note,
            user = user,
            category = category,
            creditCard = creditCard,
            account = account,
        )
    }
}

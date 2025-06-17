package com.pucetec.expensemate.mappers

import com.pucetec.expensemate.models.entities.Movement
import com.pucetec.expensemate.models.responses.*
import org.springframework.stereotype.Component

@Component
class MovementMapper(
    private val userMapper: UserMapper
) {
    fun toResponse(movement: Movement): MovementResponse {
        return MovementResponse(
            id = movement.id,
            type = movement.type,
            amount = movement.amount,
            date = movement.date,
            note = movement.note,
            category = CategoryResponse(
                id = movement.category.id,
                name = movement.category.name
            ),
            creditCard = movement.creditCard?.let {
                CreditCardSummaryResponse(
                    id = it.id,
                    name = it.name,
                    lastFourDigits = it.lastFourDigits,
                    courtDate = it.courtDate,
                    maximumPaymentDate = it.maximumPaymentDate
                )
            },
            account = movement.account?.let {
                AccountSummaryResponse(
                    id = it.id,
                    bank = it.bank,
                    accountNumber = it.accountNumber
                )
            },
            user = userMapper.toSummary(movement.user)
        )
    }
}

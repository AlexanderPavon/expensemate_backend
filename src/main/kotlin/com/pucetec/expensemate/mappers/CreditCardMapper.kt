package com.pucetec.expensemate.mappers

import com.pucetec.expensemate.models.entities.CreditCard
import com.pucetec.expensemate.models.responses.*
import org.springframework.stereotype.Component

@Component
class CreditCardMapper(
    private val userMapper: UserMapper
) {
    fun toResponse(card: CreditCard): CreditCardResponse {
        return CreditCardResponse(
            id = card.id,
            name = card.name,
            lastFourDigits = card.lastFourDigits,
            courtDate = card.courtDate,
            maximumPaymentDate = card.maximumPaymentDate,
            user = userMapper.toSummary(card.user)
        )
    }

    fun toSummary(card: CreditCard): CreditCardSummaryResponse {
        return CreditCardSummaryResponse(
            id = card.id,
            name = card.name,
            lastFourDigits = card.lastFourDigits,
            courtDate = card.courtDate,
            maximumPaymentDate = card.maximumPaymentDate
        )
    }
}

package com.pucetec.expensemate.mappers

import com.pucetec.expensemate.models.entities.User
import com.pucetec.expensemate.models.responses.*
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toSummary(user: User): UserSummaryResponse {
        return UserSummaryResponse(
            id = user.id,
            name = user.name,
            email = user.email
        )
    }

    fun toResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            name = user.name,
            email = user.email,
            movements = user.movements.map { movement ->
                MovementSummaryResponse(
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
                        CreditCardResponse(
                            id = it.id,
                            name = it.name,
                            lastFourDigits = it.lastFourDigits,
                            courtDate = it.courtDate,
                            maximumPaymentDate = it.maximumPaymentDate,
                            user = toSummary(it.user)
                        )
                    },
                    account = movement.account?.let {
                        AccountResponse(
                            id = it.id,
                            bank = it.bank,
                            accountNumber = it.accountNumber,
                            user = toSummary(it.user)
                        )
                    }
                )
            },
            creditCards = user.creditCards.map { card ->
                CreditCardSummaryResponse(
                    id = card.id,
                    name = card.name,
                    lastFourDigits = card.lastFourDigits,
                    courtDate = card.courtDate,
                    maximumPaymentDate = card.maximumPaymentDate
                )
            },
            accounts = user.accounts.map { account ->
                AccountSummaryResponse(
                    id = account.id,
                    bank = account.bank,
                    accountNumber = account.accountNumber
                )
            }
        )
    }
}

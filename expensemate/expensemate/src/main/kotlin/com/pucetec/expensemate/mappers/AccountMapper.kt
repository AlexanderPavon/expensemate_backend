package com.pucetec.expensemate.mappers

import com.pucetec.expensemate.models.entities.Account
import com.pucetec.expensemate.models.responses.*
import org.springframework.stereotype.Component

@Component
class AccountMapper(
    private val userMapper: UserMapper
) {
    fun toResponse(account: Account): AccountResponse {
        return AccountResponse(
            id = account.id,
            bank = account.bank,
            accountNumber = account.accountNumber,
            user = userMapper.toSummary(account.user)
        )
    }

    fun toSummary(account: Account): AccountSummaryResponse {
        return AccountSummaryResponse(
            id = account.id,
            bank = account.bank,
            accountNumber = account.accountNumber
        )
    }
}

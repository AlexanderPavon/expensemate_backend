package com.pucetec.expensemate.models.responses

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.annotation.JsonFormat
import com.pucetec.expensemate.models.entities.ReminderStatus
import com.pucetec.expensemate.models.entities.ReminderType
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PaymentReminderResponse(
    val id: Long,
    val creditCardId: Long,
    val creditCardName: String,
    val lastFourDigits: String,
    val cycle: String,
    val type: ReminderType,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val scheduledDate: LocalDate,
    val status: ReminderStatus,
    val message: String
)

package com.pucetec.expensemate.mappers

import com.pucetec.expensemate.models.entities.PaymentReminder
import com.pucetec.expensemate.models.entities.ReminderType
import com.pucetec.expensemate.models.responses.PaymentReminderResponse

fun PaymentReminder.toResponse(): PaymentReminderResponse =
    PaymentReminderResponse(
        id = requireNotNull(id) { "PaymentReminder.id no debe ser null" },
        creditCardId = requireNotNull(creditCard.id) { "CreditCard.id no debe ser null" },
        creditCardName = creditCard.name,
        lastFourDigits = creditCard.lastFourDigits,
        cycle = cycleYyyyMm,
        type = type,
        scheduledDate = scheduledDate,
        status = status,
        message = reminderMessage(type)
    )

/** Helper para producir el mensaje amigable según el tipo de recordatorio. */
private fun reminderMessage(type: ReminderType): String = when (type) {
    ReminderType.AFTER_CUT  -> "Se generó tu estado de cuenta (corte)."
    ReminderType.BEFORE_DUE -> "Tu pago vence en 3 días."
    ReminderType.ON_DUE     -> "Hoy vence tu pago."
}

fun List<PaymentReminder>.toResponses(): List<PaymentReminderResponse> = map { it.toResponse() }

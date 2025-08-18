package com.pucetec.expensemate.controllers

import com.pucetec.expensemate.mappers.toResponses
import com.pucetec.expensemate.models.requests.MarkPaidRequest
import com.pucetec.expensemate.models.responses.PaymentReminderResponse
import com.pucetec.expensemate.services.ReminderService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("\${api.prefix:/api/expensemate}/reminders")
class ReminderController(
    private val reminderService: ReminderService
) {
    @GetMapping("/by-user/{userId}")
    fun getPending(@PathVariable userId: Long): List<PaymentReminderResponse> =
        reminderService.getPendingByUser(userId).toResponses()

    @PostMapping("/mark-paid/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun markPaid(
        @PathVariable userId: Long,
        @RequestBody body: MarkPaidRequest
    ) {
        reminderService.markCyclePaid(userId, body.creditCardId, body.cycle)
    }

}

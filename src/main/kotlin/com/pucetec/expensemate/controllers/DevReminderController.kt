package com.pucetec.expensemate.controllers

import com.pucetec.expensemate.services.ReminderService
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Profile("dev")
@RestController
@RequestMapping("\${api.prefix:/api/expensemate}/reminders/dev")
class DevReminderController(
    private val service: ReminderService
) {
    // GET /api/expensemate/reminders/dev/generate?date=2025-08-17
    @GetMapping("/generate")
    fun generateFor(@RequestParam date: String): String {
        val parsed = LocalDate.parse(date)
        service.generateFor(parsed)
        return "Generated reminders for $parsed"
    }
}

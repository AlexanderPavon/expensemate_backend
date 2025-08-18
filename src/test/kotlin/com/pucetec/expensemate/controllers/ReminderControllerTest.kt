package com.pucetec.expensemate.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.models.entities.*
import com.pucetec.expensemate.models.requests.MarkPaidRequest
import com.pucetec.expensemate.services.ReminderService
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import kotlin.test.assertEquals

@WebMvcTest(ReminderController::class)
@Import(ReminderMockConfig::class)
class ReminderControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var reminderService: ReminderService

    private lateinit var objectMapper: ObjectMapper

    private val baseUrl = "/api/expensemate/reminders"

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    // Helpers para crear entidades mínimas
    private fun user(id: Long = 1L) =
        User(name = "Alex", email = "alex@example.com").also { setId(it, id) }

    private fun card(id: Long = 22L, owner: User = user()) =
        CreditCard(
            name = "Visa",
            lastFourDigits = "1234",
            courtDate = 5,
            maximumPaymentDate = 20,
            user = owner
        ).also { setId(it, id) }

    private fun reminder(
        id: Long,
        u: User,
        cc: CreditCard,
        cycle: String,
        type: ReminderType,
        status: ReminderStatus,
        date: java.time.LocalDate
    ) = PaymentReminder(
        user = u,
        creditCard = cc,
        cycleYyyyMm = cycle,
        type = type,
        scheduledDate = date,
        status = status
    ).also { setId(it, id) }

    private fun setId(target: Any, id: Long) {
        var c: Class<*>? = target.javaClass
        var f = c?.declaredFields?.find { it.name == "id" }
        while (f == null && c != null) {
            c = c.superclass
            f = c?.declaredFields?.find { it.name == "id" }
        }
        requireNotNull(f)
        f.isAccessible = true
        f.set(target, id)
    }

    @Test
    fun should_return_pending_by_user() {
        val u = user(1L)
        val cc = card(22L, u)
        val cycle = "2025-08"
        val r1 = reminder(
            id = 100L, u = u, cc = cc, cycle = cycle,
            type = ReminderType.BEFORE_DUE, status = ReminderStatus.PENDING,
            date = java.time.LocalDate.of(2025, 8, 17)
        )
        val r2 = reminder(
            id = 101L, u = u, cc = cc, cycle = cycle,
            type = ReminderType.ON_DUE, status = ReminderStatus.PENDING,
            date = java.time.LocalDate.of(2025, 8, 20)
        )

        `when`(reminderService.getPendingByUser(1L)).thenReturn(listOf(r1, r2))

        val result = mockMvc.get("$baseUrl/by-user/1")
            .andExpect {
                status { isOk() }
                // tamaño del array
                jsonPath("$.size()") { value(2) }
                // verificaciones suaves para no acoplarse a todos los campos del mapper
                content { string(containsString("BEFORE_DUE")) }
                content { string(containsString("ON_DUE")) }
                content { string(containsString("2025-08")) }
            }
            .andReturn()

        assertEquals(200, result.response.status)
        verify(reminderService).getPendingByUser(1L)
    }

    @Test
    fun should_mark_paid_and_return_204() {
        val body = MarkPaidRequest(
            creditCardId = 22L,
            cycle = "2025-08"
        )

        doNothing().`when`(reminderService).markCyclePaid(1L, 22L, "2025-08")

        val json = objectMapper.writeValueAsString(body)

        val result = mockMvc.post("$baseUrl/mark-paid/1") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isNoContent() }
        }.andReturn()

        assertEquals(204, result.response.status)
        verify(reminderService).markCyclePaid(1L, 22L, "2025-08")
    }

    @Test
    fun should_return_404_when_mark_paid_card_not_found() {
        val body = MarkPaidRequest(creditCardId = 999L, cycle = "2025-08")
        doThrow(ResourceNotFoundException("Credit card not found"))
            .`when`(reminderService).markCyclePaid(1L, 999L, "2025-08")

        val json = objectMapper.writeValueAsString(body)

        val result = mockMvc.post("$baseUrl/mark-paid/1") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error") { value("Credit card not found") }
        }.andReturn()

        assertEquals(404, result.response.status)
    }
}

@TestConfiguration
class ReminderMockConfig {
    @Bean
    fun reminderService(): ReminderService = mock(ReminderService::class.java)
}

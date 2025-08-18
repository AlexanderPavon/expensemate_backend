package com.pucetec.expensemate.controllers

import com.pucetec.expensemate.services.ReminderService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.assertEquals
import java.time.LocalDate

@ActiveProfiles("dev")
@WebMvcTest(DevReminderController::class)
@Import(DevReminderMockConfig::class)
class DevReminderControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var reminderService: ReminderService

    private val baseUrl = "/api/expensemate/reminders/dev"

    @BeforeEach
    fun resetMocks() {
        reset(reminderService)
    }

    @Test
    fun should_generate_when_valid_date_param() {
        val date = "2025-08-17"
        doNothing().`when`(reminderService).generateFor(LocalDate.parse(date))

        val result = mockMvc.get("$baseUrl/generate") {
            param("date", date)
            accept = MediaType.TEXT_PLAIN
        }.andExpect {
            status { isOk() }
            content { string("Generated reminders for 2025-08-17") }
        }.andReturn()

        assertEquals(200, result.response.status)
        verify(reminderService).generateFor(LocalDate.of(2025, 8, 17))
        verifyNoMoreInteractions(reminderService)
    }

    @Test
    fun should_return_5xx_when_missing_date_param() {
        val result = mockMvc.get("$baseUrl/generate")
            .andExpect {
                status { is5xxServerError() } // tu handler actual lo está mapeando a 500
            }.andReturn()

        assertEquals(true, result.response.status in 500..599)
        // No se llama al servicio porque ni siquiera entra al método
        verifyNoInteractions(reminderService)
    }

    @Test
    fun should_return_5xx_when_invalid_date_format() {
        val result = mockMvc.get("$baseUrl/generate") {
            param("date", "17-08-2025") // formato inválido
        }.andExpect {
            status { is5xxServerError() } // tu handler actual devuelve 500
        }.andReturn()

        assertEquals(true, result.response.status in 500..599)
        // La excepción ocurre al parsear antes de invocar al servicio
        verifyNoInteractions(reminderService)
    }
}

@TestConfiguration
class DevReminderMockConfig {
    @Bean
    fun reminderService(): ReminderService = mock(ReminderService::class.java)
}

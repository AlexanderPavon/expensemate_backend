package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.mappers.CreditCardMapper
import com.pucetec.expensemate.models.entities.CreditCard
import com.pucetec.expensemate.models.entities.User
import com.pucetec.expensemate.models.requests.CreateCreditCardRequest
import com.pucetec.expensemate.models.responses.CreditCardResponse
import com.pucetec.expensemate.models.responses.UserSummaryResponse
import com.pucetec.expensemate.repositories.CreditCardRepository
import com.pucetec.expensemate.repositories.UserRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.*

class CreditCardServiceTest {

    private lateinit var creditCardRepository: CreditCardRepository
    private lateinit var userRepository: UserRepository
    private lateinit var creditCardMapper: CreditCardMapper
    private lateinit var creditCardService: CreditCardService

    @BeforeEach
    fun setUp() {
        creditCardRepository = mock(CreditCardRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        creditCardMapper = mock(CreditCardMapper::class.java)
        creditCardService = CreditCardService(creditCardRepository, userRepository, creditCardMapper)
    }

    @Test
    fun should_create_new_credit_card() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val request = CreateCreditCardRequest(
            name = "Visa Alex",
            lastFourDigits = "1234",
            courtDate = "2025-07-15",
            maximumPaymentDate = "2025-07-30",
            userId = 1L
        )
        val creditCard = CreditCard(
            name = request.name,
            lastFourDigits = request.lastFourDigits,
            courtDate = request.courtDate,
            maximumPaymentDate = request.maximumPaymentDate,
            user = user
        )
        val userSummary = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        val response = CreditCardResponse(1L, "Visa Alex", "1234", "2025-07-15", "2025-07-30", userSummary)

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(creditCardRepository.save(any(CreditCard::class.java))).thenReturn(creditCard)
        `when`(creditCardMapper.toResponse(creditCard)).thenReturn(response)

        val result = creditCardService.createCard(request)

        assertEquals("Visa Alex", result.name)
        assertEquals("1234", result.lastFourDigits)
        assertEquals("Alexander Pavón", result.user.name)
    }

    @Test
    fun should_return_all_credit_cards() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val creditCard = CreditCard("Visa", "5678", "2025-06-01", "2025-06-15", user)
        val userSummary = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        val response = CreditCardResponse(1L, "Visa", "5678", "2025-06-01", "2025-06-15", userSummary)

        `when`(creditCardRepository.findAll()).thenReturn(listOf(creditCard))
        `when`(creditCardMapper.toResponse(creditCard)).thenReturn(response)

        val result = creditCardService.getAllCards()

        assertEquals(1, result.size)
        assertEquals("Visa", result[0].name)
        assertEquals("Alexander Pavón", result[0].user.name)
    }

    @Test
    fun should_return_card_by_id() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val creditCard = CreditCard("Master", "9999", "2025-08-01", "2025-08-20", user)
        val userSummary = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        val response = CreditCardResponse(1L, "Master", "9999", "2025-08-01", "2025-08-20", userSummary)

        `when`(creditCardRepository.findById(1L)).thenReturn(Optional.of(creditCard))
        `when`(creditCardMapper.toResponse(creditCard)).thenReturn(response)

        val result = creditCardService.getCardById(1L)

        assertEquals("Master", result.name)
        assertEquals("Alexander Pavón", result.user.name)
    }

    @Test
    fun should_throw_exception_when_card_by_id_not_found() {
        `when`(creditCardRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            creditCardService.getCardById(1L)
        }
    }

    @Test
    fun should_update_credit_card() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val existingCard = CreditCard("OldCard", "0000", "2025-01-01", "2025-01-15", user)
        val request = CreateCreditCardRequest("UpdatedCard", "4321", "2025-09-01", "2025-09-20", 1L)
        val updatedCard = CreditCard("UpdatedCard", "4321", "2025-09-01", "2025-09-20", user)
        val userSummary = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        val response = CreditCardResponse(1L, "UpdatedCard", "4321", "2025-09-01", "2025-09-20", userSummary)

        `when`(creditCardRepository.findById(1L)).thenReturn(Optional.of(existingCard))
        `when`(creditCardRepository.save(existingCard)).thenReturn(updatedCard)
        `when`(creditCardMapper.toResponse(updatedCard)).thenReturn(response)

        val result = creditCardService.updateCard(1L, request)

        assertEquals("UpdatedCard", result.name)
        assertEquals("4321", result.lastFourDigits)
        assertEquals("Alexander Pavón", result.user.name)
    }

    @Test
    fun should_throw_exception_when_updating_non_existent_card() {
        val request = CreateCreditCardRequest("New", "0000", "2025-10-01", "2025-10-15", 1L)

        `when`(creditCardRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            creditCardService.updateCard(1L, request)
        }
    }

    @Test
    fun should_delete_credit_card() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val card = CreditCard("ToDelete", "8888", "2025-07-01", "2025-07-15", user)

        `when`(creditCardRepository.findById(1L)).thenReturn(Optional.of(card))

        creditCardService.deleteCard(1L)

        verify(creditCardRepository).delete(card)
    }

    @Test
    fun should_throw_exception_when_deleting_non_existent_card() {
        `when`(creditCardRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            creditCardService.deleteCard(1L)
        }
    }
}

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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
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
            courtDate = "15",
            maximumPaymentDate = "30",
            userId = 1L
        )
        val savedCard = CreditCard(
            name = request.name,
            lastFourDigits = request.lastFourDigits,
            courtDate = request.courtDate,
            maximumPaymentDate = request.maximumPaymentDate,
            user = user
        )

        val response = CreditCardResponse(
            id = 1L,
            name = "Visa Alex",
            lastFourDigits = "1234",
            courtDate = "15",
            maximumPaymentDate = "30",
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", 5000.0)
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(creditCardRepository.save(any(CreditCard::class.java))).thenReturn(savedCard)
        `when`(creditCardMapper.toResponse(savedCard)).thenReturn(response)

        val result = creditCardService.createCard(request)

        assertEquals("Visa Alex", result.name)
        assertEquals("1234", result.lastFourDigits)
        assertEquals("15", result.courtDate)
        assertEquals("30", result.maximumPaymentDate)
        assertEquals("Alexander Pavón", result.user.name)
        assertEquals(5000.0, result.user.totalBalance)

        verify(userRepository).findById(1L)
        verify(creditCardRepository).save(any(CreditCard::class.java))
        verify(creditCardMapper).toResponse(savedCard)
        verifyNoMoreInteractions(userRepository, creditCardRepository, creditCardMapper)
    }

    @Test
    fun should_throw_when_user_not_found_on_create() {
        val request = CreateCreditCardRequest("Visa", "1111", "15", "30", 99L)

        `when`(userRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            creditCardService.createCard(request)
        }

        verify(userRepository).findById(99L)
        verifyNoInteractions(creditCardRepository, creditCardMapper)
    }

    @Test
    fun should_return_all_credit_cards() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val card1 = CreditCard("Visa", "5678", "15", "30", user)
        val card2 = CreditCard("MasterCard", "9999", "15", "30", user)

        val resp1 = CreditCardResponse(
            1L, "Visa", "5678", "15", "30",
            UserSummaryResponse(1L, user.name, user.email, 5000.0)
        )
        val resp2 = CreditCardResponse(
            2L, "MasterCard", "9999", "15", "30",
            UserSummaryResponse(1L, user.name, user.email, 5000.0)
        )

        `when`(creditCardRepository.findAll()).thenReturn(listOf(card1, card2))
        `when`(creditCardMapper.toResponse(card1)).thenReturn(resp1)
        `when`(creditCardMapper.toResponse(card2)).thenReturn(resp2)

        val result = creditCardService.getAllCards()

        assertEquals(2, result.size)
        assertEquals("Visa", result[0].name)
        assertEquals("5678", result[0].lastFourDigits)
        assertEquals("MasterCard", result[1].name)
        assertEquals("9999", result[1].lastFourDigits)

        verify(creditCardRepository).findAll()
        verify(creditCardMapper).toResponse(card1)
        verify(creditCardMapper).toResponse(card2)
        verifyNoMoreInteractions(creditCardRepository, creditCardMapper)
        verifyNoInteractions(userRepository)
    }

    @Test
    fun should_return_card_by_id() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val card = CreditCard("Master", "9999", "15", "30", user)

        val response = CreditCardResponse(
            1L, "Master", "9999", "15", "30",
            UserSummaryResponse(1L, user.name, user.email, 7000.0)
        )

        `when`(creditCardRepository.findById(1L)).thenReturn(Optional.of(card))
        `when`(creditCardMapper.toResponse(card)).thenReturn(response)

        val result = creditCardService.getCardById(1L)

        assertEquals("Master", result.name)
        assertEquals("9999", result.lastFourDigits)
        assertEquals("15", result.courtDate)
        assertEquals("30", result.maximumPaymentDate)
        assertEquals("Alexander Pavón", result.user.name)
        assertEquals(7000.0, result.user.totalBalance)

        verify(creditCardRepository).findById(1L)
        verify(creditCardMapper).toResponse(card)
        verifyNoMoreInteractions(creditCardRepository, creditCardMapper)
        verifyNoInteractions(userRepository)
    }

    @Test
    fun should_throw_exception_when_card_by_id_not_found() {
        `when`(creditCardRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            creditCardService.getCardById(1L)
        }

        verify(creditCardRepository).findById(1L)
        verifyNoInteractions(creditCardMapper, userRepository)
    }

    @Test
    fun should_update_credit_card() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val existingCard = CreditCard("OldCard", "0000", "15", "30", user)
        val request = CreateCreditCardRequest("UpdatedCard", "4321", "15", "30", 1L)
        val updatedCard = CreditCard("UpdatedCard", "4321", "15", "30", user)

        val response = CreditCardResponse(
            1L, "UpdatedCard", "4321", "15", "30",
            UserSummaryResponse(1L, user.name, user.email, 7777.0)
        )

        `when`(creditCardRepository.findById(1L)).thenReturn(Optional.of(existingCard))
        `when`(creditCardRepository.save(existingCard)).thenReturn(updatedCard)
        `when`(creditCardMapper.toResponse(updatedCard)).thenReturn(response)

        val result = creditCardService.updateCard(1L, request)

        assertEquals("UpdatedCard", result.name)
        assertEquals("4321", result.lastFourDigits)
        assertEquals("15", result.courtDate)
        assertEquals("30", result.maximumPaymentDate)
        assertEquals("Alexander Pavón", result.user.name)
        assertEquals(7777.0, result.user.totalBalance)

        verify(creditCardRepository).findById(1L)
        verify(creditCardRepository).save(existingCard)
        verify(creditCardMapper).toResponse(updatedCard)
        verifyNoMoreInteractions(creditCardRepository, creditCardMapper)
        verifyNoInteractions(userRepository)
    }

    @Test
    fun should_throw_exception_when_updating_non_existent_card() {
        val request = CreateCreditCardRequest("New", "0000", "15", "30", 1L)

        `when`(creditCardRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            creditCardService.updateCard(1L, request)
        }

        verify(creditCardRepository).findById(1L)
        verifyNoMoreInteractions(creditCardRepository)
        verifyNoInteractions(creditCardMapper, userRepository)
    }

    @Test
    fun should_delete_credit_card() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val card = CreditCard("ToDelete", "8888", "15", "30", user)

        `when`(creditCardRepository.findById(1L)).thenReturn(Optional.of(card))

        creditCardService.deleteCard(1L)

        verify(creditCardRepository).findById(1L)
        verify(creditCardRepository).delete(card)
        verifyNoMoreInteractions(creditCardRepository)
        verifyNoInteractions(userRepository, creditCardMapper)
    }

    @Test
    fun should_throw_exception_when_deleting_non_existent_card() {
        `when`(creditCardRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            creditCardService.deleteCard(1L)
        }

        verify(creditCardRepository).findById(1L)
        verifyNoMoreInteractions(creditCardRepository)
        verifyNoInteractions(userRepository, creditCardMapper)
    }
}

package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.InvalidRequestException
import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.mappers.MovementMapper
import com.pucetec.expensemate.models.entities.*
import com.pucetec.expensemate.models.requests.CreateMovementRequest
import com.pucetec.expensemate.models.responses.*
import com.pucetec.expensemate.repositories.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.*

class MovementServiceTest {

    private lateinit var movementRepository: MovementRepository
    private lateinit var userRepository: UserRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var creditCardRepository: CreditCardRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var movementMapper: MovementMapper
    private lateinit var movementService: MovementService

    @BeforeEach
    fun setUp() {
        movementRepository = mock(MovementRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        categoryRepository = mock(CategoryRepository::class.java)
        creditCardRepository = mock(CreditCardRepository::class.java)
        accountRepository = mock(AccountRepository::class.java)
        movementMapper = mock(MovementMapper::class.java)
        movementService = MovementService(
            movementRepository,
            userRepository,
            categoryRepository,
            creditCardRepository,
            accountRepository,
            movementMapper
        )
    }

    @Test
    fun should_create_income_movement_and_increase_account_balance() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Salario")
        val account = Account(bank = "Pichincha", accountNumber = "1111222233", balance = 1000.0, user = user)

        val request = CreateMovementRequest(
            type = MovementType.INCOME,
            amount = 200.0,
            note = "Pago recibido",
            userId = 1L,
            categoryId = 10L,
            creditCardId = null,
            accountId = 4L
        )

        val now = LocalDateTime.of(2025, 7, 1, 10, 0, 0)
        val response = MovementResponse(
            id = 1L,
            type = "income",
            amount = 200.0,
            date = now,
            note = "Pago recibido",
            category = CategoryResponse(10L, "Salario"),
            creditCard = null,
            account = AccountSummaryResponse(4L, "Pichincha", "1111222233", balance = 1200.0),
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", totalBalance = 5200.0)
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(categoryRepository.findById(10L)).thenReturn(Optional.of(category))
        `when`(accountRepository.findById(4L)).thenReturn(Optional.of(account))

        val savedMovement = mock(Movement::class.java)
        `when`(movementRepository.save(any(Movement::class.java))).thenReturn(savedMovement)
        `when`(movementMapper.toResponse(savedMovement)).thenReturn(response)

        val result = movementService.createMovement(request)

        assertEquals("income", result.type)
        assertEquals(200.0, result.amount)
        assertEquals(1200.0, account.balance, 0.0001)
        verify(accountRepository).findById(4L)
        verify(accountRepository).save(account)
        verify(movementRepository).save(any(Movement::class.java))
        verify(movementMapper).toResponse(savedMovement)
        verifyNoMoreInteractions(movementRepository, movementMapper)

    }

    @Test
    fun should_create_expense_movement_and_decrease_account_balance() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Servicios")
        val account = Account(bank = "Pichincha", accountNumber = "1111222233", balance = 400.0, user = user)

        val request = CreateMovementRequest(
            type = MovementType.EXPENSE,
            amount = 150.0,
            note = "Pago luz",
            userId = 1L,
            categoryId = 20L,
            creditCardId = null,
            accountId = 4L
        )

        val now = LocalDateTime.of(2025, 7, 2, 12, 0, 0)
        val response = MovementResponse(
            id = 2L,
            type = "expense",
            amount = 150.0,
            date = now,
            note = "Pago luz",
            category = CategoryResponse(20L, "Servicios"),
            creditCard = null,
            account = AccountSummaryResponse(4L, "Pichincha", "1111222233", balance = 250.0),
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", totalBalance = 4850.0)
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(categoryRepository.findById(20L)).thenReturn(Optional.of(category))
        `when`(accountRepository.findById(4L)).thenReturn(Optional.of(account))

        val savedMovement = mock(Movement::class.java)
        `when`(movementRepository.save(any(Movement::class.java))).thenReturn(savedMovement)
        `when`(movementMapper.toResponse(savedMovement)).thenReturn(response)

        val result = movementService.createMovement(request)

        assertEquals("expense", result.type)
        assertEquals(150.0, result.amount)
        assertEquals(250.0, account.balance, 0.0001)
        verify(accountRepository).findById(4L)
        verify(accountRepository).save(account)
        verify(movementRepository).save(any(Movement::class.java))
        verify(movementMapper).toResponse(savedMovement)
        verifyNoMoreInteractions(movementRepository, movementMapper)
    }

    @Test
    fun should_throw_invalid_request_when_expense_exceeds_balance() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Compras")
        val account = Account(bank = "Pichincha", accountNumber = "1111222233", balance = 100.0, user = user)

        val request = CreateMovementRequest(
            type = MovementType.EXPENSE,
            amount = 300.0,
            note = "Compra grande",
            userId = 1L,
            categoryId = 30L,
            creditCardId = null,
            accountId = 4L
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(categoryRepository.findById(30L)).thenReturn(Optional.of(category))
        `when`(accountRepository.findById(4L)).thenReturn(Optional.of(account))

        val ex = assertThrows<InvalidRequestException> {
            movementService.createMovement(request)
        }
        assertEquals("Insufficient balance in account", ex.message)
        assertEquals(100.0, account.balance, 0.0001)

        verify(accountRepository, never()).save(any(Account::class.java))
        verifyNoInteractions(movementRepository, movementMapper)
    }

    @Test
    fun should_create_movement_without_credit_card_or_account() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Transporte")

        val request = CreateMovementRequest(
            type = MovementType.EXPENSE,
            amount = 25.0,
            note = "Taxi",
            userId = 1L,
            categoryId = 2L,
            creditCardId = null,
            accountId = null
        )

        val now = LocalDateTime.of(2025, 7, 2, 8, 0, 0)
        val response = MovementResponse(
            id = 3L,
            type = "expense",
            amount = 25.0,
            date = now,
            note = "Taxi",
            category = CategoryResponse(2L, "Transporte"),
            creditCard = null,
            account = null,
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", totalBalance = 4975.0)
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))

        val savedMovement = mock(Movement::class.java)
        `when`(movementRepository.save(any(Movement::class.java))).thenReturn(savedMovement)
        `when`(movementMapper.toResponse(savedMovement)).thenReturn(response)

        val result = movementService.createMovement(request)

        assertEquals("expense", result.type)
        assertEquals(25.0, result.amount)
        assertEquals("Taxi", result.note)
        assertNull(result.creditCard)
        assertNull(result.account)
        verifyNoInteractions(accountRepository, creditCardRepository)
    }

    @Test
    fun should_return_all_movements() {
        val now = LocalDateTime.of(2025, 6, 15, 12, 0, 0)
        val response = MovementResponse(
            id = 1L,
            type = "expense",
            amount = 50.0,
            date = now,
            note = "Almuerzo",
            category = CategoryResponse(1L, "Comida"),
            creditCard = null,
            account = null,
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", totalBalance = 4950.0)
        )

        val m1 = mock(Movement::class.java)
        `when`(movementRepository.findAll()).thenReturn(listOf(m1))
        `when`(movementMapper.toResponse(m1)).thenReturn(response)

        val result = movementService.getAllMovements()

        assertEquals(1, result.size)
        assertEquals("expense", result[0].type)
        verify(movementRepository).findAll()
        verify(movementMapper).toResponse(m1)
        verifyNoMoreInteractions(movementRepository, movementMapper)
        verifyNoInteractions(userRepository, categoryRepository, accountRepository, creditCardRepository)
    }

    @Test
    fun should_return_movement_by_id() {
        val now = LocalDateTime.of(2025, 7, 3, 10, 0, 0)
        val response = MovementResponse(
            id = 1L,
            type = "income",
            amount = 200.0,
            date = now,
            note = null,
            category = CategoryResponse(1L, "Educación"),
            creditCard = null,
            account = null,
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", totalBalance = 5000.0)
        )

        val found = mock(Movement::class.java)
        `when`(movementRepository.findById(1L)).thenReturn(Optional.of(found))
        `when`(movementMapper.toResponse(found)).thenReturn(response)

        val result = movementService.getMovementById(1L)

        assertEquals("income", result.type)
        assertEquals(200.0, result.amount)
        verify(movementRepository).findById(1L)
        verify(movementMapper).toResponse(found)
        verifyNoMoreInteractions(movementRepository, movementMapper)
    }

    @Test
    fun should_throw_exception_when_movement_by_id_not_found() {
        `when`(movementRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            movementService.getMovementById(1L)
        }

        verify(movementRepository).findById(1L)
        verifyNoInteractions(movementMapper)
    }

    @Test
    fun should_update_movement_successfully_with_card_and_account() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Salud")
        val creditCard = CreditCard("Visa", "1234", "2025-01-01", "2025-01-20", user)
        val account = Account("Pichincha", "1111222233", balance = 1300.0, user = user)

        val existingMovement = mock(Movement::class.java)

        val request = CreateMovementRequest(
            type = MovementType.INCOME,
            amount = 200.0,
            note = "Actualizado",
            userId = 1L,
            categoryId = 2L,
            creditCardId = 3L,
            accountId = 4L
        )

        val now = LocalDateTime.of(2025, 7, 5, 9, 0, 0)
        val response = MovementResponse(
            id = 1L,
            type = "income",
            amount = 200.0,
            date = now,
            note = "Actualizado",
            category = CategoryResponse(2L, "Salud"),
            creditCard = CreditCardSummaryResponse(3L, "Visa", "1234", "2025-01-01", "2025-01-20"),
            account = AccountSummaryResponse(4L, "Pichincha", "1111222233", balance = 1300.0),
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", totalBalance = 5200.0)
        )

        `when`(movementRepository.findById(1L)).thenReturn(Optional.of(existingMovement))
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))
        `when`(creditCardRepository.findById(3L)).thenReturn(Optional.of(creditCard))
        `when`(accountRepository.findById(4L)).thenReturn(Optional.of(account))
        `when`(movementRepository.save(existingMovement)).thenReturn(existingMovement)
        `when`(movementMapper.toResponse(existingMovement)).thenReturn(response)

        val result = movementService.updateMovement(1L, request)

        assertEquals("income", result.type)
        assertEquals(200.0, result.amount)
        assertEquals("Actualizado", result.note)
        verify(movementRepository).findById(1L)
        verify(userRepository).findById(1L)
        verify(categoryRepository).findById(2L)
        verify(creditCardRepository).findById(3L)
        verify(accountRepository).findById(4L)
        verify(movementRepository).save(existingMovement)
        verify(movementMapper).toResponse(existingMovement)
    }

    @Test
    fun should_update_movement_without_credit_card_or_account() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Educación")
        val existingMovement = mock(Movement::class.java)

        val request = CreateMovementRequest(
            type = MovementType.EXPENSE,
            amount = 150.0,
            note = "Beca devolución",
            userId = 1L,
            categoryId = 2L,
            creditCardId = null,
            accountId = null
        )

        val now = LocalDateTime.of(2025, 7, 2, 13, 0, 0)
        val response = MovementResponse(
            id = 1L,
            type = "expense",
            amount = 150.0,
            date = now,
            note = "Beca devolución",
            category = CategoryResponse(2L, "Educación"),
            creditCard = null,
            account = null,
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec", totalBalance = 4850.0)
        )

        `when`(movementRepository.findById(1L)).thenReturn(Optional.of(existingMovement))
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))
        `when`(movementRepository.save(existingMovement)).thenReturn(existingMovement)
        `when`(movementMapper.toResponse(existingMovement)).thenReturn(response)

        val result = movementService.updateMovement(1L, request)

        assertEquals("expense", result.type)
        assertEquals(150.0, result.amount)
        assertEquals("Beca devolución", result.note)
        assertNull(result.creditCard)
        assertNull(result.account)
        verify(creditCardRepository, never()).findById(anyLong())
        verify(accountRepository, never()).findById(anyLong())
    }

    @Test
    fun should_throw_exception_when_credit_card_not_found_in_create() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Salud")

        val request = CreateMovementRequest(
            type = MovementType.INCOME,
            amount = 100.0,
            note = "Prueba",
            userId = 1L,
            categoryId = 2L,
            creditCardId = 3L,
            accountId = null
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))
        `when`(creditCardRepository.findById(3L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            movementService.createMovement(request)
        }

        verifyNoInteractions(movementRepository, movementMapper, accountRepository)
    }

    @Test
    fun should_throw_exception_when_account_not_found_in_create() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Salud")

        val request = CreateMovementRequest(
            type = MovementType.INCOME,
            amount = 100.0,
            note = "Prueba",
            userId = 1L,
            categoryId = 2L,
            creditCardId = null,
            accountId = 4L
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))
        `when`(accountRepository.findById(4L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            movementService.createMovement(request)
        }

        verifyNoInteractions(movementRepository, movementMapper)
    }

    @Test
    fun should_throw_exception_when_updating_non_existent_movement() {
        val request = CreateMovementRequest(
            type = MovementType.EXPENSE,
            amount = 150.0,
            note = "No existe",
            userId = 1L,
            categoryId = 2L,
            creditCardId = null,
            accountId = null
        )

        `when`(movementRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            movementService.updateMovement(1L, request)
        }
    }

    @Test
    fun should_delete_movement() {
        val existing = mock(Movement::class.java)
        `when`(movementRepository.findById(1L)).thenReturn(Optional.of(existing))

        movementService.deleteMovement(1L)

        verify(movementRepository).delete(existing)
    }

    @Test
    fun should_throw_exception_when_deleting_non_existent_movement() {
        `when`(movementRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            movementService.deleteMovement(1L)
        }
    }
}

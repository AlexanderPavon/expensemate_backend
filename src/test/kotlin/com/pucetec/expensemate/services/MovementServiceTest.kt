package com.pucetec.expensemate.services

import com.pucetec.expensemate.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.expensemate.mappers.MovementMapper
import com.pucetec.expensemate.models.entities.*
import com.pucetec.expensemate.models.requests.CreateMovementRequest
import com.pucetec.expensemate.models.responses.*
import com.pucetec.expensemate.repositories.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDate
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
    fun should_create_new_movement() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Salud")
        val creditCard = CreditCard("Visa", "1234", "2025-01-01", "2025-01-20", user)
        val account = Account("Banco Pichincha", "1111222233", user)

        val request = CreateMovementRequest(
            type = "ingreso",
            amount = 100.0,
            date = LocalDate.of(2025, 7, 1),
            note = "Pago recibido",
            userId = 1L,
            categoryId = 2L,
            creditCardId = 3L,
            accountId = 4L
        )

        val movement = Movement(
            type = request.type,
            amount = request.amount,
            date = request.date,
            note = request.note,
            user = user,
            category = category,
            creditCard = creditCard,
            account = account
        )

        val response = MovementResponse(
            id = 1L,
            type = "ingreso",
            amount = 100.0,
            date = request.date,
            note = "Pago recibido",
            category = CategoryResponse(2L, "Salud"),
            creditCard = CreditCardSummaryResponse(3L, "Visa", "1234", "2025-01-01", "2025-01-20"),
            account = AccountSummaryResponse(4L, "Banco Pichincha", "1111222233"),
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))
        `when`(creditCardRepository.findById(3L)).thenReturn(Optional.of(creditCard))
        `when`(accountRepository.findById(4L)).thenReturn(Optional.of(account))
        `when`(movementRepository.save(any(Movement::class.java))).thenReturn(movement)
        `when`(movementMapper.toResponse(movement)).thenReturn(response)

        val result = movementService.createMovement(request)

        assertEquals("ingreso", result.type)
        assertEquals(100.0, result.amount)
        assertEquals("Pago recibido", result.note)
        assertEquals("Alexander Pavón", result.user.name)
        assertEquals("Salud", result.category.name)
        assertEquals("1234", result.creditCard?.lastFourDigits)
        assertEquals("Banco Pichincha", result.account?.bank)
    }

    @Test
    fun should_create_movement_without_credit_card_or_account() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Transporte")

        val request = CreateMovementRequest(
            type = "egreso",
            amount = 25.0,
            date = LocalDate.of(2025, 7, 2),
            note = "Taxi",
            userId = 1L,
            categoryId = 2L,
            creditCardId = null,
            accountId = null
        )

        val movement = Movement(
            type = request.type,
            amount = request.amount,
            date = request.date,
            note = request.note,
            user = user,
            category = category,
            creditCard = null,
            account = null
        )

        val response = MovementResponse(
            id = 2L,
            type = "egreso",
            amount = 25.0,
            date = request.date,
            note = "Taxi",
            category = CategoryResponse(2L, "Transporte"),
            creditCard = null,
            account = null,
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))
        `when`(movementRepository.save(any(Movement::class.java))).thenReturn(movement)
        `when`(movementMapper.toResponse(movement)).thenReturn(response)

        val result = movementService.createMovement(request)

        assertEquals("egreso", result.type)
        assertEquals(25.0, result.amount)
        assertEquals("Taxi", result.note)
        assertNull(result.creditCard)
        assertNull(result.account)
    }

    @Test
    fun should_return_all_movements() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Comida")
        val movement = Movement("egreso", 50.0, LocalDate.of(2025, 6, 15), "Almuerzo", user, category)

        val response = MovementResponse(
            id = 1L,
            type = "egreso",
            amount = 50.0,
            date = movement.date,
            note = "Almuerzo",
            category = CategoryResponse(1L, "Comida"),
            creditCard = null,
            account = null,
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        )

        `when`(movementRepository.findAll()).thenReturn(listOf(movement))
        `when`(movementMapper.toResponse(movement)).thenReturn(response)

        val result = movementService.getAllMovements()

        assertEquals(1, result.size)
        assertEquals("egreso", result[0].type)
    }

    @Test
    fun should_return_movement_by_id() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Educación")
        val movement = Movement("ingreso", 200.0, LocalDate.now(), null, user, category)

        val response = MovementResponse(
            id = 1L,
            type = "ingreso",
            amount = 200.0,
            date = LocalDate.now(),
            note = null,
            category = CategoryResponse(1L, "Educación"),
            creditCard = null,
            account = null,
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        )

        `when`(movementRepository.findById(1L)).thenReturn(Optional.of(movement))
        `when`(movementMapper.toResponse(movement)).thenReturn(response)

        val result = movementService.getMovementById(1L)

        assertEquals("ingreso", result.type)
        assertEquals(200.0, result.amount)
    }

    @Test
    fun should_throw_exception_when_movement_by_id_not_found() {
        `when`(movementRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            movementService.getMovementById(1L)
        }
    }

    @Test
    fun should_update_movement_successfully() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Salud")
        val creditCard = CreditCard("Visa", "1234", "2025-01-01", "2025-01-20", user)
        val account = Account("Banco Pichincha", "1111222233", user)

        val existingMovement = Movement(
            type = "egreso",
            amount = 50.0,
            date = LocalDate.of(2025, 6, 15),
            note = "Viejo",
            user = user,
            category = category,
            creditCard = creditCard,
            account = account
        )

        val request = CreateMovementRequest(
            type = "ingreso",
            amount = 200.0,
            date = LocalDate.of(2025, 7, 1),
            note = "Actualizado",
            userId = 1L,
            categoryId = 2L,
            creditCardId = 3L,
            accountId = 4L
        )

        val response = MovementResponse(
            id = 1L,
            type = "ingreso",
            amount = 200.0,
            date = request.date,
            note = "Actualizado",
            category = CategoryResponse(2L, "Salud"),
            creditCard = CreditCardSummaryResponse(3L, "Visa", "1234", "2025-01-01", "2025-01-20"),
            account = AccountSummaryResponse(4L, "Banco Pichincha", "1111222233"),
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        )

        `when`(movementRepository.findById(1L)).thenReturn(Optional.of(existingMovement))
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))
        `when`(creditCardRepository.findById(3L)).thenReturn(Optional.of(creditCard))
        `when`(accountRepository.findById(4L)).thenReturn(Optional.of(account))
        `when`(movementRepository.save(any(Movement::class.java))).thenReturn(existingMovement)
        `when`(movementMapper.toResponse(existingMovement)).thenReturn(response)

        val result = movementService.updateMovement(1L, request)

        assertEquals("ingreso", result.type)
        assertEquals(200.0, result.amount)
        assertEquals("Actualizado", result.note)
    }

    @Test
    fun should_update_movement_without_credit_card_or_account() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Educación")

        val existingMovement = Movement(
            type = "egreso",
            amount = 50.0,
            date = LocalDate.of(2025, 6, 10),
            note = "Cuaderno",
            user = user,
            category = category
        )

        val updatedRequest = CreateMovementRequest(
            type = "ingreso",
            amount = 150.0,
            date = LocalDate.of(2025, 7, 2),
            note = "Beca",
            userId = 1L,
            categoryId = 2L,
            creditCardId = null,
            accountId = null
        )

        val updatedMovement = Movement(
            type = updatedRequest.type,
            amount = updatedRequest.amount,
            date = updatedRequest.date,
            note = updatedRequest.note,
            user = user,
            category = category,
            creditCard = null,
            account = null
        )

        val response = MovementResponse(
            id = 1L,
            type = "ingreso",
            amount = 150.0,
            date = updatedRequest.date,
            note = "Beca",
            category = CategoryResponse(2L, "Educación"),
            creditCard = null,
            account = null,
            user = UserSummaryResponse(1L, "Alexander Pavón", "afpavon@puce.edu.ec")
        )

        `when`(movementRepository.findById(1L)).thenReturn(Optional.of(existingMovement))
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))
        `when`(movementRepository.save(any(Movement::class.java))).thenReturn(updatedMovement)
        `when`(movementMapper.toResponse(updatedMovement)).thenReturn(response)

        val result = movementService.updateMovement(1L, updatedRequest)

        assertEquals("ingreso", result.type)
        assertEquals(150.0, result.amount)
        assertEquals("Beca", result.note)
        assertEquals("Educación", result.category.name)
        assertEquals("Alexander Pavón", result.user.name)
        assertNull(result.creditCard)
        assertNull(result.account)
    }

    @Test
    fun should_throw_exception_when_credit_card_not_found_in_create() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Salud")

        val request = CreateMovementRequest(
            type = "ingreso",
            amount = 100.0,
            date = LocalDate.of(2025, 7, 1),
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
    }

    @Test
    fun should_throw_exception_when_account_not_found_in_create() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Salud")

        val request = CreateMovementRequest(
            type = "ingreso",
            amount = 100.0,
            date = LocalDate.of(2025, 7, 1),
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
    }

    @Test
    fun should_throw_exception_when_updating_non_existent_movement() {
        val request = CreateMovementRequest(
            type = "egreso",
            amount = 150.0,
            date = LocalDate.now(),
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
    fun should_throw_exception_when_deleting_non_existent_movement() {
        `when`(movementRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            movementService.deleteMovement(1L)
        }
    }

    @Test
    fun should_delete_movement() {
        val user = User(name = "Alexander Pavón", email = "afpavon@puce.edu.ec")
        val category = Category(name = "Servicios")
        val movement = Movement("egreso", 70.0, LocalDate.now(), "Luz", user, category)

        `when`(movementRepository.findById(1L)).thenReturn(Optional.of(movement))

        movementService.deleteMovement(1L)

        verify(movementRepository).delete(movement)
    }
}

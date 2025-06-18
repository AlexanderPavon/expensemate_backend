package com.pucetec.expensemate.controllers

import com.pucetec.expensemate.models.requests.CreateMovementRequest
import com.pucetec.expensemate.models.responses.MovementResponse
import com.pucetec.expensemate.routes.Routes
import com.pucetec.expensemate.services.MovementService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Routes.MOVEMENTS)
class MovementController(
    private val movementService: MovementService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createMovement(@RequestBody request: CreateMovementRequest): MovementResponse =
        movementService.createMovement(request)

    @GetMapping
    fun getAllMovements(): List<MovementResponse> = movementService.getAllMovements()

    @GetMapping("/{id}")
    fun getMovementById(@PathVariable id: Long): MovementResponse =
        movementService.getMovementById(id)

    @PutMapping("/{id}")
    fun updateMovement(@PathVariable id: Long, @RequestBody request: CreateMovementRequest): MovementResponse =
        movementService.updateMovement(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMovement(@PathVariable id: Long) = movementService.deleteMovement(id)
}
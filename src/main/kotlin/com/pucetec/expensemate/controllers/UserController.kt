package com.pucetec.expensemate.controllers

import com.pucetec.expensemate.models.requests.CreateUserRequest
import com.pucetec.expensemate.models.responses.UserResponse
import com.pucetec.expensemate.models.responses.UserSummaryResponse
import com.pucetec.expensemate.routes.Routes
import com.pucetec.expensemate.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Routes.USERS)
class UserController(
    private val userService: UserService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@RequestBody request: CreateUserRequest): UserResponse =
        userService.createUser(request)

    @GetMapping
    fun getAllUsers(): List<UserResponse> = userService.getAllUsers()

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): UserResponse = userService.getUserById(id)

    @GetMapping("/email/{email:.+}")
    fun getUserByEmail(@PathVariable email: String): UserSummaryResponse =
        userService.getUserByEmail(email.trim())

    @GetMapping("/{id}/summary")
    fun getUserSummary(@PathVariable id: Long): ResponseEntity<UserSummaryResponse> {
        return ResponseEntity.ok(userService.getUserSummary(id))
    }

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody request: CreateUserRequest): UserResponse =
        userService.updateUser(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable id: Long) = userService.deleteUser(id)
}

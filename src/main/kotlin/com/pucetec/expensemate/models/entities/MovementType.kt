package com.pucetec.expensemate.models.entities

enum class MovementType(val code: String) {
    INCOME("I"),
    EXPENSE("E");

    override fun toString(): String = name.lowercase()
}
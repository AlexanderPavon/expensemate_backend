package com.pucetec.expensemate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ExpensemateApplication

fun main(args: Array<String>) {
	runApplication<ExpensemateApplication>(*args)
}

package com.pucetec.expensemate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class ExpensemateApplication

fun main(args: Array<String>) {
	runApplication<ExpensemateApplication>(*args)
}
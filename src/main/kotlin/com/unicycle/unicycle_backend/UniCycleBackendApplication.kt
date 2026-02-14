package com.unicycle.unicycle_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UniCycleBackendApplication

fun main(args: Array<String>) {
	println("hi this is a test")
	runApplication<UniCycleBackendApplication>(*args)
}

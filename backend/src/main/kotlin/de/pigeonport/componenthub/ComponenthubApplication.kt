package de.pigeonport.componenthub

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ComponenthubApplication

fun main(args: Array<String>) {
	runApplication<ComponenthubApplication>(*args)
}

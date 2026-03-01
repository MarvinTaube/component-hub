package de.pigeonport.componentwarehousespring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class ComponentWarehouseSpringApplication

fun main(args: Array<String>) {
    runApplication<ComponentWarehouseSpringApplication>(*args)
}

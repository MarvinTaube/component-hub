package de.pigeonport.componentwarehousespring

import de.pigeonport.componentwarehousespring.model.entity.Part
import org.springframework.data.repository.CrudRepository

interface PartRepository : CrudRepository<Part, Long> {
    fun findPartById(id: Long): Part?
}
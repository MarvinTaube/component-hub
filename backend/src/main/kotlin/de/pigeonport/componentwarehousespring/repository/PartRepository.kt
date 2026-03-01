package de.pigeonport.componentwarehousespring.repository

import de.pigeonport.componentwarehousespring.model.entity.Part
import org.springframework.data.repository.CrudRepository

import org.springframework.stereotype.Repository

@Repository
interface PartRepository : CrudRepository<Part, Long> {
    fun findPartById(id: Long): Part?
}
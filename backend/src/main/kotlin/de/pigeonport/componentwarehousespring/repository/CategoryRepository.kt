package de.pigeonport.componentwarehousespring.repository

import de.pigeonport.componentwarehousespring.model.entity.Category
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : CrudRepository<Category, Long> {
    fun findByName(name: String): Category?
}

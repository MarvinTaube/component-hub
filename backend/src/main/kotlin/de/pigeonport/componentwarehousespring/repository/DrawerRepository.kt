package de.pigeonport.componentwarehousespring.repository

import de.pigeonport.componentwarehousespring.model.entity.Drawer
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DrawerRepository : CrudRepository<Drawer, Long> {
    fun findByNumber(number: Int): Drawer?
}

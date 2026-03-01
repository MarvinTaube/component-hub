package de.pigeonport.componentwarehousespring.service

import de.pigeonport.componentwarehousespring.model.entity.Drawer
import de.pigeonport.componentwarehousespring.model.request.DrawerRequest
import de.pigeonport.componentwarehousespring.repository.DrawerRepository
import org.springframework.stereotype.Service

@Service
class DrawerService(private val drawerRepository: DrawerRepository) {

    fun getAll(): Iterable<Drawer> = drawerRepository.findAll()

    fun getById(id: Long): Drawer? = drawerRepository.findById(id).orElse(null)

    fun create(request: DrawerRequest): Drawer {
        val drawer = Drawer(
            id = 0L,
            number = request.number
        )
        return drawerRepository.save(drawer)
    }

    fun update(id: Long, request: DrawerRequest): Drawer? {
        val existing = drawerRepository.findById(id).orElse(null) ?: return null
        val updated = Drawer(
            id = existing.id,
            number = request.number
        )
        return drawerRepository.save(updated)
    }

    fun delete(id: Long): Boolean {
        if (!drawerRepository.existsById(id)) return false
        drawerRepository.deleteById(id)
        return true
    }
}

package de.pigeonport.componentwarehousespring.service

import de.pigeonport.componentwarehousespring.model.entity.Section
import de.pigeonport.componentwarehousespring.model.request.SectionRequest
import de.pigeonport.componentwarehousespring.repository.DrawerRepository
import de.pigeonport.componentwarehousespring.repository.SectionRepository
import org.springframework.stereotype.Service

@Service
class SectionService(
    private val sectionRepository: SectionRepository,
    private val drawerRepository: DrawerRepository
) {

    fun getAll(): Iterable<Section> = sectionRepository.findAll()

    fun getById(id: Long): Section? = sectionRepository.findById(id).orElse(null)

    fun create(request: SectionRequest): Section? {
        val drawer = drawerRepository.findById(request.drawerId).orElse(null) ?: return null
        val section = Section(
            id = 0L,
            position = request.position,
            drawer = drawer
        )
        return sectionRepository.save(section)
    }

    fun update(id: Long, request: SectionRequest): Section? {
        val existing = sectionRepository.findById(id).orElse(null) ?: return null
        val drawer = drawerRepository.findById(request.drawerId).orElse(null) ?: return null
        val updated = Section(
            id = existing.id,
            position = request.position,
            drawer = drawer
        )
        return sectionRepository.save(updated)
    }

    fun delete(id: Long): Boolean {
        if (!sectionRepository.existsById(id)) return false
        sectionRepository.deleteById(id)
        return true
    }
}

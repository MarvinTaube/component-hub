package de.pigeonport.componentwarehousespring.service

import de.pigeonport.componentwarehousespring.model.entity.Part
import de.pigeonport.componentwarehousespring.model.request.PartRequest
import de.pigeonport.componentwarehousespring.repository.PartRepository
import org.springframework.stereotype.Service

@Service
class PartService(private val partRepository: PartRepository) {

    fun getAll(): Iterable<Part> = partRepository.findAll()

    fun getById(id: Long): Part? = partRepository.findById(id).orElse(null)

    fun create(request: PartRequest): Part {
        val part = Part(
            id = 0L,
            name = request.name,
            description = request.description,
            stock = request.stock,
            links = request.links,
            notes = request.notes,
            imageLink = request.imageLink
        )
        return partRepository.save(part)
    }

    fun update(id: Long, request: PartRequest): Part? {
        val existing = partRepository.findById(id).orElse(null) ?: return null
        val updated = Part(
            id = existing.id,
            name = request.name,
            description = request.description,
            stock = request.stock,
            links = request.links,
            notes = request.notes,
            imageLink = request.imageLink,
            section = existing.section,
            category = existing.category,
            tag = existing.tag
        )
        return partRepository.save(updated)
    }

    fun delete(id: Long): Boolean {
        if (!partRepository.existsById(id)) return false
        partRepository.deleteById(id)
        return true
    }
}

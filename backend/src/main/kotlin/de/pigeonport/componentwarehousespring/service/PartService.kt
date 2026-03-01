package de.pigeonport.componentwarehousespring.service

import de.pigeonport.componentwarehousespring.model.entity.Part
import de.pigeonport.componentwarehousespring.model.request.PartRequest
import de.pigeonport.componentwarehousespring.repository.CategoryRepository
import de.pigeonport.componentwarehousespring.repository.DrawerRepository
import de.pigeonport.componentwarehousespring.repository.PartRepository
import de.pigeonport.componentwarehousespring.repository.TagRepository
import org.springframework.stereotype.Service

@Service
class PartService(
    private val partRepository: PartRepository,
    private val drawerRepository: DrawerRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
) {

    fun getAll(): Iterable<Part> = partRepository.findAll()

    fun getById(id: Long): Part? = partRepository.findById(id).orElse(null)

    fun create(request: PartRequest): Part {
        val drawer = request.drawerId?.let { drawerRepository.findById(it).orElse(null) }
        val category = request.categoryId?.let { categoryRepository.findById(it).orElse(null) }
        val tags = request.tagIds?.let { tagRepository.findAllById(it).toMutableList() } ?: mutableListOf()

        val part = Part(
            id = 0L,
            name = request.name,
            description = request.description,
            stock = request.stock,
            links = request.links,
            notes = request.notes,
            imageLink = request.imageLink,
            drawer = drawer,
            sectionNumber = request.sectionNumber ?: 1,
            category = category,
            tag = tags
        )
        return partRepository.save(part)
    }

    fun update(id: Long, request: PartRequest): Part? {
        val existing = partRepository.findById(id).orElse(null) ?: return null

        val drawer = if (request.drawerId != null) {
            drawerRepository.findById(request.drawerId).orElse(null)
        } else {
            existing.drawer
        }

        val category = if (request.categoryId != null) {
            categoryRepository.findById(request.categoryId).orElse(null)
        } else {
            existing.category
        }

        val tags = if (request.tagIds != null) {
            tagRepository.findAllById(request.tagIds).toMutableList()
        } else {
            existing.tag
        }

        val updated = Part(
            id = existing.id,
            name = request.name,
            description = request.description,
            stock = request.stock,
            links = request.links,
            notes = request.notes,
            imageLink = request.imageLink,
            drawer = drawer,
            sectionNumber = request.sectionNumber ?: existing.sectionNumber,
            category = category,
            tag = tags
        )
        return partRepository.save(updated)
    }

    fun delete(id: Long): Boolean {
        if (!partRepository.existsById(id)) return false
        partRepository.deleteById(id)
        return true
    }
}

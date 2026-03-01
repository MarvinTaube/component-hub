package de.pigeonport.componentwarehousespring.service

import de.pigeonport.componentwarehousespring.model.entity.Category
import de.pigeonport.componentwarehousespring.model.request.CategoryRequest
import de.pigeonport.componentwarehousespring.repository.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(private val categoryRepository: CategoryRepository) {

    fun getAll(): Iterable<Category> = categoryRepository.findAll()

    fun getById(id: Long): Category? = categoryRepository.findById(id).orElse(null)

    fun create(request: CategoryRequest): Category {
        val category = Category(
            id = 0L,
            name = request.name,
            notes = request.notes
        )
        return categoryRepository.save(category)
    }

    fun update(id: Long, request: CategoryRequest): Category? {
        val existing = categoryRepository.findById(id).orElse(null) ?: return null
        val updated = Category(
            id = existing.id,
            name = request.name,
            notes = request.notes
        )
        return categoryRepository.save(updated)
    }

    fun delete(id: Long): Boolean {
        if (!categoryRepository.existsById(id)) return false
        categoryRepository.deleteById(id)
        return true
    }
}

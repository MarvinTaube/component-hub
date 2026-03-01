package de.pigeonport.componentwarehousespring.service

import de.pigeonport.componentwarehousespring.model.entity.Tag
import de.pigeonport.componentwarehousespring.model.request.TagRequest
import de.pigeonport.componentwarehousespring.repository.TagRepository
import org.springframework.stereotype.Service

@Service
class TagService(private val tagRepository: TagRepository) {

    fun getAll(): Iterable<Tag> = tagRepository.findAll()

    fun getById(id: Long): Tag? = tagRepository.findById(id).orElse(null)

    fun create(request: TagRequest): Tag {
        val tag = Tag(
            id = 0L,
            name = request.name,
            color = request.color
        )
        return tagRepository.save(tag)
    }

    fun update(id: Long, request: TagRequest): Tag? {
        val existing = tagRepository.findById(id).orElse(null) ?: return null
        val updated = Tag(
            id = existing.id,
            name = request.name,
            color = request.color
        )
        return tagRepository.save(updated)
    }

    fun delete(id: Long): Boolean {
        if (!tagRepository.existsById(id)) return false
        tagRepository.deleteById(id)
        return true
    }
}

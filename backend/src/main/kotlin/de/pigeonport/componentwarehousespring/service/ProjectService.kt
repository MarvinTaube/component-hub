package de.pigeonport.componentwarehousespring.service

import de.pigeonport.componentwarehousespring.model.entity.Project
import de.pigeonport.componentwarehousespring.model.request.ProjectRequest
import de.pigeonport.componentwarehousespring.repository.ProjectRepository
import org.springframework.stereotype.Service

@Service
class ProjectService(private val projectRepository: ProjectRepository) {

    fun getAll(): Iterable<Project> = projectRepository.findAll()

    fun getById(id: Long): Project? = projectRepository.findById(id).orElse(null)

    fun create(request: ProjectRequest): Project {
        val project = Project(
            id = 0L,
            name = request.name,
            description = request.description,
            finished = request.finished,
            published = request.published,
            blogLink = request.blogLink
        )
        return projectRepository.save(project)
    }

    fun update(id: Long, request: ProjectRequest): Project? {
        val existing = projectRepository.findById(id).orElse(null) ?: return null
        val updated = Project(
            id = existing.id,
            name = request.name,
            description = request.description,
            finished = request.finished,
            published = request.published,
            blogLink = request.blogLink,
            transactions = existing.transactions
        )
        return projectRepository.save(updated)
    }

    fun delete(id: Long): Boolean {
        if (!projectRepository.existsById(id)) return false
        projectRepository.deleteById(id)
        return true
    }
}

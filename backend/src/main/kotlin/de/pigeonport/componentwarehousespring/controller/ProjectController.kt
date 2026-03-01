package de.pigeonport.componentwarehousespring.controller

import de.pigeonport.componentwarehousespring.model.entity.Project
import de.pigeonport.componentwarehousespring.model.request.ProjectRequest
import de.pigeonport.componentwarehousespring.service.ProjectService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/projects")
class ProjectController(private val projectService: ProjectService) {

    @GetMapping
    fun getAll(): Iterable<Project> = projectService.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Project> {
        val project = projectService.getById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(project)
    }

    @PostMapping
    fun create(@RequestBody request: ProjectRequest): ResponseEntity<Project> {
        val created = projectService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: ProjectRequest): ResponseEntity<Project> {
        val updated = projectService.update(id, request) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!projectService.delete(id)) return ResponseEntity.notFound().build()
        return ResponseEntity.noContent().build()
    }
}

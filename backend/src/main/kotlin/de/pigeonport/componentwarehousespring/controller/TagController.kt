package de.pigeonport.componentwarehousespring.controller

import de.pigeonport.componentwarehousespring.model.entity.Tag
import de.pigeonport.componentwarehousespring.model.request.TagRequest
import de.pigeonport.componentwarehousespring.service.TagService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tags")
class TagController(private val tagService: TagService) {

    @GetMapping
    fun getAll(): Iterable<Tag> = tagService.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Tag> {
        val tag = tagService.getById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(tag)
    }

    @PostMapping
    fun create(@RequestBody request: TagRequest): ResponseEntity<Tag> {
        val created = tagService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: TagRequest): ResponseEntity<Tag> {
        val updated = tagService.update(id, request) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!tagService.delete(id)) return ResponseEntity.notFound().build()
        return ResponseEntity.noContent().build()
    }
}

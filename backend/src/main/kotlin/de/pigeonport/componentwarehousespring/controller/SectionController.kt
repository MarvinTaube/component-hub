package de.pigeonport.componentwarehousespring.controller

import de.pigeonport.componentwarehousespring.model.entity.Section
import de.pigeonport.componentwarehousespring.model.request.SectionRequest
import de.pigeonport.componentwarehousespring.service.SectionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sections")
class SectionController(private val sectionService: SectionService) {

    @GetMapping
    fun getAll(): Iterable<Section> = sectionService.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Section> {
        val section = sectionService.getById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(section)
    }

    @PostMapping
    fun create(@RequestBody request: SectionRequest): ResponseEntity<Section> {
        val created = sectionService.create(request) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: SectionRequest): ResponseEntity<Section> {
        val updated = sectionService.update(id, request) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!sectionService.delete(id)) return ResponseEntity.notFound().build()
        return ResponseEntity.noContent().build()
    }
}

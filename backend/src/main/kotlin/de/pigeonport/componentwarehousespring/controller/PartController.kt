package de.pigeonport.componentwarehousespring.controller

import de.pigeonport.componentwarehousespring.model.entity.Part
import de.pigeonport.componentwarehousespring.model.request.PartRequest
import de.pigeonport.componentwarehousespring.service.PartService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/parts")
class PartController(
    private val partService: PartService,
) {

    @GetMapping
    fun getAll(): Iterable<Part> =
        partService.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Part> {
        val part = partService.getById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(part)
    }

    @PostMapping
    fun create(@RequestBody body: PartRequest): ResponseEntity<Part> {
        val created = partService.create(body)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody body: PartRequest): ResponseEntity<Part> {
        val updated = partService.update(id, body)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!partService.delete(id)) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.noContent().build()
    }
}
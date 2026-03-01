package de.pigeonport.componentwarehousespring.controller

import de.pigeonport.componentwarehousespring.PartRepository
import de.pigeonport.componentwarehousespring.model.entity.Part
import de.pigeonport.componentwarehousespring.model.request.PartRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/parts")
class PartController(
    private val partRepository: PartRepository,
) {

    @GetMapping
    fun getAll(): Iterable<Part> =
        partRepository.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Part> {
        val part = partRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(part)
    }

    @PostMapping
    fun create(@RequestBody body: PartRequest): ResponseEntity<Part> {
        val created = partRepository.save(
            Part(
                id = 0L, // required by your constructor; JPA will generate the real ID
                name = body.name,
                description = body.description,
                stock = body.stock,
                links = body.links,
                notes = body.notes,
                imageLink = body.imageLink,
                section = null,
                category = null,
                tag = mutableListOf(),
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody body: PartRequest): ResponseEntity<Part> {
        val existing = partRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        val updated = partRepository.save(
            Part(
                id = existing.id,
                name = body.name,
                description = body.description,
                stock = body.stock,
                links = body.links,
                notes = body.notes,
                imageLink = body.imageLink,
                section = existing.section,
                category = existing.category,
                tag = existing.tag,
            )
        )
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!partRepository.existsById(id)) {
            return ResponseEntity.notFound().build()
        }
        partRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
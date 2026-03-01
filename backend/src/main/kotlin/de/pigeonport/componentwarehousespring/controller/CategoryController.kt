package de.pigeonport.componentwarehousespring.controller

import de.pigeonport.componentwarehousespring.model.entity.Category
import de.pigeonport.componentwarehousespring.model.request.CategoryRequest
import de.pigeonport.componentwarehousespring.service.CategoryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/categories")
class CategoryController(private val categoryService: CategoryService) {

    @GetMapping
    fun getAll(): Iterable<Category> = categoryService.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Category> {
        val category = categoryService.getById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(category)
    }

    @PostMapping
    fun create(@RequestBody request: CategoryRequest): ResponseEntity<Category> {
        val created = categoryService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: CategoryRequest): ResponseEntity<Category> {
        val updated = categoryService.update(id, request) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!categoryService.delete(id)) return ResponseEntity.notFound().build()
        return ResponseEntity.noContent().build()
    }
}

package de.pigeonport.componentwarehousespring.controller

import de.pigeonport.componentwarehousespring.model.entity.Drawer
import de.pigeonport.componentwarehousespring.model.request.DrawerRequest
import de.pigeonport.componentwarehousespring.service.DrawerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/drawers")
class DrawerController(private val drawerService: DrawerService) {

    @GetMapping
    fun getAll(): Iterable<Drawer> = drawerService.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Drawer> {
        val drawer = drawerService.getById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(drawer)
    }

    @PostMapping
    fun create(@RequestBody request: DrawerRequest): ResponseEntity<Drawer> {
        val created = drawerService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: DrawerRequest): ResponseEntity<Drawer> {
        val updated = drawerService.update(id, request) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!drawerService.delete(id)) return ResponseEntity.notFound().build()
        return ResponseEntity.noContent().build()
    }
}

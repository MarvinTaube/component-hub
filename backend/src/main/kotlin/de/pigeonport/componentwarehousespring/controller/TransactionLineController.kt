package de.pigeonport.componentwarehousespring.controller

import de.pigeonport.componentwarehousespring.model.entity.TransactionLine
import de.pigeonport.componentwarehousespring.model.request.TransactionLineRequest
import de.pigeonport.componentwarehousespring.service.TransactionLineService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/transaction-lines")
class TransactionLineController(private val transactionLineService: TransactionLineService) {

    @GetMapping
    fun getAll(): Iterable<TransactionLine> = transactionLineService.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<TransactionLine> {
        val line = transactionLineService.getById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(line)
    }

    @PostMapping
    fun create(@RequestBody request: TransactionLineRequest): ResponseEntity<TransactionLine> {
        val created = transactionLineService.create(request) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: TransactionLineRequest): ResponseEntity<TransactionLine> {
        val updated = transactionLineService.update(id, request) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!transactionLineService.delete(id)) return ResponseEntity.notFound().build()
        return ResponseEntity.noContent().build()
    }
}

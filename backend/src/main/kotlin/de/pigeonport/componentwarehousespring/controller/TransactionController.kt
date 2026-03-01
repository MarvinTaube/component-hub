package de.pigeonport.componentwarehousespring.controller

import de.pigeonport.componentwarehousespring.model.entity.Transaction
import de.pigeonport.componentwarehousespring.model.request.TransactionRequest
import de.pigeonport.componentwarehousespring.service.TransactionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/transactions")
class TransactionController(private val transactionService: TransactionService) {

    @GetMapping
    fun getAll(): Iterable<Transaction> = transactionService.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Transaction> {
        val transaction = transactionService.getById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(transaction)
    }

    @PostMapping
    fun create(@RequestBody request: TransactionRequest): ResponseEntity<Transaction> {
        val created = transactionService.create(request) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: TransactionRequest): ResponseEntity<Transaction> {
        val updated = transactionService.update(id, request) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!transactionService.delete(id)) return ResponseEntity.notFound().build()
        return ResponseEntity.noContent().build()
    }
}

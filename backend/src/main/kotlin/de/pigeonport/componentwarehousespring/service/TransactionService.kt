package de.pigeonport.componentwarehousespring.service

import de.pigeonport.componentwarehousespring.model.entity.Transaction
import de.pigeonport.componentwarehousespring.model.request.TransactionRequest
import de.pigeonport.componentwarehousespring.repository.ProjectRepository
import de.pigeonport.componentwarehousespring.repository.TransactionRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val projectRepository: ProjectRepository
) {

    fun getAll(): Iterable<Transaction> = transactionRepository.findAll()

    fun getById(id: Long): Transaction? = transactionRepository.findById(id).orElse(null)

    fun create(request: TransactionRequest): Transaction? {
        val project = projectRepository.findById(request.projectId).orElse(null) ?: return null
        val transaction = Transaction(
            id = 0L,
            project = project,
            createdAt = LocalDateTime.now() // PrePersist will also set it, but we need it for the constructor
        )
        return transactionRepository.save(transaction)
    }

    fun update(id: Long, request: TransactionRequest): Transaction? {
        val existing = transactionRepository.findById(id).orElse(null) ?: return null
        val project = projectRepository.findById(request.projectId).orElse(null) ?: return null
        
        // Transaction entity is defined with 'val' fields mostly, let's see if we can update it
        // Actually the Transaction class is 'open' and has val project and val transactionLines
        // We'll create a new one to update.
        val updated = Transaction(
            id = existing.id,
            project = project,
            createdAt = existing.createdAt
        )
        return transactionRepository.save(updated)
    }

    fun delete(id: Long): Boolean {
        if (!transactionRepository.existsById(id)) return false
        transactionRepository.deleteById(id)
        return true
    }
}

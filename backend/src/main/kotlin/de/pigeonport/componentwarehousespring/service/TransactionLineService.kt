package de.pigeonport.componentwarehousespring.service

import de.pigeonport.componentwarehousespring.model.entity.TransactionLine
import de.pigeonport.componentwarehousespring.model.request.TransactionLineRequest
import de.pigeonport.componentwarehousespring.repository.PartRepository
import de.pigeonport.componentwarehousespring.repository.TransactionLineRepository
import de.pigeonport.componentwarehousespring.repository.TransactionRepository
import org.springframework.stereotype.Service

@Service
class TransactionLineService(
    private val transactionLineRepository: TransactionLineRepository,
    private val transactionRepository: TransactionRepository,
    private val partRepository: PartRepository
) {

    fun getAll(): Iterable<TransactionLine> = transactionLineRepository.findAll()

    fun getById(id: Long): TransactionLine? = transactionLineRepository.findById(id).orElse(null)

    fun create(request: TransactionLineRequest): TransactionLine? {
        val transaction = transactionRepository.findById(request.transactionId).orElse(null) ?: return null
        val part = partRepository.findById(request.partId).orElse(null) ?: return null
        val line = TransactionLine(
            id = 0L,
            transaction = transaction,
            part = part,
            quantity = request.quantity
        )
        return transactionLineRepository.save(line)
    }

    fun update(id: Long, request: TransactionLineRequest): TransactionLine? {
        val existing = transactionLineRepository.findById(id).orElse(null) ?: return null
        val transaction = transactionRepository.findById(request.transactionId).orElse(null) ?: return null
        val part = partRepository.findById(request.partId).orElse(null) ?: return null
        val updated = TransactionLine(
            id = existing.id,
            transaction = transaction,
            part = part,
            quantity = request.quantity
        )
        return transactionLineRepository.save(updated)
    }

    fun delete(id: Long): Boolean {
        if (!transactionLineRepository.existsById(id)) return false
        transactionLineRepository.deleteById(id)
        return true
    }
}

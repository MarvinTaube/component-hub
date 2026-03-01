package de.pigeonport.componentwarehousespring.repository

import de.pigeonport.componentwarehousespring.model.entity.Transaction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : CrudRepository<Transaction, Long>

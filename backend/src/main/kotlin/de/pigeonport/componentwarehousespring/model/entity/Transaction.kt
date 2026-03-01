package de.pigeonport.componentwarehousespring.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import java.time.LocalDateTime

@Entity
open class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @ManyToOne
    val project: Project,

    @OneToMany(mappedBy = "transaction")
    val transactionLines: MutableList<TransactionLine> = mutableListOf(),
    var createdAt: LocalDateTime,
){
    @PrePersist
    protected fun onCreate() {
        createdAt = LocalDateTime.now()
    }
}

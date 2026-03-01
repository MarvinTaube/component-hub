package de.pigeonport.componentwarehousespring.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
data class TransactionLine(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @ManyToOne
    val transaction: Transaction,

    @ManyToOne
    val part: Part,
    val quantity: Int,
)

package de.pigeonport.componentwarehousespring.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
data class Project(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val name: String,
    val description: String,
    @OneToMany(mappedBy = "project")
    val transactions: MutableList<Transaction> = mutableListOf(),
    val finished: Boolean = false,
    val published: Boolean = false,
    val blogLink: String? = null
)

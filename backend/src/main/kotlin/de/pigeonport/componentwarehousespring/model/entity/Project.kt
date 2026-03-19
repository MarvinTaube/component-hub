package de.pigeonport.componentwarehousespring.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Column

@Entity
data class Project(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val name: String,
    @Column(columnDefinition = "TEXT")
    val description: String,
    @OneToMany(mappedBy = "project")
    val transactions: MutableList<Transaction> = mutableListOf(),
    val finished: Boolean = false,
    val published: Boolean = false,
    @Column(columnDefinition = "TEXT")
    val blogLink: String? = null
)

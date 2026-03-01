package de.pigeonport.componentwarehousespring.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.*

@Entity
data class Part(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val name: String,
    val description: String,
    val stock: Int,
    val links: List<String> = emptyList(),
    val notes: String? = null,
    val imageLink: String? = null,
    @OneToOne
    val section: Section? = null,
    @ManyToOne
    val category: Category? = null,
    @ManyToMany
    val tag: MutableList<Tag> = mutableListOf(),
)

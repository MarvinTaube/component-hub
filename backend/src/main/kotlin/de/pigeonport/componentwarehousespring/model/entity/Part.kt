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
    @Column(columnDefinition = "TEXT")
    val description: String,
    val stock: Int,
    @ElementCollection
    @CollectionTable(name = "part_links", joinColumns = [JoinColumn(name = "part_id")])
    @Column(name = "link", columnDefinition = "TEXT")
    val links: List<String> = emptyList(),
    @Column(columnDefinition = "TEXT")
    val notes: String? = null,
    @Column(columnDefinition = "TEXT")
    val imageLink: String? = null,
    @ManyToOne
    val drawer: Drawer? = null,
    val sectionNumber: Int = 1,
    @ManyToOne
    val category: Category? = null,
    @ManyToMany
    val tag: MutableList<Tag> = mutableListOf(),
)

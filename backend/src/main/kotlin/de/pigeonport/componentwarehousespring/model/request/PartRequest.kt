package de.pigeonport.componentwarehousespring.model.request

data class PartRequest(
    val name: String,
    val description: String,
    val stock: Int,
    val links: List<String> = emptyList(),
    val notes: String? = null,
    val imageLink: String? = null,
)

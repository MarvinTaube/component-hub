package de.pigeonport.componentwarehousespring.model.request

data class CategoryRequest(
    val name: String,
    val notes: String? = null
)

data class DrawerRequest(
    val number: Int
)

data class ProjectRequest(
    val name: String,
    val description: String,
    val finished: Boolean = false,
    val published: Boolean = false,
    val blogLink: String? = null
)

data class TagRequest(
    val name: String,
    val color: String
)

data class SectionRequest(
    val position: Int,
    val drawerId: Long
)

data class TransactionRequest(
    val projectId: Long
)

data class TransactionLineRequest(
    val transactionId: Long,
    val partId: Long,
    val quantity: Int
)

data class PartRequest(
    val name: String,
    val description: String,
    val stock: Int,
    val links: List<String> = emptyList(),
    val notes: String? = null,
    val imageLink: String? = null,
)

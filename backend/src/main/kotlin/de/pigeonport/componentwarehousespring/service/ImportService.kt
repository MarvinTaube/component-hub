package de.pigeonport.componentwarehousespring.service
import de.pigeonport.componentwarehousespring.model.entity.Category
import de.pigeonport.componentwarehousespring.model.entity.Drawer
import de.pigeonport.componentwarehousespring.model.entity.Part
import de.pigeonport.componentwarehousespring.repository.CategoryRepository
import de.pigeonport.componentwarehousespring.repository.DrawerRepository
import de.pigeonport.componentwarehousespring.repository.PartRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue

@Service
class ImportService(
    private val partRepository: PartRepository,
    private val drawerRepository: DrawerRepository,
    private val categoryRepository: CategoryRepository
) {
    private val logger = LoggerFactory.getLogger(ImportService::class.java)
    private val mapper = jacksonObjectMapper()

    @PostConstruct
    fun onStart() {
        if (partRepository.count() == 0L) {
            logger.info("No parts found in database, starting import from import_items.json")
            //importItems()
        } else {
            logger.info("Database already contains parts, skipping import")
        }
    }

    @Transactional
    fun importItems() {
        val resource = ClassPathResource("import_items.json")
        if (!resource.exists()) {
            logger.warn("import_items.json not found in classpath - skipping import")
            return
        }

        try {
            val items: List<Map<String, Any>> = mapper.readValue(resource.inputStream)
            items.forEach { item ->
                val name = item["name"] as? String ?: return@forEach
                val description = item["description"] as? String ?: ""
                val categoryName = item["category"] as? String ?: ""
                val notes = item["notes"] as? String
                val quantity = (item["quantity"] as? Number)?.toInt() ?: 0
                val imageLink = item["imageLink"] as? String
                val boxNumber = (item["box"] as? Number)?.toInt()

                val category = if (!categoryName.isNullOrBlank()) {
                    categoryRepository.findByName(categoryName) 
                        ?: categoryRepository.save(Category(id = 0, name = categoryName))
                } else null

                val drawer = boxNumber?.let {
                    drawerRepository.findByNumber(it) 
                        ?: drawerRepository.save(Drawer(id = 0, number = it))
                }

                val part = Part(
                    id = 0,
                    name = name,
                    description = description,
                    stock = quantity,
                    links = (item["links"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    notes = notes,
                    imageLink = imageLink,
                    drawer = drawer,
                    category = category,
                    sectionNumber = 1,
                    tag = mutableListOf()
                )
                partRepository.save(part)
            }
            logger.info("Imported ${items.size} items successfully")
        } catch (e: Exception) {
            logger.error("Failed to import items from JSON", e)
        }
    }
}

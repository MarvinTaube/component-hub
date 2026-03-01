package de.pigeonport.componentwarehousespring.repository

import de.pigeonport.componentwarehousespring.model.entity.Section
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SectionRepository : CrudRepository<Section, Long>

package de.pigeonport.componentwarehousespring.repository

import de.pigeonport.componentwarehousespring.model.entity.Tag
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TagRepository : CrudRepository<Tag, Long>

package de.pigeonport.componentwarehouse.database

import kotlinx.serialization.json.Json
import org.bson.Document

data class Drawer(
    val number: Int
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): Drawer = json.decodeFromString(document.toJson())
    }
}
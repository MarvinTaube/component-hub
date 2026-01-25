package de.pigeonport.componentwarehouse.database

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.discardingSink
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.types.ObjectId

@Serializable
data class Car(
    val brandName: String,
    val model: String,
    val number: String
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): Car = json.decodeFromString(document.toJson())
    }
}

//Used as Transfer object where the ID is needed.
// Especially for GET / UPDATE / DELETE Requests where the ID is needed in the first place
@Serializable
data class CarDto(
    val id: String,
    val brandName: String,
    val model: String,
    val number: String
)

class CarService(private val database: MongoDatabase) {
    var collection: MongoCollection<Document>

    init {
        if (!database.listCollectionNames().contains("cars")){
            database.createCollection("cars")
        }
        collection = database.getCollection("cars")
    }

    // Create new car
    suspend fun create(car: Car): String = withContext(Dispatchers.IO) {
        val doc = car.toDocument()
        collection.insertOne(doc)
        doc["_id"].toString()
    }

    // Read a car (return DTO with id)
    suspend fun read(id: String): CarDto? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("_id", ObjectId(id))).first()?.let { doc ->
            val car = Car.fromDocument(doc)
            CarDto(
                id = doc.getObjectId("_id").toHexString(),
                brandName = car.brandName,
                model = car.model,
                number = car.number
            )
        }
    }

    suspend fun readAll(): List<CarDto>{
        val cars = collection.find().toList().map { doc ->
            val car = Car.fromDocument(doc)
            return@map CarDto(
                id = doc.getObjectId("_id").toHexString(),
                brandName = car.brandName,
                model = car.model,
                number = car.number
            )
        }
        return cars
    }

    // Update a car
    suspend fun update(id: String, car: Car): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndReplace(Filters.eq("_id", ObjectId(id)), car.toDocument())
    }

    // Delete a car
    suspend fun delete(id: String): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))
    }
}


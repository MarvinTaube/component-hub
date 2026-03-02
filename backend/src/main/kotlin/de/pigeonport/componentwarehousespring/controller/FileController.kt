package de.pigeonport.componentwarehousespring.controller

import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@RestController
@RequestMapping("/api/files")
class FileController(
    @org.springframework.beans.factory.annotation.Value("\${app.data-dir:data}")
    private val dataDirProperty: String
) {

    private val dataDir by lazy {
        Paths.get(dataDirProperty).toAbsolutePath().normalize()
    }
    private val imgDir by lazy { dataDir.resolve("img").resolve("items") }
    private val docDir by lazy { dataDir.resolve("doc") }

    @GetMapping("/getFile")
    fun getFile(
        @RequestParam file: String,
        @RequestParam type: String
    ): ResponseEntity<Resource> {
        val targetDir = when (type) {
            "itemImage" -> imgDir
            "doc" -> docDir
            else -> return ResponseEntity.badRequest().build()
        }

        // Security check: ensure the file name doesn't contain path traversal characters
        if (file.contains("..") || file.contains("/") || file.contains("\\")) {
            return ResponseEntity.status(403).build()
        }

        val filePath = targetDir.resolve(file)
        val resource = FileSystemResource(filePath.toFile())

        if (!resource.exists()) {
            return ResponseEntity.notFound().build()
        }

        val contentType = try {
            Files.probeContentType(filePath)
        } catch (e: Exception) {
            null
        } ?: "application/octet-stream"

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${resource.filename}\"")
            .body(resource)
    }

    @PostMapping("/upload")
    fun uploadFile(
        @RequestParam("file") file: org.springframework.web.multipart.MultipartFile,
        @RequestParam("type") type: String
    ): ResponseEntity<Map<String, String>> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Empty file"))
        }

        val targetDir = when (type) {
            "itemImage" -> imgDir
            "doc" -> docDir
            else -> return ResponseEntity.badRequest().body(mapOf("error" to "Invalid type"))
        }

        val originalFilename = file.originalFilename ?: "unknown"
        
        // Security check
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            return ResponseEntity.status(403).body(mapOf("error" to "Invalid filename"))
        }

        // Ensure directories exist
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir)
        }

        // Generate a unique filename to avoid overwrites
        val ext = originalFilename.substringAfterLast('.', "")
        val uniqueFilename = "${java.util.UUID.randomUUID()}${if (ext.isNotEmpty()) ".$ext" else ""}"
        
        val targetFile = targetDir.resolve(uniqueFilename).toFile()
        file.transferTo(targetFile)

        return ResponseEntity.ok(mapOf("filename" to uniqueFilename, "originalName" to originalFilename))
    }
}

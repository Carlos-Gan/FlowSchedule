package com.mocas.data.backup

import android.content.Context
import java.io.File

data class AutomaticBackupInfo(
    val fileName: String,
    val createdAtMillis: Long,
    val reason: String,
    val sizeBytes: Long
)

class AutomaticBackupManager(context: Context) {
    private val directory = File(context.filesDir, "automatic_backups")

    fun create(json: String, reasonCode: String): AutomaticBackupInfo {
        check(directory.exists() || directory.mkdirs()) { "No se pudo crear la carpeta de respaldos." }
        var timestamp = System.currentTimeMillis()
        var file = File(directory, "backup_${timestamp}_${reasonCode}.json")
        while (file.exists()) {
            timestamp++
            file = File(directory, "backup_${timestamp}_${reasonCode}.json")
        }
        val temporary = File(directory, "${file.name}.tmp")
        temporary.writeText(json, Charsets.UTF_8)
        check(temporary.renameTo(file)) { "No se pudo guardar el respaldo automático." }
        listFiles().drop(MAX_BACKUPS).forEach { File(directory, it.fileName).delete() }
        return file.toInfo()
    }

    fun listFiles(): List<AutomaticBackupInfo> = directory.listFiles()
        .orEmpty()
        .filter { it.isFile && it.extension == "json" && it.name.startsWith("backup_") }
        .map { it.toInfo() }
        .sortedByDescending { it.createdAtMillis }

    fun read(fileName: String): String {
        val file = safeFile(fileName)
        require(file.isFile) { "El respaldo automático ya no existe." }
        return file.readText(Charsets.UTF_8)
    }

    fun delete(fileName: String): Boolean = safeFile(fileName).delete()

    private fun safeFile(fileName: String): File {
        require(fileName == File(fileName).name) { "Nombre de respaldo inválido." }
        return File(directory, fileName)
    }

    private fun File.toInfo(): AutomaticBackupInfo {
        val parts = nameWithoutExtension.split('_', limit = 3)
        val timestamp = parts.getOrNull(1)?.toLongOrNull() ?: lastModified()
        val reason = when (parts.getOrNull(2)) {
            "import" -> "Antes de importar"
            "clear" -> "Antes de borrar datos"
            "restore" -> "Antes de restaurar"
            "scheduled" -> "Respaldo programado"
            else -> "Respaldo automático"
        }
        return AutomaticBackupInfo(name, timestamp, reason, length())
    }

    companion object {
        private const val MAX_BACKUPS = 5
    }
}

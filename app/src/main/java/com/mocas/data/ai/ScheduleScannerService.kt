package com.mocas.data.ai

import android.graphics.Bitmap
import android.util.Base64
import com.mocas.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class DetectedSubjectItem(
    val name: String,
    val professor: String,
    val dayOfWeek: Int, // 1=Lunes .. 6=Sábado
    val startTime: String, // "08:00"
    val endTime: String,   // "09:30"
    val room: String,
    val colorHex: String = "#3B82F6",
    val isSelected: Boolean = true
)

object ScheduleScannerService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeScheduleImage(bitmap: Bitmap): List<DetectedSubjectItem> =
        withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            require(apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY")) {
                "Configura GEMINI_API_KEY para analizar imágenes."
            }
            val responseJson = callGeminiVision(apiKey, bitmapToBase64(bitmap))
            val items = parseGeminiResponse(responseJson)
            check(items.isNotEmpty()) { "No se detectaron materias en la imagen." }
            items
        }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun callGeminiVision(apiKey: String, base64Image: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        val prompt = """
            Eres un asistente especializado en extraer horarios de clases de fotografías.
            Analiza la imagen del horario escolar y devuelve un arreglo JSON con las materias detectadas.
            Formato requerido:
            [
              {
                "name": "Nombre de la materia",
                "professor": "Profesor o docente",
                "dayOfWeek": 1, // 1 para Lunes, 2 para Martes, 3 para Miércoles, 4 para Jueves, 5 para Viernes, 6 para Sábado
                "startTime": "08:00", // formato HH:mm
                "endTime": "09:30",   // formato HH:mm
                "room": "Salón o laboratorio",
                "colorHex": "#3B82F6"
              }
            ]
            Devuelve ÚNICAMENTE el JSON válido sin formato markdown ni texto adicional.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("El servicio de IA respondió HTTP ${response.code}.")
            }
            return response.body.string()
        }
    }

    private fun parseGeminiResponse(responseStr: String): List<DetectedSubjectItem> {
        val root = JSONObject(responseStr)
        val candidates = root.getJSONArray("candidates")
        val text = candidates.getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
        val cleanJson = text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val array = JSONArray(cleanJson)
        return List(array.length()) { index ->
            val obj = array.getJSONObject(index)
            DetectedSubjectItem(
                name = obj.getString("name").trim(),
                professor = obj.optString("professor").trim(),
                dayOfWeek = obj.getInt("dayOfWeek"),
                startTime = obj.getString("startTime"),
                endTime = obj.getString("endTime"),
                room = obj.optString("room").trim(),
                colorHex = obj.optString("colorHex", "#3B82F6"),
                isSelected = true
            )
        }
    }
}

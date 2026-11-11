package com.mocas.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.core.graphics.toColorInt

data class SharePrivacyOptions(
    val showProfessor: Boolean = true,
    val showRoom: Boolean = true,
    val includePrivateActivities: Boolean = false
)

object ScheduleShareExporter {
    fun createBitmap(
        subjects: List<SubjectWithSlots>,
        events: List<SchoolEventWithSubject>,
        weekStart: LocalDate,
        options: SharePrivacyOptions
    ): Bitmap {
        val dayItems = (1..7).associateWith { day ->
            val date = weekStart.plusDays((day - 1).toLong())
            val classes = subjects.flatMap { subject ->
                subject.slots.filter { it.dayOfWeek == day }.map { slot ->
                    ShareLine(
                        time = slot.startTime,
                        title = subject.subject.name,
                        detail = buildList {
                            if (options.showRoom) add(slot.room.ifBlank { subject.subject.defaultRoom })
                            if (options.showProfessor) add(subject.subject.professor)
                        }.filter { it.isNotBlank() }.joinToString(" · "),
                        color = parseColor(subject.subject.colorHex)
                    )
                }
            }
            val activities = events.filter { item ->
                item.event.startDate <= date.toString() && item.event.endDate >= date.toString() &&
                    (options.includePrivateActivities || item.event.organizationTag != "PERSONAL")
            }.map { item ->
                ShareLine(item.event.startTime ?: "Todo el día", "• ${item.event.title}", "Actividad", Color.rgb(139, 92, 246))
            }
            (classes + activities).sortedBy { it.time }
        }
        val maxRows = dayItems.values.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1
        val width = 1960
        val height = (300 + maxRows * 150).coerceAtLeast(850)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.rgb(247, 248, 252))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(29, 38, 58); paint.textSize = 58f; paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        canvas.drawText("FlowSchedule", 70f, 82f, paint)
        paint.textSize = 32f; paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText("Horario semanal · ${rangeLabel(weekStart)}", 70f, 132f, paint)
        val margin = 55f
        val gap = 12f
        val columnWidth = (width - margin * 2 - gap * 6) / 7f
        val names = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        (1..7).forEach { day ->
            val left = margin + (day - 1) * (columnWidth + gap)
            paint.color = Color.rgb(88, 71, 235)
            canvas.drawRoundRect(RectF(left, 175f, left + columnWidth, 245f), 22f, 22f, paint)
            paint.color = Color.WHITE; paint.textSize = 28f; paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
            canvas.drawText("${names[day - 1]} ${weekStart.plusDays((day - 1).toLong()).dayOfMonth}", left + 18f, 220f, paint)
            dayItems.getValue(day).forEachIndexed { index, line ->
                val top = 270f + index * 145f
                paint.color = Color.WHITE
                canvas.drawRoundRect(RectF(left, top, left + columnWidth, top + 125f), 18f, 18f, paint)
                paint.color = line.color
                canvas.drawRoundRect(RectF(left, top, left + 8f, top + 125f), 8f, 8f, paint)
                paint.color = Color.rgb(88, 71, 235); paint.textSize = 21f; paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
                canvas.drawText(line.time, left + 18f, top + 30f, paint)
                paint.color = Color.rgb(29, 38, 58); paint.textSize = 22f
                drawEllipsized(canvas, line.title, left + 18f, top + 65f, columnWidth - 34f, paint)
                paint.color = Color.rgb(100, 116, 139); paint.textSize = 17f; paint.typeface = android.graphics.Typeface.DEFAULT
                drawEllipsized(canvas, line.detail, left + 18f, top + 96f, columnWidth - 34f, paint)
            }
        }
        return bitmap
    }

    fun sharePng(context: Context, bitmap: Bitmap): Intent {
        val file = exportFile(context, "horario-semanal.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return shareIntent(context, file, "image/png")
    }

    fun sharePdf(context: Context, bitmap: Bitmap): Intent {
        val file = exportFile(context, "horario-semanal.pdf")
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create())
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)
        FileOutputStream(file).use(document::writeTo)
        document.close()
        return shareIntent(context, file, "application/pdf")
    }

    fun shareFlow(context: Context, json: String): Intent {
        val file = exportFile(context, "FlowSchedule.flow")
        FileOutputStream(file).use { it.write(json.toByteArray(Charsets.UTF_8)) }
        return shareIntent(context, file, "application/octet-stream")
    }

    private fun shareIntent(context: Context, file: File, mime: String): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun exportFile(context: Context, name: String): File =
        File(context.cacheDir, "exports").apply { mkdirs() }.resolve(name)

    private fun drawEllipsized(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, paint: Paint) {
        val value = if (paint.measureText(text) <= maxWidth) text else {
            var shortened = text
            while (shortened.isNotEmpty() && paint.measureText("$shortened…") > maxWidth) shortened = shortened.dropLast(1)
            "$shortened…"
        }
        canvas.drawText(value, x, y, paint)
    }

    private fun parseColor(hex: String): Int = runCatching { hex.toColorInt() }.getOrDefault(Color.rgb(59, 130, 246))
    private fun rangeLabel(start: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
        return "${start.format(formatter)} – ${start.plusDays(6).format(formatter)} ${start.year}"
    }

    private data class ShareLine(val time: String, val title: String, val detail: String, val color: Int)
}

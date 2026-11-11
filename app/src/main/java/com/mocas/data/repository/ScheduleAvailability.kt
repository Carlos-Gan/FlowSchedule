package com.mocas.data.repository

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.util.DateTimeUtils
import java.time.LocalDate

/** A privacy-safe weekly schedule: only busy/free half-hour blocks are shared. */
data class SharedAvailability(val weekStart: LocalDate, val busy: BooleanArray)

object ScheduleAvailability {
    private const val SLOTS_PER_DAY = 48
    private const val TOTAL_SLOTS = 7 * SLOTS_PER_DAY
    private const val SCHEME = "flowschedule"
    private const val HOST = "compare"

    fun fromSchedule(
        subjects: List<SubjectWithSlots>,
        weekStart: LocalDate,
        events: List<SchoolEventWithSubject> = emptyList(),
        classExceptions: List<ClassExceptionEntity> = emptyList()
    ): SharedAvailability {
        val busy = BooleanArray(TOTAL_SLOTS)
        fun mark(day: Int, start: Int, end: Int) {
            if (day !in 0..6) return
            for (halfHour in 0 until SLOTS_PER_DAY) {
                val minute = halfHour * 30
                if (minute < end && minute + 30 > start) busy[day * SLOTS_PER_DAY + halfHour] = true
            }
        }
        (0..6).forEach { day ->
            val localDate = weekStart.plusDays(day.toLong())
            val date = localDate.toString()
            subjects.filter { item ->
                val starts = DateTimeUtils.parseDate(item.subject.semesterStart)
                val ends = DateTimeUtils.parseDate(item.subject.semesterEnd)
                starts != null && ends != null && localDate in starts..ends
            }.flatMap { item ->
                item.slots.filter { it.dayOfWeek == day + 1 }.mapNotNull { slot ->
                    val exception = classExceptions.firstOrNull { it.slotId == slot.id && it.date == date }
                    when {
                        exception == null -> slot
                        exception.type == ClassExceptionType.CANCELED -> null
                        else -> slot.copy(
                            startTime = exception.newStartTime ?: slot.startTime,
                            endTime = exception.newEndTime ?: slot.endTime
                        )
                    }
                }
            }.forEach slotLoop@{ slot ->
                val start = parseMinutes(slot.startTime) ?: return@slotLoop
                val end = parseMinutes(slot.endTime) ?: return@slotLoop
                mark(day, start, end)
            }
            events.filter { it.event.startDate <= date && it.event.endDate >= date }.forEach { item ->
                val event = item.event
                if (event.isAllDay || event.startTime == null || event.endTime == null) mark(day, 0, 24 * 60)
                else mark(day, parseMinutes(event.startTime) ?: 0, parseMinutes(event.endTime) ?: 24 * 60)
            }
        }
        return SharedAvailability(weekStart, busy)
    }

    private fun parseMinutes(value: String): Int? = runCatching {
        value.substring(0, 5).split(":").let { it[0].toInt() * 60 + it[1].toInt() }
    }.getOrNull()

    fun toUri(availability: SharedAvailability): Uri {
        val bytes = ByteArray((TOTAL_SLOTS + 7) / 8)
        availability.busy.forEachIndexed { index, occupied -> if (occupied) bytes[index / 8] = (bytes[index / 8].toInt() or (1 shl (index % 8))).toByte() }
        return Uri.Builder().scheme(SCHEME).authority(HOST)
            .appendQueryParameter("week", availability.weekStart.toString())
            .appendQueryParameter("busy", Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
            .build()
    }

    fun fromUri(uri: Uri): SharedAvailability? = runCatching {
        require(uri.scheme == SCHEME && uri.host == HOST)
        val week = LocalDate.parse(uri.getQueryParameter("week"))
        val bytes = Base64.decode(uri.getQueryParameter("busy"), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        require(bytes.size == (TOTAL_SLOTS + 7) / 8)
        SharedAvailability(week, BooleanArray(TOTAL_SLOTS) { index -> bytes[index / 8].toInt() and (1 shl (index % 8)) != 0 })
    }.getOrNull()

    fun commonFree(participants: List<SharedAvailability>): BooleanArray {
        if (participants.isEmpty()) return BooleanArray(TOTAL_SLOTS)
        return BooleanArray(TOTAL_SLOTS) { index -> participants.all { !it.busy[index] } }
    }

    fun qrBitmap(uri: Uri, size: Int = 720): Bitmap {
        val matrix = MultiFormatWriter().encode(uri.toString(), BarcodeFormat.QR_CODE, size, size, mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 2
        ))
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { bitmap ->
            for (x in 0 until size) for (y in 0 until size) bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }

    fun slotLabel(index: Int): String {
        val minutes = (index % SLOTS_PER_DAY) * 30
        return "%02d:%02d".format(minutes / 60, minutes % 60)
    }

    fun dayIndex(index: Int) = index / SLOTS_PER_DAY
}

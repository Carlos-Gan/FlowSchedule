package com.mocas.data.backup

import android.content.Context
import com.mocas.R
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.local.SubtaskEntity
import com.mocas.data.local.EventPriority
import com.mocas.data.local.RecurrenceType
import com.mocas.data.local.GradeCategoryEntity
import com.mocas.data.local.GradeItemEntity
import com.mocas.data.local.GradeUnitCategoryWeightEntity
import com.mocas.data.local.GradeUnitEntity
import com.mocas.data.local.ActivityGradeEntity
import org.json.JSONArray
import org.json.JSONObject

data class ScheduleBackupData(
    val periods: List<AcademicPeriodEntity>,
    val subjects: List<SubjectWithSlots>,
    val events: List<SchoolEventEntity>,
    val exceptions: List<ClassExceptionEntity>,
    val subtasks: List<SubtaskEntity> = emptyList(),
    val gradeCategories: List<GradeCategoryEntity> = emptyList(),
    val gradeItems: List<GradeItemEntity> = emptyList(),
    val gradeUnits: List<GradeUnitEntity> = emptyList(),
    val gradeUnitCategoryWeights: List<GradeUnitCategoryWeightEntity> = emptyList(),
    val activityGrades: List<ActivityGradeEntity> = emptyList()
)

data class BackupImportSummary(
    val subjects: Int,
    val sessions: Int,
    val activities: Int,
    val periods: Int
)

object ScheduleBackupCodec {
    private const val FORMAT = "flowschedule-backup"
    private const val VERSION = 6

    fun encode(data: ScheduleBackupData): String = JSONObject().apply {
        put("format", FORMAT)
        put("version", VERSION)
        put("exportedAtMillis", System.currentTimeMillis())
        put("periods", JSONArray().apply { data.periods.forEach { put(it.toJson()) } })
        put("subjects", JSONArray().apply {
            data.subjects.forEach { item ->
                put(item.subject.toJson().apply {
                    put("sessions", JSONArray().apply { item.slots.forEach { put(it.toJson()) } })
                })
            }
        })
        put("activities", JSONArray().apply { data.events.forEach { put(it.toJson()) } })
        put("exceptions", JSONArray().apply { data.exceptions.forEach { put(it.toJson()) } })
        put("subtasks", JSONArray().apply { data.subtasks.forEach { put(it.toJson()) } })
        put("gradeCategories", JSONArray().apply { data.gradeCategories.forEach { put(it.toJson()) } })
        put("gradeItems", JSONArray().apply { data.gradeItems.forEach { put(it.toJson()) } })
        put("gradeUnits", JSONArray().apply { data.gradeUnits.forEach { put(it.toJson()) } })
        put("gradeUnitCategoryWeights", JSONArray().apply { data.gradeUnitCategoryWeights.forEach { put(it.toJson()) } })
        put("activityGrades", JSONArray().apply { data.activityGrades.forEach { put(it.toJson()) } })
    }.toString(2)

    fun decode(context: Context, json: String): ScheduleBackupData {
        require(json.length <= 10_000_000) { "El respaldo supera el tamaño permitido." }
        val root = runCatching { JSONObject(json) }.getOrElse {
            throw IllegalArgumentException(context.getString(R.string.error_respaldo_json_invalido))
        }
        val format = root.optString("format")
        require(format == FORMAT || format == "snapmyschedule-backup") {
            context.getString(R.string.error_formato_invalido)
        }
        val version = root.optInt("version", -1)
        require(version in 1..VERSION) {
            context.getString(R.string.error_version_incompatible, version)
        }

        val periods = root.arrayOrEmpty("periods").objects().map { it.toPeriod() }
        val subjects = root.arrayOrEmpty("subjects").objects().map { subjectJson ->
            val subject = subjectJson.toSubject()
            SubjectWithSlots(
                subject = subject,
                slots = subjectJson.arrayOrEmpty("sessions").objects().map { it.toSlot(subject.id) }
            )
        }
        val events = root.arrayOrEmpty("activities").objects().map { it.toEvent() }
        val exceptions = root.arrayOrEmpty("exceptions").objects().map { it.toException() }
        val subtasks = root.arrayOrEmpty("subtasks").objects().map { it.toSubtask() }
        val gradeCategories = root.arrayOrEmpty("gradeCategories").objects().map { it.toGradeCategory() }
        val gradeItems = root.arrayOrEmpty("gradeItems").objects().map { it.toGradeItem(context) }
        val gradeUnits = root.arrayOrEmpty("gradeUnits").objects().map { it.toGradeUnit() }
        val gradeUnitCategoryWeights = root.arrayOrEmpty("gradeUnitCategoryWeights").objects().map { it.toGradeUnitCategoryWeight() }
        val activityGrades = root.arrayOrEmpty("activityGrades").objects().map { it.toActivityGrade() }
        return ScheduleBackupData(periods, subjects, events, exceptions, subtasks, gradeCategories, gradeItems, gradeUnits, gradeUnitCategoryWeights, activityGrades)
    }
}

private fun AcademicPeriodEntity.toJson() = JSONObject().apply {
    put("id", id); put("name", name); put("startDate", startDate); put("endDate", endDate)
    put("colorHex", colorHex); put("createdAtMillis", createdAtMillis)
}

private fun SubjectEntity.toJson() = JSONObject().apply {
    put("id", id); put("name", name); put("code", code); put("professor", professor)
    put("defaultRoom", defaultRoom); put("colorHex", colorHex); put("organizationTag", organizationTag)
    put("isImportant", isImportant); put("semesterStart", semesterStart); put("semesterEnd", semesterEnd)
    put("reminderMinutesBefore", reminderMinutesBefore); put("syncCalendar", syncCalendar)
    put("createdAtMillis", createdAtMillis); put("updatedAtMillis", updatedAtMillis)
}

private fun ScheduleSlotEntity.toJson() = JSONObject().apply {
    put("id", id); put("subjectId", subjectId); put("dayOfWeek", dayOfWeek)
    put("startTime", startTime); put("endTime", endTime); put("room", room)
}

private fun SchoolEventEntity.toJson() = JSONObject().apply {
    put("id", id); put("title", title); put("type", type.name); putNullable("subjectId", subjectId)
    put("countsTowardGrade", countsTowardGrade); putNullable("gradeUnitId", gradeUnitId)
    put("startDate", startDate); put("endDate", endDate); putNullable("startTime", startTime)
    putNullable("endTime", endTime); put("isAllDay", isAllDay); put("location", location)
    put("description", description); put("organizationTag", organizationTag); put("isImportant", isImportant)
    put("priority", priority.name); put("recurrenceType", recurrenceType.name)
    put("recurrenceInterval", recurrenceInterval); putNullable("recurrenceEndDate", recurrenceEndDate)
    putNullable("recurrenceGroupId", recurrenceGroupId)
    put("reminderMinutes", reminderMinutes); put("isCompleted", isCompleted)
    putNullable("completedAtMillis", completedAtMillis)
    put("createdAtMillis", createdAtMillis); put("updatedAtMillis", updatedAtMillis)
}

private fun SubtaskEntity.toJson() = JSONObject().apply {
    put("id", id); put("eventId", eventId); put("title", title); put("isCompleted", isCompleted)
    put("sortOrder", sortOrder); put("createdAtMillis", createdAtMillis); put("updatedAtMillis", updatedAtMillis)
}

private fun GradeCategoryEntity.toJson() = JSONObject().apply {
    put("id", id); put("subjectId", subjectId); put("name", name); put("weightPercent", weightPercent)
    put("sortOrder", sortOrder); put("createdAtMillis", createdAtMillis)
}

private fun GradeItemEntity.toJson() = JSONObject().apply {
    put("id", id); put("categoryId", categoryId); put("unitId", unitId); put("name", name); put("score", score)
    put("unitName", unitName); put("createdAtMillis", createdAtMillis)
}

private fun GradeUnitEntity.toJson() = JSONObject().apply {
    put("id", id); put("subjectId", subjectId); put("name", name); put("sortOrder", sortOrder)
    put("createdAtMillis", createdAtMillis)
}

private fun GradeUnitCategoryWeightEntity.toJson() = JSONObject().apply {
    put("unitId", unitId); put("categoryId", categoryId); put("weightPercent", weightPercent)
}

private fun ActivityGradeEntity.toJson() = JSONObject().apply {
    put("eventId", eventId); put("categoryId", categoryId); put("gradeItemId", gradeItemId)
    put("recordedAtMillis", recordedAtMillis)
}

private fun ClassExceptionEntity.toJson() = JSONObject().apply {
    put("id", id); put("subjectId", subjectId); put("slotId", slotId); put("date", date)
    put("type", type.name); putNullable("newStartTime", newStartTime); putNullable("newEndTime", newEndTime)
    putNullable("newRoom", newRoom); put("note", note); put("createdAtMillis", createdAtMillis)
    put("updatedAtMillis", updatedAtMillis); put("isInTrash", isInTrash)
}

private fun JSONObject.toPeriod() = AcademicPeriodEntity(
    id = requiredLong("id"), name = requiredString("name"), startDate = requiredString("startDate"),
    endDate = requiredString("endDate"), colorHex = optString("colorHex", "#10B981"),
    createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis())
)

private fun JSONObject.toSubject() = SubjectEntity(
    id = requiredLong("id"), name = requiredString("name"), code = optString("code"),
    professor = optString("professor"), defaultRoom = optString("defaultRoom"),
    colorHex = optString("colorHex", "#3B82F6"), organizationTag = optString("organizationTag", "UNIVERSIDAD"),
    isImportant = optBoolean("isImportant"), semesterStart = requiredString("semesterStart"),
    semesterEnd = requiredString("semesterEnd"), reminderMinutesBefore = optInt("reminderMinutesBefore", 15),
    syncCalendar = false, createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis()),
    updatedAtMillis = optLong("updatedAtMillis", System.currentTimeMillis())
)

private fun JSONObject.toSlot(fallbackSubjectId: Long) = ScheduleSlotEntity(
    id = requiredLong("id"), subjectId = optLong("subjectId", fallbackSubjectId),
    dayOfWeek = requiredInt("dayOfWeek"), startTime = requiredString("startTime"),
    endTime = requiredString("endTime"), room = optString("room")
)

private fun JSONObject.toEvent() = SchoolEventEntity(
    id = requiredLong("id"), title = requiredString("title"),
    type = SchoolEventType.fromString(optString("type")), subjectId = nullableLong("subjectId"),
    countsTowardGrade = optBoolean("countsTowardGrade"), gradeUnitId = nullableLong("gradeUnitId"),
    startDate = requiredString("startDate"), endDate = requiredString("endDate"),
    startTime = nullableString("startTime"), endTime = nullableString("endTime"),
    isAllDay = optBoolean("isAllDay"), location = optString("location"), description = optString("description"),
    organizationTag = optString("organizationTag", "UNIVERSIDAD"), isImportant = optBoolean("isImportant"),
    priority = EventPriority.fromString(optString("priority", "MEDIUM")),
    recurrenceType = RecurrenceType.fromString(optString("recurrenceType", "NONE")),
    recurrenceInterval = optInt("recurrenceInterval", 1),
    recurrenceEndDate = nullableString("recurrenceEndDate"),
    recurrenceGroupId = nullableString("recurrenceGroupId"),
    reminderMinutes = optInt("reminderMinutes", 30), isCompleted = optBoolean("isCompleted"),
    completedAtMillis = nullableLong("completedAtMillis"),
    syncCalendar = false, calendarEventId = null, calendarId = null, lastCalendarSyncMillis = null,
    createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis()),
    updatedAtMillis = optLong("updatedAtMillis", System.currentTimeMillis())
)

private fun JSONObject.toSubtask() = SubtaskEntity(
    id = requiredLong("id"), eventId = requiredLong("eventId"), title = requiredString("title"),
    isCompleted = optBoolean("isCompleted"), sortOrder = optInt("sortOrder", 0),
    createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis()),
    updatedAtMillis = optLong("updatedAtMillis", System.currentTimeMillis())
)

private fun JSONObject.toActivityGrade() = ActivityGradeEntity(
    eventId = requiredLong("eventId"), categoryId = requiredLong("categoryId"),
    gradeItemId = requiredLong("gradeItemId"), recordedAtMillis = optLong("recordedAtMillis", System.currentTimeMillis())
)

private fun JSONObject.toGradeCategory() = GradeCategoryEntity(
    id = requiredLong("id"), subjectId = requiredLong("subjectId"), name = requiredString("name"),
    weightPercent = getDouble("weightPercent"), sortOrder = optInt("sortOrder", 0),
    createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis())
)

private fun JSONObject.toGradeItem(context: Context) = GradeItemEntity(
    id = requiredLong("id"), categoryId = requiredLong("categoryId"), name = requiredString("name"),
    unitId = optLong("unitId", 0), score = getDouble("score"), unitName = optString("unitName", context.getString(R.string.unit_default_name)),
    createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis())
)

private fun JSONObject.toGradeUnit() = GradeUnitEntity(
    id = requiredLong("id"), subjectId = requiredLong("subjectId"), name = requiredString("name"),
    sortOrder = optInt("sortOrder", 0), createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis())
)

private fun JSONObject.toGradeUnitCategoryWeight() = GradeUnitCategoryWeightEntity(
    unitId = requiredLong("unitId"),
    categoryId = requiredLong("categoryId"),
    weightPercent = getDouble("weightPercent")
)

private fun JSONObject.toException() = ClassExceptionEntity(
    id = requiredLong("id"), subjectId = requiredLong("subjectId"), slotId = requiredLong("slotId"),
    date = requiredString("date"), type = runCatching {
        ClassExceptionType.valueOf(requiredString("type"))
    }.getOrElse { throw IllegalArgumentException("El respaldo contiene un tipo de excepción inválido.") },
    newStartTime = nullableString("newStartTime"), newEndTime = nullableString("newEndTime"),
    newRoom = nullableString("newRoom"), note = optString("note"),
    createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis()),
    updatedAtMillis = optLong("updatedAtMillis", System.currentTimeMillis()),
    isInTrash = optBoolean("isInTrash", true)
)

private fun JSONObject.requiredString(key: String): String =
    optString(key).takeIf { it.isNotBlank() }
        ?: throw IllegalArgumentException("El respaldo no contiene el campo obligatorio '$key'.")

private fun JSONObject.requiredLong(key: String): Long =
    if (has(key) && !isNull(key)) getLong(key) else throw IllegalArgumentException("Falta el campo '$key'.")

private fun JSONObject.requiredInt(key: String): Int =
    if (has(key) && !isNull(key)) getInt(key) else throw IllegalArgumentException("Falta el campo '$key'.")

private fun JSONObject.nullableString(key: String): String? =
    if (!has(key) || isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

private fun JSONObject.nullableLong(key: String): Long? =
    if (!has(key) || isNull(key)) null else getLong(key)

private fun JSONObject.arrayOrEmpty(key: String): JSONArray = optJSONArray(key) ?: JSONArray()
private fun JSONArray.objects(): List<JSONObject> = List(length()) { getJSONObject(it) }
private fun JSONObject.putNullable(key: String, value: Any?) { put(key, value ?: JSONObject.NULL) }

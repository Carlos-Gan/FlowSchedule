package com.mocas.util

import com.mocas.data.repository.ScheduleAvailability

fun freeRanges(common: BooleanArray, day: Int): String {
    val slots = common.indices.filter { ScheduleAvailability.dayIndex(it) == day && it % 48 in 14..45 && common[it] }
    val ranges = mutableListOf<String>()
    var index = 0
    while (index < slots.size) {
        val start = slots[index]
        var end = start
        while (index + 1 < slots.size && slots[index + 1] == end + 1) {
            index++
            end++
        }
        ranges += "${ScheduleAvailability.slotLabel(start)}–${ScheduleAvailability.slotLabel(end + 1)}"
        index++
    }
    return ranges.joinToString(" · ")
}

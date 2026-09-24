package com.mocas

import com.mocas.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionTimeTest {

    @Test
    fun endTimeDefaultsToOneHourAfterStart() {
        assertEquals("09:15", DateTimeUtils.getEndTime("08:15"))
        assertEquals("18:45", DateTimeUtils.getEndTime("17:45"))
    }
}

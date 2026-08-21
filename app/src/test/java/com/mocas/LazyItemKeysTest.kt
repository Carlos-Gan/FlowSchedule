package com.mocas

import com.mocas.ui.util.lazyItemKey
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class LazyItemKeysTest {

    @Test
    fun sameDatabaseIdFromDifferentEntityTypesProducesUniqueKeys() {
        val classKey = lazyItemKey("agenda-class", 1L)
        val eventKey = lazyItemKey("agenda-event", 1L)

        assertNotEquals(classKey, eventKey)
    }

    @Test
    fun keyIsStableForRecomposition() {
        assertEquals(
            lazyItemKey("calendar-event", 42L),
            lazyItemKey("calendar-event", 42L)
        )
    }
}

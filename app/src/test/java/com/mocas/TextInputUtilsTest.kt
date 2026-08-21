package com.mocas

import com.mocas.ui.util.capitalizeFirstLetter
import org.junit.Assert.assertEquals
import org.junit.Test

class TextInputUtilsTest {

    @Test
    fun firstTypedLetterIsAlwaysUppercase() {
        assertEquals("Materia", capitalizeFirstLetter("materia"))
        assertEquals("  Álgebra", capitalizeFirstLetter("  álgebra"))
        assertEquals("¿Examen?", capitalizeFirstLetter("¿examen?"))
    }

    @Test
    fun existingCapitalizationAndEmptyInputArePreserved() {
        assertEquals("Redes avanzadas", capitalizeFirstLetter("Redes avanzadas"))
        assertEquals("", capitalizeFirstLetter(""))
        assertEquals("123", capitalizeFirstLetter("123"))
    }
}

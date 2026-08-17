package com.moca.snapmyschedule.data.ocr.model

import com.moca.snapmyschedule.data.model.WeekDay
import com.moca.snapmyschedule.data.ocr.RecognizedElement

data class DetectedDayColumn(
    val day: WeekDay,
    val headerText: String,

    /*
     * Todos estos valores están normalizados:
     *
     * 0.0 = inicio de la imagen
     * 1.0 = final de la imagen
     */
    val leftBoundary: Float,
    val rightBoundary: Float,
    val contentTop: Float
) {
    fun contains(
        element: RecognizedElement,
        imageWidth: Int,
        imageHeight: Int
    ): Boolean {
        if (
            imageWidth <= 0 ||
            imageHeight <= 0
        ) {
            return false
        }

        val normalizedX =
            element.centerX.toFloat() /
                    imageWidth.toFloat()

        val normalizedY =
            element.centerY.toFloat() /
                    imageHeight.toFloat()

        return normalizedX in
                leftBoundary..rightBoundary &&
                normalizedY >= contentTop
    }
}
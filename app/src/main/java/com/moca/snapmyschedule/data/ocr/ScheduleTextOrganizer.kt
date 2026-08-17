package com.moca.snapmyschedule.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ScheduleTextRecognizer(
    context: Context
) : AutoCloseable {

    private val appContext =
        context.applicationContext

    private val recognizer: TextRecognizer =
        TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

    fun recognizeSchedule(
        imageUri: Uri,
        onSuccess: (ScheduleOcrResult) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val inputImage = try {
            InputImage.fromFilePath(
                appContext,
                imageUri
            )
        } catch (exception: Exception) {
            onFailure(exception)
            return
        }

        recognizer
            .process(inputImage)
            .addOnSuccessListener { detectedText ->

                val recognizedLines =
                    detectedText.textBlocks
                        .flatMap { block ->
                            block.lines
                        }
                        .mapNotNull { line ->
                            val box =
                                line.boundingBox
                                    ?: return@mapNotNull null

                            RecognizedTextLine(
                                text = line.text.trim(),
                                left = box.left,
                                top = box.top,
                                right = box.right,
                                bottom = box.bottom
                            )
                        }
                        .filter { line ->
                            line.text.isNotBlank()
                        }
                        .sortedWith(
                            compareBy<RecognizedTextLine> {
                                it.top
                            }.thenBy {
                                it.left
                            }
                        )

                val recognizedElements =
                    detectedText.textBlocks
                        .flatMap { block ->
                            block.lines
                        }
                        .flatMap { line ->
                            line.elements
                        }
                        .mapNotNull { element ->
                            val box =
                                element.boundingBox
                                    ?: return@mapNotNull null

                            RecognizedElement(
                                text = element.text.trim(),
                                left = box.left,
                                top = box.top,
                                right = box.right,
                                bottom = box.bottom
                            )
                        }
                        .filter { element ->
                            element.text.isNotBlank()
                        }
                        .sortedWith(
                            compareBy<RecognizedElement> {
                                it.top
                            }.thenBy {
                                it.left
                            }
                        )

                onSuccess(
                    ScheduleOcrResult(
                        fullText =
                            detectedText.text.trim(),
                        imageWidth = inputImage.width,
                        imageHeight = inputImage.height,
                        lines = recognizedLines,
                        elements = recognizedElements
                    )
                )
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    override fun close() {
        recognizer.close()
    }
}
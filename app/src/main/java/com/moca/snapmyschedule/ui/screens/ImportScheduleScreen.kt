package com.moca.snapmyschedule.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moca.snapmyschedule.data.model.ClassFormData
import com.moca.snapmyschedule.data.ocr.ScheduleTextRecognizer
import com.moca.snapmyschedule.data.ocr.model.DetectedDayColumn
import com.moca.snapmyschedule.data.ocr.parser.DayColumnDetector
import com.moca.snapmyschedule.data.ocr.parser.LayoutDetectionResult
import com.moca.snapmyschedule.data.ocr.parser.LayoutDetector
import com.moca.snapmyschedule.ui.widgets.import_schedule.AnalysisErrorCard
import com.moca.snapmyschedule.ui.widgets.import_schedule.DetectedColumnsCard
import com.moca.snapmyschedule.ui.widgets.import_schedule.EmptyImageSelector
import com.moca.snapmyschedule.ui.widgets.import_schedule.LayoutDetectionCard
import com.moca.snapmyschedule.ui.widgets.import_schedule.RecognizedTextCard
import com.moca.snapmyschedule.ui.widgets.import_schedule.SelectedImagePreview
import com.moca.snapmyschedule.data.ocr.model.TableParseResult
import com.moca.snapmyschedule.data.ocr.parser.ScheduleLayoutType
import com.moca.snapmyschedule.data.ocr.parser.TableScheduleParser
import com.moca.snapmyschedule.ui.widgets.import_schedule.ImportedClassesCard
import com.moca.snapmyschedule.data.ocr.model.GridParseResult
import com.moca.snapmyschedule.data.ocr.parser.GridScheduleParser
import com.moca.snapmyschedule.data.ocr.mapper.toClassFormData
import com.moca.snapmyschedule.data.ocr.model.ImportedClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScheduleScreen(
    onOpenDrawer: () -> Unit,
    onImportClasses: (List<ClassFormData>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val textRecognizer = remember(context) {
        ScheduleTextRecognizer(
            context = context
        )
    }

    val dayColumnDetector = remember {
        DayColumnDetector()
    }

    val layoutDetector = remember {
        LayoutDetector()
    }

    val tableScheduleParser = remember {
        TableScheduleParser()
    }

    val gridScheduleParser = remember {
        GridScheduleParser()
    }

    DisposableEffect(textRecognizer) {
        onDispose {
            textRecognizer.close()
        }
    }

    var selectedImageUri by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }

    var recognizedText by rememberSaveable {
        mutableStateOf("")
    }

    var analysisError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var isAnalyzing by rememberSaveable {
        mutableStateOf(false)
    }

    var hasAnalyzedImage by rememberSaveable {
        mutableStateOf(false)
    }

    var detectedDayColumns by remember {
        mutableStateOf<List<DetectedDayColumn>>(
            emptyList()
        )
    }

    var layoutDetection by remember {
        mutableStateOf<LayoutDetectionResult?>(null)
    }

    var tableParseResult by remember {
        mutableStateOf<TableParseResult?>(null)
    }

    var gridParseResult by remember {
        mutableStateOf<GridParseResult?>(null)
    }

    var showImportConfirmation by rememberSaveable {
        mutableStateOf(false)
    }

    var importFinished by rememberSaveable {
        mutableStateOf(false)
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.PickVisualMedia()
        ) { uri ->

            if (uri != null) {
                selectedImageUri = uri
                recognizedText = ""
                detectedDayColumns = emptyList()
                layoutDetection = null
                analysisError = null
                hasAnalyzedImage = false
                isAnalyzing = false
                tableParseResult = null
                gridParseResult = null
            }
        }

    val detectedImportedClasses:
            List<ImportedClass> = when {

        tableParseResult != null ->
            tableParseResult
                ?.classes
                .orEmpty()

        gridParseResult != null ->
            gridParseResult
                ?.classes
                .orEmpty()

        else ->
            emptyList()
    }

    val validImportedClasses =
        detectedImportedClasses.filter { importedClass ->

            importedClass.subjectName.isNotBlank() &&
                    importedClass.scheduleBlocks.isNotEmpty() &&
                    importedClass.scheduleBlocks.all { block ->

                        block.days.isNotEmpty() &&
                                block.startTime <
                                block.endTime
                    }
        }

    fun openImagePicker() {
        imagePickerLauncher.launch(
            PickVisualMediaRequest(
                mediaType =
                    ActivityResultContracts
                        .PickVisualMedia
                        .ImageOnly
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Importar horario")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.Menu,
                            contentDescription =
                                "Abrir menú"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Selecciona una imagen",
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "Elige una captura o fotografía donde " +
                            "se vea claramente tu horario.",
                style =
                    MaterialTheme.typography.bodyLarge,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            val imageUri = selectedImageUri

            if (imageUri == null) {
                EmptyImageSelector(
                    onSelectImage = {
                        openImagePicker()
                    }
                )
            } else {
                SelectedImagePreview(
                    imageUri = imageUri,
                    onChangeImage = {
                        openImagePicker()
                    },
                    onRemoveImage = {
                        selectedImageUri = null
                        recognizedText = ""
                        detectedDayColumns = emptyList()
                        layoutDetection = null
                        analysisError = null
                        hasAnalyzedImage = false
                        isAnalyzing = false
                        tableParseResult = null
                        gridParseResult = null
                    }
                )

                Button(
                    onClick = {
                        isAnalyzing = true
                        analysisError = null
                        recognizedText = ""
                        detectedDayColumns =
                            emptyList()
                        layoutDetection = null
                        hasAnalyzedImage = false
                        tableParseResult = null
                        gridParseResult = null

                        textRecognizer.recognizeSchedule(
                            imageUri = imageUri,
                            onSuccess = { result ->
                                isAnalyzing = false
                                hasAnalyzedImage = true

                                recognizedText =
                                    result.fullText

                                detectedDayColumns =
                                    dayColumnDetector.detect(result)

                                val detectedLayout =
                                    layoutDetector.detect(result)

                                layoutDetection =
                                    detectedLayout

                                tableParseResult = null
                                gridParseResult = null

                                when (detectedLayout.type) {
                                    ScheduleLayoutType.TABLE -> {
                                        tableParseResult =
                                            tableScheduleParser.parse(result)
                                    }

                                    ScheduleLayoutType.GRID -> {
                                        gridParseResult =
                                            gridScheduleParser.parse(result)
                                    }

                                    ScheduleLayoutType.UNKNOWN -> {
                                        /*
                                         * Dejamos ambos resultados vacíos.
                                         * El usuario podrá intentar con otra imagen.
                                         */
                                    }
                                }
                            },
                            onFailure = { exception ->
                                isAnalyzing = false
                                hasAnalyzedImage = true

                                recognizedText = ""
                                detectedDayColumns =
                                    emptyList()
                                layoutDetection = null
                                tableParseResult = null
                                gridParseResult = null

                                analysisError =
                                    exception.message
                                        ?: "No se pudo analizar la imagen."
                            }
                        )
                    },
                    enabled = !isAnalyzing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAnalyzing) {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier =
                                    Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )

                            Text("Analizando...")
                        }
                    } else {
                        Text("Analizar imagen")
                    }
                }

                analysisError?.let { message ->
                    AnalysisErrorCard(
                        message = message
                    )
                }

                if (
                    hasAnalyzedImage &&
                    analysisError == null
                ) {

                    if (validImportedClasses.isNotEmpty()) {
                        Button(
                            onClick = {
                                showImportConfirmation = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text =
                                    "Guardar " +
                                            "${validImportedClasses.size} materias"
                            )
                        }
                    }

                    if (importFinished) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme
                                        .primaryContainer
                            )
                        ) {
                            Text(
                                text =
                                    "Las materias se guardaron " +
                                            "correctamente en tu horario.",
                                color =
                                    MaterialTheme.colorScheme
                                        .onPrimaryContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    layoutDetection?.let { detection ->
                        LayoutDetectionCard(
                            detection = detection
                        )
                    }
                    tableParseResult?.let { parseResult ->
                        ImportedClassesCard(
                            title = "Materias detectadas en la tabla",
                            classes = parseResult.classes,
                            warnings = parseResult.warnings
                        )
                    }
                    gridParseResult?.let { parseResult ->
                        ImportedClassesCard(
                            title = "Materias detectadas en la cuadrícula",
                            classes = parseResult.classes,
                            warnings = parseResult.warnings
                        )
                    }

                    DetectedColumnsCard(
                        columns = detectedDayColumns
                    )

                    RecognizedTextCard(
                        recognizedText = recognizedText
                    )
                }
            }
        }
    }

    if (showImportConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirmation = false
            },
            title = {
                Text("Guardar materias")
            },
            text = {
                Text(
                    text =
                        "Se guardarán " +
                                "${validImportedClasses.size} " +
                                "materias en tu horario. " +
                                "Podrás editarlas después."
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportConfirmation = false
                    }
                ) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportConfirmation = false

                        onImportClasses(
                            validImportedClasses.map { importedClass ->

                                importedClass
                                    .toClassFormData()
                            }
                        )

                        importFinished = true
                    }
                ) {
                    Text("Guardar")
                }
            }
        )
    }

}
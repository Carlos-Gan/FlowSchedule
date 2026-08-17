# SnapMySchedule

Aplicación Android para crear, organizar e importar horarios escolares desde una imagen.

El proyecto está desarrollado con **Kotlin**, **Jetpack Compose** y **Material 3**. Los horarios se guardan localmente con **Room**, y la importación de imágenes utiliza **ML Kit Text Recognition** para detectar texto, días, horas, salones y materias.

## Estado actual

El proyecto ya cuenta con un flujo funcional para administrar materias manualmente y una primera versión del importador mediante OCR.

### Implementado

- Visualización del horario por fecha y día de la semana.
- Carrusel horizontal de fechas.
- Navegación entre días mediante `HorizontalPager`.
- Registro manual de materias.
- Varios bloques de horario por materia.
- Días, horas y salón independientes para cada bloque.
- Persistencia local con Room.
- Edición de materias.
- Eliminación de materias completas.
- Pantalla de detalles de cada materia.
- Detección de choques o traslapes de horario.
- Menú lateral de navegación.
- Pantalla para importar horarios.
- Selección de imágenes mediante Android Photo Picker.
- Vista previa de la imagen elegida.
- Reconocimiento de texto con ML Kit.
- Detección automática del tipo de horario.
- Soporte inicial para horarios tipo cuadrícula.
- Soporte inicial para horarios tipo tabla académica.
- Detección de columnas de días.
- Extracción de materias, profesores, horas y salones.
- Formato legible para nombres detectados por OCR.
- Advertencias cuando el OCR detecta datos incompletos o recortados.

## Tipos de horario compatibles

### 1. Cuadrícula visual

Formato en el que:

- Los días aparecen como columnas.
- Las horas aparecen en el lado izquierdo.
- Las materias aparecen como tarjetas dentro de la cuadrícula.

Ejemplo:

```text
Hora   Lunes       Martes      Miércoles
08:00  Matemáticas Programación Redes
09:00  Física      Base de datos
```

Este formato es procesado por:

```text
GridScheduleParser
```

### 2. Tabla por materias

Formato en el que:

- Cada fila representa una materia.
- Existen columnas como `Materia`, `Gpo`, `Cr`, `Lunes`, `Martes`, etc.
- Cada celda de día contiene una hora y, generalmente, un salón.

Ejemplo:

```text
Materia                  Gpo Cr  Lunes          Martes
Ecuaciones diferenciales 4Y  5   12:00-13:00    12:00-13:00
                                SC10            SC10
```

Este formato es procesado por:

```text
TableScheduleParser
```

## Arquitectura

La aplicación sigue una arquitectura separada por capas:

```text
Jetpack Compose
      ↓
ScheduleViewModel
      ↓
ScheduleRepository
      ↓
OfflineScheduleRepository
      ↓
ClassSessionDao
      ↓
Room
```

Para la importación:

```text
Imagen
  ↓
ML Kit Text Recognition
  ↓
ScheduleOcrResult
  ↓
LayoutDetector
  ├── GRID  → GridScheduleParser
  ├── TABLE → TableScheduleParser
  └── UNKNOWN
  ↓
ImportedClass
  ↓
ClassFormData
  ↓
Room
```

## Estructura principal del proyecto

```text
com.moca.snapmyschedule
│
├── MainActivity.kt
├── SnapMyScheduleApplication.kt
│
├── navigation
│   ├── AppNavHost.kt
│   ├── AppRoute.kt
│   └── AppDrawer.kt
│
├── data
│   ├── local
│   │   ├── AppDatabase.kt
│   │   ├── dao
│   │   │   └── ClassSessionDao.kt
│   │   └── entity
│   │       └── ClassSessionEntity.kt
│   │
│   ├── mapper
│   │   └── ClassSessionMapper.kt
│   │
│   ├── model
│   │   ├── ClassFormData.kt
│   │   ├── ClassSession.kt
│   │   ├── ScheduleBlock.kt
│   │   └── WeekDay.kt
│   │
│   ├── repository
│   │   ├── ScheduleRepository.kt
│   │   └── OfflineScheduleRepository.kt
│   │
│   └── ocr
│       ├── RecognizedElement.kt
│       ├── RecognizedTextLine.kt
│       ├── ScheduleOcrResult.kt
│       ├── ScheduleTextRecognizer.kt
│       │
│       ├── formatter
│       │   └── OcrTextFormatter.kt
│       │
│       ├── mapper
│       │   └── ImportedClassMapper.kt
│       │
│       ├── model
│       │   ├── DetectedDayColumn.kt
│       │   ├── GridParseResult.kt
│       │   ├── ImportedClass.kt
│       │   └── TableParseResult.kt
│       │
│       └── parser
│           ├── DayColumnDetector.kt
│           ├── GridScheduleParser.kt
│           ├── LayoutDetector.kt
│           ├── TableScheduleParser.kt
│           └── TextNormalizer.kt
│
├── di
│   └── AppContainer.kt
│
├── util
│   └── ScheduleConflictValidator.kt
│
└── ui
    ├── screens
    │   ├── AddClassScreen.kt
    │   ├── ClassDetailsScreen.kt
    │   ├── ImportScheduleScreen.kt
    │   └── ScheduleScreen.kt
    │
    ├── viewmodel
    │   ├── ScheduleViewModel.kt
    │   └── ScheduleViewModelFactory.kt
    │
    └── widgets
        ├── add_class
        │   └── TimePickerField.kt
        ├── import_schedule
        │   ├── AnalysisErrorCard.kt
        │   ├── DetectedColumnsCard.kt
        │   ├── EmptyImageSelector.kt
        │   ├── ImportedClassesCard.kt
        │   ├── LayoutDetectionCard.kt
        │   ├── RecognizedTextCard.kt
        │   └── SelectedImagePreview.kt
        └── schedule_screen
            ├── ClassCard.kt
            ├── DateCarousel.kt
            ├── DayScheduleContent.kt
            └── EmptySchedule.kt
```

La estructura puede variar ligeramente según cómo se hayan organizado los widgets.

## Modelos principales

### `ClassSession`

Representa una sesión individual almacenada en el horario.

```kotlin
data class ClassSession(
    val id: Long = 0,
    val courseId: String = "",
    val subjectName: String,
    val subjectCode: String = "",
    val teacher: String = "",
    val room: String = "",
    val day: WeekDay,
    val startTime: String,
    val endTime: String
)
```

Todas las sesiones pertenecientes a una misma materia comparten el mismo `courseId`.

### `ScheduleBlock`

Permite que una materia tenga varios horarios y salones.

```kotlin
data class ScheduleBlock(
    val days: Set<WeekDay>,
    val startTime: String,
    val endTime: String,
    val room: String
)
```

Ejemplo:

```text
Programación

Lunes, martes y jueves
08:00-09:00
SC9

Viernes
10:00-12:00
LCRBD
```

### `ClassFormData`

Modelo utilizado por el formulario para agregar o editar materias.

```kotlin
data class ClassFormData(
    val subjectName: String,
    val subjectCode: String,
    val teacher: String,
    val scheduleBlocks: List<ScheduleBlock>
)
```

## Persistencia local

Room almacena una fila por cada sesión de clase.

Una materia con cinco días puede producir cinco registros:

```text
courseId: abc123
Lunes      08:00-09:00  SC9
Martes     08:00-09:00  SC9
Miércoles  08:00-09:00  SC9
Jueves     08:00-09:00  SC9
Viernes    10:00-12:00  LCRBD
```

El uso de `courseId` permite editar o eliminar toda la materia al mismo tiempo.

## Detección de conflictos

Antes de guardar una materia se comprueban:

- Choques entre bloques del mismo formulario.
- Choques con materias ya almacenadas.
- Conflictos durante la edición.
- Horarios consecutivos válidos.

Ejemplo de conflicto:

```text
Matemáticas: 08:00-10:00
Programación: 09:00-11:00
```

Ejemplo permitido:

```text
Matemáticas: 08:00-10:00
Programación: 10:00-11:00
```

## Importación mediante OCR

La pantalla de importación permite:

1. Elegir una imagen.
2. Mostrar una vista previa.
3. Analizarla con ML Kit.
4. Obtener texto y coordenadas.
5. Detectar los encabezados de días.
6. Clasificar el diseño como `GRID`, `TABLE` o `UNKNOWN`.
7. Ejecutar el parser correspondiente.
8. Mostrar las materias detectadas.
9. Mostrar advertencias de datos posiblemente incorrectos.

### Datos espaciales

El OCR conserva:

- Texto completo.
- Líneas.
- Elementos individuales.
- Coordenadas.
- Ancho y alto de la imagen.

Esto permite calcular posiciones normalizadas entre `0.0` y `1.0`, por lo que el parser no depende de una resolución fija.

## Formato de texto

El OCR suele devolver nombres completamente en mayúsculas:

```text
CONMUTACION Y ENRUTAMIENTO
```

La aplicación los transforma a un formato más legible:

```text
Conmutacion y Enrutamiento
```

También conserva conectores en minúscula y números romanos en mayúscula:

```text
INGENIERIA DE SOFTWARE
→ Ingenieria de Software

LENGUAJES Y AUTOMATAS II
→ Lenguajes y Automatas II
```

La aplicación no agrega automáticamente acentos que no hayan sido detectados por el OCR.

## Dependencias principales

- Jetpack Compose
- Material 3
- Navigation Compose
- Lifecycle ViewModel
- Lifecycle Runtime Compose
- Room
- Kotlin Coroutines
- ML Kit Text Recognition

Ejemplo de dependencia para OCR:

```kotlin
implementation(
    "com.google.mlkit:text-recognition:16.0.1"
)
```

El proyecto requiere `minSdk 23` o superior para el flujo actual de ML Kit.

## Permisos

La selección de imágenes utiliza Android Photo Picker, por lo que no es necesario solicitar acceso general a toda la galería.

No se necesitan estos permisos para el selector actual:

```xml
<uses-permission
    android:name="android.permission.READ_MEDIA_IMAGES" />

<uses-permission
    android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Cómo ejecutar

1. Clonar o abrir el proyecto en Android Studio.
2. Verificar que el SDK de Android esté instalado.
3. Sincronizar Gradle.
4. Compilar el proyecto.
5. Ejecutarlo en un dispositivo o emulador con Android 6.0 o superior.

```text
Build → Clean Project
Build → Rebuild Project
Run
```

## Limitaciones actuales

- El OCR no puede recuperar texto que ya aparece recortado con `...` en la imagen.
- La calidad depende de la resolución y claridad de la captura.
- Fotografías inclinadas o borrosas pueden producir errores.
- Algunos salones pueden confundirse con siglas de materias.
- Los parsers actuales usan reglas heurísticas.
- Los nombres no reciben acentos automáticamente.
- La revisión editable antes del guardado masivo todavía es el siguiente paso principal.
- La detección puede necesitar calibración para diseños poco comunes.

## Próximos pasos

- Crear una pantalla de revisión de importación.
- Editar cada materia antes de guardarla.
- Eliminar materias incorrectas de la importación.
- Guardar varias materias en Room desde una sola operación.
- Detectar materias duplicadas.
- Aplicar la validación de choques durante la importación.
- Mostrar las columnas y filas detectadas encima de la imagen.
- Permitir ajustar manualmente los límites de las columnas.
- Mejorar la detección de salones y profesores.
- Restaurar acentos mediante un diccionario opcional.
- Agregar soporte para más diseños de horarios.
- Agregar pruebas unitarias para parsers y validadores.
- Agregar ajustes de tema, idioma y formato horario.

## Objetivo del proyecto

SnapMySchedule busca reducir el tiempo necesario para capturar un horario escolar. El usuario podrá tomar una captura o fotografía, revisar los datos detectados y guardar todas sus materias en pocos pasos, manteniendo siempre la opción de agregar, editar o eliminar información manualmente.

## Licencia

Todavía no se ha definido una licencia para el proyecto.

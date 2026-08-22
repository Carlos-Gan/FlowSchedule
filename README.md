<div align="center">

# SnapMySchedule

### Tu horario, actividades y recordatorios en un solo lugar

Aplicación Android para organizar materias, sesiones, tareas, exámenes, periodos académicos y vacaciones. Incluye notificaciones configurables, widgets para la pantalla de inicio e importación opcional de horarios mediante una fotografía.

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Version](https://img.shields.io/badge/versión-1.3-5847EB)

</div>

## Funciones principales

### Horarios y materias

- Vista semanal, vista diaria y lista de materias.
- Navegación entre semanas y regreso rápido a la semana actual.
- Varias sesiones por materia y varios días por sesión.
- Selector de hora mediante reloj y cálculo automático de una hora de duración.
- Validación de cruces para evitar dos clases en el mismo horario.
- Colores, profesor, código, salón y recordatorio individual por materia.
- Excepciones para cancelar o modificar una sola clase sin cambiar toda la materia.

### Periodos académicos y vacaciones

- Periodos con fecha inicial y final, contemplando día, mes y año.
- Soporte para periodos que abarcan años diferentes.
- Edición, colores y copia de materias entre periodos.
- Detección opcional de los días fuera de los periodos como vacaciones.
- Vacaciones visibles en calendario y horario, con opción para ocultarlas.

### Actividades y calendario

- Tareas, exámenes, exposiciones, reuniones, eventos escolares, vacaciones y otros eventos.
- Actividades de uno o varios días.
- Selectores de fecha con calendario y selectores de hora con reloj.
- Actividades vinculadas a una materia y control de completadas o pendientes.
- Integración con el calendario del teléfono mediante `CalendarContract`.

### Notificaciones

- Próxima clase.
- Tareas, exámenes y eventos.
- Actividades vencidas.
- Resumen de las actividades del día siguiente.
- Configuración independiente por categoría.
- Anticipación personalizable: minutos, horas o días antes.
- Restauración automática de recordatorios después de reiniciar el teléfono.

### Organización y búsqueda

- Etiquetas `Universidad`, `Trabajo` y `Personal`.
- Marcado de materias y actividades como importantes.
- Búsqueda global por materia, actividad, profesor, salón o código.
- Filtros por etiqueta e importancia.

### Exportación y respaldos

- Respaldo portátil en JSON con periodos, materias, sesiones, excepciones y actividades.
- Restauración completa en el mismo dispositivo o en otro teléfono.
- Validación del archivo antes de reemplazar los datos actuales.
- Exportación `.ics` independiente para Google Calendar, Apple Calendar y Outlook.

### Widgets

- **Próxima clase:** muestra materia, hora, salón, tiempo restante y total de pendientes.
- **Horario del día (4×2):** lista desplazable con las clases de hoy, salón, indicador de clase actual y pendientes vinculados a cada materia.
- Actualización automática al modificar el horario, las actividades o las excepciones.

### Experiencia de uso

- Pantalla de bienvenida y configuración inicial del estudiante.
- Temas claro, oscuro y automático.
- Diseño Material 3 adaptado a navegación por gestos.
- Campos de texto con capitalización inicial automática.
- Funciones de IA completamente desactivables desde Ajustes.

## Importación de horarios con IA

La importación desde foto utiliza Gemini 2.5 Flash para reconocer materias, días, horarios, profesores y salones. Esta función es opcional: el resto de SnapMySchedule funciona sin una clave de API.

1. Copia `.env.example` como `.env` en la raíz del proyecto.
2. Agrega tu clave:

```properties
GEMINI_API_KEY=tu_clave_de_gemini
```

3. Sincroniza nuevamente el proyecto con Gradle.

Puedes crear una clave desde [Google AI Studio](https://aistudio.google.com/app/apikey). El archivo `.env` está excluido de Git y no debe subirse al repositorio.

> [!IMPORTANT]
> Las claves incluidas en una aplicación cliente pueden extraerse del APK. Para una publicación real, restringe la clave y considera mover las solicitudes de IA a un backend propio.

## Tecnologías

- Kotlin y Coroutines.
- Jetpack Compose con Material 3.
- Room para almacenamiento local.
- ViewModel y StateFlow para estado reactivo.
- AlarmManager y notificaciones nativas de Android.
- App Widgets con `RemoteViews`.
- OkHttp para la integración opcional con Gemini.
- KSP para generación de código de Room.
- JUnit, Robolectric, Compose UI Test y Roborazzi.

## Arquitectura

El proyecto mantiene una arquitectura por capas sencilla:

```text
app/src/main/java/com/mocas/
├── data/
│   ├── ai/             # Lectura opcional de horarios con Gemini
│   ├── local/          # Entidades, DAO, Room y migraciones
│   ├── notifications/  # Planificación y publicación de recordatorios
│   ├── preferences/    # Preferencias de la aplicación
│   ├── repository/     # Operaciones de datos y calendario
│   └── widget/         # Widgets de próxima clase y horario diario
├── ui/
│   ├── components/     # Componentes reutilizables
│   ├── dialogs/        # Formularios y selectores
│   ├── model/          # Modelos de presentación
│   ├── screens/        # Pantallas principales
│   ├── theme/          # Colores, tipografía y temas
│   ├── util/           # Utilidades de interfaz
│   └── viewmodel/      # Estado y acciones de la aplicación
└── util/               # Utilidades generales de fecha y hora
```

Los datos del usuario se almacenan localmente. Solo la importación de horarios con IA envía la imagen seleccionada al servicio de Gemini.

## Requisitos

- Android Studio con soporte para AGP 9.3.
- JDK 17.
- Android SDK 37 para compilar.
- Dispositivo o emulador con Android 8.0 (API 26) o superior.
- Clave de Gemini únicamente si se utilizará la importación con IA.

## Ejecutar el proyecto

1. Clona el repositorio:

```bash
git clone URL_DE_TU_REPOSITORIO.git
cd snapmyschedule
```

2. Abre la carpeta en Android Studio.
3. Espera la sincronización de Gradle.
4. Opcionalmente configura `GEMINI_API_KEY` en `.env`.
5. Ejecuta la configuración `app` en un emulador o dispositivo físico.

También puedes compilar desde la terminal:

```bash
# Windows
gradlew.bat assembleDebug

# macOS o Linux
./gradlew assembleDebug
```

El APK de desarrollo se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Pruebas y calidad

```bash
# Pruebas unitarias
./gradlew testDebugUnitTest

# Análisis estático
./gradlew lintDebug

# Validación completa
./gradlew testDebugUnitTest lintDebug assembleDebug
```

## Firma de publicación

La compilación `release` lee los siguientes valores del entorno:

```text
KEYSTORE_PATH
STORE_PASSWORD
KEY_PASSWORD
```

La configuración actual utiliza `upload` como alias de la clave.

No subas archivos de firma, contraseñas, `local.properties` ni `.env` al repositorio.

## Estado del proyecto

- Versión actual: **1.3**.
- `applicationId`: `com.gaco.snapmyschedule`.
- SDK mínimo: **26**.
- SDK objetivo: **37**.

## Licencia

Este repositorio todavía no incluye una licencia de distribución. Agrega una antes de aceptar contribuciones o publicar el código para reutilización por terceros.

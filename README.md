<div align="center">

# FlowSchedule

### Tu horario, actividades y recordatorios en un solo lugar

Aplicación Android para organizar materias, sesiones, tareas, exámenes, periodos académicos y vacaciones. Incluye notificaciones configurables, widgets para la pantalla de inicio e importación opcional de horarios mediante una fotografía.

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Version](https://img.shields.io/badge/versión-1.5.0-5847EB)

</div>

## Funciones principales

### Horarios y materias

- Vista semanal, vista diaria y lista de materias.
- Navegación entre semanas y regreso rápido a la semana actual.
- *Rango horario dinámico que se ajusta a tus clases para ahorrar espacio vertical.*
- *Línea de tiempo en tiempo real que indica la hora actual y la clase en curso.*
- Varias sesiones por materia con **salón independiente por sesión**.
- Selector de hora con **ajuste automático de duración (+1h)** para evitar errores.
- Validación de cruces para evitar dos clases en el mismo horario.
- Colores, profesor, código y recordatorio individual por materia.
- Excepciones para cancelar o modificar una sola clase sin cambiar toda la materia.

### Periodos académicos y vacaciones

- Periodos con fecha inicial y final, contemplando día, mes y año.
- Soporte para periodos que abarcan años diferentes.
- Edición, colores y copia de materias entre periodos.
- Detección opcional de los días fuera de los periodos como vacaciones.
- Vacaciones visibles en calendario y horario, con opción para ocultarlas.

### Actividades y calendario

- Tareas, exámenes, exposiciones, reuniones, eventos escolares, vacaciones y otros eventos.
- *Sistema de **Advertencia Visual**: Borde y línea lateral que cambia a rojo/ámbar según la urgencia.*
- *Carrusel de exámenes próximos con fechas relativas (Hoy, Mañana, etc.).*
- *Formulario inteligente: Sugiere automáticamente la hora basada en tus clases del día.*
- *Filtros inteligentes por categorías con iconos minimalistas de Material 3.*
- Actividades recurrentes semanales o mensuales con fecha de finalización.
- Prioridad con etiquetas visuales y subtareas persistentes con barra de progreso.
- Integración con el calendario del teléfono mediante `CalendarContract`.

### Perfil y Calificaciones

- *Perfil académico personalizable: edita tu nombre, carrera/grado e institución.*
- *Contador de **Racha Productiva**: Días consecutivos completando actividades (se pierde si hay tareas vencidas).*
- *Cálculo de promedio real basado en todas tus calificaciones registradas.*
- *Conversión instantánea a escala GPA (4.0) desde los ajustes.*
- Espacio independiente de calificaciones por materia con unidades y categorías ponderadas.
- Simulador guiado con comparación entre promedio actual y proyectado.

### Compartir el horario

- Imagen PNG semanal con diseño limpio y marca de agua de **FlowSchedule**.
- Documento PDF listo para imprimir o enviar.
- Archivo `.ics` compatible con calendarios de compañeros.
- Controles de privacidad para ocultar profesor o salón.

### Experiencia de uso

- **Soporte Multi-idioma**: Traducción completa al **Inglés** y Español.
- **Modo OLED (True Black)**: Tema oscuro optimizado para ahorrar batería y reducir fatiga.
- **Onboarding Interactivo**: Nuevo asistente de configuración por pasos con animaciones.
- Diseño Material 3 avanzado (`SegmentedButtons`, `Wavy Indicators`, `Carousels`).
- Funciones de IA detectan automáticamente la disponibilidad de la API Key.

## Importación de horarios con IA

La importación desde foto utiliza Gemini 2.5 Flash para reconocer materias, días, horarios, profesores y salones. 

1. Copia `.env.example` como `.env` en la raíz del proyecto.
2. Agrega tu clave: `GEMINI_API_KEY=tu_clave_de_gemini`.
3. Sincroniza el proyecto. Si no se detecta la clave, la opción se ocultará automáticamente por seguridad.

## Tecnologías

- Kotlin, Coroutines y Jetpack Compose (M3).
- Room para almacenamiento local.
- ViewModel y StateFlow para estado reactivo.
- AlarmManager y notificaciones nativas de Android.
- OkHttp para integración con Gemini.
- KSP, JUnit, Robolectric y Roborazzi.

## Requisitos

- Android Studio 2026.1.3 o superior.
- SDK 37 (compilación) / Android 8.0+ (ejecución).

## Estado del proyecto

- Versión actual: **1.5.0**.
- `applicationId`: `com.mocas.flowschedule`.
- SDK mínimo: **26**.
- SDK objetivo: **37**.

## Licencia

Este proyecto está bajo la **Licencia MIT**. Puedes consultar el archivo [LICENSE](LICENSE) para más detalles.

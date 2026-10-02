# FlowSchedule

### Your schedule, activities, and reminders in one place

FlowSchedule is a native Android application designed to organize academic life in one place. It combines class schedules, subjects, assignments, exams, academic periods, vacations, reminders, widgets, backups, and productivity tools in a single application.

The project was built as a real-world personal application and is continuously developed based on actual academic use cases.

## ✨ Features

### 📅 Schedules & Subjects

- Weekly and daily schedule views.
- Navigation between weeks with quick access to the current week.
- Dynamic time range that adapts to scheduled classes.
- Real-time timeline indicating the current time and active class.
- Multiple sessions per subject with independent classrooms.
- Material 3 time picker with automatic duration adjustment.
- Schedule conflict detection.
- Custom subject colors, professors, codes, and reminders.
- Exceptions for canceling or modifying individual class sessions without changing the entire subject.

### 🏫 Academic Periods & Vacations

- Academic periods with configurable start and end dates.
- Support for periods spanning multiple calendar years.
- Subject editing, colors, and copying between academic periods.
- Optional detection of days outside an academic period as vacations.
- Vacation visualization in the calendar and schedule.

### 📝 Activities & Calendar

- Assignments, exams, presentations, meetings, school events, vacations, and custom events.
- Visual urgency indicators based on how close an activity is to its due date.
- Upcoming exam carousel with relative dates such as Today and Tomorrow.
- Smart event creation with suggested times based on the user's schedule.
- Category filters with Material 3 icons.
- Recurring weekly or monthly activities with configurable end dates.
- Priority labels and persistent subtasks with progress tracking.
- Integration with the device calendar through Android `CalendarContract`.

### 🎓 Academic Profile & Grades

- Customizable academic profile.
- Productivity streak based on completed activities.
- GPA and academic average calculations.
- GPA 4.0 scale conversion.
- Grade management by subject, including units and weighted categories.
- Guided grade simulator comparing current and projected averages.

### 📱 Widgets & Backups

- Home screen widgets for daily schedules, full schedules, and academic period tracking.
- Automatic and manual backup management with robust backup/restore encoding.

### 📤 Schedule Sharing

FlowSchedule can export and share schedules in multiple formats:

- PNG weekly schedule.
- Printable PDF.
- `.ics` calendar file.

### 🎨 User Experience

- Spanish and English localization.
- OLED-friendly True Black dark theme.
- Interactive onboarding experience.
- Material 3 UI components and motion.
- Responsive schedule-oriented layouts.

---

## 🏗️ Architecture

FlowSchedule follows a modern Android architecture focused on separation of concerns and reactive state management.

```text
UI
│
├── Jetpack Compose
├── Material 3
└── Navigation
        │
        ▼
Presentation
│
├── ViewModel
└── StateFlow
        │
        ▼
Data
│
├── Room
├── Repositories
└── Local persistence
```

The application uses reactive state with `StateFlow` and separates UI state from data persistence through ViewModels and repository-based data access.

---

## 🛠️ Tech Stack

### Android

- Kotlin
- Jetpack Compose
- Material 3
- Android SDK
- ViewModel
- StateFlow
- Navigation

### Data & Persistence

- Room
- Kotlin Coroutines
- KSP
- Local-first data management
- Backup & restore codecs (`ScheduleBackupCodec`)

### System Integration

- AlarmManager & Notification Scheduler
- Home screen app widgets (AppWidgetProvider)
- `CalendarContract` integration
- Calendar `.ics` export
- Automatic backup manager

### Networking & Development

- OkHttp
- Git / GitHub
- Android Studio

### Testing & Quality

- JUnit
- Robolectric
- Roborazzi

---

## 🧪 Testing

The project includes automated testing for application logic and Android-specific behavior using:

- JUnit
- Robolectric
- Roborazzi

Visual regression testing is used to help validate Compose UI changes and reduce unintended visual regressions.

---

## 🚀 Getting Started

### Requirements

- Android Studio 2026.1.3 or newer
- Android SDK 37
- Android 8.0+ (API 26)

### Clone the repository

```bash
git clone https://github.com/Carlos-Gan/FlowSchedule.git
```

Open the project in Android Studio, allow Gradle to synchronize, and run the application on an Android device or emulator.

---

## 📌 Project Status

**Current version:** `1.7.0`

**Application ID:** `dev.charlesmoran.flowschedule`

**Minimum SDK:** 26  
**Target SDK:** 37

FlowSchedule is an actively developed project. New features and improvements are added based on real-world academic use and testing.

---

## 🧠 Engineering Focus

Some of the main engineering challenges addressed by the project include:

- Detecting overlapping class sessions.
- Supporting subjects with multiple independent schedule sessions.
- Handling class exceptions without modifying the base schedule.
- Managing academic periods across different calendar years.
- Synchronizing academic events with the Android calendar.
- Persisting reactive application state using Room and `StateFlow`.
- Generating shareable schedules in different formats.
- Designing a schedule interface that remains readable with many classes and events.
- Supporting both Spanish and English interfaces.
- Building reactive home screen widgets and robust local backup codecs.

---

## 🗺️ Roadmap

Potential future improvements include:

- Cloud synchronization.
- Multi-device support.
- Advanced schedule import using OCR.
- Additional calendar integrations.
- More automated UI and integration tests.
- Improved accessibility.
- Additional customization options.

---

## 📄 License

This project is licensed under the **MIT License**.

See the [LICENSE](https://github.com/Carlos-Gan/FlowSchedule/blob/v2/LICENSE) file for more information.

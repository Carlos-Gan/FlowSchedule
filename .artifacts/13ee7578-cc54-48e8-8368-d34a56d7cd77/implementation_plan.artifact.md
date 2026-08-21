# Fix Kotlin Compose Compiler Incompatibility

The project is experiencing a build error due to an incompatible Compose compiler plugin registrar. This is caused by the use of Kotlin 2.2.10 while the build system is attempting to load a legacy version of the Compose compiler plugin (`androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar`).

In Kotlin 2.0 and later, the Compose compiler is integrated into the Kotlin repository and should be applied via the `org.jetbrains.kotlin.plugin.compose` Gradle plugin. While the project attempts to apply this plugin, it is missing the foundational `kotlin-android` plugin, which likely prevents the Android Gradle Plugin (AGP) from correctly coordinating with the new Kotlin Compose compiler mechanism.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Charles/Downloads/snapmyschedule/gradle/libs.versions.toml)
- Add the `kotlin-android` plugin definition to the `[plugins]` section, using the same version as the Kotlin compiler (2.2.10).

#### [MODIFY] [root build.gradle.kts](file:///C:/Users/Charles/Downloads/snapmyschedule/build.gradle.kts)
- Declare the `kotlin-android` plugin in the top-level `plugins` block to make it available to sub-projects.

#### [MODIFY] [app build.gradle.kts](file:///C:/Users/Charles/Downloads/snapmyschedule/app/build.gradle.kts)
- Apply the `kotlin-android` plugin in the `plugins` block. This ensures that the project is correctly recognized as a Kotlin Android project, allowing the `kotlin-compose` plugin and AGP to coordinate correctly.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that the Kotlin compilation succeeds without the `PluginProcessingError`.
- Run `./gradlew assembleDebug` to ensure the entire build process completes successfully.

### Manual Verification
- Verify that Compose-related features (like Previews or running the app) work as expected in the IDE after the sync.

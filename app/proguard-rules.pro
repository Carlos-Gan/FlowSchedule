# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Preserve Line Numbers for Crash Logs (Útil para depurar errores en Google Play Console)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# AndroidX Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
}
-dontwarn androidx.room.paging.**

# Entity Classes & DAOs (Mantiene tus modelos de datos de Room si están en este paquete)
-keep class dev.charlesmoran.flowschedule.** { *; }

# Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.** {
    public <init>();
}
-dontwarn kotlinx.coroutines.**

# Jetpack Compose & Material 3
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# OkHttp & Okio (Red / HTTP)
-keepattributes Signature
-keepattributes AnnotationDefault
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
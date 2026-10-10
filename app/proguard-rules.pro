# Keep SourceFile & LineNumberTable for crash stack trace de-obfuscation in Play Console
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve Room Database constructor
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
}

# Preserve Room entities and data models for Room persistence and backup serialization
-keep class com.mocas.data.local.** { *; }
-keepclassmembers class com.mocas.data.local.** { *; }

# Preserve backup codec classes
-keep class com.mocas.data.backup.** { *; }

# Note: Libraries like Jetpack Compose, Room, Coroutines, and OkHttp automatically include
# their own consumer ProGuard rules inside their AARs. Removing broad rules like
# `-keep class package.** { *; }` allows R8 to shrink, obfuscate, and optimize over 85%+ of the code.

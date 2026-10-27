# ProGuard / R8 rules for Makarios

# ONNX Runtime (Native JNI bindings and session options)
-keep class ai.onnxruntime.** { *; }
-dontwarn ai.onnxruntime.**

# Room Database
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Timber
-keep class timber.log.** { *; }
-dontwarn timber.log.**

# Coil Image Loader
-keep class coil.** { *; }
-dontwarn coil.**

# Makarios Data & Local Models
-keep class com.makarios.app.data.** { *; }
-keep class com.makarios.app.data.bible.** { *; }
-keep class com.makarios.app.data.local.** { *; }
-keep class com.makarios.app.ui.wallpaper.StyleSpec { *; }
-keep class com.makarios.app.ui.wallpaper.ExportFormat { *; }
-keep class com.makarios.app.util.PerformanceTier { *; }
-keep class com.makarios.app.util.PerformanceMetrics { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# AndroidX Credentials & Google Identity
-keep class androidx.credentials.** { *; }
-keep class androidx.credentials.playservices.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.googleid.**


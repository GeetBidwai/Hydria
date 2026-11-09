# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Room Database rules
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomOpenHelper
-dontwarn androidx.room.paging.**
-keep class * implements androidx.room.migration.Migration

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Preserve line numbers for stack traces
-keepattributes SourceFile,LineNumberTable

# Jetpack Glance ActionCallback
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    public <init>();
}


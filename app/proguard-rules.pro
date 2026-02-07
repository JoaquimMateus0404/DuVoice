# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for debugging crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# DuVoice - Regras de ProGuard para Produção
# ============================================

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Navigation SafeArgs
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

# DataStore
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep data classes
-keep class com.cleansoft.duvoice.data.model.** { *; }
-keep class com.cleansoft.duvoice.data.local.entity.** { *; }

# Keep ViewBinding classes
-keep class com.cleansoft.duvoice.databinding.** { *; }

# Keep Widget classes
-keep class com.cleansoft.duvoice.widget.** { *; }

# Keep Service
-keep class com.cleansoft.duvoice.service.** { *; }

# Material Design
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }

# AndroidX
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# SplashScreen
-keep class androidx.core.splashscreen.** { *; }

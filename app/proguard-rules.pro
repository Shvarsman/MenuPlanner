# Keep Room entities and DAOs (consumer rules usually cover this; belt-and-suspenders).
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# kotlinx.serialization DTOs used for backup JSON.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.shvarsman.coolinar.data.backup.**$$serializer { *; }
-keepclassmembers class com.shvarsman.coolinar.data.backup.** {
    *** Companion;
}
-keepclasseswithmembers class com.shvarsman.coolinar.data.backup.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Enums persisted by Room / backup via name / valueOf.
-keepclassmembers enum com.shvarsman.coolinar.domain.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
}

# Hilt references optional errorprone annotations not on the classpath.
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue

# Игнорировать отсутствующие классы Android XR / ARCore
-dontwarn com.android.extensions.xr.**
-dontwarn com.google.androidxr.**
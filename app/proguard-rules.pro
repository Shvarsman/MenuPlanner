# Keep Room entities and DAOs (consumer rules usually cover this; belt-and-suspenders).
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# kotlinx.serialization DTOs used for backup JSON.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.shvarsman.menuplanner.data.backup.**$$serializer { *; }
-keepclassmembers class com.shvarsman.menuplanner.data.backup.** {
    *** Companion;
}
-keepclasseswithmembers class com.shvarsman.menuplanner.data.backup.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Enums persisted by Room / backup via name / valueOf.
-keepclassmembers enum com.shvarsman.menuplanner.domain.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
}

# Hilt references optional errorprone annotations not on the classpath.
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue

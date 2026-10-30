# Navigation type-safe routes are serialized with kotlinx.serialization.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.dd.friction.**$$serializer { *; }
-keepclassmembers class com.dd.friction.** {
    *** Companion;
}
-keepclasseswithmembers class com.dd.friction.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.dd.friction.ui.navigation.** { *; }

# Add project specific ProGuard rules here.
# Keep Room entities
-keep class com.quicpos.app.data.local.entity.** { *; }

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.quicpos.app.domain.model.** { *; }

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# ESC/POS Printer library
-keep class com.dantsu.escposprinter.** { *; }

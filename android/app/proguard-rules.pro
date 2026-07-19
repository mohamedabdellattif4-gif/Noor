# Noor intentionally avoids reflection-based JSON serializers. Room, Hilt, WorkManager,
# Compose, and Media3 publish their own consumer rules. Keep only runtime annotations and
# generic signatures used by dependency injection and coroutine metadata.
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault

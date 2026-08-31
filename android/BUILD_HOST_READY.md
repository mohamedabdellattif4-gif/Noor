# NOOR Android Build Host

The repository Build Host is pinned to:

- JDK 17
- Android API 37
- Android Build Tools 36.0.0
- Gradle 9.4.1

The workflow restores Android SDK and Gradle dependency caches by toolchain profile, warms missing dependencies on a connected runner, then proves debug APK and release AAB builds with Gradle `--offline`.

The cache is a reusable acceleration layer, not a secret store. Signing keys and credentials are intentionally excluded.

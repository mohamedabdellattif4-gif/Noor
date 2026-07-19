# Gradle Wrapper

Noor pins Gradle 9.4.1 because Android Gradle Plugin 9.2 requires Gradle 9.4.1 and JDK 17.

A production repository should commit all Wrapper files, including `gradle-wrapper.jar`. The execution
sandbox used for this phase cannot download or retain that official binary, so this archive keeps a
strict recovery path rather than embedding an unverified replacement.

When `gradle-wrapper.jar` is absent, run one of these commands:

- macOS/Linux: `./bootstrap-gradle-wrapper.sh`
- Windows PowerShell: `./bootstrap-gradle-wrapper.ps1`

The bootstrap downloads only the official Gradle 9.4.1 Wrapper JAR over HTTPS, uses a temporary file,
performs bounded retries, verifies SHA-256 before replacement, and cleans temporary files on failure.
Both `gradlew` launchers verify the same checksum again before executing Java.

Expected Gradle 9.4.1 Wrapper JAR SHA-256:

`55243ef57851f12b070ad14f7f5bb8302daceeebc5bce5ece5fa6edb23e1145c`

Expected Gradle 9.4.1 binary distribution SHA-256:

`2ab2958f2a1e51120c326cad6f385153bb11ee93b3c216c5fccebfdfbb7ec6cb`

After bootstrapping, verify the complete foundation with:

```bash
python3 tools/verify_build_foundation.py --require-wrapper-jar
```

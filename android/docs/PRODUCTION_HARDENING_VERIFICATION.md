# Production hardening verification

**Date:** 2026-07-17

## Passed gates

- `tools/verify_final.py`: passed.
- `tools/verify_hardening.py`: passed.
- Python bytecode compilation for every repository tool: passed.
- POSIX shell syntax for bootstrap and release scripts: passed.
- GitHub Actions and Dependabot YAML parsing: passed.
- Baseline Profile update tool positive path: passed.
- Release preflight with simulated JDK 17, SDK 37, semantic version, and external keystore: passed.
- Release preflight partial-signing rejection: passed.
- Release preflight rejects a symlink that resolves to a keystore inside the repository: passed.
- Synthetic AAB structural/content verification: passed.
- Synthetic unsigned AAB rejection when signature is required: passed.
- Synthetic signed AAB JAR signature verification: passed.
- Quran/Room/FTS/data-integrity and Android-free Kotlin regression gates: passed.

## Latest source metrics

```text
Kotlin/Kotlin DSL files: 107
XML files: 28
Python verification/release tools: 10
GitHub Actions workflows: 5
Test/benchmark/profile classes: 12
SQLite FTS host p95: 0.460 ms
Prepackaged database: 3,252,224 bytes
Database SHA-256: a77c6155204cbe13133ab09a253d030622d4cc2ac0c5aa2514cda007d24bfda1
```

The FTS timing is a host regression check and varies slightly by machine. It is not an Android-device
performance claim.

## Android build attempt

The checksum-pinned Wrapper bootstrap was retried after the hardening pass and stopped with:

```text
curl: (6) Could not resolve host: services.gradle.org
```

The sandbox also has JDK 21 instead of the pinned JDK 17 and no Android SDK or Wrapper JAR. Therefore
AGP configuration, dependency resolution, Compose/Hilt/KSP/Room compilation, Android Lint, R8,
instrumentation, Macrobenchmark execution, and signed production AAB generation remain external.

# Google Play readiness review

## Repository-ready items

- Application ID is defined; release version code/name are environment-configurable and validated.
- targetSdk is 36 and compileSdk is 37.
- Release is non-debuggable with R8 optimization and resource shrinking.
- Android Lint is configured to abort on errors and treat warnings as errors.
- Legacy, adaptive, round and Android 13 monochrome launcher resources are present.
- Cleartext is disabled and only necessary permissions are declared.
- Media playback uses an exported Media3 `MediaSessionService`, a `mediaPlayback` foreground-service
  type and the two required foreground-service permissions.
- In-app privacy/source notices and bundled license notices are present.
- Arabic and English are declared through Android per-app `localeConfig`.
- Release signing preflight and AAB signature verification are automated.
- Android CI, CodeQL, Dependency Review, Dependabot, and Gradle dependency submission are configured.
- Baseline Profile generation and startup/reader Macrobenchmarks are implemented in a dedicated test module.
- Backup is disabled and no analytics, ads, account or sensitive-permission SDK is present.
- Source contains no native/NDK code; the built AAB must still be inspected for transitive `.so` files
  and tested for the Google Play 16 KB page-size requirement.
- A Data Safety draft can state no developer collection or sharing while disclosing that
  user-initiated audio/tafsir requests contact third-party providers.

## Mandatory publisher/build gates before upload

1. Supply JDK 17, Android SDK Platform 37 and the checksum-pinned Gradle distribution; run every
   build/lint/test command in `RELEASE.md`.
2. Generate and commit the current Room compiler-exported `4.json`. Recover authoritative historical schemas from their tagged source/build artifacts for every version covered by the upgrade policy; do not recreate them manually. Run the direct 1→4 migration instrumentation test, then add `MigrationTestHelper` coverage when those historical JSON files are available.
3. Configure a private upload/signing key outside version control and build a signed AAB.
4. Inspect the final AAB/APK for transitive native libraries and validate 16 KB page-size compatibility.
5. Host `PRIVACY_POLICY.md` at a public HTTPS URL and insert the legal publisher name and support/
   privacy contact.
6. Prepare store descriptions, phone/tablet screenshots, feature graphic, high-resolution icon,
   category, content rating, countries and support details.
7. Complete Data Safety, advertising-ID, content-rating, target-audience and foreground-service
   declarations based on the final artifact.
8. Confirm current written use/streaming terms for EveryAyah recitations and QuranEnc.
9. Upload to internal testing, inspect App Bundle Explorer, run pre-launch reports and test offline,
   RTL, dark mode, background audio, process death, database open/migration and upgrades.
10. Generate a fresh Baseline Profile, retain physical-device Macrobenchmark evidence, and confirm
    that CodeQL/dependency submission/review complete successfully against the resolved Gradle graph.

## Readiness verdict

The source repository is a reviewed release candidate. It is not a signed or publishable Play
artifact until the external build, provider-rights, legal/publisher, signing, device and Play Console
gates above are completed.

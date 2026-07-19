# Noor — Release Signing Configuration

## Keystore Metadata

| Field              | Value                                        |
|--------------------|----------------------------------------------|
| **Keystore file**  | `noor-release.jks`                           |
| **Keystore type**  | PKCS12 (industry standard)                   |
| **Key alias**      | `noor`                                       |
| **Key algorithm**  | RSA                                          |
| **Key size**       | 4096 bits                                    |
| **Validity**       | 100 years (36,500 days from July 19, 2026)   |
| **Expires**        | June 25, 2126                                |
| **Signature algo** | SHA384withRSA                                |

## Certificate Fingerprints

```
SHA-1:   BD:10:1D:0E:BA:B3:3A:43:A1:29:8D:21:51:C7:1B:48:7D:4A:CE:60
SHA-256: F5:F5:C5:71:74:C1:7A:5D:ED:36:E3:B6:71:B6:A8:25:50:44:47:4D:78:C5:3C:DF:E4:83:46:76:52:C3:AD:49
```

## Keystore Location (at build time)

```
/home/runner/keystore/noor-release.jks
```

The keystore is deliberately stored **outside the project/repository root** (enforced by
`app/build.gradle.kts`). It must never be committed to version control.

## Signing Credentials (Environment Variables)

The build reads four environment variables at build time. No passwords are
stored in source code or committed files.

| Variable               | Purpose                        |
|------------------------|--------------------------------|
| `NOOR_KEYSTORE_PATH`   | Absolute path to the JKS file  |
| `NOOR_KEYSTORE_PASSWORD` | Keystore/store password      |
| `NOOR_KEY_ALIAS`       | Key alias (`noor`)             |
| `NOOR_KEY_PASSWORD`    | Key password (same as store in PKCS12) |

Credentials are stored in `/home/runner/keystore/signing.properties`
(mode `600`, outside the repository). Source this file before running a
release build:

```bash
source /home/runner/keystore/signing.properties
export NOOR_KEYSTORE_PATH NOOR_KEYSTORE_PASSWORD NOOR_KEY_ALIAS NOOR_KEY_PASSWORD

cd android/
./gradlew bundleRelease        # → signed Release AAB
./gradlew assembleRelease      # → signed Release APK
```

## How to Back Up the Keystore

Back up **all three** of the following immediately after generating the keystore.
Store each backup in a separate physical/geographic location.

### Files to back up

1. `/home/runner/keystore/noor-release.jks` — the keystore file itself
2. `/home/runner/keystore/signing.properties` — the credentials file
3. `/home/runner/keystore/noor-release.jks.old` — the original JKS backup
   created automatically during PKCS12 migration

### Recommended backup destinations (use at least two)

- **Password manager** (1Password, Bitwarden, etc.) — attach the `.jks` file as a
  secure attachment and store the passwords as a secure note
- **Encrypted cloud storage** (Google Drive with client-side encryption, iCloud
  Keychain, etc.)
- **Offline encrypted USB drive** stored in a physically secure location
- **Team secret manager** (HashiCorp Vault, AWS Secrets Manager, Google Secret
  Manager) — ideal for CI/CD pipelines

### Backup command (downloads to your local machine via Replit's file manager)

```bash
# Verify the keystore is intact before backing up
keytool -list -v \
  -keystore /home/runner/keystore/noor-release.jks \
  -storepass "$NOOR_KEYSTORE_PASSWORD" \
  -alias noor 2>&1 | grep -E "Alias|Valid|SHA"
```

## ⚠️  CRITICAL WARNING

> **Losing the keystore permanently locks you out of your own Play Store listing.**

Google Play ties every app update to the exact cryptographic key used to sign
the first upload. If the keystore file **or** its password is lost:

- You **cannot** publish any future update under the same package name
  (`com.noor.app`)
- You **cannot** recover the key from Google — even with ownership proof
- Your only option would be to publish an entirely new app under a new package
  name, starting from zero reviews, ratings, and installs
- Users of the old app would **never** receive updates automatically

**This file and the keystore are among the most important assets in this project.
Treat them with the same care as production database credentials or private keys.**

## Play App Signing (Recommended)

Google offers **Play App Signing**, where Google holds a copy of your upload key
and re-signs the APK for distribution. This provides a recovery path if you lose
your upload key. To enrol:

1. In the Google Play Console → your app → **Setup → App signing**
2. Follow the enrolment wizard (you provide the AAB; Google extracts the key)
3. After enrolment, the SHA-256 fingerprint shown in Play Console is the
   *delivery* key Google uses — the fingerprint above is your *upload* key

Even with Play App Signing enabled, back up the upload keystore — you still need
it to authenticate every future upload.

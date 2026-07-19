#!/usr/bin/env python3
"""Verify Noor's pinned Android build foundation without resolving dependencies."""
from __future__ import annotations

import argparse
import hashlib
import sys
import tomllib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WRAPPER_JAR = ROOT / "gradle/wrapper/gradle-wrapper.jar"
WRAPPER_JAR_SHA256 = "55243ef57851f12b070ad14f7f5bb8302daceeebc5bce5ece5fa6edb23e1145c"
GRADLE_DISTRIBUTION_SHA256 = "2ab2958f2a1e51120c326cad6f385153bb11ee93b3c216c5fccebfdfbb7ec6cb"
EXPECTED_VERSIONS = {
    "agp": "9.2.1",
    "kotlin": "2.3.10",
    "ksp": "2.3.10",
    "composeBom": "2026.06.01",
    "coreKtx": "1.19.0",
    "navigationCompose": "2.9.8",
}
ERRORS: list[str] = []
NOTES: list[str] = []


def require(condition: bool, message: str) -> None:
    if not condition:
        ERRORS.append(message)


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def verify_versions() -> None:
    catalog = tomllib.loads(read("gradle/libs.versions.toml"))
    versions = catalog.get("versions", {})
    for key, expected in EXPECTED_VERSIONS.items():
        require(versions.get(key) == expected, f"{key} must be pinned to {expected}")


def verify_wrapper_configuration(require_jar: bool) -> None:
    properties = read("gradle/wrapper/gradle-wrapper.properties")
    require(
        "distributionUrl=https\\://services.gradle.org/distributions/gradle-9.4.1-bin.zip" in properties,
        "Gradle distribution must be pinned to 9.4.1-bin",
    )
    require(
        f"distributionSha256Sum={GRADLE_DISTRIBUTION_SHA256}" in properties,
        "Gradle distribution SHA-256 is missing or incorrect",
    )
    require("validateDistributionUrl=true" in properties, "Gradle distribution URL validation is disabled")
    require("networkTimeout=10000" in properties, "Gradle Wrapper network timeout is not pinned")

    for relative in [
        "gradlew",
        "gradlew.bat",
        "bootstrap-gradle-wrapper.sh",
        "bootstrap-gradle-wrapper.ps1",
    ]:
        content = read(relative)
        require(WRAPPER_JAR_SHA256 in content, f"{relative} does not verify the Wrapper JAR checksum")

    shell_bootstrap = read("bootstrap-gradle-wrapper.sh")
    require("trap cleanup EXIT HUP INT TERM" in shell_bootstrap, "POSIX bootstrap does not clean temporary files")
    require("--retry-all-errors" in shell_bootstrap, "POSIX bootstrap has no bounded retry policy")
    require("mv -f \"$TEMP_FILE\" \"$WRAPPER_PATH\"" in shell_bootstrap,
            "POSIX bootstrap does not install the verified JAR atomically")

    windows_launcher = read("gradlew.bat")
    require("$env:NOOR_WRAPPER_JAR" in windows_launcher,
            "Windows launcher embeds the project path directly in PowerShell instead of using an environment variable")
    require('set "ACTUAL_WRAPPER_SHA256="' in windows_launcher,
            "Windows launcher does not clear the checksum variable before verification")

    powershell_bootstrap = read("bootstrap-gradle-wrapper.ps1")
    require("Get-FileHash" in powershell_bootstrap, "PowerShell bootstrap does not verify SHA-256")
    require("finally" in powershell_bootstrap, "PowerShell bootstrap does not clean temporary files")
    require("Move-Item -LiteralPath $temporaryPath" in powershell_bootstrap,
            "PowerShell bootstrap does not install the verified JAR atomically")

    if WRAPPER_JAR.is_file():
        actual_sha256 = sha256(WRAPPER_JAR)
        if actual_sha256 == WRAPPER_JAR_SHA256:
            NOTES.append("Checksum-verified Gradle Wrapper JAR is present.")
        else:
            ERRORS.append("Gradle Wrapper JAR checksum mismatch")
    elif require_jar:
        ERRORS.append("gradle-wrapper.jar is missing; run the checksum-verified bootstrap first")
    else:
        NOTES.append("Wrapper JAR is absent in this sandbox; bootstrap is checksum-pinned and required before Gradle execution.")


def verify_gradle_and_ci() -> None:
    properties = read("gradle.properties")
    for setting in [
        "org.gradle.caching=true",
        "org.gradle.configuration-cache=true",
        "org.gradle.parallel=true",
        "android.nonTransitiveRClass=true",
        "android.nonFinalResIds=true",
        "android.useAndroidX=true",
    ]:
        require(setting in properties, f"Missing production Gradle setting: {setting}")

    settings = read("settings.gradle.kts")
    require("RepositoriesMode.FAIL_ON_PROJECT_REPOS" in settings,
            "Repositories must be centralized to prevent module-level repository drift")
    require("google()" in settings and "mavenCentral()" in settings,
            "Required dependency repositories are not configured")

    app = read("app/build.gradle.kts")
    for fragment in [
        "compileSdk = 37",
        "targetSdk = 36",
        "JavaVersion.VERSION_17",
        "warningsAsErrors = true",
        "abortOnError = true",
        "checkReleaseBuilds = true",
    ]:
        require(fragment in app, f"App build configuration is missing: {fragment}")

    ci = read(".github/workflows/android-ci.yml")
    require("java-version: '17'" in ci, "Android CI must use JDK 17")
    require('sdkmanager "platforms;android-37" "build-tools;36.0.0"' in ci,
            "Android CI SDK versions do not match AGP 9.2 requirements")
    require("verify_build_foundation.py --allow-missing-wrapper-jar" in ci,
            "Source CI does not run the build-foundation verifier")
    require("verify_build_foundation.py --require-wrapper-jar" in ci,
            "Android CI does not verify the installed Wrapper JAR")


def main() -> int:
    parser = argparse.ArgumentParser()
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--require-wrapper-jar", action="store_true")
    mode.add_argument("--allow-missing-wrapper-jar", action="store_true")
    args = parser.parse_args()

    verify_versions()
    verify_wrapper_configuration(require_jar=not args.allow_missing_wrapper_jar)
    verify_gradle_and_ci()

    for message in NOTES:
        print(f"NOTE: {message}")
    if ERRORS:
        for message in ERRORS:
            print(f"ERROR: {message}", file=sys.stderr)
        print(f"BUILD FOUNDATION VERIFICATION: FAILED ({len(ERRORS)} issue(s))", file=sys.stderr)
        return 1

    print("BUILD FOUNDATION VERIFICATION: PASSED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

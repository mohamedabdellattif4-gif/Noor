#!/usr/bin/env python3
"""Validate security, content, and performance-critical entries in a built Noor APK or AAB."""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path

MAX_ARTIFACT_BYTES = 50 * 1024 * 1024
MAX_BASELINE_PROFILE_BYTES = 1_500_000
FORBIDDEN_SUFFIXES = (".jks", ".keystore", ".pem", ".p12", ".pfx")
SIGNATURE_TOOL_TIMEOUT_SECONDS = 60


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def find_apksigner() -> Path | None:
    direct = shutil.which("apksigner")
    if direct:
        return Path(direct)
    sdk_value = os.getenv("ANDROID_SDK_ROOT") or os.getenv("ANDROID_HOME")
    if not sdk_value:
        return None
    build_tools = Path(sdk_value) / "build-tools"
    candidates = sorted(build_tools.glob("*/apksigner"), reverse=True)
    return candidates[0] if candidates else None


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("artifact", type=Path)
    parser.add_argument("--report", type=Path)
    parser.add_argument("--require-signature", action="store_true")
    args = parser.parse_args()

    artifact = args.artifact.resolve()
    errors: list[str] = []
    notes: list[str] = []
    if not artifact.is_file():
        errors.append(f"Artifact does not exist: {artifact}")
    elif artifact.suffix.lower() not in {".apk", ".aab"}:
        errors.append("Artifact must be an APK or AAB")

    report: dict[str, object] = {
        "artifact": str(artifact),
        "errors": errors,
        "notes": notes,
    }
    if errors:
        return finish(report, args.report)

    size = artifact.stat().st_size
    report["size_bytes"] = size
    report["sha256"] = sha256(artifact)
    if size > MAX_ARTIFACT_BYTES:
        errors.append(f"Artifact exceeds {MAX_ARTIFACT_BYTES} bytes")

    is_bundle = artifact.suffix.lower() == ".aab"
    prefixes = {
        "database": "base/assets/database/noor.db" if is_bundle else "assets/database/noor.db",
        "quran_license": (
            "base/assets/licenses/TANZIL_QURAN_TEXT_LICENSE.txt"
            if is_bundle else "assets/licenses/TANZIL_QURAN_TEXT_LICENSE.txt"
        ),
        "metadata_license": (
            "base/assets/licenses/QURAN_META_MIT.txt"
            if is_bundle else "assets/licenses/QURAN_META_MIT.txt"
        ),
        "profile": (
            "BUNDLE-METADATA/com.android.tools.build.profiles/baseline.prof"
            if is_bundle else "assets/dexopt/baseline.prof"
        ),
    }

    with zipfile.ZipFile(artifact) as archive:
        names = set(archive.namelist())
        report["entry_count"] = len(names)
        for key, expected in prefixes.items():
            if expected not in names:
                errors.append(f"Missing {key} entry: {expected}")

        if not any(name.endswith(".dex") for name in names):
            errors.append("No DEX payload found")

        forbidden = sorted(
            name for name in names
            if name.lower().endswith(FORBIDDEN_SUFFIXES)
        )
        if forbidden:
            errors.append(f"Private key material found in artifact: {forbidden}")

        native_libraries = sorted(name for name in names if name.endswith(".so"))
        report["native_libraries"] = native_libraries
        if native_libraries:
            notes.append(
                "Native libraries are present; verify 16 KB page compatibility with bundletool/APK Analyzer.",
            )
        else:
            notes.append("No native libraries found; native ELF 16 KB alignment is not applicable.")

        profile_name = prefixes["profile"]
        if profile_name in names:
            profile_size = archive.getinfo(profile_name).file_size
            report["baseline_profile_bytes"] = profile_size
            if profile_size <= 0:
                errors.append("Compiled Baseline Profile is empty")
            elif profile_size > MAX_BASELINE_PROFILE_BYTES:
                errors.append("Compiled Baseline Profile exceeds the 1.5 MB platform limit")

        database_name = prefixes["database"]
        if database_name in names:
            report["database_bytes"] = archive.getinfo(database_name).file_size

        if is_bundle:
            signature_entries = sorted(
                name for name in names
                if name.upper().startswith("META-INF/")
                and name.upper().endswith((".RSA", ".DSA", ".EC"))
            )
            report["signature_entries"] = signature_entries
            if signature_entries or args.require_signature:
                jarsigner = shutil.which("jarsigner")
                if jarsigner is None:
                    if args.require_signature:
                        errors.append("jarsigner is unavailable; AAB signature cannot be verified")
                    else:
                        notes.append("Signed AAB detected, but jarsigner is unavailable for verification.")
                else:
                    try:
                        result = subprocess.run(
                            [jarsigner, "-verify", "-certs", str(artifact)],
                            capture_output=True,
                            text=True,
                            check=False,
                            timeout=SIGNATURE_TOOL_TIMEOUT_SECONDS,
                        )
                        signature_ok = result.returncode == 0 and "jar verified" in (
                            result.stdout + result.stderr
                        ).lower()
                    except subprocess.TimeoutExpired:
                        signature_ok = False
                        errors.append("AAB signature verification timed out")
                    report["signature_verified"] = signature_ok
                    if not signature_ok:
                        errors.append("AAB JAR signature verification failed")
            elif not signature_entries:
                notes.append("AAB is unsigned; signature verification was not required for this gate.")
        else:
            apksigner = find_apksigner()
            if apksigner is None:
                if args.require_signature:
                    errors.append("apksigner is unavailable; APK signature cannot be verified")
                else:
                    notes.append("APK signature was not checked because apksigner is unavailable.")
            else:
                try:
                    result = subprocess.run(
                        [str(apksigner), "verify", "--verbose", "--print-certs", str(artifact)],
                        capture_output=True,
                        text=True,
                        check=False,
                        timeout=SIGNATURE_TOOL_TIMEOUT_SECONDS,
                    )
                    signature_ok = result.returncode == 0
                except subprocess.TimeoutExpired:
                    signature_ok = False
                    errors.append("APK signature verification timed out")
                report["signature_verified"] = signature_ok
                if args.require_signature and not signature_ok:
                    errors.append("APK signature verification failed")
                elif not signature_ok:
                    notes.append("APK is unsigned or its signature could not be verified.")

    return finish(report, args.report)


def finish(report: dict[str, object], report_path: Path | None) -> int:
    payload = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    if report_path is not None:
        report_path.parent.mkdir(parents=True, exist_ok=True)
        report_path.write_text(payload, encoding="utf-8")
    print(payload, end="")
    return 1 if report["errors"] else 0


if __name__ == "__main__":
    sys.exit(main())

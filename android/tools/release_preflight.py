#!/usr/bin/env python3
"""Validate the local workstation and secret inputs before creating a Play release."""
from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SIGNING_KEYS = (
    "NOOR_KEYSTORE_PATH",
    "NOOR_KEYSTORE_PASSWORD",
    "NOOR_KEY_ALIAS",
    "NOOR_KEY_PASSWORD",
)
SEMVER = re.compile(r"\d+\.\d+\.\d+(?:[-+][0-9A-Za-z.-]+)?\Z")


def fail(message: str) -> None:
    print(f"ERROR: {message}", file=sys.stderr)


def java_major() -> int | None:
    java = shutil.which("java")
    if java is None:
        return None
    try:
        result = subprocess.run(
            [java, "-version"],
            capture_output=True,
            text=True,
            check=False,
            timeout=15,
        )
    except subprocess.TimeoutExpired:
        return None
    match = re.search(r'version "(?:1\.)?(\d+)', result.stderr + result.stdout)
    return int(match.group(1)) if match else None


def inside_repo(path: Path) -> bool:
    try:
        path.resolve().relative_to(ROOT.resolve())
        return True
    except ValueError:
        return False


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--require-signing", action="store_true")
    parser.add_argument("--require-version", action="store_true")
    args = parser.parse_args()
    errors: list[str] = []

    major = java_major()
    if major != 17:
        errors.append(f"JDK 17 is required; detected {major if major is not None else 'none'}.")

    sdk_value = os.getenv("ANDROID_SDK_ROOT") or os.getenv("ANDROID_HOME")
    if not sdk_value:
        errors.append("ANDROID_SDK_ROOT or ANDROID_HOME is not configured.")
    else:
        sdk = Path(sdk_value).expanduser()
        if not (sdk / "platforms" / "android-37").is_dir():
            errors.append(f"Android SDK Platform 37 is missing under {sdk}.")

    version_code = os.getenv("NOOR_VERSION_CODE")
    version_name = os.getenv("NOOR_VERSION_NAME")
    if args.require_version and (not version_code or not version_name):
        errors.append("NOOR_VERSION_CODE and NOOR_VERSION_NAME are required for a release.")
    if version_code:
        try:
            if int(version_code) <= 0:
                raise ValueError
        except ValueError:
            errors.append("NOOR_VERSION_CODE must be a positive integer.")
    if version_name and not SEMVER.fullmatch(version_name):
        errors.append("NOOR_VERSION_NAME must be semantic, for example 1.2.3 or 1.2.3-rc.1.")

    supplied = {key: os.getenv(key) for key in SIGNING_KEYS}
    present = [key for key, value in supplied.items() if value]
    if present and len(present) != len(SIGNING_KEYS):
        missing = sorted(set(SIGNING_KEYS) - set(present))
        errors.append(f"Release signing is partially configured; missing: {', '.join(missing)}.")
    if args.require_signing and len(present) != len(SIGNING_KEYS):
        errors.append("All NOOR_KEYSTORE_* variables are required for a signed release.")
    if len(present) == len(SIGNING_KEYS):
        key_path = Path(supplied["NOOR_KEYSTORE_PATH"] or "").expanduser()
        if not key_path.is_file():
            errors.append(f"Release keystore does not exist: {key_path}.")
        elif inside_repo(key_path):
            errors.append("Release keystore must be stored outside the source repository.")

    for error in errors:
        fail(error)
    if errors:
        return 1

    print("Release preflight: PASSED")
    print(f"- JDK: {major}")
    print(f"- SDK: {sdk_value}")
    print(f"- Version: {version_name or 'Gradle default'} ({version_code or 'Gradle default'})")
    print(f"- Signing: {'configured' if len(present) == len(SIGNING_KEYS) else 'not required'}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

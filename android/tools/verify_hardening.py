#!/usr/bin/env python3
"""Regression tests for Noor's production-hardening and release tools."""
from __future__ import annotations

import json
import os
import shutil
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PYTHON = sys.executable
COMMAND_TIMEOUT_SECONDS = 120


def run(
    command: list[str],
    *,
    env: dict[str, str] | None = None,
    expect_success: bool = True,
) -> subprocess.CompletedProcess[str]:
    try:
        result = subprocess.run(
            command,
            cwd=ROOT,
            env=env,
            capture_output=True,
            text=True,
            check=False,
            timeout=COMMAND_TIMEOUT_SECONDS,
        )
    except subprocess.TimeoutExpired as error:
        raise AssertionError(
            f"Command exceeded {COMMAND_TIMEOUT_SECONDS}s: {' '.join(command)}",
        ) from error
    succeeded = result.returncode == 0
    if succeeded != expect_success:
        expectation = "success" if expect_success else "failure"
        raise AssertionError(
            f"Expected {expectation}: {' '.join(command)}\n"
            f"exit={result.returncode}\nstdout={result.stdout}\nstderr={result.stderr}",
        )
    return result


def write_synthetic_aab(path: Path) -> None:
    entries = {
        "base/assets/database/noor.db": b"db",
        "base/assets/licenses/TANZIL_QURAN_TEXT_LICENSE.txt": b"license",
        "base/assets/licenses/QURAN_META_MIT.txt": b"license",
        "BUNDLE-METADATA/com.android.tools.build.profiles/baseline.prof": b"profile",
        "base/dex/classes.dex": b"dex",
    }
    with zipfile.ZipFile(path, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for name, payload in entries.items():
            archive.writestr(name, payload)


def test_profile_installer(temp: Path) -> None:
    output = temp / "profile-output" / "device" / "Noor-generate-baseline-prof.txt"
    output.parent.mkdir(parents=True)
    output.write_text(
        "Lcom/noor/app/MainActivity;\nLcom/noor/app/ui/home/HomeRouteKt;\n",
        encoding="utf-8",
    )
    app_source = temp / "app-source"
    run([
        PYTHON,
        "tools/update_baseline_profile.py",
        "--search-root",
        str(temp / "profile-output"),
        "--app-source",
        str(app_source),
    ])
    installed = app_source / "baseline-prof.txt"
    assert installed.is_file() and "com/noor/" in installed.read_text(encoding="utf-8")


def test_release_preflight(temp: Path) -> None:
    if os.name == "nt":
        return
    fake_bin = temp / "bin"
    fake_bin.mkdir()
    fake_java = fake_bin / "java"
    fake_java.write_text("#!/bin/sh\necho 'openjdk version \"17.0.12\"' >&2\n", encoding="utf-8")
    fake_java.chmod(0o755)
    sdk = temp / "sdk"
    (sdk / "platforms" / "android-37").mkdir(parents=True)
    key = temp / "noor-upload.jks"
    key.write_bytes(b"test-keystore")
    env = os.environ.copy()
    env.update({
        "PATH": f"{fake_bin}{os.pathsep}{env.get('PATH', '')}",
        "ANDROID_SDK_ROOT": str(sdk),
        "NOOR_VERSION_CODE": "7",
        "NOOR_VERSION_NAME": "1.2.3-rc.1",
        "NOOR_KEYSTORE_PATH": str(key),
        "NOOR_KEYSTORE_PASSWORD": "test",
        "NOOR_KEY_ALIAS": "noor",
        "NOOR_KEY_PASSWORD": "test",
    })
    run([PYTHON, "tools/release_preflight.py", "--require-signing", "--require-version"], env=env)

    partial = env.copy()
    partial.pop("NOOR_KEY_PASSWORD")
    run(
        [PYTHON, "tools/release_preflight.py", "--require-signing", "--require-version"],
        env=partial,
        expect_success=False,
    )

    repository_key = ROOT / ".verification" / "inside-repository.jks"
    repository_key.parent.mkdir(parents=True, exist_ok=True)
    repository_key.write_bytes(b"test-keystore")
    linked_key = temp / "linked-keystore.jks"
    try:
        linked_key.symlink_to(repository_key)
        linked = env.copy()
        linked["NOOR_KEYSTORE_PATH"] = str(linked_key)
        run(
            [PYTHON, "tools/release_preflight.py", "--require-signing", "--require-version"],
            env=linked,
            expect_success=False,
        )
    finally:
        repository_key.unlink(missing_ok=True)
        try:
            repository_key.parent.rmdir()
        except OSError:
            pass


def test_artifact_verifier(temp: Path) -> None:
    artifact = temp / "noor-test.aab"
    report = temp / "artifact.json"
    write_synthetic_aab(artifact)
    run([PYTHON, "tools/verify_android_artifact.py", str(artifact), "--report", str(report)])
    assert not json.loads(report.read_text(encoding="utf-8"))["errors"]
    run(
        [PYTHON, "tools/verify_android_artifact.py", str(artifact), "--require-signature"],
        expect_success=False,
    )

    keytool = shutil.which("keytool")
    jarsigner = shutil.which("jarsigner")
    if keytool is None or jarsigner is None:
        print("- Signature positive-path test skipped: keytool/jarsigner unavailable")
        return
    keystore = temp / "signing.jks"
    run([
        keytool,
        "-genkeypair",
        "-noprompt",
        "-keystore",
        str(keystore),
        "-storepass",
        "changeit",
        "-keypass",
        "changeit",
        "-alias",
        "noor",
        "-keyalg",
        "RSA",
        "-keysize",
        "2048",
        "-validity",
        "3650",
        "-dname",
        "CN=Noor Test, OU=QA, O=Noor, L=Cairo, C=EG",
    ])
    run([
        jarsigner,
        "-keystore",
        str(keystore),
        "-storepass",
        "changeit",
        "-keypass",
        "changeit",
        str(artifact),
        "noor",
    ])
    signed = run([
        PYTHON,
        "tools/verify_android_artifact.py",
        str(artifact),
        "--require-signature",
    ])
    payload = json.loads(signed.stdout)
    assert payload.get("signature_verified") is True and not payload["errors"]


def test_workflow_yaml() -> None:
    try:
        import yaml
    except ImportError:
        print("- Workflow YAML parser check skipped: PyYAML unavailable")
        return
    for path in sorted((ROOT / ".github/workflows").glob("*.yml")):
        yaml.safe_load(path.read_text(encoding="utf-8"))
    yaml.safe_load((ROOT / ".github/dependabot.yml").read_text(encoding="utf-8"))


def main() -> int:
    with tempfile.TemporaryDirectory(prefix="noor-hardening-") as directory:
        temp = Path(directory)
        test_profile_installer(temp)
        test_release_preflight(temp)
        test_artifact_verifier(temp)
    test_workflow_yaml()
    print("HARDENING VERIFICATION: PASSED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

#!/usr/bin/env python3
"""Static integrity checks for the Noor Phase 1 foundation."""

from __future__ import annotations

import re
import sys
import tomllib
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ERRORS: list[str] = []


def require(condition: bool, message: str) -> None:
    if not condition:
        ERRORS.append(message)


def read(relative_path: str) -> str:
    path = ROOT / relative_path
    require(path.is_file(), f"Missing required file: {relative_path}")
    return path.read_text(encoding="utf-8") if path.is_file() else ""


settings = read("settings.gradle.kts")
required_modules = {
    ":app",
    ":core:common",
    ":core:designsystem",
    ":core:navigation",
    ":domain",
    ":data",
}
included_modules = set(re.findall(r'include\("([^"]+)"\)', settings))
require(included_modules == required_modules, "settings.gradle.kts module set is incorrect")

catalog_path = ROOT / "gradle/libs.versions.toml"
with catalog_path.open("rb") as catalog_file:
    catalog = tomllib.load(catalog_file)
require("versions" in catalog and "libraries" in catalog and "plugins" in catalog, "Version Catalog is incomplete")
require(catalog["versions"].get("agp") == "9.2.1", "AGP must be pinned to 9.2.1")
require(catalog["versions"].get("kotlin") == "2.3.10", "Kotlin must align with AGP 9.2 built-in Kotlin")
require(catalog["versions"].get("ksp") == "2.3.10", "KSP must be pinned to 2.3.10")

for xml_file in ROOT.rglob("*.xml"):
    try:
        ET.parse(xml_file)
    except ET.ParseError as error:
        ERRORS.append(f"Invalid XML in {xml_file.relative_to(ROOT)}: {error}")

for gradle_file in ROOT.rglob("*.gradle.kts"):
    content = gradle_file.read_text(encoding="utf-8")
    for opening, closing in (("(", ")"), ("{", "}"), ("[", "]")):
        require(
            content.count(opening) == content.count(closing),
            f"Unbalanced {opening}{closing} in {gradle_file.relative_to(ROOT)}",
        )

all_gradle = "\n".join(path.read_text(encoding="utf-8") for path in ROOT.rglob("*.gradle.kts"))
for project_reference in re.findall(r'project\("([^"]+)"\)', all_gradle):
    require(project_reference in required_modules, f"Unknown project dependency: {project_reference}")

for kotlin_file in ROOT.rglob("*.kt"):
    content = kotlin_file.read_text(encoding="utf-8")
    package_match = re.search(r"^package\s+([\w.]+)", content, re.MULTILINE)
    require(package_match is not None, f"Missing package declaration: {kotlin_file.relative_to(ROOT)}")
    require("TODO(" not in content, f"Unresolved TODO in {kotlin_file.relative_to(ROOT)}")
    require("Foundation ready\"" not in content, f"UI text must be in resources: {kotlin_file.relative_to(ROOT)}")

manifest = read("app/src/main/AndroidManifest.xml")
require('android:name=".NoorApplication"' in manifest, "Hilt Application is not registered")
require('android:supportsRtl="true"' in manifest, "RTL support is not enabled")
require('android:allowBackup="false"' in manifest, "Backup policy must be explicit")

gradle_properties = read("gradle.properties")
require("android.useAndroidX=true" in gradle_properties, "AndroidX must be enabled")
require("kotlin.code.style=official" in gradle_properties, "Official Kotlin code style must be enabled")

application = read("app/src/main/kotlin/com/noor/app/NoorApplication.kt")
activity = read("app/src/main/kotlin/com/noor/app/MainActivity.kt")
require("@HiltAndroidApp" in application, "NoorApplication is missing @HiltAndroidApp")
require("@AndroidEntryPoint" in activity, "MainActivity is missing @AndroidEntryPoint")

navigation = read("app/src/main/kotlin/com/noor/app/navigation/NoorNavHost.kt")
require("NavHost(" in navigation and "composable(" in navigation, "Navigation host is incomplete")

for forbidden in ("datastore", "media3", "work-runtime"):
    require(forbidden not in all_gradle.lower(), f"Out-of-scope dependency detected: {forbidden}")

for gradle_file in ROOT.rglob("*.gradle.kts"):
    content = gradle_file.read_text(encoding="utf-8").lower()
    if "androidx.room" in content or "libs.plugins.room" in content:
        require(
            gradle_file.relative_to(ROOT).as_posix() in {"build.gradle.kts", "data/build.gradle.kts"},
            f"Room must remain isolated to the data module: {gradle_file.relative_to(ROOT)}",
        )

wrapper_properties = read("gradle/wrapper/gradle-wrapper.properties")
require("gradle-9.4.1-bin.zip" in wrapper_properties, "Gradle 9.4.1 is not pinned")
require("distributionSha256Sum=" in wrapper_properties, "Gradle distribution checksum is missing")

if ERRORS:
    print("Phase 1 verification failed:")
    for error in ERRORS:
        print(f"- {error}")
    sys.exit(1)

print("Phase 1 static verification passed.")
print(f"Checked {len(list(ROOT.rglob('*.kt')))} Kotlin files and {len(list(ROOT.rglob('*.xml')))} XML files.")

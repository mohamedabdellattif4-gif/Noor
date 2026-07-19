#!/usr/bin/env python3
"""Static integrity and design-token checks for Noor Phase 2."""

from __future__ import annotations

import re
import subprocess
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


def resource_keys(relative_path: str) -> set[str]:
    path = ROOT / relative_path
    require(path.is_file(), f"Missing resource file: {relative_path}")
    if not path.is_file():
        return set()
    return {
        element.attrib["name"]
        for element in ET.parse(path).getroot().findall("string")
    }


def relative_luminance(hex_color: str) -> float:
    channels = [int(hex_color[index:index + 2], 16) / 255 for index in (0, 2, 4)]
    linear = [
        channel / 12.92 if channel <= 0.04045 else ((channel + 0.055) / 1.055) ** 2.4
        for channel in channels
    ]
    return 0.2126 * linear[0] + 0.7152 * linear[1] + 0.0722 * linear[2]


def contrast_ratio(first: str, second: str) -> float:
    first_luminance = relative_luminance(first)
    second_luminance = relative_luminance(second)
    lighter = max(first_luminance, second_luminance)
    darker = min(first_luminance, second_luminance)
    return (lighter + 0.05) / (darker + 0.05)


phase1 = subprocess.run(
    [sys.executable, str(ROOT / "tools/verify_phase1.py")],
    cwd=ROOT,
    capture_output=True,
    text=True,
    check=False,
)
require(
    phase1.returncode == 0,
    "Phase 1 regression verification failed:\n" + (phase1.stdout + phase1.stderr).strip(),
)

required_design_files = {
    "core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/Color.kt",
    "core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/Typography.kt",
    "core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/Shape.kt",
    "core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/Spacing.kt",
    "core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/NoorTheme.kt",
    "core/designsystem/src/main/kotlin/com/noor/core/designsystem/component/NoorButton.kt",
    "core/designsystem/src/main/kotlin/com/noor/core/designsystem/component/NoorCard.kt",
    "core/designsystem/src/main/kotlin/com/noor/core/designsystem/component/NoorTopAppBar.kt",
    "core/designsystem/src/main/kotlin/com/noor/core/designsystem/component/NoorLoadingIndicator.kt",
    "core/designsystem/src/main/kotlin/com/noor/core/designsystem/preview/NoorPreviews.kt",
    "core/designsystem/src/main/kotlin/com/noor/core/designsystem/preview/ComponentPreview.kt",
}
for required_file in required_design_files:
    require((ROOT / required_file).is_file(), f"Missing design-system file: {required_file}")

with (ROOT / "gradle/libs.versions.toml").open("rb") as catalog_file:
    catalog = tomllib.load(catalog_file)
require(
    "androidx-compose-foundation" in catalog.get("libraries", {}),
    "Compose Foundation must be declared in the Version Catalog",
)

xml_files = list(ROOT.rglob("*.xml"))
for xml_file in xml_files:
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

kotlin_files = list(ROOT.rglob("*.kt"))
all_kotlin_source = "\n".join(path.read_text(encoding="utf-8") for path in kotlin_files)
require(
    all_kotlin_source.count("internal val NoorTypography") == 1,
    "NoorTypography must have exactly one declaration",
)
require(
    all_kotlin_source.count("fun NoorTheme(") == 1,
    "NoorTheme must have exactly one declaration",
)
require(
    not (ROOT / "core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/Type.kt").exists(),
    "Stale Type.kt must not coexist with Typography.kt",
)
require(
    not (ROOT / "core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/NoorThemePreview.kt").exists(),
    "Stale theme-package preview must not coexist with the shared preview package",
)
require(
    "fun NoorButton(" not in all_kotlin_source,
    "Removed NoorButton API is still declared; use NoorFilledButton or NoorOutlinedButton",
)

for kotlin_file in kotlin_files:
    content = kotlin_file.read_text(encoding="utf-8")
    relative_path = kotlin_file.relative_to(ROOT)
    require(
        re.search(r"^package\s+[\w.]+", content, re.MULTILINE) is not None,
        f"Missing package declaration: {relative_path}",
    )
    require("TODO(" not in content, f"Unresolved TODO in {relative_path}")
    require("FIXME" not in content, f"Unresolved FIXME in {relative_path}")
    require("padding(left" not in content, f"LTR-only padding in {relative_path}")
    require("padding(right" not in content, f"LTR-only padding in {relative_path}")
    require("absoluteLeft" not in content, f"LTR-only absoluteLeft in {relative_path}")
    require("absoluteRight" not in content, f"LTR-only absoluteRight in {relative_path}")
    if relative_path.as_posix() != (
        "core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/Color.kt"
    ):
        require(
            "Color(0x" not in content,
            f"Hardcoded Compose color outside Color.kt: {relative_path}",
        )

manifest = read("app/src/main/AndroidManifest.xml")
require('android:supportsRtl="true"' in manifest, "RTL support is not enabled")

require(
    resource_keys("app/src/main/res/values/strings.xml")
    == resource_keys("app/src/main/res/values-ar/strings.xml"),
    "App Arabic and default string resource keys must match",
)
require(
    resource_keys("core/designsystem/src/main/res/values/strings.xml")
    == resource_keys("core/designsystem/src/main/res/values-ar/strings.xml"),
    "Design-system Arabic and default string resource keys must match",
)

for theme_file in (
    "app/src/main/res/values-night/themes.xml",
    "app/src/main/res/values-v27/themes.xml",
    "app/src/main/res/values-night-v27/themes.xml",
    "app/src/main/res/values/colors.xml",
    "app/src/main/res/values-night/colors.xml",
):
    require((ROOT / theme_file).is_file(), f"Missing theme resource: {theme_file}")

theme = read("core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/NoorTheme.kt")
for expected in (
    "isSystemInDarkTheme()",
    "useDynamicColor: Boolean = false",
    "Build.VERSION_CODES.S",
    "NoorDarkColorScheme",
    "NoorLightColorScheme",
    "dynamicDarkColorScheme",
    "dynamicLightColorScheme",
    "NoorTypography",
    "NoorShapes",
    "LocalNoorSpacing",
    "LocalNoorExtendedTypography",
):
    require(expected in theme, f"NoorTheme is missing: {expected}")

typography = read("core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/Typography.kt")
require("FontFamily.SansSerif" in typography, "UI font family is not defined")
require("FontFamily.Serif" in typography, "Quran fallback font family is not defined")
require("TextDirection.ContentOrRtl" in typography, "Quran typography is not RTL-safe")
require("letterSpacing = 0.sp" in typography, "Arabic-safe zero letter spacing is missing")

preview = read("core/designsystem/src/main/kotlin/com/noor/core/designsystem/preview/NoorPreviews.kt")
component_preview = read(
    "core/designsystem/src/main/kotlin/com/noor/core/designsystem/preview/ComponentPreview.kt"
)
require('locale = "ar"' in preview, "Arabic RTL preview is missing")
require("UI_MODE_NIGHT_YES" in preview, "Dark mode preview is missing")
for component_name in (
    "NoorCard",
    "NoorFilledButton",
    "NoorOutlinedButton",
    "NoorLoadingIndicator",
):
    require(component_name in component_preview, f"Component preview is missing {component_name}")

all_gradle = "\n".join(path.read_text(encoding="utf-8") for path in ROOT.rglob("*.gradle.kts"))
for forbidden in (
    "androidx.datastore",
    "androidx.media3",
    "androidx.work",
):
    require(forbidden not in all_gradle.lower(), f"Out-of-scope dependency detected: {forbidden}")

design_gradle = read("core/designsystem/build.gradle.kts").lower()
require("androidx.room" not in design_gradle, "Design system must not depend on Room")
require("libs.plugins.room" not in design_gradle, "Design system must not apply the Room plugin")

color_source = read("core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/Color.kt")
colors = dict(re.findall(r"val\s+(\w+)\s*=\s*Color\(0xFF([0-9A-Fa-f]{6})\)", color_source))
colors["White"] = "FFFFFF"
colors["Black"] = "000000"
contrast_pairs = {
    "light primary": ("Emerald40", "White"),
    "light primary container": ("Emerald90", "Emerald10"),
    "light secondary": ("Sage40", "White"),
    "light secondary container": ("Sage90", "Sage10"),
    "light tertiary": ("Gold40", "White"),
    "light tertiary container": ("Gold90", "Gold10"),
    "light error": ("Error40", "White"),
    "light error container": ("Error90", "Error10"),
    "light background": ("Neutral99", "Neutral10"),
    "dark primary": ("Emerald80", "Emerald20"),
    "dark primary container": ("Emerald30", "Emerald90"),
    "dark secondary": ("Sage80", "Sage20"),
    "dark secondary container": ("Sage30", "Sage90"),
    "dark tertiary": ("Gold80", "Gold20"),
    "dark tertiary container": ("Gold30", "Gold90"),
    "dark error": ("Error80", "Error20"),
    "dark error container": ("Error30", "Error90"),
    "dark background": ("Neutral4", "Neutral90"),
}
for label, (foreground_name, background_name) in contrast_pairs.items():
    require(foreground_name in colors, f"Missing color token: {foreground_name}")
    require(background_name in colors, f"Missing color token: {background_name}")
    if foreground_name in colors and background_name in colors:
        ratio = contrast_ratio(colors[foreground_name], colors[background_name])
        require(ratio >= 4.5, f"Insufficient {label} contrast: {ratio:.2f}:1")

font_files = [
    path for path in ROOT.rglob("*")
    if path.is_file() and path.suffix.lower() in {".ttf", ".otf", ".woff", ".woff2"}
]
require(not font_files, "Bundled font files are not permitted in Phase 2")

if ERRORS:
    print("Phase 2 verification failed:")
    for error in ERRORS:
        print(f"- {error}")
    sys.exit(1)

print("Phase 2 static verification passed.")
print(f"Checked {len(kotlin_files)} Kotlin files and {len(xml_files)} XML files.")
print(f"Validated {len(contrast_pairs)} critical color contrast pairs at 4.5:1 or higher.")

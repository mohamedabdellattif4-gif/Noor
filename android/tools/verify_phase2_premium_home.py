#!/usr/bin/env python3
"""Static and behavioral checks for Noor's premium Home implementation."""
from __future__ import annotations

import math
import re
import subprocess
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def read(relative: str) -> str:
    path = ROOT / relative
    require(path.is_file(), f"Missing required file: {relative}")
    return path.read_text(encoding="utf-8")


def resource_names(path: Path) -> set[str]:
    root = ET.parse(path).getroot()
    return {
        node.attrib["name"]
        for node in root
        if node.tag in {"string", "plurals"} and "name" in node.attrib
    }


def luminance(hex_color: str) -> float:
    channels = [int(hex_color[index:index + 2], 16) / 255.0 for index in (0, 2, 4)]
    linear = [
        value / 12.92 if value <= 0.04045 else ((value + 0.055) / 1.055) ** 2.4
        for value in channels
    ]
    return 0.2126 * linear[0] + 0.7152 * linear[1] + 0.0722 * linear[2]


def contrast(first: str, second: str) -> float:
    bright, dark = sorted((luminance(first), luminance(second)), reverse=True)
    return (bright + 0.05) / (dark + 0.05)


def check_color_contrast() -> None:
    source = read(
        "core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/"
        "NoorPremiumColors.kt",
    )
    colors = dict(re.findall(r"(\w+) = Color\(0xFF([0-9A-Fa-f]{6})\)", source))
    required = {
        "emeraldDeep",
        "emeraldElevated",
        "ivory",
        "goldHighlight",
        "onEmerald",
        "onIvory",
        "onIvoryMuted",
    }
    require(required <= colors.keys(), "Premium palette is incomplete")
    pairs = [
        ("onEmerald", "emeraldDeep", 7.0),
        ("onEmerald", "emeraldElevated", 7.0),
        ("goldHighlight", "emeraldDeep", 4.5),
        ("onIvory", "ivory", 7.0),
        ("onIvoryMuted", "ivory", 4.5),
    ]
    for foreground, background, minimum in pairs:
        ratio = contrast(colors[foreground], colors[background])
        require(
            ratio >= minimum,
            f"Insufficient contrast {foreground}/{background}: {ratio:.2f}",
        )


def check_strings() -> None:
    default_path = ROOT / "app/src/main/res/values/strings.xml"
    arabic_path = ROOT / "app/src/main/res/values-ar/strings.xml"
    default_names = resource_names(default_path)
    arabic_names = resource_names(arabic_path)
    require(default_names == arabic_names, "Arabic/default resource parity is broken")
    arabic = arabic_path.read_text(encoding="utf-8")
    require(
        "هذا التطبيق صدقة جارية على روح والدي" in arabic,
        "The exact memorial note is missing from the Arabic in-app resources",
    )
    for key in {
        "islamic_greeting",
        "memorial_note",
        "daily_reading",
        "reading_progress_pages",
        "quick_access",
        "recent_reciter",
        "home",
        "streak_days",
    }:
        require(key in default_names, f"Missing localized Home resource: {key}")


def check_ui_structure() -> None:
    home = read("app/src/main/kotlin/com/noor/app/ui/home/HomeRoute.kt")
    components = read("app/src/main/kotlin/com/noor/app/ui/home/HomeComponents.kt")
    for symbol in {
        "NoorPatternBackground",
        "MemorialCard",
        "ContinueReadingCard",
        "DailyReadingRow",
        "QuickAccessGrid",
        "RecentReciterCard",
        "NoorHomeBottomBar",
        "PremiumSurahCard",
    }:
        require(symbol in home or symbol in components, f"Missing Home component: {symbol}")
    require("material-icons-extended" not in read("gradle/libs.versions.toml"),
            "Deprecated Material Icons dependency must not be introduced")
    require("NoorLineIcon" in home and "NoorLineIcon" in components,
            "Local vector icon system is not used consistently")
    require("SURAH_LIST_ITEM_INDEX = 6" in home, "Surah quick-scroll index is incorrect")


def check_progress_persistence() -> None:
    settings_model = read("domain/src/main/kotlin/com/noor/domain/model/UserSettings.kt")
    settings_contract = read("domain/src/main/kotlin/com/noor/domain/repository/SettingsRepository.kt")
    preferences = read("data/src/main/kotlin/com/noor/data/settings/PreferencesSettingsRepository.kt")
    reader = read("app/src/main/kotlin/com/noor/app/ui/reader/ReaderViewModel.kt")
    calculator = read("data/src/main/kotlin/com/noor/data/settings/ReadingProgressCalculator.kt")
    for token in {"dailyPagesRead", "readingStreakDays"}:
        require(token in settings_model, f"Missing persisted reading state: {token}")
    require("recordReading" in settings_contract, "Reading progress contract is missing")
    for key in {"READING_DAY", "READING_PAGES", "READING_STREAK"}:
        require(key in preferences, f"Missing DataStore key: {key}")
    require(
        reader.count("recordReadingSafely(") >= 3,
        "Reader does not route user reading actions through safe progress persistence",
    )
    require(
        "settingsRepository.recordReading(ayah.surahNumber, ayah.id, ayah.pageNumber)" in reader,
        "Reader is not persisting canonical surah, ayah, and page progress",
    )
    require("previousDayKey" in calculator, "Consecutive-day streak logic is missing")


def check_pure_kotlin_compilation() -> None:
    compiler = subprocess.run(
        ["bash", "-lc", "command -v kotlinc"],
        cwd=ROOT,
        capture_output=True,
        text=True,
        check=False,
    )
    if compiler.returncode != 0:
        print("- Pure Kotlin compilation skipped: kotlinc unavailable")
        return
    with tempfile.TemporaryDirectory(prefix="noor-phase2-") as directory:
        destination = Path(directory) / "check.jar"
        command = [
            compiler.stdout.strip(),
            "domain/src/main/kotlin/com/noor/domain/model/ThemeMode.kt",
            "domain/src/main/kotlin/com/noor/domain/model/Reciter.kt",
            "domain/src/main/kotlin/com/noor/domain/model/UserSettings.kt",
            "data/src/main/kotlin/com/noor/data/settings/ReadingProgressCalculator.kt",
            "-Werror",
            "-d",
            str(destination),
        ]
        result = subprocess.run(
            command,
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
        )
        require(
            result.returncode == 0,
            f"Pure Kotlin compilation failed:\n{result.stdout}\n{result.stderr}",
        )


def check_no_placeholders() -> None:
    relevant = [
        ROOT / "app/src/main/kotlin/com/noor/app/ui/home",
        ROOT / "core/designsystem/src/main/kotlin/com/noor/core/designsystem/component",
        ROOT / "data/src/main/kotlin/com/noor/data/settings",
    ]
    forbidden = re.compile(r"\b(?:TODO|FIXME)\b|NotImplementedError|TODO\(")
    for directory in relevant:
        for path in directory.rglob("*.kt"):
            require(not forbidden.search(path.read_text(encoding="utf-8")),
                    f"Placeholder found in {path.relative_to(ROOT)}")


def main() -> int:
    check_color_contrast()
    check_strings()
    check_ui_structure()
    check_progress_persistence()
    check_pure_kotlin_compilation()
    check_no_placeholders()
    print("PHASE 2 PREMIUM HOME VERIFICATION: PASSED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

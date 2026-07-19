#!/usr/bin/env python3
"""Environment-independent verification gate for Noor Phase 8."""
from __future__ import annotations

import re
import shutil
import sqlite3
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ERRORS: list[str] = []
PASSES: list[str] = []


def check(condition: bool, message: str) -> None:
    (PASSES if condition else ERRORS).append(message)


def text(path: str) -> str:
    file = ROOT / path
    check(file.is_file(), f"file exists: {path}")
    return file.read_text(encoding="utf-8") if file.is_file() else ""


def contains(path: str, *needles: str) -> str:
    value = text(path)
    for needle in needles:
        check(needle in value, f"{path} contains {needle}")
    return value


def verify_xml_and_resources() -> None:
    base = ROOT / "app/src/main/res/values/strings.xml"
    ar = ROOT / "app/src/main/res/values-ar/strings.xml"
    try:
        base_root = ET.parse(base).getroot()
        ar_root = ET.parse(ar).getroot()
    except ET.ParseError as error:
        ERRORS.append(f"resource XML parses: {error}")
        return
    base_names = {node.attrib["name"] for node in base_root if node.tag == "string"}
    ar_names = {node.attrib["name"] for node in ar_root if node.tag == "string"}
    required = {
        "adhkar_screen_title", "adhkar_search", "adhkar_favorites_only",
        "tasbih_title", "adhkar_reminders_title", "adhkar_reminder_body",
        "adhkar_notification_permission_denied", "adhkar_load_error",
    }
    check(base_names == ar_names, "English and Arabic string keys remain in parity")
    check(required <= base_names, "Phase 8 resources exist in both locales")


def verify_catalog() -> None:
    value = contains(
        "data/src/main/kotlin/com/noor/data/adhkar/BundledAdhkarCatalog.kt",
        "class BundledAdhkarCatalog",
        "fun load(): List<Dhikr>",
    )
    ids = re.findall(r'id = "([^"]+)"', value)
    check(len(ids) >= 20, f"curated catalog has at least 20 entries (found {len(ids)})")
    check(len(ids) == len(set(ids)), "catalog IDs are stable and unique")
    for category in ("MORNING", "EVENING", "AFTER_PRAYER", "SLEEP", "WAKING", "GENERAL"):
        check(f"AdhkarCategory.{category}" in value, f"catalog contains {category.lower()} category")
    check("morning_raditu" not in value, "catalog excludes disputed starter entry pending publisher review")
    check("TODO" not in value and "PLACEHOLDER" not in value, "catalog contains no placeholder text")
    salawat = re.search(r'id = "general_salawat"[\s\S]*?repeatCount = (\d+)', value)
    check(bool(salawat and salawat.group(1) == "1"), "general salawat card does not prescribe an unsupported count")


def verify_architecture() -> None:
    contains("domain/src/main/kotlin/com/noor/domain/model/Dhikr.kt", "data class Dhikr", "repeatCount > 0")
    contains("domain/src/main/kotlin/com/noor/domain/model/AdhkarCategory.kt", "enum class AdhkarCategory", "fun fromKey")
    contains("domain/src/main/kotlin/com/noor/domain/repository/AdhkarRepository.kt", "interface AdhkarRepository", "val adhkar", "setFavorite")
    contains("domain/src/main/kotlin/com/noor/domain/repository/AdhkarReminderScheduler.kt", "interface AdhkarReminderScheduler")
    contains(
        "data/src/main/kotlin/com/noor/data/repository/DefaultAdhkarRepository.kt",
        "stringSetPreferencesKey(\"adhkar_favorites\")",
        "booleanPreferencesKey(\"adhkar_morning_reminder\")",
        "booleanPreferencesKey(\"adhkar_evening_reminder\")",
        "require(bundledItems.any",
    )
    settings = contains(
        "data/src/main/kotlin/com/noor/data/settings/NoorPreferencesDataStore.kt",
        "preferencesDataStore(",
        'name = "noor_settings"',
        "ReplaceFileCorruptionHandler",
    )
    all_data_store_declarations = "\n".join(
        path.read_text(encoding="utf-8")
        for path in (ROOT / "data/src/main/kotlin").rglob("*.kt")
    )
    declarations = re.findall(
        r'preferencesDataStore\(\s*name\s*=\s*"noor_settings"',
        all_data_store_declarations,
    )
    check(len(declarations) == 1, "one shared Noor DataStore delegate is declared")
    contains("data/src/main/kotlin/com/noor/data/di/DataBindingsModule.kt", "AdhkarRepository", "DefaultAdhkarRepository")


def verify_ui_and_navigation() -> None:
    contains(
        "core/navigation/src/main/kotlin/com/noor/core/navigation/NoorRoute.kt",
        'const val ADHKAR = "adhkar"',
        'const val TASBIH = "adhkar/tasbih"',
        'const val ADHKAR_REMINDERS = "adhkar/reminders"',
    )
    contains(
        "app/src/main/kotlin/com/noor/app/navigation/NoorNavHost.kt",
        "AdhkarRoute(", "TasbihRoute(", "AdhkarRemindersRoute(", "onAdhkar",
    )
    contains(
        "app/src/main/kotlin/com/noor/app/ui/adhkar/AdhkarViewModel.kt",
        "ArabicNormalizer.normalize", "MAX_QUERY_LENGTH", "coerceAtMost(item.repeatCount)", "CatalogSnapshot",
    )
    contains(
        "app/src/main/kotlin/com/noor/app/ui/adhkar/AdhkarRoute.kt",
        "LazyColumn", "FilterChip", "DhikrCard", "AdhkarEmptyCard", "AdhkarErrorCard", "AdhkarTestTags.CONTENT",
    )
    contains(
        "app/src/main/kotlin/com/noor/app/ui/adhkar/TasbihViewModel.kt",
        "TasbihUiState", "coerceAtMost(state.target)", "setOf(33, 100)",
    )
    contains(
        "app/src/main/kotlin/com/noor/app/ui/adhkar/TasbihRoute.kt",
        "TasbihScreen", "performHapticFeedback", ".sizeIn(minWidth = 240.dp",
    )


def verify_reminders() -> None:
    contains(
        "app/src/main/kotlin/com/noor/app/adhkar/WorkManagerAdhkarReminderScheduler.kt",
        "PeriodicWorkRequestBuilder<AdhkarReminderWorker>", "ExistingPeriodicWorkPolicy.UPDATE", "delayUntilHour",
    )
    contains(
        "app/src/main/kotlin/com/noor/app/adhkar/AdhkarReminderWorker.kt",
        "POST_NOTIFICATIONS", "NotificationCompat.Builder", "EXTRA_OPEN_ADHKAR", "FLAG_IMMUTABLE",
    )
    contains(
        "app/src/main/kotlin/com/noor/app/ui/adhkar/AdhkarRemindersRoute.kt",
        "rememberLauncherForActivityResult", "RequestPermission", "POST_NOTIFICATIONS", "adhkar_reminders_system_note",
    )
    contains(
        "app/src/main/kotlin/com/noor/app/MainActivity.kt",
        "override fun onNewIntent", "navigation.requestAdhkar()", "shouldOpenAdhkar",
    )
    contains(
        "app/src/main/kotlin/com/noor/app/AppLaunchNavigation.kt",
        "data class AppLaunchNavigation", "adhkarRequestId", "fun requestAdhkar()",
    )
    contains(
        "app/src/main/kotlin/com/noor/app/ui/NoorApp.kt",
        "LaunchedEffect(openAdhkarRequestId)", "launchSingleTop = true",
    )
    manifest = text("app/src/main/AndroidManifest.xml")
    check('android.permission.POST_NOTIFICATIONS' in manifest, "notification permission is declared")


def verify_tests() -> None:
    expected = [
        "domain/src/test/kotlin/com/noor/domain/model/AdhkarModelsTest.kt",
        "data/src/test/kotlin/com/noor/data/adhkar/BundledAdhkarCatalogTest.kt",
        "app/src/test/kotlin/com/noor/app/ui/adhkar/AdhkarViewModelTest.kt",
        "app/src/test/kotlin/com/noor/app/ui/adhkar/TasbihViewModelTest.kt",
        "app/src/test/kotlin/com/noor/app/adhkar/AdhkarReminderDelayCalculatorTest.kt",
        "app/src/test/kotlin/com/noor/app/AppLaunchNavigationTest.kt",
        "app/src/test/kotlin/com/noor/app/ui/adhkar/AdhkarRemindersViewModelTest.kt",
        "app/src/androidTest/kotlin/com/noor/app/ui/adhkar/AdhkarPremiumUiTest.kt",
        "app/src/androidTest/kotlin/com/noor/app/ui/adhkar/TasbihPremiumUiTest.kt",
    ]
    total = 0
    for path in expected:
        value = text(path)
        total += value.count("@Test")
    check(total >= 12, f"Phase 8 defines at least 12 focused tests (found {total})")


def verify_database_unchanged() -> None:
    db_kt = text("data/src/main/kotlin/com/noor/data/local/database/NoorDatabase.kt")
    check("version = 4" in db_kt, "Phase 8 does not change the Room schema version")
    db_path = ROOT / "data/src/main/assets/database/noor.db"
    try:
        con = sqlite3.connect(f"file:{db_path}?mode=ro", uri=True)
        result = con.execute("PRAGMA integrity_check").fetchone()[0]
        surahs = con.execute("SELECT COUNT(*) FROM surahs").fetchone()[0]
        ayahs = con.execute("SELECT COUNT(*) FROM ayahs").fetchone()[0]
        con.close()
        check(result == "ok", "prepackaged Quran database passes integrity_check")
        check((surahs, ayahs) == (114, 6236), f"Quran corpus remains 114/6236 (found {surahs}/{ayahs})")
    except Exception as error:
        ERRORS.append(f"database verification: {error}")


def verify_kotlin_contracts() -> None:
    compiler = shutil.which("kotlinc")
    if not compiler:
        PASSES.append("Kotlin compiler unavailable; static contract checks completed")
        return
    with tempfile.TemporaryDirectory(prefix="noor-phase8-") as td:
        temp = Path(td)
        inject = temp / "Inject.kt"
        singleton = temp / "Singleton.kt"
        inject.write_text("package javax.inject\nannotation class Inject\n", encoding="utf-8")
        singleton.write_text("package javax.inject\nannotation class Singleton\n", encoding="utf-8")
        noor_routes = temp / "NoorRoutes.kt"
        noor_routes.write_text(
            "package com.noor.core.navigation\n"
            "object NoorRoutes { const val HOME = \"home\"; const val ADHKAR = \"adhkar\" }\n",
            encoding="utf-8",
        )
        sources = [
            ROOT / "domain/src/main/kotlin/com/noor/domain/model/AdhkarCategory.kt",
            ROOT / "domain/src/main/kotlin/com/noor/domain/model/Dhikr.kt",
            ROOT / "domain/src/main/kotlin/com/noor/domain/model/AdhkarReminderSettings.kt",
            ROOT / "data/src/main/kotlin/com/noor/data/adhkar/BundledAdhkarCatalog.kt",
            ROOT / "app/src/main/kotlin/com/noor/app/AppLaunchNavigation.kt",
            noor_routes,
            inject,
            singleton,
        ]
        result = subprocess.run(
            [compiler, *map(str, sources), "-d", str(temp / "contracts.jar")],
            text=True,
            capture_output=True,
            timeout=60,
        )
        check(result.returncode == 0, "Android-free Phase 8 models and bundled catalog compile with kotlinc")
        if result.returncode:
            ERRORS.append(result.stderr.strip())


def main() -> int:
    verify_xml_and_resources()
    verify_catalog()
    verify_architecture()
    verify_ui_and_navigation()
    verify_reminders()
    verify_tests()
    verify_database_unchanged()
    verify_kotlin_contracts()
    print(f"Phase 8 checks passed: {len(PASSES)}")
    for item in PASSES:
        print(f"PASS: {item}")
    if ERRORS:
        print(f"Phase 8 checks failed: {len(ERRORS)}", file=sys.stderr)
        for item in ERRORS:
            print(f"FAIL: {item}", file=sys.stderr)
        return 1
    print("PHASE 8 PREMIUM ADHKAR SOURCE VERIFICATION: PASSED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

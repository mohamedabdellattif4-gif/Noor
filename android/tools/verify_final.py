#!/usr/bin/env python3
"""Repository-level final integrity, architecture, security, data and release checks for Noor."""
from __future__ import annotations

import hashlib
import json
import re
import shutil
import sqlite3
import statistics
import subprocess
import sys
import tempfile
import time
import tomllib
import unicodedata
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ERRORS: list[str] = []
NOTES: list[str] = []


def require(condition: bool, message: str) -> None:
    if not condition:
        ERRORS.append(message)


def note(message: str) -> None:
    NOTES.append(message)


def text(path: str | Path) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def all_files(pattern: str) -> list[Path]:
    return sorted(ROOT.rglob(pattern))


def normalize_arabic(value: str) -> str:
    value = unicodedata.normalize("NFKD", value)
    replacements = str.maketrans({
        "أ": "ا", "إ": "ا", "آ": "ا", "ٱ": "ا", "ى": "ي",
        "ؤ": "و", "ئ": "ي", "ة": "ه", "ـ": " ",
    })
    value = value.translate(replacements)
    chars = []
    for char in value:
        category = unicodedata.category(char)
        if category.startswith("M"):
            continue
        chars.append(char if (char.isalnum() or char.isspace()) else " ")
    return re.sub(r"\s+", " ", "".join(chars)).strip()


def verify_structure() -> None:
    settings = text("settings.gradle.kts")
    expected_modules = {
        ":app", ":baseline-profile", ":core:common", ":core:designsystem",
        ":core:navigation", ":core:media", ":domain", ":data",
    }
    found = set(re.findall(r'include\("([^"]+)"\)', settings))
    require(found == expected_modules, f"Unexpected module set: {sorted(found)}")

    required_paths = [
        "app/src/main/kotlin/com/noor/app/MainActivity.kt",
        "app/src/main/kotlin/com/noor/app/NoorApplication.kt",
        "app/src/main/kotlin/com/noor/app/navigation/NoorNavHost.kt",
        "app/src/test/kotlin/com/noor/app/ui/home/HomeViewModelTest.kt",
        "app/src/test/kotlin/com/noor/app/ui/bookmarks/BookmarksViewModelTest.kt",
        "app/src/test/kotlin/com/noor/app/ui/reader/ReaderViewModelTest.kt",
        "core/common/src/main/kotlin/com/noor/core/common/text/ArabicNormalizer.kt",
        "core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/NoorTheme.kt",
        "core/media/src/main/kotlin/com/noor/core/media/QuranPlaybackService.kt",
        "core/media/src/main/kotlin/com/noor/core/media/QuranAudioPlayer.kt",
        "core/media/src/main/kotlin/com/noor/core/media/QuranAudioModule.kt",
        "domain/src/main/kotlin/com/noor/domain/repository/QuranRepository.kt",
        "data/src/main/kotlin/com/noor/data/local/database/NoorDatabase.kt",
        "data/src/main/assets/database/noor.db",
        "baseline-profile/build.gradle.kts",
        "baseline-profile/src/main/kotlin/com/noor/baselineprofile/BaselineProfileGenerator.kt",
        "baseline-profile/src/main/kotlin/com/noor/baselineprofile/StartupBenchmark.kt",
        "baseline-profile/src/main/kotlin/com/noor/baselineprofile/ReaderScrollBenchmark.kt",
        "baseline-profile/src/main/kotlin/com/noor/baselineprofile/NoorJourneys.kt",
        "app/src/benchmark/AndroidManifest.xml",
        "app/src/nonMinifiedRelease/AndroidManifest.xml",
        "app/src/main/baseline-prof.txt",
        "app/src/main/res/xml/locales_config.xml",
        "tools/update_baseline_profile.py",
        "tools/verify_build_foundation.py",
        "tools/verify_android_artifact.py",
        "tools/release_preflight.py",
        "tools/verify_hardening.py",
        "tools/release_gate.sh",
        ".github/workflows/android-ci.yml",
        ".github/workflows/baseline-profile.yml",
        ".github/workflows/dependency-review.yml",
        ".github/workflows/dependency-submission.yml",
        ".github/workflows/codeql.yml",
        ".github/dependabot.yml",
        "SECURITY.md",
        "CONTRIBUTING.md",
        "docs/PRODUCTION_HARDENING.md",
        "docs/PRODUCTION_HARDENING_VERIFICATION.md",
    ]
    for relative in required_paths:
        require((ROOT / relative).is_file(), f"Missing required file: {relative}")

    reader_view_model = text("app/src/main/kotlin/com/noor/app/ui/reader/ReaderViewModel.kt")
    require("QuranAudioPlayer" in reader_view_model and "QuranAudioController" not in reader_view_model,
            "ReaderViewModel must depend on the testable audio abstraction")
    controller = text("core/media/src/main/kotlin/com/noor/core/media/QuranAudioController.kt")
    require(": QuranAudioPlayer" in controller, "Media3 controller must implement QuranAudioPlayer")
    media_module = text("core/media/src/main/kotlin/com/noor/core/media/QuranAudioModule.kt")
    require("bindQuranAudioPlayer" in media_module and "@Binds" in media_module,
            "Hilt binding for QuranAudioPlayer is missing")
    require("@Singleton" in media_module and "@Singleton" not in controller,
            "QuranAudioPlayer must have exactly one Hilt singleton scope at the interface binding")


def verify_toml_and_gradle() -> None:
    catalog_path = ROOT / "gradle/libs.versions.toml"
    catalog = tomllib.loads(catalog_path.read_text(encoding="utf-8"))
    versions = catalog.get("versions", {})
    required = {
        "agp", "kotlin", "ksp", "hilt", "composeBom", "room",
        "datastore", "work", "media3", "benchmark", "profileInstaller", "uiAutomator",
    }
    require(required <= set(versions), "Version Catalog misses required versions")
    require(versions.get("work") == "2.11.2", "WorkManager version is not pinned to 2.11.2")
    require(versions.get("media3") == "1.10.1", "Media3 version is not pinned to 1.10.1")
    require(versions.get("datastore") == "1.2.1", "DataStore version is not pinned to 1.2.1")
    require(versions.get("room") == "2.8.4", "Room version is not pinned to 2.8.4")
    require(versions.get("benchmark") == "1.4.1", "Benchmark version is not pinned to 1.4.1")
    require(versions.get("profileInstaller") == "1.4.1", "ProfileInstaller version is not pinned to 1.4.1")
    require(versions.get("uiAutomator") == "2.4.0", "UiAutomator version is not pinned to 2.4.0")
    require(versions.get("kotlin") == "2.3.10", "Kotlin is not pinned to the AGP 9.2 built-in Kotlin version 2.3.10")
    require(versions.get("ksp") == "2.3.10", "KSP is not pinned to 2.3.10")
    require(versions.get("composeBom") == "2026.06.01", "Compose BOM is not pinned to 2026.06.01")
    require(versions.get("coreKtx") == "1.19.0", "Core KTX is not pinned to 1.19.0")
    require(versions.get("navigationCompose") == "2.9.8", "Navigation Compose is not pinned to 2.9.8")

    app_gradle = text("app/build.gradle.kts")
    for fragment in [
        "compileSdk = 37", "targetSdk = 36", "minSdk = 23",
        "versionCode = noorVersionCode", "versionName = noorVersionName",
        "isMinifyEnabled = true", "isShrinkResources = true",
        "buildConfig = true", "JavaVersion.VERSION_17",
        "abortOnError = true", "checkReleaseBuilds = true", "warningsAsErrors = true",
        "implementation(libs.androidx.profileinstaller)",
        'create("benchmark")', 'create("nonMinifiedRelease")',
        'initWith(getByName("release"))',
        'signingConfig = signingConfigs.getByName("debug")',
        'matchingFallbacks += listOf("release")',
        "isMinifyEnabled = false", "isShrinkResources = false",
        "NOOR_KEYSTORE_PATH", "animationsDisabled = true",
        "Version code must be a positive integer",
        "Version name must use semantic versioning",
        "Release keystore must be stored outside the source repository",
        "configuredStore = file(requireNotNull(signingValues[\"storeFile\"])).canonicalFile",
        "rootDir.canonicalFile.toPath()",
    ]:
        require(fragment in app_gradle, f"App Gradle missing release requirement: {fragment}")
    require("androidx.baselineprofile" not in text("gradle/libs.versions.toml"),
            "Stable Baseline Profile plugin is incompatible with AGP 9 new DSL and must not be applied")
    require("baselineProfile(project" not in app_gradle,
            "App must use the checked-in profile without the incompatible consumer plugin")
    baseline_gradle = text("baseline-profile/build.gradle.kts")
    for fragment in [
        "alias(libs.plugins.benchmark)", 'targetProjectPath = ":app"',
        'create("benchmark")', 'create("nonMinifiedRelease")',
        'matchingFallbacks += listOf("release")',
        'systemImageSource = "aosp"', "androidx.benchmark.macro.junit4",
        "androidx.test.uiautomator",
    ]:
        require(fragment in baseline_gradle, f"Benchmark configuration missing: {fragment}")
    require("libs.plugins.baseline.profile" not in baseline_gradle,
            "Benchmark module must not apply the incompatible Baseline Profile Gradle plugin")
    root_gradle = text("build.gradle.kts")
    require("alias(libs.plugins.benchmark) apply false" in root_gradle,
            "Stable Benchmark Gradle plugin must be registered at the root")
    require("android.newDsl=false" not in text("gradle.properties"),
            "Legacy AGP DSL must not be re-enabled for benchmark tooling")
    generator = text("baseline-profile/src/main/kotlin/com/noor/baselineprofile/BaselineProfileGenerator.kt")
    require('packageName = NOOR_PACKAGE_NAME' in generator, "Baseline Profile producer must target the app")
    require("includeInStartupProfile = true" in generator, "Startup profile collection is missing")
    journeys = text("baseline-profile/src/main/kotlin/com/noor/baselineprofile/NoorJourneys.kt")
    require("Until.findObject" in journeys and "?: error" in journeys,
            "Critical benchmark journey must fail when the target UI never appears")
    require("openAlFatiha()" in generator and "scrollReader(count = 3)" in generator,
            "Baseline Profile must cover startup, reader navigation, and scrolling")
    home_route = text("app/src/main/kotlin/com/noor/app/ui/home/HomeRoute.kt")
    require("ReportDrawnWhen { !uiState.isLoading }" in home_route, "TTFD reporting is not tied to Home readiness")

    profile_lines = [
        line.strip() for line in text("app/src/main/baseline-prof.txt").splitlines()
        if line.strip() and not line.lstrip().startswith("#")
    ]
    require(profile_lines, "Source Baseline Profile is empty")
    require(len(profile_lines) == len(set(profile_lines)), "Source Baseline Profile contains duplicate rules")
    require(any("com/noor/" in line for line in profile_lines), "Source Baseline Profile has no Noor rules")
    require(len(profile_lines) <= 10_000, "Source Baseline Profile is unexpectedly broad")

    wrapper_checksum = "55243ef57851f12b070ad14f7f5bb8302daceeebc5bce5ece5fa6edb23e1145c"
    for launcher in ["gradlew", "gradlew.bat", "bootstrap-gradle-wrapper.sh", "bootstrap-gradle-wrapper.ps1"]:
        require(wrapper_checksum in text(launcher), f"{launcher} does not pin the Gradle Wrapper checksum")

    android_ci = text(".github/workflows/android-ci.yml")
    require("verify_build_foundation.py --allow-missing-wrapper-jar" in android_ci,
            "CI source verification does not run the build-foundation gate")
    require("verify_build_foundation.py --require-wrapper-jar" in android_ci,
            "CI does not verify the installed Gradle Wrapper JAR")
    require(":app:assembleBenchmark" in android_ci, "CI does not compile the benchmark app variant")
    require(":app:assembleNonMinifiedRelease" in android_ci,
            "CI does not compile the non-minified profile-generation app variant")
    require(":baseline-profile:assembleBenchmark" in android_ci, "CI does not compile the benchmark test module")
    require(":baseline-profile:assembleNonMinifiedRelease" in android_ci,
            "CI does not compile the profile-generation test variant")
    profile_ci = text(".github/workflows/baseline-profile.yml")
    require(":baseline-profile:pixel6Api35NonMinifiedReleaseAndroidTest" in profile_ci,
            "Baseline Profile workflow does not run the managed-device producer")
    require("tools/update_baseline_profile.py" in profile_ci,
            "Baseline Profile workflow does not validate and install generated rules")
    codeql = text(".github/workflows/codeql.yml")
    require("github/codeql-action/init@v4" in codeql and "languages: java-kotlin" in codeql,
            "CodeQL Java/Kotlin analysis is not configured")
    dependency_submission = text(".github/workflows/dependency-submission.yml")
    require("gradle/actions/dependency-submission@v6" in dependency_submission,
            "Gradle dependency graph submission is not configured")
    release_gate = text("tools/release_gate.sh")
    require("release_preflight.py --require-signing --require-version" in release_gate,
            "Release gate does not require explicit versioning and signing")
    require("--require-signature" in release_gate,
            "Release gate does not verify the built artifact signature")

    properties = text("gradle.properties")
    for fragment in [
        "org.gradle.caching=true", "org.gradle.configuration-cache=true",
        "android.nonTransitiveRClass=true", "android.useAndroidX=true",
    ]:
        require(fragment in properties, f"Gradle property missing: {fragment}")


def verify_xml_and_resources() -> None:
    xml_files = all_files("*.xml")
    for path in xml_files:
        try:
            ET.parse(path)
        except ET.ParseError as exc:
            ERRORS.append(f"Invalid XML {path.relative_to(ROOT)}: {exc}")

    android_ns = "{http://schemas.android.com/apk/res/android}"
    for base in ["app/src/main/res", "core/designsystem/src/main/res", "core/media/src/main/res"]:
        default_path = ROOT / base / "values/strings.xml"
        arabic_path = ROOT / base / "values-ar/strings.xml"
        if not default_path.exists():
            continue
        default_keys = {n.attrib["name"] for n in ET.parse(default_path).getroot() if n.tag == "string"}
        arabic_keys = {n.attrib["name"] for n in ET.parse(arabic_path).getroot() if n.tag == "string"}
        require(default_keys == arabic_keys, f"String key mismatch in {base}: {default_keys ^ arabic_keys}")

    manifest = ET.parse(ROOT / "app/src/main/AndroidManifest.xml").getroot()
    permissions = {node.attrib[f"{android_ns}name"] for node in manifest.findall("uses-permission")}
    allowed = {
        "android.permission.INTERNET",
        "android.permission.FOREGROUND_SERVICE",
        "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK",
        "android.permission.WAKE_LOCK",
        "android.permission.POST_NOTIFICATIONS",
    }
    require(permissions == allowed, f"Unexpected manifest permissions: {sorted(permissions)}")
    application = manifest.find("application")
    require(application is not None, "Application node missing")
    if application is not None:
        require(application.attrib.get(f"{android_ns}allowBackup") == "false", "Backup must be disabled")
        require(application.attrib.get(f"{android_ns}usesCleartextTraffic") == "false", "Cleartext traffic must be disabled")
        require(application.attrib.get(f"{android_ns}supportsRtl") == "true", "RTL support must be enabled")
        require(application.attrib.get(f"{android_ns}icon") == "@mipmap/ic_launcher", "Adaptive launcher icon not configured")
        require(application.attrib.get(f"{android_ns}localeConfig") == "@xml/locales_config",
                "Per-app language localeConfig is not configured")
        exported = []
        for child in application:
            value = child.attrib.get(f"{android_ns}exported")
            if value == "true":
                exported.append((child.tag, child.attrib.get(f"{android_ns}name")))
        require(exported == [("activity", ".MainActivity")], f"Unexpected exported components: {exported}")
        require(application.find("profileable") is None,
                "Production manifest must not expose profileable shell access")

    for variant in ("benchmark", "nonMinifiedRelease"):
        variant_manifest = ET.parse(ROOT / f"app/src/{variant}/AndroidManifest.xml").getroot()
        variant_application = variant_manifest.find("application")
        require(variant_application is not None, f"{variant} manifest application node missing")
        if variant_application is not None:
            profileable = variant_application.find("profileable")
            require(profileable is not None, f"{variant} variant must be profileable")
            if profileable is not None:
                require(profileable.attrib.get(f"{android_ns}shell") == "true",
                        f"{variant} profileable shell access is missing")

    locale_root = ET.parse(ROOT / "app/src/main/res/xml/locales_config.xml").getroot()
    locales = {node.attrib.get(f"{android_ns}name") for node in locale_root.findall("locale")}
    require(locales == {"ar", "en"}, f"Unexpected supported locales: {sorted(locales)}")

    media_manifest = ET.parse(ROOT / "core/media/src/main/AndroidManifest.xml").getroot()
    service = media_manifest.find("application/service")
    require(service is not None, "MediaSessionService declaration missing")
    if service is not None:
        require(service.attrib.get(f"{android_ns}exported") == "true", "Playback service must be exported for trusted system media controllers")
        require(service.attrib.get(f"{android_ns}foregroundServiceType") == "mediaPlayback", "Playback FGS type missing")
        actions = {
            action.attrib.get(f"{android_ns}name")
            for action in service.findall("intent-filter/action")
        }
        require(
            actions == {"androidx.media3.session.MediaSessionService"},
            f"Unexpected playback service actions: {sorted(actions)}",
        )

    for path in [
        "app/src/main/res/mipmap-anydpi/ic_launcher.xml",
        "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
        "app/src/main/res/mipmap-anydpi-v33/ic_launcher.xml",
        "app/src/main/res/drawable/ic_noor_monochrome.xml",
        "app/src/main/res/xml/network_security_config.xml",
    ]:
        require((ROOT / path).is_file(), f"Release resource missing: {path}")
    adaptive33 = text("app/src/main/res/mipmap-anydpi-v33/ic_launcher.xml")
    require("<monochrome" in adaptive33, "Android 13 monochrome icon missing")


def verify_architecture() -> None:
    forbidden_by_root = {
        ROOT / "domain": ("import android.", "import androidx.", "import com.noor.data", "import com.noor.app"),
        ROOT / "core/common": ("import android.", "import androidx.", "import com.noor.data", "import com.noor.app"),
        ROOT / "data": ("import com.noor.app", "import com.noor.core.media"),
        ROOT / "core/media": ("import com.noor.data", "import com.noor.app"),
        ROOT / "core/designsystem": ("import com.noor.data", "import com.noor.app"),
    }
    for module_root, forbidden in forbidden_by_root.items():
        for path in module_root.rglob("*.kt"):
            content = path.read_text(encoding="utf-8")
            for fragment in forbidden:
                require(fragment not in content, f"Architecture violation in {path.relative_to(ROOT)}: {fragment}")

    domain_gradle = text("domain/build.gradle.kts")
    require("com.android" not in domain_gradle and "androidx." not in domain_gradle, "Domain module must remain Android-free")
    require("project(\":data\")" not in domain_gradle, "Domain must not depend on data")

    data_gradle = text("data/build.gradle.kts")
    require("project(\":domain\")" in data_gradle, "Data must implement domain contracts")
    require("fallbackToDestructiveMigration" not in "".join(p.read_text(encoding="utf-8") for p in (ROOT / "data").rglob("*.kt")), "Destructive migration is forbidden")
    migrations = text("data/src/main/kotlin/com/noor/data/local/database/NoorDatabaseMigrations.kt")
    require(
        "createAyahFtsSyncTriggers(database)" in migrations,
        "The 2 -> 3 migration must recreate FTS synchronization triggers after Room drops them",
    )


def verify_code_hygiene() -> None:
    kotlin_files = all_files("*.kt") + all_files("*.kts")
    secret_pattern = re.compile(r'(?i)(api[_-]?key|client[_-]?secret|private[_-]?key|access[_-]?token)\s*[=:]\s*["\'][^"\']+["\']')
    for path in kotlin_files:
        content = path.read_text(encoding="utf-8")
        relative = path.relative_to(ROOT)
        if "/build/" in str(relative):
            continue
        require("TODO(" not in content and "FIXME" not in content, f"Unresolved marker in {relative}")
        require(not re.search(r"^import .*\*$", content, flags=re.MULTILINE), f"Wildcard import in {relative}")
        require("GlobalScope" not in content, f"GlobalScope usage in {relative}")
        require("printStackTrace" not in content and "System.out" not in content and "println(" not in content, f"Direct console logging in {relative}")
        require(not secret_pattern.search(content), f"Possible hard-coded secret in {relative}")
        if "src/test" not in str(relative) and "src/androidTest" not in str(relative):
            require("runBlocking" not in content, f"Blocking coroutine usage in production: {relative}")
            require("allowMainThreadQueries" not in content, f"Main-thread Room access in production: {relative}")
        for match in re.finditer(r'http://', content):
            require(False, f"Cleartext URL in Kotlin file {relative}:{content.count(chr(10), 0, match.start()) + 1}")

    # Display strings must be resource-backed in Compose presentation code.
    for path in (ROOT / "app/src/main/kotlin").rglob("*.kt"):
        content = path.read_text(encoding="utf-8")
        require(not re.search(r'\bText\(\s*"', content), f"Hardcoded Compose text in {path.relative_to(ROOT)}")

    # Ensure cancellation is not swallowed by broad catches in network/search/worker boundaries.
    for relative in [
        "app/src/main/kotlin/com/noor/app/ui/search/SearchViewModel.kt",
        "app/src/main/kotlin/com/noor/app/ui/tafsir/TafsirViewModel.kt",
        "data/src/main/kotlin/com/noor/data/work/ContentMaintenanceWorker.kt",
    ]:
        content = text(relative)
        require("CancellationException" in content, f"Coroutine cancellation handling missing: {relative}")



def verify_migration_fts_sync_contract() -> None:
    migrations = text("data/src/main/kotlin/com/noor/data/local/database/NoorDatabaseMigrations.kt")
    expected_names = {
        "room_fts_content_sync_ayahs_fts_AFTER_INSERT",
        "room_fts_content_sync_ayahs_fts_BEFORE_DELETE",
        "room_fts_content_sync_ayahs_fts_BEFORE_UPDATE",
        "room_fts_content_sync_ayahs_fts_AFTER_UPDATE",
    }
    trigger_sql = re.findall(
        r'"""\s*(CREATE TRIGGER IF NOT EXISTS\s+room_fts_content_sync_ayahs_fts_[A-Z_]+.*?END)\s*"""\.trimIndent\(\)',
        migrations,
        flags=re.DOTALL,
    )
    found_names = {
        re.search(r"CREATE TRIGGER IF NOT EXISTS\s+(\S+)", statement).group(1)
        for statement in trigger_sql
    }
    require(found_names == expected_names, f"Migration FTS trigger set mismatch: {sorted(found_names)}")
    if found_names != expected_names:
        return

    connection = sqlite3.connect(":memory:")
    try:
        connection.executescript(
            """
            CREATE TABLE ayahs (
                id INTEGER NOT NULL PRIMARY KEY,
                text_simple TEXT NOT NULL
            );
            CREATE VIRTUAL TABLE ayahs_fts USING FTS4(text_simple, content='ayahs');
            """
        )
        for statement in trigger_sql:
            connection.execute(statement)

        connection.execute("INSERT INTO ayahs(id, text_simple) VALUES(1, 'الرحمن الرحيم')")
        require(
            connection.execute(
                "SELECT COUNT(*) FROM ayahs_fts WHERE ayahs_fts MATCH ?",
                ('"الرحمن*"',),
            ).fetchone()[0] == 1,
            "Migrated FTS triggers do not synchronize inserts",
        )
        connection.execute("UPDATE ayahs SET text_simple='مالك يوم الدين' WHERE id=1")
        require(
            connection.execute(
                "SELECT COUNT(*) FROM ayahs_fts WHERE ayahs_fts MATCH ?",
                ('"الرحمن*"',),
            ).fetchone()[0] == 0,
            "Migrated FTS triggers leave stale terms after updates",
        )
        require(
            connection.execute(
                "SELECT COUNT(*) FROM ayahs_fts WHERE ayahs_fts MATCH ?",
                ('"مالك*"',),
            ).fetchone()[0] == 1,
            "Migrated FTS triggers do not synchronize updated terms",
        )
        connection.execute("DELETE FROM ayahs WHERE id=1")
        require(
            connection.execute("SELECT COUNT(*) FROM ayahs_fts").fetchone()[0] == 0,
            "Migrated FTS triggers do not synchronize deletes",
        )
    finally:
        connection.close()


def verify_database() -> None:
    db_path = ROOT / "data/src/main/assets/database/noor.db"
    require(db_path.stat().st_size < 10 * 1024 * 1024, "Prepackaged database exceeds 10 MiB")
    con = sqlite3.connect(f"file:{db_path}?mode=ro", uri=True)
    try:
        require(con.execute("PRAGMA integrity_check").fetchone()[0] == "ok", "SQLite integrity_check failed")
        require(not con.execute("PRAGMA foreign_key_check").fetchall(), "Foreign-key violations found")
        require(con.execute("PRAGMA user_version").fetchone()[0] == 4, "Unexpected database schema version")
        tables = {row[0] for row in con.execute("SELECT name FROM sqlite_master WHERE type IN ('table','view')")}
        for table in ["surahs", "ayahs", "ayahs_fts", "bookmarks", "tafsirs", "app_metadata"]:
            require(table in tables, f"Database table missing: {table}")
        require(con.execute("SELECT COUNT(*) FROM surahs").fetchone()[0] == 114, "Surah count is not 114")
        require(con.execute("SELECT COUNT(*) FROM ayahs").fetchone()[0] == 6236, "Ayah count is not 6236")
        require(con.execute("SELECT COUNT(*) FROM ayahs_fts").fetchone()[0] == 6236, "FTS row count mismatch")
        trigger_names = {row[0] for row in con.execute("SELECT name FROM sqlite_master WHERE type='trigger'")}
        expected_triggers = {
            "room_fts_content_sync_ayahs_fts_AFTER_INSERT",
            "room_fts_content_sync_ayahs_fts_BEFORE_DELETE",
            "room_fts_content_sync_ayahs_fts_BEFORE_UPDATE",
            "room_fts_content_sync_ayahs_fts_AFTER_UPDATE",
        }
        require(trigger_names == expected_triggers, f"Unexpected FTS trigger set: {sorted(trigger_names)}")
        require(con.execute("SELECT MIN(page_number), MAX(page_number) FROM ayahs").fetchone() == (1, 604), "Page range mismatch")
        require(con.execute("SELECT MIN(juz_number), MAX(juz_number) FROM ayahs").fetchone() == (1, 30), "Juz range mismatch")
        require(con.execute("SELECT MIN(hizb_quarter), MAX(hizb_quarter) FROM ayahs").fetchone() == (1, 240), "Rub el hizb range mismatch")
        require(con.execute("SELECT COUNT(*) FROM ayahs WHERE trim(text_uthmani)='' OR trim(text_simple)='' ").fetchone()[0] == 0, "Blank Quran text found")
        require(con.execute("SELECT COUNT(*) FROM ayahs_fts WHERE ayahs_fts MATCH ?", ("\"الرحمن*\" AND \"الرحيم*\"",)).fetchone()[0] > 0, "Arabic FTS query failed")

        tanzil_root = ET.parse(ROOT / "tools/vendor/tanzil-quran-uthmani.xml").getroot()
        expected = [aya.attrib["text"] for sura in tanzil_root.findall("sura") for aya in sura.findall("aya")]
        actual = [row[0] for row in con.execute("SELECT text_uthmani FROM ayahs ORDER BY id")]
        require(actual == expected, "Displayed Quran text differs from vendored Tanzil source")
        normalized_actual = [row[0] for row in con.execute("SELECT text_simple FROM ayahs ORDER BY id")]
        require(normalized_actual == [normalize_arabic(item) for item in expected], "Search normalization differs from generator contract")

        meta = dict(con.execute("SELECT key, value FROM app_metadata"))
        for key in ["corpus_source", "chapter_metadata_source", "pagination_source", "database_schema_version"]:
            require(key in meta, f"Attribution metadata missing: {key}")

        queries = ["الله*", "الرحمن*", "موسي*", "الذين* امنوا*", "الجنه*", "النار*"]
        timings: list[float] = []
        for _ in range(30):
            for query in queries:
                start = time.perf_counter()
                con.execute("SELECT rowid FROM ayahs_fts WHERE ayahs_fts MATCH ? LIMIT 100", (query,)).fetchall()
                timings.append((time.perf_counter() - start) * 1000)
        p95 = statistics.quantiles(timings, n=20)[18]
        note(f"SQLite FTS host benchmark p95: {p95:.3f} ms")
        require(p95 < 50.0, f"FTS host benchmark is unexpectedly slow: {p95:.3f} ms")
    finally:
        con.close()

    # Generator reproducibility: compare the complete logical dataset, not SQLite page bytes.
    with tempfile.TemporaryDirectory() as tmp:
        regenerated = Path(tmp) / "noor.db"
        subprocess.run(
            [sys.executable, str(ROOT / "tools/generate_quran_database.py"), "--output", str(regenerated)],
            cwd=ROOT,
            check=True,
            stdout=subprocess.DEVNULL,
        )
        def logical_digest(path: Path) -> str:
            connection = sqlite3.connect(path)
            digest = hashlib.sha256()
            try:
                for table, columns, order in [
                    ("surahs", "*", "number"),
                    ("ayahs", "*", "id"),
                    ("app_metadata", "*", "key"),
                ]:
                    for row in connection.execute(f"SELECT {columns} FROM {table} ORDER BY {order}"):
                        digest.update(json.dumps(row, ensure_ascii=False, separators=(",", ":")).encode("utf-8"))
                        digest.update(b"\n")
            finally:
                connection.close()
            return digest.hexdigest()
        require(logical_digest(db_path) == logical_digest(regenerated), "Database generator is not logically reproducible")
    require(not (ROOT / "tools/vendor/quran.json").exists(), "Redundant quran-json source must not be bundled")
    require(not (ROOT / "app/src/main/assets/licenses/QURAN_JSON_CC_BY_SA_4_0.txt").exists(), "Unused quran-json license asset remains")


def verify_security_and_network() -> None:
    network = text("app/src/main/res/xml/network_security_config.xml")
    require('cleartextTrafficPermitted="false"' in network, "Network security config allows cleartext")
    require('src="system"' in network, "Network security config must rely on system trust anchors")

    remote = text("data/src/main/kotlin/com/noor/data/remote/QuranEncTafsirDataSource.kt")
    for fragment in [
        'https://quranenc.com/', "HttpsURLConnection", "instanceFollowRedirects = false",
        "connectTimeout", "readTimeout", "MAX_RESPONSE_BYTES", "ByteArrayOutputStream", 'Accept", "application/json',
    ]:
        require(fragment in remote, f"Tafsir network hardening missing: {fragment}")
    require("builder.length > MAX_RESPONSE_BYTES" not in remote, "Response cap must be byte-based, not character-based")
    audio = text("core/media/src/main/kotlin/com/noor/core/media/QuranAudioUrlFactory.kt")
    require('https://everyayah.com/' in audio, "Audio URL must use HTTPS")
    require("require(ayah.surahNumber in 1..114" in audio, "Audio URL input validation missing")

    manifest_text = text("app/src/main/AndroidManifest.xml")
    dangerous = ["READ_CONTACTS", "ACCESS_FINE_LOCATION", "CAMERA", "RECORD_AUDIO", "READ_MEDIA", "READ_EXTERNAL_STORAGE"]
    for permission in dangerous:
        require(permission not in manifest_text, f"Unnecessary sensitive permission declared: {permission}")


def verify_features_and_tests() -> None:
    routes = text("core/navigation/src/main/kotlin/com/noor/core/navigation/NoorRoute.kt")
    for route in ["HOME", "SEARCH", "BOOKMARKS", "SETTINGS", "LEGAL", "READER", "TAFSIR"]:
        require(f"const val {route}" in routes, f"Navigation route missing: {route}")
    feature_paths = [
        "app/src/main/kotlin/com/noor/app/ui/home/HomeRoute.kt",
        "app/src/main/kotlin/com/noor/app/ui/reader/ReaderRoute.kt",
        "app/src/main/kotlin/com/noor/app/ui/search/SearchRoute.kt",
        "app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksRoute.kt",
        "app/src/main/kotlin/com/noor/app/ui/tafsir/TafsirRoute.kt",
        "app/src/main/kotlin/com/noor/app/ui/settings/SettingsRoute.kt",
    ]
    for path in feature_paths:
        require((ROOT / path).is_file(), f"Feature implementation missing: {path}")
    reader = text("app/src/main/kotlin/com/noor/app/ui/reader/ReaderRoute.kt")
    require(
        all(fragment in reader for fragment in [
            "snapshotFlow",
            "layoutInfo.visibleItemsInfo",
            "READER_HEADER_ITEMS",
            "LAST_READ_DEBOUNCE_MS",
        ]),
        "Reader must persist the settled visible ayah rather than a header-relative list index",
    )
    search = text("app/src/main/kotlin/com/noor/app/ui/search/SearchViewModel.kt")
    require("MAX_QUERY_LENGTH" in search, "Search input complexity is not bounded")
    require("hasSearched = true" in search and "hasSearched = false" in search, "Search completion state is incomplete")
    tafsir = text("app/src/main/kotlin/com/noor/app/ui/tafsir/TafsirViewModel.kt")
    require("loadJob?.cancel()" in tafsir, "Repeated tafsir retries must cancel the prior request")
    bookmarks = text("app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksViewModel.kt")
    require("BookmarksUiState" in bookmarks and "fun retry()" in bookmarks, "Bookmark loading/error state is incomplete")
    for path in feature_paths:
        content = text(path)
        require("NoorContentContainer" in content, f"Adaptive content width missing: {path}")
    tests = [
        "core/common/src/test/kotlin/com/noor/core/common/text/ArabicNormalizerTest.kt",
        "domain/src/test/kotlin/com/noor/domain/model/QuranModelsTest.kt",
        "core/media/src/test/kotlin/com/noor/core/media/QuranAudioUrlFactoryTest.kt",
        "data/src/androidTest/kotlin/com/noor/data/local/database/NoorDatabaseTest.kt",
        "app/src/androidTest/kotlin/com/noor/app/DesignSystemSmokeTest.kt",
        "app/src/test/kotlin/com/noor/app/ui/search/SearchViewModelTest.kt",
        "app/src/test/kotlin/com/noor/app/ui/home/HomeViewModelTest.kt",
        "app/src/test/kotlin/com/noor/app/ui/bookmarks/BookmarksViewModelTest.kt",
        "app/src/test/kotlin/com/noor/app/ui/reader/ReaderViewModelTest.kt",
        "app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksUiState.kt",
    ]
    for path in tests:
        require((ROOT / path).is_file(), f"Required test missing: {path}")


def verify_release_docs() -> None:
    required = [
        "README.md",
        "docs/ARCHITECTURE.md",
        "docs/DATABASE.md",
        "docs/DESIGN_SYSTEM.md",
        "docs/PRIVACY_POLICY.md",
        "docs/DATA_ATTRIBUTIONS.md",
        "docs/ARCHITECTURE_REVIEW.md",
        "docs/CODE_REVIEW.md",
        "docs/SECURITY_REVIEW.md",
        "docs/PERFORMANCE_REVIEW.md",
        "docs/GOOGLE_PLAY_READINESS.md",
        "docs/FINAL_VERIFICATION.md",
        "docs/FINAL_PROJECT_REPORT.md",
        "docs/FINAL_BUILD_FACTS.json",
        "docs/RELEASE.md",
        "app/proguard-rules.pro",
        "app/src/main/baseline-prof.txt",
        "app/src/main/assets/licenses/TANZIL_QURAN_TEXT_LICENSE.txt",
        "app/src/main/assets/licenses/QURAN_META_MIT.txt",
    ]
    for path in required:
        require((ROOT / path).is_file(), f"Release/documentation artifact missing: {path}")
    security_review = text("docs/SECURITY_REVIEW.md")
    require(
        "playback service and startup provider are not exported" not in security_review,
        "Security review contains stale playback-service export guidance",
    )


def verify_text_hygiene() -> None:
    for path in ROOT.rglob("*"):
        if not path.is_file() or path.suffix in {".db", ".jar", ".zip", ".png", ".jpg", ".webp"}:
            continue
        relative = path.relative_to(ROOT)
        # Vendored source and license notices remain byte-for-byte as supplied by their publishers.
        if str(relative).startswith("tools/vendor/") or str(relative).startswith("app/src/main/assets/licenses/"):
            continue
        try:
            raw = path.read_bytes()
            content = raw.decode("utf-8")
        except UnicodeDecodeError:
            continue
        require(b"\r\n" not in raw and b"\r" not in raw, f"Non-LF line endings: {path.relative_to(ROOT)}")
        for number, line in enumerate(content.splitlines(), start=1):
            require(line.rstrip() == line, f"Trailing whitespace: {path.relative_to(ROOT)}:{number}")
        require(not content or content.endswith("\n"), f"Missing final newline: {path.relative_to(ROOT)}")



def verify_pure_kotlin_compilation() -> None:
    """Compile Android-free boundaries with the locally installed Kotlin compiler when available."""
    kotlinc = shutil.which("kotlinc")
    if not kotlinc:
        note("Pure Kotlin JVM 17 compilation skipped: kotlinc unavailable")
        return
    kotlin_home = Path(kotlinc).resolve().parents[1]
    coroutines_jar = kotlin_home / "lib/kotlinx-coroutines-core-jvm.jar"
    if not coroutines_jar.is_file():
        note("Pure Kotlin JVM 17 compilation skipped: local coroutines JAR unavailable")
        return
    sources = sorted((ROOT / "core/common/src/main/kotlin").rglob("*.kt"))
    sources += sorted((ROOT / "domain/src/main/kotlin").rglob("*.kt"))
    sources += [
        ROOT / "core/media/src/main/kotlin/com/noor/core/media/QuranAudioUrlFactory.kt",
        ROOT / "core/media/src/main/kotlin/com/noor/core/media/QuranPlaybackState.kt",
    ]
    with tempfile.TemporaryDirectory() as tmp:
        output = Path(tmp) / "noor-pure.jar"
        result = subprocess.run(
            [
                kotlinc,
                "-Werror",
                "-jvm-target",
                "17",
                *map(str, sources),
                "-classpath",
                str(coroutines_jar),
                "-d",
                str(output),
            ],
            cwd=ROOT,
            capture_output=True,
            text=True,
        )
        require(
            result.returncode == 0,
            "Pure Kotlin JVM 17 compilation failed: " + (result.stderr.strip() or result.stdout.strip()),
        )
        if result.returncode == 0:
            note(f"Pure Kotlin JVM 17 files compiled with warnings as errors: {len(sources)}")

def optional_syntax_scan() -> None:
    try:
        from tree_sitter import Language, Parser
        import tree_sitter_kotlin
    except Exception:
        note("Tree-sitter Kotlin syntax scan skipped: optional package unavailable")
        return
    parser = Parser(Language(tree_sitter_kotlin.language()))
    errors = []
    files = all_files("*.kt") + all_files("*.kts")
    for path in files:
        tree = parser.parse(path.read_bytes())
        if tree.root_node.has_error:
            errors.append(str(path.relative_to(ROOT)))
    require(not errors, f"Kotlin syntax errors detected: {errors}")
    note(f"Tree-sitter Kotlin files parsed: {len(files)}")


def main() -> int:
    checks = [
        verify_structure,
        verify_toml_and_gradle,
        verify_xml_and_resources,
        verify_architecture,
        verify_code_hygiene,
        verify_migration_fts_sync_contract,
        verify_database,
        verify_security_and_network,
        verify_features_and_tests,
        verify_release_docs,
        verify_text_hygiene,
        verify_pure_kotlin_compilation,
        optional_syntax_scan,
    ]
    for check in checks:
        try:
            check()
        except Exception as exc:  # the verifier must report all gates instead of stopping at the first
            ERRORS.append(f"{check.__name__} raised {type(exc).__name__}: {exc}")

    if ERRORS:
        print("FINAL VERIFICATION: FAILED")
        for item in ERRORS:
            print(f"- {item}")
        if NOTES:
            print("Notes:")
            for item in NOTES:
                print(f"- {item}")
        return 1

    print("FINAL VERIFICATION: PASSED")
    for item in NOTES:
        print(f"- {item}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

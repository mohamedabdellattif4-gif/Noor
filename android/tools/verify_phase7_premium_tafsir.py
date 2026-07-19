#!/usr/bin/env python3
"""Static, database, and pure-Kotlin checks for Noor Phase 7 premium tafsir."""
from __future__ import annotations

import re
import sqlite3
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


def check_navigation_and_canonical_identity() -> None:
    routes = read("core/navigation/src/main/kotlin/com/noor/core/navigation/NoorRoute.kt")
    nav_host = read("app/src/main/kotlin/com/noor/app/navigation/NoorNavHost.kt")
    view_model = read("app/src/main/kotlin/com/noor/app/ui/tafsir/TafsirViewModel.kt")

    require('const val TAFSIR = "tafsir/{ayahId}"' in routes,
            "Tafsir route must carry only the canonical ayah id")
    require("fun tafsir(ayahId: Int): String" in routes,
            "Typed tafsir route builder is missing")
    require("surahNumber" not in routes.split('const val TAFSIR = "', 1)[1].split('"', 1)[0],
            "Tafsir route still carries untrusted surah metadata")
    require("NoorRoutes.tafsir(ayah.id)" in nav_host,
            "Reader/home must navigate with canonical ayah id")
    require('navArgument("ayahId")' in nav_host,
            "Tafsir destination ayah id argument is missing")
    require('savedStateHandle.get<Int>("ayahId")?.takeIf { it > 0 }' in view_model,
            "Invalid tafsir navigation arguments are not rejected")
    require("quranRepository.getAyah(it)" in view_model,
            "Canonical ayah is not loaded from the Quran repository")
    require("tafsirRepository.getTafsir(" in view_model and "ayah = canonicalAyah" in view_model,
            "Tafsir service is not called with the canonical Quran ayah")


def check_remote_contract_and_security() -> None:
    remote = read("data/src/main/kotlin/com/noor/data/remote/QuranEncTafsirDataSource.kt")
    interface = read("data/src/main/kotlin/com/noor/data/remote/TafsirRemoteDataSource.kt")

    for symbol in {
        "suspend fun fetch(ayah: Ayah): Tafsir",
        "withContext(Dispatchers.IO)",
        'check(url.protocol == "https" && url.host == EXPECTED_HOST)',
        'EXPECTED_HOST = "quranenc.com"',
        'BASE_URL = "https://quranenc.com/api/v1/translation/aya/arabic_moyassar"',
        "instanceFollowRedirects = false",
        "useCaches = false",
        "connectTimeout = CONNECT_TIMEOUT_MS",
        "readTimeout = READ_TIMEOUT_MS",
        'setRequestProperty("Accept", "application/json")',
        "MAX_RESPONSE_BYTES = 1_048_576",
        "contentType.startsWith(\"application/json\")",
        "QuranEncTafsirParser.parse",
    }:
        require(symbol in remote + interface, f"Remote tafsir hardening is missing: {symbol}")

    for symbol in {
        'result.optInt("sura", -1)',
        'result.optInt("aya", -1)',
        "responseSurah != ayah.surahNumber",
        "responseAyah != ayah.numberInSurah",
        'result.optString("translation")',
        'result.optString("footnotes")',
        "TafsirOrigin.NETWORK",
    }:
        require(symbol in remote, f"QuranEnc response validation is missing: {symbol}")


def check_repository_cache_and_concurrency() -> None:
    repository = read("data/src/main/kotlin/com/noor/data/repository/DefaultTafsirRepository.kt")
    contract = read("domain/src/main/kotlin/com/noor/domain/repository/TafsirRepository.kt")
    model = read("domain/src/main/kotlin/com/noor/domain/model/Tafsir.kt")

    require("getTafsir(ayah: Ayah, forceRefresh: Boolean = false)" in contract,
            "Tafsir repository contract is not canonical/cache-aware")
    for symbol in {
        "dao.getByAyahId(ayah.id)",
        "if (!forceRefresh && cachedBeforeLock != null)",
        "registryMutex",
        "ayahLocks",
        "completedFetchGenerations",
        "ticket.mutex.withLock",
        "remote.fetch(ayah)",
        "dao.upsert(fetched.asEntity())",
        "require(fetched.ayahId == ayah.id)",
        "origin = TafsirOrigin.CACHE",
        "footnotes = footnotes",
    }:
        require(symbol in repository, f"Tafsir cache/concurrency behavior is missing: {symbol}")

    for symbol in {
        "enum class TafsirOrigin",
        "val footnotes: String? = null",
        "require(ayahId > 0)",
        "require(text.isNotBlank())",
        "require(sourceKey.isNotBlank())",
        "require(sourceName.isNotBlank())",
    }:
        require(symbol in model, f"Tafsir domain invariant is missing: {symbol}")


def check_database() -> None:
    database = read("data/src/main/kotlin/com/noor/data/local/database/NoorDatabase.kt")
    migrations = read("data/src/main/kotlin/com/noor/data/local/database/NoorDatabaseMigrations.kt")
    entity = read("data/src/main/kotlin/com/noor/data/local/entity/TafsirEntity.kt")
    generator = read("tools/generate_quran_database.py")

    require("version = 4" in database, "Room database version must be 4")
    require("MIGRATION_3_4" in migrations, "Room migration 3 to 4 is missing")
    require("ALTER TABLE tafsirs ADD COLUMN footnotes TEXT" in migrations,
            "Tafsir footnotes migration is missing")
    require("MIGRATION_3_4" in migrations.split("val ALL", 1)[1],
            "Migration 3 to 4 is not registered")
    require('@ColumnInfo(name = "footnotes") val footnotes: String?' in entity,
            "Room tafsir entity does not persist footnotes")
    require('PRAGMA user_version = 4' in generator,
            "Prepackaged Quran database generator is not on schema 4")

    asset = ROOT / "data/src/main/assets/database/noor.db"
    require(asset.is_file(), "Prepackaged Quran database is missing")
    connection = sqlite3.connect(f"file:{asset}?mode=ro", uri=True)
    try:
        version = connection.execute("PRAGMA user_version").fetchone()[0]
        integrity = connection.execute("PRAGMA integrity_check").fetchone()[0]
        columns = {row[1]: row[2] for row in connection.execute("PRAGMA table_info(tafsirs)")}
        surahs = connection.execute("SELECT COUNT(*) FROM surahs").fetchone()[0]
        ayahs = connection.execute("SELECT COUNT(*) FROM ayahs").fetchone()[0]
    finally:
        connection.close()
    require(version == 4, f"Prepackaged database user_version is {version}, expected 4")
    require(integrity == "ok", f"Prepackaged database integrity failed: {integrity}")
    require(columns.get("footnotes", "missing") == "TEXT",
            "Prepackaged tafsirs table is missing nullable TEXT footnotes")
    require(surahs == 114, f"Unexpected surah count: {surahs}")
    require(ayahs == 6236, f"Unexpected ayah count: {ayahs}")


def check_ui_state_and_tests() -> None:
    route = read("app/src/main/kotlin/com/noor/app/ui/tafsir/TafsirRoute.kt")
    components = read("app/src/main/kotlin/com/noor/app/ui/tafsir/TafsirComponents.kt")
    view_model = read("app/src/main/kotlin/com/noor/app/ui/tafsir/TafsirViewModel.kt")
    preview = read("app/src/main/kotlin/com/noor/app/ui/tafsir/TafsirPreview.kt")
    vm_test = read("app/src/test/kotlin/com/noor/app/ui/tafsir/TafsirViewModelTest.kt")
    ui_test = read("app/src/androidTest/kotlin/com/noor/app/ui/tafsir/TafsirPremiumUiTest.kt")
    repo_test = read("data/src/test/kotlin/com/noor/data/repository/DefaultTafsirRepositoryTest.kt")
    parser_test = read("data/src/androidTest/kotlin/com/noor/data/remote/QuranEncTafsirParserTest.kt")
    model_test = read("domain/src/test/kotlin/com/noor/domain/model/TafsirTest.kt")

    for symbol in {
        "NoorPatternBackground",
        "SnackbarHostState",
        "LazyColumn",
        "TafsirAyahCard",
        "TafsirExplanationCard",
        "TafsirFootnotesCard",
        "TafsirSourceCard",
        "TafsirTestTags.CONTENT",
    }:
        require(symbol in route, f"Premium tafsir route is missing: {symbol}")
    for symbol in {
        "TafsirTopBar",
        "TafsirStatusPanel",
        "TafsirTestTags.REFRESH",
        "TafsirTestTags.RETRY",
        "TafsirOrigin.CACHE",
        "tafsir.sourceName",
    }:
        require(symbol in components, f"Premium tafsir component is missing: {symbol}")
    for symbol in {
        "Channel<TafsirEvent>",
        "TafsirEvent.RefreshFailed",
        "isRefreshing = true",
        "if (state.isLoading || state.isRefreshing",
        "catch (cancelled: CancellationException)",
        "forceRefresh = forceRefresh",
    }:
        require(symbol in view_model, f"Tafsir state safety is missing: {symbol}")

    require(preview.count("@Preview") >= 2, "Arabic and dark tafsir previews are required")
    require(vm_test.count("@Test") >= 7, "Tafsir ViewModel coverage is incomplete")
    require(ui_test.count("@Test") >= 4, "Premium tafsir Compose coverage is incomplete")
    require(repo_test.count("@Test") >= 6, "Tafsir repository coverage is incomplete")
    require(parser_test.count("@Test") >= 4, "QuranEnc parser coverage is incomplete")
    require(model_test.count("@Test") >= 2, "Tafsir domain invariant coverage is incomplete")

    expected_names = {
        "canonicalAyahIsLoadedByIdAndPassedToTafsirRepository",
        "invalidNavigationArgumentFailsSafelyInsteadOfCrashing",
        "refreshFailureKeepsContentVisibleAndEmitsFeedback",
        "duplicateRefreshIsIgnoredWhileTheFirstRefreshIsRunning",
        "concurrentCacheMissesShareOneNetworkRequest",
        "concurrentForcedRefreshesShareOneCompletedRefresh",
        "mismatchedRemoteAyahIsRejectedBeforeCaching",
        "mismatchedCoordinatesAreRejected",
    }
    combined_tests = "\n".join((vm_test, repo_test, parser_test))
    for name in expected_names:
        require(name in combined_tests, f"Required tafsir regression test is missing: {name}")

    default_path = ROOT / "app/src/main/res/values/strings.xml"
    arabic_path = ROOT / "app/src/main/res/values-ar/strings.xml"
    default_names = resource_names(default_path)
    arabic_names = resource_names(arabic_path)
    require(default_names == arabic_names, "Arabic/default string-resource parity is broken")
    required = {
        "tafsir_screen_title", "tafsir_refresh", "tafsir_loading_title",
        "tafsir_loading_body", "tafsir_error_title", "tafsir_error_body",
        "tafsir_refresh_error", "tafsir_ayah_label", "tafsir_ayah_position",
        "tafsir_explanation_title", "tafsir_footnotes_title",
        "tafsir_cached_status", "tafsir_network_status",
    }
    require(required <= default_names, f"Missing tafsir resources: {sorted(required - default_names)}")

    forbidden = re.compile(r"\b(?:TODO|FIXME)\b|NotImplementedError|TODO\(")
    directories = [
        ROOT / "app/src/main/kotlin/com/noor/app/ui/tafsir",
        ROOT / "app/src/test/kotlin/com/noor/app/ui/tafsir",
        ROOT / "app/src/androidTest/kotlin/com/noor/app/ui/tafsir",
        ROOT / "data/src/main/kotlin/com/noor/data/remote",
        ROOT / "data/src/main/kotlin/com/noor/data/repository",
        ROOT / "data/src/test/kotlin/com/noor/data/repository",
    ]
    for directory in directories:
        for path in directory.rglob("*.kt"):
            require(not forbidden.search(path.read_text(encoding="utf-8")),
                    f"Placeholder found in {path.relative_to(ROOT)}")


def check_pure_kotlin_contract() -> None:
    compiler = subprocess.run(
        ["bash", "-lc", "command -v kotlinc"], cwd=ROOT,
        capture_output=True, text=True, check=False, timeout=15,
    )
    if compiler.returncode != 0:
        print("- Pure Kotlin tafsir-contract compilation skipped: kotlinc unavailable")
        return
    with tempfile.TemporaryDirectory(prefix="noor-tafsir-") as directory:
        destination = Path(directory) / "tafsir-contract.jar"
        harness = Path(directory) / "TafsirContractHarness.kt"
        harness.write_text(
            """import com.noor.domain.model.Tafsir\nimport com.noor.domain.model.TafsirOrigin\n\nfun main() {\n    val item = Tafsir(1, \"تفسير\", \"هامش\", \"arabic_moyassar\", \"QuranEnc.com\", 1L, TafsirOrigin.NETWORK)\n    check(item.ayahId == 1)\n    check(item.footnotes == \"هامش\")\n    check(item.origin == TafsirOrigin.NETWORK)\n    check(runCatching { Tafsir(0, \"تفسير\", null, \"key\", \"source\", 0L, TafsirOrigin.CACHE) }.isFailure)\n}\n""",
            encoding="utf-8",
        )
        result = subprocess.run(
            [
                compiler.stdout.strip(),
                "domain/src/main/kotlin/com/noor/domain/model/Tafsir.kt",
                str(harness), "-Werror", "-include-runtime", "-d", str(destination),
            ],
            cwd=ROOT, capture_output=True, text=True, check=False, timeout=90,
        )
        require(result.returncode == 0,
                f"Tafsir contract compilation failed:\n{result.stdout}\n{result.stderr}")
        execution = subprocess.run(
            ["java", "-jar", str(destination)], cwd=ROOT,
            capture_output=True, text=True, check=False, timeout=30,
        )
        require(execution.returncode == 0,
                f"Tafsir contract behavior failed:\n{execution.stdout}\n{execution.stderr}")


def main() -> int:
    check_navigation_and_canonical_identity()
    check_remote_contract_and_security()
    check_repository_cache_and_concurrency()
    check_database()
    check_ui_state_and_tests()
    check_pure_kotlin_contract()
    print("PHASE 7 PREMIUM TAFSIR VERIFICATION: PASSED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

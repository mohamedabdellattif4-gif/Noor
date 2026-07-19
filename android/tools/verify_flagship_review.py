#!/usr/bin/env python3
"""Verify repository-level invariants added by the flagship architecture review."""

from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ANDROID_NS = "{http://schemas.android.com/apk/res/android}"
failures: list[str] = []
checks = 0


def read(relative: str) -> str:
    path = ROOT / relative
    if not path.is_file():
        failures.append(f"Missing required file: {relative}")
        return ""
    return path.read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    global checks
    checks += 1
    if not condition:
        failures.append(message)


def require_contains(relative: str, *needles: str) -> None:
    text = read(relative)
    for needle in needles:
        require(needle in text, f"{relative} must contain: {needle}")


def string_names(relative: str) -> set[str]:
    root = ET.parse(ROOT / relative).getroot()
    return {
        element.attrib["name"]
        for element in root
        if element.tag in {"string", "plurals"} and "name" in element.attrib
    }


# Notification deep links must be repeatable for warm intents, not stored as a sticky Boolean.
require_contains(
    "app/src/main/kotlin/com/noor/app/AppLaunchNavigation.kt",
    "val adhkarRequestId: Long",
    "fun requestAdhkar(): AppLaunchNavigation",
    "Long.MAX_VALUE",
)
require_contains(
    "app/src/main/kotlin/com/noor/app/MainActivity.kt",
    "navigation = navigation.requestAdhkar()",
    "setIntent(intent)",
)
require_contains(
    "app/src/main/kotlin/com/noor/app/ui/NoorApp.kt",
    "LaunchedEffect(openAdhkarRequestId)",
    "launchSingleTop = true",
)
require_contains(
    "app/src/test/kotlin/com/noor/app/AppLaunchNavigationTest.kt",
    "everyWarmNotificationProducesANewOneShotRequest",
    "requestCounterWrapsWithoutReturningToNoRequestSentinel",
)

# Persistence failures must be surfaced without crashing viewModelScope jobs.
require_contains(
    "app/src/main/kotlin/com/noor/app/ui/settings/SettingsViewModel.kt",
    "catch (cancelled: CancellationException)",
    "eventChannel.send(SettingsEvent.UpdateFailed)",
)
require_contains(
    "data/src/main/kotlin/com/noor/data/settings/NoorPreferencesDataStore.kt",
    "ReplaceFileCorruptionHandler",
    "emptyPreferences()",
)
require_contains(
    "app/src/main/kotlin/com/noor/app/ui/reader/ReaderViewModel.kt",
    "pendingBookmarkAyahIds",
    "ReaderEvent.BookmarkUpdateFailed",
    "ReaderEvent.TextScaleUpdateFailed",
    "ReaderEvent.ReadingProgressUpdateFailed",
)


# Reminder scheduling failures must not terminate the settings stream.
require_contains(
    "app/src/main/kotlin/com/noor/app/ui/adhkar/AdhkarRemindersViewModel.kt",
    "AdhkarReminderEvent.SchedulingFailed",
    "eventChannel.send(AdhkarReminderEvent.SchedulingFailed)",
    "catch (cancelled: CancellationException)",
)
require_contains(
    "app/src/test/kotlin/com/noor/app/ui/adhkar/AdhkarRemindersViewModelTest.kt",
    "schedulerFailureEmitsFeedbackAndKeepsSettingsFlowAlive",
    "repositoryWriteFailureEmitsUpdateFeedback",
)

# Tasbih and favorites must expose true button/toggle state and remain flexible at large font scales.
require_contains(
    "app/src/main/kotlin/com/noor/app/ui/adhkar/TasbihRoute.kt",
    ".sizeIn(minWidth = 240.dp, minHeight = 240.dp)",
    "role = Role.Button",
    "enabled = !uiState.isComplete",
    "stateDescription = progressDescription",
)
require_contains(
    "app/src/main/kotlin/com/noor/app/ui/adhkar/AdhkarRoute.kt",
    "IconToggleButton(",
    "AdhkarTestTags.FAVORITE_PREFIX",
    "R.string.adhkar_add_favorite",
    "R.string.adhkar_remove_favorite",
)

# Whole rows, not only small controls, must expose radio/switch semantics.
require_contains(
    "app/src/main/kotlin/com/noor/app/ui/settings/SettingsRoute.kt",
    "Modifier.selectableGroup()",
    ".selectable(",
    ".toggleable(",
    "role = Role.Switch",
    "onCheckedChange = null",
    "onClick = null",
)
require_contains(
    "app/src/main/kotlin/com/noor/app/ui/adhkar/AdhkarRemindersRoute.kt",
    ".toggleable(",
    "role = Role.Switch",
    "onCheckedChange = null",
)

# Stable content types keep heterogeneous home lazy-list reuse predictable.
home = read("app/src/main/kotlin/com/noor/app/ui/home/HomeRoute.kt")
for content_type in (
    'contentType = "header"',
    'contentType = "memorial"',
    'contentType = "continue"',
    'contentType = "daily_progress"',
    'contentType = "quick_access"',
    'contentType = "recent_reciter"',
    'contentType = "surah_heading"',
    'contentType = { "surah" }',
):
    require(content_type in home, f"Home lazy content type missing: {content_type}")

# Release automation must execute all source gates, data tests, release lint, and managed-device tests.
ci = read(".github/workflows/android-ci.yml")
for marker in (
    "python3 tools/verify_phase7_premium_tafsir.py",
    "python3 tools/verify_phase8_premium_adhkar.py",
    "python3 tools/verify_flagship_review.py",
    ":data:testDebugUnitTest",
    ":app:lintRelease",
    ":data:pixel6Api35DebugAndroidTest",
    ":app:pixel6Api35DebugAndroidTest",
):
    require(marker in ci, f"CI gate missing: {marker}")

release_gate = read("tools/release_gate.sh")
for marker in (
    "python3 tools/verify_flagship_review.py",
    ":data:testDebugUnitTest",
    ":app:lintRelease",
    "--require-signature",
):
    require(marker in release_gate, f"Release gate missing: {marker}")

# Manifest security boundaries.
manifest_root = ET.parse(ROOT / "app/src/main/AndroidManifest.xml").getroot()
application = manifest_root.find("application")
require(application is not None, "Application node missing from AndroidManifest.xml")
if application is not None:
    require(application.get(ANDROID_NS + "allowBackup") == "false", "Android backup must stay disabled")
    require(
        application.get(ANDROID_NS + "usesCleartextTraffic") == "false",
        "Cleartext traffic must stay disabled",
    )
    require(application.get(ANDROID_NS + "supportsRtl") == "true", "RTL support must stay enabled")

permissions = {
    node.get(ANDROID_NS + "name")
    for node in manifest_root.findall("uses-permission")
}
expected_permissions = {
    "android.permission.INTERNET",
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.WAKE_LOCK",
    "android.permission.FOREGROUND_SERVICE",
    "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK",
}
require(permissions == expected_permissions, f"Unexpected manifest permissions: {sorted(permissions)}")

media_manifest = ET.parse(ROOT / "core/media/src/main/AndroidManifest.xml").getroot()
media_service = media_manifest.find("./application/service")
require(media_service is not None, "MediaSessionService declaration missing")
if media_service is not None:
    require(media_service.get(ANDROID_NS + "exported") == "true", "MediaSessionService must remain exported")
    require(
        media_service.get(ANDROID_NS + "foregroundServiceType") == "mediaPlayback",
        "MediaSessionService must remain scoped to mediaPlayback",
    )

# Room source truth and schema documentation must agree without fabricating generated JSON.
database = read("data/src/main/kotlin/com/noor/data/local/database/NoorDatabase.kt")
migrations = read("data/src/main/kotlin/com/noor/data/local/database/NoorDatabaseMigrations.kt")
schema_readme = read("data/schemas/README.md")
require(re.search(r"version\s*=\s*4\b", database) is not None, "NoorDatabase must remain at version 4")
for marker in ("MIGRATION_1_2", "MIGRATION_2_3", "MIGRATION_3_4"):
    require(marker in migrations, f"Room migration missing: {marker}")
require("currently at version 4" in schema_readme, "Schema README must state database version 4")
require("No generated schema JSON is committed" in schema_readme, "Schema README must disclose missing generated JSON")
require("Do not hand-author" in schema_readme, "Schema README must prohibit fabricated schemas")


require_contains(
    "data/src/androidTest/kotlin/com/noor/data/local/database/NoorDatabaseMigrationTest.kt",
    "migrationFromVersion1To4PreservesAyahAndBuildsSearchInfrastructure",
    ".addMigrations(*NoorDatabaseMigrations.ALL)",
    "database.version = 1",
    "ArabicNormalizer.toFtsPrefixQuery",
)

# Arabic and English resources must remain structurally aligned.
english = string_names("app/src/main/res/values/strings.xml")
arabic = string_names("app/src/main/res/values-ar/strings.xml")
require(english == arabic, f"String resource parity mismatch: en-only={sorted(english-arabic)}, ar-only={sorted(arabic-english)}")


# Noor's fixed premium palette must preserve WCAG AA contrast for normal text.
def relative_luminance(argb: str) -> float:
    rgb = [int(argb[index:index + 2], 16) / 255.0 for index in (2, 4, 6)]

    def linearize(channel: float) -> float:
        return channel / 12.92 if channel <= 0.04045 else ((channel + 0.055) / 1.055) ** 2.4

    red, green, blue = map(linearize, rgb)
    return 0.2126 * red + 0.7152 * green + 0.0722 * blue


def contrast_ratio(first: str, second: str) -> float:
    first_luminance = relative_luminance(first)
    second_luminance = relative_luminance(second)
    lighter = max(first_luminance, second_luminance)
    darker = min(first_luminance, second_luminance)
    return (lighter + 0.05) / (darker + 0.05)


premium_source = read("core/designsystem/src/main/kotlin/com/noor/core/designsystem/theme/NoorPremiumColors.kt")
for palette_name in ("NoorLightPremiumColors", "NoorDarkPremiumColors"):
    match = re.search(
        rf"{palette_name}\s*=\s*NoorPremiumColors\((.*?)\n\)",
        premium_source,
        flags=re.DOTALL,
    )
    require(match is not None, f"Premium palette missing: {palette_name}")
    if match is None:
        continue
    values = dict(re.findall(r"(\w+)\s*=\s*Color\(0x([0-9A-Fa-f]{8})\)", match.group(1)))
    for foreground, background in (
        ("onEmerald", "emeraldDeep"),
        ("onIvory", "ivory"),
        ("onIvoryMuted", "ivory"),
    ):
        require(foreground in values and background in values, f"Missing contrast pair in {palette_name}: {foreground}/{background}")
        if foreground in values and background in values:
            ratio = contrast_ratio(values[foreground], values[background])
            require(
                ratio >= 4.5,
                f"{palette_name} contrast {foreground}/{background} is {ratio:.2f}, below 4.5",
            )


# Production source must not accumulate unfinished markers.
source_roots = [
    ROOT / "app/src/main",
    ROOT / "core",
    ROOT / "data/src/main",
    ROOT / "domain/src/main",
]
unfinished_pattern = re.compile(r"\b(?:TODO|FIXME)\b")
for source_root in source_roots:
    for path in source_root.rglob("*"):
        if path.is_file() and path.suffix in {".kt", ".kts", ".xml"}:
            text = path.read_text(encoding="utf-8")
            require(
                unfinished_pattern.search(text) is None,
                f"Unfinished marker found in production source: {path.relative_to(ROOT)}",
            )

if failures:
    print("FLAGSHIP REVIEW VERIFICATION FAILED")
    for failure in failures:
        print(f"- {failure}")
    sys.exit(1)

print(f"FLAGSHIP REVIEW VERIFICATION PASSED ({checks} checks)")

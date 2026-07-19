#!/usr/bin/env python3
"""Static and behavioral checks for Noor's premium Quran reader implementation."""
from __future__ import annotations

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


def check_reader_structure() -> None:
    route = read("app/src/main/kotlin/com/noor/app/ui/reader/ReaderRoute.kt")
    components = read("app/src/main/kotlin/com/noor/app/ui/reader/ReaderComponents.kt")
    required_route = {
        "LazyColumn",
        "ReaderTopBar",
        "ReaderReadingSurface",
        "ReaderOpeningHeader",
        "ReaderAyahRow",
        "ReaderBottomBar",
        "ReaderTextScaleDialog",
        "resolveReaderListenAction",
        "READER_HEADER_ITEMS = 1",
        'contentType = { "ayah" }',
    }
    for symbol in required_route:
        require(symbol in route, f"Reader route is missing: {symbol}")
    required_components = {
        "AyahNumberMedallion",
        "ReaderCornerOrnaments",
        "OrnamentalDivider",
        "NoorLineIconType.Bookmark",
        "NoorLineIconType.Document",
        "NoorLineIconType.Headphones",
        "navigationBarsPadding",
        "statusBarsPadding",
        "sizeIn(minWidth = 48.dp, minHeight = 64.dp)",
    }
    for symbol in required_components:
        require(symbol in components, f"Reader component is missing: {symbol}")
    require("ayah.textUthmani" in components, "Reader must render the stored Uthmani text directly")
    require("Image(" not in components, "Reader UI must be implemented in Compose, not as a screenshot")
    require("AyahCard" not in route, "Legacy card-per-ayah reader remains active")
    require("verticalScroll" not in route, "Reader must remain lazy for long surahs")
    require(
        "shouldShowReaderBasmala" in route,
        "Basmala presentation must avoid duplication in Al-Fatihah and omission rule for At-Tawbah",
    )


def check_actions_and_persistence() -> None:
    view_model = read("app/src/main/kotlin/com/noor/app/ui/reader/ReaderViewModel.kt")
    audio_controller = read("core/media/src/main/kotlin/com/noor/core/media/QuranAudioController.kt")
    action = read("app/src/main/kotlin/com/noor/app/ui/reader/ReaderPlaybackAction.kt")
    presentation_rules = read(
        "app/src/main/kotlin/com/noor/app/ui/reader/ReaderPresentationRules.kt",
    )
    tests = read("app/src/test/kotlin/com/noor/app/ui/reader/ReaderPlaybackActionTest.kt")
    view_model_tests = read("app/src/test/kotlin/com/noor/app/ui/reader/ReaderViewModelTest.kt")
    ui_test = read("app/src/androidTest/kotlin/com/noor/app/ui/reader/ReaderPremiumUiTest.kt")
    require("fun setTextScale(value: Float)" in view_model, "Reader text-scale persistence is missing")
    require("UserSettings.MIN_TEXT_SCALE" in view_model, "Reader text scale is not lower-bounded")
    require("UserSettings.MAX_TEXT_SCALE" in view_model, "Reader text scale is not upper-bounded")
    require("settingsRepository.setQuranTextScale" in view_model, "Reader text scale is not persisted")
    require("controller.playWhenReady" in audio_controller,
            "Buffering audio cannot be paused reliably")
    require("ReaderListenAction.PLAY_CURRENT" in action, "Play-current audio decision is missing")
    require("ReaderListenAction.TOGGLE_PAUSE" in action, "Pause/resume audio decision is missing")
    require("currentMediaId" in action and "ayah:$currentAyahId" in action,
            "Active ayah Media3 identity is not checked across paused/buffering states")
    require(tests.count("@Test") >= 4, "Reader behavior regression coverage is incomplete")
    require("surahNumber != 1 && surahNumber != 9" in presentation_rules,
            "Basmala presentation rules are incomplete")
    require("textScaleIsClampedBeforePersistence" in view_model_tests,
            "Text scale boundary regression test is missing")
    require("ReaderTestTags.TEXT_SIZE_DIALOG" in ui_test,
            "Premium reader Compose interaction test is missing")


def check_strings() -> None:
    default_path = ROOT / "app/src/main/res/values/strings.xml"
    arabic_path = ROOT / "app/src/main/res/values-ar/strings.xml"
    default_names = resource_names(default_path)
    arabic_names = resource_names(arabic_path)
    require(default_names == arabic_names, "Arabic/default string-resource parity is broken")
    required = {
        "reader_surah_title",
        "reader_position",
        "reader_basmala",
        "reader_play_surah",
        "reader_bookmark_action",
        "reader_tafsir_action",
        "reader_listen_action",
        "reader_pause_action",
        "reader_resume_action",
        "reader_text_size_action",
        "reader_text_size_title",
        "reader_stop_audio",
        "cancel",
        "save",
    }
    require(required <= default_names, f"Missing reader resources: {sorted(required - default_names)}")
    arabic = arabic_path.read_text(encoding="utf-8")
    require("سورة %1$s" in arabic, "Localized surah title is missing")
    require("الجزء %1$d • الصفحة %2$d" in arabic, "Localized reader metadata is missing")


def check_preview_and_no_placeholders() -> None:
    preview = read("app/src/main/kotlin/com/noor/app/ui/reader/ReaderPreview.kt")
    require(preview.count("@Preview") >= 2, "Arabic and English reader previews are required")
    require("ٱلرَّحْمَٰنُ" in preview, "Reader preview does not exercise Uthmani shaping")
    forbidden = re.compile(r"\b(?:TODO|FIXME)\b|NotImplementedError|TODO\(")
    paths = [
        ROOT / "app/src/main/kotlin/com/noor/app/ui/reader",
        ROOT / "app/src/test/kotlin/com/noor/app/ui/reader",
    ]
    for directory in paths:
        for path in directory.rglob("*.kt"):
            require(
                not forbidden.search(path.read_text(encoding="utf-8")),
                f"Placeholder found in {path.relative_to(ROOT)}",
            )


def check_pure_kotlin_compilation() -> None:
    compiler = subprocess.run(
        ["bash", "-lc", "command -v kotlinc"],
        cwd=ROOT,
        capture_output=True,
        text=True,
        check=False,
    )
    if compiler.returncode != 0:
        print("- Pure Kotlin reader-action compilation skipped: kotlinc unavailable")
        return
    with tempfile.TemporaryDirectory(prefix="noor-reader-") as directory:
        directory_path = Path(directory)
        destination = directory_path / "reader-action.jar"
        harness = directory_path / "ReaderActionHarness.kt"
        harness.write_text(
            """import com.noor.app.ui.reader.ReaderListenAction
import com.noor.app.ui.reader.resolveReaderListenAction
import com.noor.app.ui.reader.shouldShowReaderBasmala
import com.noor.core.media.QuranPlaybackState

fun main() {
    check(
        resolveReaderListenAction(QuranPlaybackState.Idle, null, 42) ==
            ReaderListenAction.PLAY_CURRENT,
    )
    check(
        resolveReaderListenAction(QuranPlaybackState.Playing("ayah:42"), "ayah:42", 42) ==
            ReaderListenAction.TOGGLE_PAUSE,
    )
    check(
        resolveReaderListenAction(QuranPlaybackState.Playing("ayah:7"), "ayah:7", 42) ==
            ReaderListenAction.PLAY_CURRENT,
    )
    check(
        resolveReaderListenAction(QuranPlaybackState.Paused, "ayah:42", 42) ==
            ReaderListenAction.TOGGLE_PAUSE,
    )
    check(
        resolveReaderListenAction(QuranPlaybackState.Paused, "ayah:7", 42) ==
            ReaderListenAction.PLAY_CURRENT,
    )
    check(!shouldShowReaderBasmala(1))
    check(!shouldShowReaderBasmala(9))
    check(shouldShowReaderBasmala(55))
}
""",
            encoding="utf-8",
        )
        result = subprocess.run(
            [
                compiler.stdout.strip(),
                "core/media/src/main/kotlin/com/noor/core/media/QuranPlaybackState.kt",
                "app/src/main/kotlin/com/noor/app/ui/reader/ReaderPlaybackAction.kt",
                "app/src/main/kotlin/com/noor/app/ui/reader/ReaderPresentationRules.kt",
                str(harness),
                "-Werror",
                "-include-runtime",
                "-d",
                str(destination),
            ],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
        )
        require(
            result.returncode == 0,
            f"Reader playback decision compilation failed:\n{result.stdout}\n{result.stderr}",
        )
        execution = subprocess.run(
            ["java", "-jar", str(destination)],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
        )
        require(
            execution.returncode == 0,
            f"Reader playback decision behavior failed:\n{execution.stdout}\n{execution.stderr}",
        )


def main() -> int:
    check_reader_structure()
    check_actions_and_persistence()
    check_strings()
    check_preview_and_no_placeholders()
    check_pure_kotlin_compilation()
    print("PHASE 3 PREMIUM READER VERIFICATION: PASSED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

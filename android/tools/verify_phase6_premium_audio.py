#!/usr/bin/env python3
"""Static and behavioral regression checks for Noor's premium audio phase."""
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


def check_audio_feature_structure() -> None:
    route = read("app/src/main/kotlin/com/noor/app/ui/audio/AudioRoute.kt")
    components = read("app/src/main/kotlin/com/noor/app/ui/audio/AudioComponents.kt")
    view_model = read("app/src/main/kotlin/com/noor/app/ui/audio/AudioViewModel.kt")
    preview = read("app/src/main/kotlin/com/noor/app/ui/audio/AudioPreview.kt")

    for symbol in {
        "AudioTopBar",
        "PremiumAudioPlayerCard",
        "ReciterSelector",
        "AudioSurahListHeader",
        "AudioSurahCard",
        "LazyColumn",
        'contentType = { "audio_surah" }',
        "SnackbarHostState",
        "AudioEvent.ReciterUpdateFailed",
    }:
        require(symbol in route, f"Premium audio route is missing: {symbol}")

    for symbol in {
        "PremiumAudioWaveform",
        "AudioProgressControl",
        "NoorLineIconType.Previous",
        "NoorLineIconType.Next",
        "NoorLineIconType.Pause",
        "NoorLineIconType.Stop",
        "sizeIn(minWidth = 48.dp, minHeight = 48.dp)",
        "AudioTestTags",
        "queueIsSelected && state is QuranPlaybackState.Buffering",
    }:
        require(symbol in components, f"Premium audio component is missing: {symbol}")

    for symbol in {
        "quranRepository.observeSurahs()",
        "quranRepository.observeAyahsForSurah",
        "settingsRepository.settings",
        "audioPlayer.playbackState",
        "audioPlayer.playbackInfo",
        "audioPlayer.playSurah",
        "audioPlayer.togglePause",
        "audioPlayer.skipToPrevious",
        "audioPlayer.skipToNext",
        "audioPlayer.seekTo",
        "contentGeneration.flatMapLatest",
        "emit(AudioContent.failed())",
        "@OptIn(ExperimentalCoroutinesApi::class)",
        "catch (cancelled: CancellationException)",
    }:
        require(symbol in view_model, f"Audio ViewModel is missing: {symbol}")

    require(preview.count("@Preview") >= 2, "Arabic and dark audio previews are required")
    require("ٱلرَّحْمَٰنُ" in preview, "Audio preview must exercise Quran metadata")
    require("Image(" not in route + components, "Audio UI must be native Compose, not a screenshot")


def check_media3_safety() -> None:
    interface = read("core/media/src/main/kotlin/com/noor/core/media/QuranAudioPlayer.kt")
    info = read("core/media/src/main/kotlin/com/noor/core/media/QuranPlaybackInfo.kt")
    controller = read("core/media/src/main/kotlin/com/noor/core/media/QuranAudioController.kt")
    service = read("core/media/src/main/kotlin/com/noor/core/media/QuranPlaybackService.kt")
    manifest = read("app/src/main/AndroidManifest.xml")
    media_manifest = read("core/media/src/main/AndroidManifest.xml")

    for symbol in {
        "val playbackInfo: StateFlow<QuranPlaybackInfo>",
        "fun skipToPrevious()",
        "fun skipToNext()",
        "fun seekTo(positionMs: Long)",
    }:
        require(symbol in interface, f"Media boundary is missing: {symbol}")

    for symbol in {
        "currentAyahId",
        "progressFraction",
        "durationMs",
        "canSkipPrevious",
        "canSkipNext",
    }:
        require(symbol in info, f"Playback information contract is missing: {symbol}")

    for symbol in {
        "MediaController.Builder",
        "SessionToken",
        "onEvents(player: Player",
        "controller.playWhenReady",
        "controller.hasPreviousMediaItem()",
        "controller.hasNextMediaItem()",
        "_playbackInfo.subscriptionCount.collect",
        "_playbackInfo.subscriptionCount.value > 0",
        "POSITION_UPDATE_INTERVAL_MS = 500L",
        "C.TIME_UNSET",
        "setMediaItems(items, startIndex, 0L)",
    }:
        require(symbol in controller, f"Media3 controller hardening is missing: {symbol}")

    require("setWakeMode(C.WAKE_MODE_LOCAL)" in service,
            "Screen-off background playback wake policy is missing")
    require("setHandleAudioBecomingNoisy(true)" in service,
            "Headphone-disconnect protection is missing")
    require("setAudioAttributes" in service and "true," in service,
            "Media3 automatic audio-focus handling is missing")
    require("android.permission.WAKE_LOCK" in manifest,
            "WAKE_LOCK permission required by the local wake mode is missing")
    require("android:foregroundServiceType=\"mediaPlayback\"" in media_manifest,
            "MediaSessionService is not declared as media playback")
    require("androidx.media3.session.MediaSessionService" in media_manifest,
            "MediaSessionService intent filter is missing")


def check_navigation_and_reader_regression() -> None:
    routes = read("core/navigation/src/main/kotlin/com/noor/core/navigation/NoorRoute.kt")
    nav_host = read("app/src/main/kotlin/com/noor/app/navigation/NoorNavHost.kt")
    home = read("app/src/main/kotlin/com/noor/app/ui/home/HomeRoute.kt")
    reader_state = read("app/src/main/kotlin/com/noor/app/ui/reader/ReaderUiState.kt")
    reader_action = read("app/src/main/kotlin/com/noor/app/ui/reader/ReaderPlaybackAction.kt")

    require('const val AUDIO = "audio"' in routes, "Stable audio navigation route is missing")
    require("composable(NoorRoutes.AUDIO)" in nav_host and "AudioRoute(" in nav_host,
            "Audio destination is not wired into Navigation Compose")
    require("onAudio = { navController.navigate(NoorRoutes.AUDIO) }" in nav_host,
            "Home does not navigate to the dedicated audio destination")
    require("title = R.string.listen" in home and "onClick = onAudio" in home,
            "Home listening shortcut still opens the reader instead of audio")
    require("QuranPlaybackInfo" in reader_state,
            "Reader cannot identify paused/buffering media after the audio contract change")
    require("currentMediaId" in reader_action,
            "Reader may resume an unrelated paused ayah")


def check_tests_and_strings() -> None:
    unit = read("app/src/test/kotlin/com/noor/app/ui/audio/AudioViewModelTest.kt")
    ui = read("app/src/androidTest/kotlin/com/noor/app/ui/audio/AudioPremiumUiTest.kt")
    info_test = read("core/media/src/test/kotlin/com/noor/core/media/QuranPlaybackInfoTest.kt")
    reader_test = read("app/src/test/kotlin/com/noor/app/ui/reader/ReaderPlaybackActionTest.kt")

    for name in {
        "lastReadSurahBecomesTheInitialListeningSelection",
        "playStartsTheSelectedSurahWithThePersistedReciter",
        "pausedSelectedQueueResumesButDifferentQueueIsReplaced",
        "transportAndSeekCommandsDelegateToTheMediaBoundary",
        "reciterFailureEmitsFeedbackAndRestoresTheControlState",
        "retryReopensRepositoryFlowsAfterARealObservationFailure",
    }:
        require(name in unit, f"Audio ViewModel regression test is missing: {name}")
    require(unit.count("@Test") >= 6, "Audio ViewModel coverage is incomplete")
    require(ui.count("@Test") >= 4, "Premium audio Compose coverage is incomplete")
    require("assertIsNotEnabled" in ui, "Transport disabled-state coverage is missing")
    require(info_test.count("@Test") >= 2, "Playback info behavior is not covered")
    require("bufferingAndPausedOnlyResumeTheVisibleAyah" in reader_test,
            "Paused-reader regression is not covered")

    default_path = ROOT / "app/src/main/res/values/strings.xml"
    arabic_path = ROOT / "app/src/main/res/values-ar/strings.xml"
    default_names = resource_names(default_path)
    arabic_names = resource_names(arabic_path)
    require(default_names == arabic_names, "Arabic/default string-resource parity is broken")
    required = {
        "audio_screen_title",
        "audio_screen_subtitle",
        "audio_surah_title",
        "audio_select_surah_title",
        "audio_previous_ayah",
        "audio_next_ayah",
        "audio_stop",
        "audio_play",
        "audio_pause",
        "audio_open_in_reader",
        "audio_choose_reciter",
        "audio_surah_list_title",
        "audio_loading_title",
        "audio_load_error_title",
        "audio_reciter_update_error",
        "audio_surah_count",
    }
    require(required <= default_names, f"Missing audio resources: {sorted(required - default_names)}")

    forbidden = re.compile(r"\b(?:TODO|FIXME)\b|NotImplementedError|TODO\(")
    for directory in (
        ROOT / "app/src/main/kotlin/com/noor/app/ui/audio",
        ROOT / "app/src/test/kotlin/com/noor/app/ui/audio",
        ROOT / "app/src/androidTest/kotlin/com/noor/app/ui/audio",
        ROOT / "core/media/src/main/kotlin/com/noor/core/media",
    ):
        for path in directory.rglob("*.kt"):
            require(
                not forbidden.search(path.read_text(encoding="utf-8")),
                f"Placeholder found in {path.relative_to(ROOT)}",
            )


def check_pure_kotlin_contract() -> None:
    compiler = subprocess.run(
        ["bash", "-lc", "command -v kotlinc"],
        cwd=ROOT,
        capture_output=True,
        text=True,
        check=False,
    )
    if compiler.returncode != 0:
        print("- Pure Kotlin audio-contract compilation skipped: kotlinc unavailable")
        return
    with tempfile.TemporaryDirectory(prefix="noor-audio-") as directory:
        directory_path = Path(directory)
        destination = directory_path / "audio-contract.jar"
        harness = directory_path / "AudioContractHarness.kt"
        harness.write_text(
            """import com.noor.core.media.QuranPlaybackInfo

fun main() {
    check(QuranPlaybackInfo(mediaId = "ayah:42").currentAyahId == 42)
    check(QuranPlaybackInfo(mediaId = "invalid").currentAyahId == null)
    check(QuranPlaybackInfo(positionMs = 500, durationMs = 1000).progressFraction == 0.5f)
    check(QuranPlaybackInfo(positionMs = 2000, durationMs = 1000).progressFraction == 1f)
}
""",
            encoding="utf-8",
        )
        result = subprocess.run(
            [
                compiler.stdout.strip(),
                "core/media/src/main/kotlin/com/noor/core/media/QuranPlaybackInfo.kt",
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
            f"Audio contract compilation failed:\n{result.stdout}\n{result.stderr}",
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
            f"Audio contract behavior failed:\n{execution.stdout}\n{execution.stderr}",
        )


def main() -> int:
    check_audio_feature_structure()
    check_media3_safety()
    check_navigation_and_reader_regression()
    check_tests_and_strings()
    check_pure_kotlin_contract()
    print("PHASE 6 PREMIUM AUDIO VERIFICATION: PASSED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

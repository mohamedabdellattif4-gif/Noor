#!/usr/bin/env python3
"""Static regression checks for Noor's premium bookmarks phase."""
from __future__ import annotations

import re
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


def check_bookmarks_ui() -> None:
    route = read("app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksRoute.kt")
    components = read("app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksComponents.kt")
    preview = read("app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksPreview.kt")

    required_route = {
        "NoorPatternBackground",
        "BookmarksTopBar",
        "BookmarksStatusPanel",
        "BookmarksSummaryHeader",
        "BookmarkCard",
        "SnackbarHostState",
        "BookmarksEvent.RemovalFailed",
        "LazyColumn",
        'contentType = { "bookmark" }',
        "key = { it.ayah.id }",
        "onRemoveBookmark = viewModel::remove",
    }
    for symbol in required_route:
        require(symbol in route, f"Premium bookmarks route is missing: {symbol}")

    required_components = {
        "BookmarkAyahMedallion",
        "NoorLineIconType.Delete",
        "bookmark.ayah.textUthmani",
        "bookmark_position",
        "sizeIn(minHeight = 48.dp)",
        "BookmarksTestTags",
        "enabled = !isRemoving",
    }
    for symbol in required_components:
        require(symbol in components, f"Premium bookmark component is missing: {symbol}")

    require("Image(" not in route + components, "Bookmarks must be native Compose, not a screenshot")
    require(preview.count("@Preview") >= 2, "Arabic and dark bookmark previews are required")
    require("ٱلرَّحْمَٰنُ" in preview, "Preview must exercise Uthmani Quran shaping")


def check_removal_safety() -> None:
    state = read("app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksUiState.kt")
    event = read("app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksEvent.kt")
    view_model = read("app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksViewModel.kt")
    tests = read("app/src/test/kotlin/com/noor/app/ui/bookmarks/BookmarksViewModelTest.kt")
    ui_tests = read("app/src/androidTest/kotlin/com/noor/app/ui/bookmarks/BookmarksPremiumUiTest.kt")

    require("pendingRemovalIds: Set<Int>" in state, "Pending bookmark removals are not modeled")
    require("RemovalFailed" in event, "Bookmark removal failure event is missing")

    required_view_model = {
        "ayahId in _uiState.value.pendingRemovalIds",
        "pendingRemovalIds = state.pendingRemovalIds + ayahId",
        "pendingRemovalIds = state.pendingRemovalIds - ayahId",
        "eventChannel.send(BookmarksEvent.RemovalFailed)",
        "catch (cancelled: CancellationException)",
        "repository.setBookmarked(ayahId, false)",
    }
    for symbol in required_view_model:
        require(symbol in view_model, f"Bookmark removal safety is missing: {symbol}")

    required_tests = {
        "retryRecoversAfterObservationFailure",
        "duplicateRemoveIsIgnoredWhileTheFirstRequestIsPending",
        "removalFailureEmitsEventAndClearsPendingState",
        "invalidAyahIdIsRejectedWithoutCallingTheRepository",
    }
    for name in required_tests:
        require(name in tests, f"Bookmark ViewModel regression test is missing: {name}")
    require(tests.count("@Test") >= 5, "Bookmark ViewModel coverage is incomplete")
    require(ui_tests.count("@Test") >= 4, "Premium bookmark Compose coverage is incomplete")
    require("assertIsNotEnabled" in ui_tests, "Pending-removal UI protection is not covered")
    require("assertFalse(opened)" in ui_tests, "Nested remove action behavior is not covered")


def check_room_contract() -> None:
    dao = read("data/src/main/kotlin/com/noor/data/local/dao/BookmarkDao.kt")
    repository = read("data/src/main/kotlin/com/noor/data/repository/DefaultBookmarkRepository.kt")

    require("INNER JOIN ayahs" in dao and "INNER JOIN surahs" in dao,
            "Bookmark display data is no longer enriched from the Quran database")
    require("ORDER BY b.created_at DESC" in dao,
            "Bookmarks are no longer ordered newest-first")
    require("@Upsert suspend fun upsert" in dao and "DELETE FROM bookmarks" in dao,
            "Bookmark persistence contract changed")
    require("System.currentTimeMillis()" in repository,
            "Bookmark creation timestamp behavior changed")


def check_strings_and_placeholders() -> None:
    default_path = ROOT / "app/src/main/res/values/strings.xml"
    arabic_path = ROOT / "app/src/main/res/values-ar/strings.xml"
    default_names = resource_names(default_path)
    arabic_names = resource_names(arabic_path)
    require(default_names == arabic_names, "Arabic/default string-resource parity is broken")

    required = {
        "bookmarks_screen_title",
        "bookmarks_screen_subtitle",
        "bookmarks_saved_title",
        "bookmarks_sorted_hint",
        "bookmarks_loading_title",
        "bookmarks_loading_body",
        "bookmarks_error_title",
        "bookmarks_empty_title",
        "bookmarks_empty_body",
        "bookmarks_browse_quran",
        "bookmark_position",
        "bookmark_open_ayah",
        "bookmark_remove_error",
        "bookmark_removing",
        "bookmarks_count",
    }
    require(required <= default_names, f"Missing bookmark resources: {sorted(required - default_names)}")

    forbidden = re.compile(r"\b(?:TODO|FIXME)\b|NotImplementedError|TODO\(")
    for directory in (
        ROOT / "app/src/main/kotlin/com/noor/app/ui/bookmarks",
        ROOT / "app/src/test/kotlin/com/noor/app/ui/bookmarks",
        ROOT / "app/src/androidTest/kotlin/com/noor/app/ui/bookmarks",
    ):
        for path in directory.rglob("*.kt"):
            require(
                not forbidden.search(path.read_text(encoding="utf-8")),
                f"Placeholder found in {path.relative_to(ROOT)}",
            )


def main() -> int:
    check_bookmarks_ui()
    check_removal_safety()
    check_room_contract()
    check_strings_and_placeholders()
    print("PHASE 5 PREMIUM BOOKMARKS VERIFICATION: PASSED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

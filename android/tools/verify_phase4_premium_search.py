#!/usr/bin/env python3
"""Static regression checks for Noor's premium offline Quran search phase."""
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


def check_search_ui() -> None:
    route = read("app/src/main/kotlin/com/noor/app/ui/search/SearchRoute.kt")
    components = read("app/src/main/kotlin/com/noor/app/ui/search/SearchComponents.kt")
    preview = read("app/src/main/kotlin/com/noor/app/ui/search/SearchPreview.kt")

    required_route = {
        "NoorPatternBackground",
        "SearchTopBar",
        "PremiumSearchField",
        "SearchStatusPanel",
        "SearchResultsHeader",
        "SearchResultCard",
        "LazyColumn",
        "imePadding",
        'contentType = { "search_result" }',
        "key = { it.ayah.id }",
        "onRetry = viewModel::retry",
        "onClear = viewModel::clearQuery",
    }
    for symbol in required_route:
        require(symbol in route, f"Premium search route is missing: {symbol}")

    required_components = {
        "OutlinedTextFieldDefaults.colors",
        "NoorLineIconType.Close",
        "SearchAyahMedallion",
        "result.ayah.textUthmani",
        "search_result_position",
        "sizeIn(minHeight = 48.dp)",
        "SearchTestTags",
    }
    for symbol in required_components:
        require(symbol in components, f"Premium search component is missing: {symbol}")

    require("Image(" not in route + components, "Search must be native Compose, not a screenshot")
    require(preview.count("@Preview") >= 2, "Arabic and dark search previews are required")
    require("ٱلرَّحْمَٰنُ" in preview, "Preview must exercise Uthmani Quran shaping")


def check_search_state_and_races() -> None:
    view_model = read("app/src/main/kotlin/com/noor/app/ui/search/SearchViewModel.kt")
    tests = read("app/src/test/kotlin/com/noor/app/ui/search/SearchViewModelTest.kt")
    ui_tests = read("app/src/androidTest/kotlin/com/noor/app/ui/search/SearchPremiumUiTest.kt")

    required_view_model = {
        "requestGeneration",
        "generation != requestGeneration",
        "fun clearQuery()",
        "fun retry()",
        "SEARCH_DEBOUNCE_MILLIS",
        "SEARCH_RESULT_LIMIT",
        "SearchUiState(\n            query = safeQuery,\n            isSearching = true",
    }
    for symbol in required_view_model:
        require(symbol in view_model, f"Search state protection is missing: {symbol}")

    required_tests = {
        "changingACompletedQueryClearsStaleResultsImmediately",
        "staleRepositoryResultCannotOverwriteTheNewerQuery",
        "retryRepeatsTheCurrentQueryAndRecoversFromFailure",
        "clearQueryCancelsSearchAndRestoresInitialState",
    }
    for name in required_tests:
        require(name in tests, f"Search regression test is missing: {name}")
    require(tests.count("@Test") >= 7, "Search ViewModel coverage is incomplete")
    require(ui_tests.count("@Test") >= 3, "Premium search Compose coverage is incomplete")
    require("SearchTestTags.RETRY" in ui_tests, "Retry UI interaction is not covered")
    require("SearchTestTags.CLEAR_QUERY" in ui_tests, "Clear-query UI interaction is not covered")


def check_offline_fts_contract() -> None:
    dao = read("data/src/main/kotlin/com/noor/data/local/dao/AyahDao.kt")
    repository = read("data/src/main/kotlin/com/noor/data/repository/DefaultQuranRepository.kt")
    normalizer = read("core/common/src/main/kotlin/com/noor/core/common/text/ArabicNormalizer.kt")

    require("FROM ayahs_fts" in dao and "MATCH :matchQuery" in dao,
            "Search is no longer backed by the offline FTS4 table")
    require("ORDER BY a.id ASC" in dao, "Canonical Quran ordering changed")
    require("ArabicNormalizer.toFtsPrefixQuery" in repository,
            "FTS input is not routed through the safe normalizer")
    require('joinToString(" AND ")' in normalizer, "Bounded FTS token conjunction changed")
    require("MAX_QUERY_TOKENS" in normalizer and "MAX_TOKEN_LENGTH" in normalizer,
            "FTS query bounds are missing")


def check_strings_and_placeholders() -> None:
    default_path = ROOT / "app/src/main/res/values/strings.xml"
    arabic_path = ROOT / "app/src/main/res/values-ar/strings.xml"
    default_names = resource_names(default_path)
    arabic_names = resource_names(arabic_path)
    require(default_names == arabic_names, "Arabic/default string-resource parity is broken")

    required = {
        "search_screen_title",
        "search_screen_subtitle",
        "search_clear",
        "search_initial_title",
        "search_initial_body",
        "search_loading_title",
        "search_loading_body",
        "search_error_body",
        "search_try_different_words",
        "search_results_title",
        "search_result_position",
        "search_open_result",
        "search_results_count",
    }
    require(required <= default_names, f"Missing search resources: {sorted(required - default_names)}")

    forbidden = re.compile(r"\b(?:TODO|FIXME)\b|NotImplementedError|TODO\(")
    for directory in (
        ROOT / "app/src/main/kotlin/com/noor/app/ui/search",
        ROOT / "app/src/test/kotlin/com/noor/app/ui/search",
        ROOT / "app/src/androidTest/kotlin/com/noor/app/ui/search",
    ):
        for path in directory.rglob("*.kt"):
            require(
                not forbidden.search(path.read_text(encoding="utf-8")),
                f"Placeholder found in {path.relative_to(ROOT)}",
            )


def main() -> int:
    check_search_ui()
    check_search_state_and_races()
    check_offline_fts_contract()
    check_strings_and_placeholders()
    print("PHASE 4 PREMIUM SEARCH VERIFICATION: PASSED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

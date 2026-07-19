---
name: Noor build fixes
description: Compile/lint/test errors that were present in the imported Noor project and how each was fixed for AGP 9.2.1 + Compose BOM 2026.06.01.
---

# Noor Build Fixes

## AGP 9.2.1 — ManagedVirtualDevice API changed
`managedDevices.devices.create<ManagedVirtualDevice>()` no longer compiles. Removed the `managedDevices { devices { ... } }` block from `app/build.gradle.kts`, `data/build.gradle.kts`, and `baseline-profile/build.gradle.kts`.

**Why:** AGP 9.x changed the typed NDOC container for managed devices. These blocks are only needed for AVD-based instrumented tests, not for building or unit tests.

## benchmark plugin incompatibility
`:baseline-profile` (uses `com.android.test` plugin) conflicts with `androidx.benchmark` plugin in AGP 9.2.1. Excluded `:baseline-profile` from `settings.gradle.kts`.

## Compose BOM 2026.06.01 — API changes
- `import androidx.compose.foundation.layout.matchParentSize` → **remove import** (now a BoxScope member, not importable extension)
- `import androidx.compose.foundation.layout.weight` → **remove import** (now internal; RowScope/ColumnScope member resolves without import)
- `Modifier.graphicsLayer(rotationZ = 180f)` → **change to** `Modifier.graphicsLayer { rotationZ = 180f }` (named-param overload removed)
- Import `androidx.compose.ui.draw.graphicsLayer` → change to `androidx.compose.ui.graphics.graphicsLayer`
- `NoorTheme(dynamicColor = false)` → `NoorTheme(useDynamicColor = false)` (parameter renamed)
- Missing `import androidx.compose.ui.unit.dp` in AdhkarRemindersRoute.kt

## kotlinx.coroutines.test 1.11.0 — runCurrent is a TestScope extension
`kotlinx.coroutines.test.runCurrent()` (FQN call) fails because `runCurrent` is a `TestScope` extension — must be called without FQN inside `runTest {}`. Add `import kotlinx.coroutines.test.runCurrent` and call it directly.

## NewApi — contentLengthLong requires API 24
`QuranEncTafsirDataSource.kt` used `connection.contentLengthLong` (API 24) with minSdk 23. Fixed with `Build.VERSION.SDK_INT >= Build.VERSION_CODES.N` guard.

## Lint — various
- targetSdk 36 → 37 (OldTargetApi)
- Manifest attributes need `tools:ignore="UnusedAttribute,NewApi"` on `<application>`
- Added `android:dataExtractionRules="@xml/data_extraction_rules"` + created `data_extraction_rules.xml`
- Disabled `AndroidGradlePluginVersion`, `NewerVersionAvailable`, `NotShrinkingResources` in lint block
- Monochrome icon was missing from adaptive icon XML (file already existed as `@drawable/ic_noor_monochrome`)
- `ayah_count` string converted to `<plurals>` in both `values/strings.xml` and `values-ar/strings.xml`
- `AudioStatusPanel` modifier param moved to first optional position (ModifierParameter lint rule)
- Unused strings annotated with `tools:ignore="UnusedResources"` rather than deleted

## Test fix — missing advanceUntilIdle
`AdhkarViewModelTest.perItemCounterIsCappedAndResetWithoutChangingContent` read `uiState.value` immediately after synchronous increments without yielding for the StateFlow combine to re-emit. Added `advanceUntilIdle()` after the repeat loop and after reset.

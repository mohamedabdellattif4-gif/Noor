# Noor design system

The Noor design system is owned by `:core:designsystem`. Feature and app modules consume its public theme and components instead of recreating visual rules.

## Direction

The visual identity uses a calm emerald primary palette and a restrained warm-gold accent. The palette is intentionally fixed by default so long-form Quran reading remains visually consistent. Android dynamic color is supported as an opt-in theme parameter rather than the default.

## Theme layers

- `Color.kt`: light and dark Material 3 color schemes.
- `Typography.kt`: complete Material 3 UI type scale plus Quran-specific text roles.
- `Shape.kt`: shared corner-radius scale.
- `Spacing.kt`: shared spacing tokens exposed through `NoorDesignSystem.spacing`.
- `NoorTheme.kt`: theme entry point and optional Android 12+ dynamic color.

## Typography decision

No external font package is bundled in Phase 2. UI text uses the platform sans-serif family and Quran text roles use the platform serif family. This keeps the APK small, avoids font licensing and download failures, and preserves broad Arabic glyph coverage. A dedicated verified Quran font can be evaluated with the reader implementation in Phase 5 because that decision affects rendering accuracy and distribution size.

## RTL

- The manifest declares `android:supportsRtl="true"`.
- Layout code uses logical `start`/`end` behavior through Compose APIs rather than physical left/right values.
- Quran text roles use `TextDirection.ContentOrRtl`.
- Arabic resources match the default resource keys.
- Light/dark previews are provided in both LTR and Arabic RTL configurations.

## Shared components

- `NoorFilledButton`
- `NoorOutlinedButton`
- `NoorCard`
- `NoorClickableCard`
- `NoorTopAppBar`
- `NoorLoadingIndicator`
- `NoorLoadingScreen`

Components remain intentionally small wrappers around Material 3. This centralizes defaults without hiding the underlying Compose model or creating a rigid framework.

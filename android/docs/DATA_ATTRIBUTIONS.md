# Data and service attributions

## Tanzil Uthmani Quran text

- Asset: `tools/vendor/tanzil-quran-uthmani.xml`
- Version: 1.0.2
- Use: displayed Quran text, copied verbatim
- License: Creative Commons Attribution 3.0 plus Tanzil distribution terms
- Notice: the Quran text must not be changed; source attribution and a link to Tanzil must remain

The complete source notice is bundled at
`app/src/main/assets/licenses/TANZIL_QURAN_TEXT_LICENSE.txt`.

## quran-meta

- Version: 6.0.17, Hafs metadata
- Use: Arabic chapter names, revelation classification, page, juz, rub el hizb, and chapter start-page metadata
- License: MIT
- License bundled at `app/src/main/assets/licenses/QURAN_META_MIT.txt`

## QuranEnc

- Use: Arabic Muyassar tafsir requested per ayah and cached locally
- Distribution: not bundled as a corpus in the application
- Integration: fixed HTTPS endpoint, response-size cap, timeouts, JSON content check, no redirects
- Content handling: translation and footnotes are preserved without modification
- Display attribution: `التفسير الميسر — QuranEnc.com`

QuranEnc terms require clear source attribution, unmodified content, version identification when republishing, and updates to the latest provider version. The app currently fetches content per ayah rather than bundling a corpus. The publisher must fetch and record the active `arabic_moyassar` version from the provider translation-list endpoint and reconfirm current API/content terms before every release.

## EveryAyah

- Use: on-demand MP3 recitation streaming
- Distribution: audio files are not included or redistributed in the APK/AAB

The publisher must obtain and retain written confirmation that the selected recitations and hosting
may be used by the released application. Provider availability and terms are outside the repository.

## Curated adhkar and duas

- Use: a small offline starter catalog for morning, evening, after-prayer, sleep, waking, and general remembrance.
- Sources: Quran verses and well-known narrations identified by their primary collection in each card.
- Packaging: text and references are bundled locally; the feature makes no adhkar network requests.
- Editorial rule: the app does not add promised virtues or prescribed repetition counts unless the bundled reference explicitly supports them. User-selectable tasbih targets are counting tools, not religious rulings.

The publisher should conduct a qualified Arabic proofread and source review before public release and
record any later wording or catalog changes in release notes.

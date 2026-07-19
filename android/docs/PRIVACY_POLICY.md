# Noor privacy policy draft

Effective date: July 16, 2026

Noor is designed without accounts, advertising, analytics, tracking SDKs, crash-reporting SDKs,
or social sign-in.

## Data stored on the device

Noor stores the following information only in the application's private device storage:

- Quran bookmarks.
- Last-read surah and ayah.
- Theme, dynamic-color, Quran text-size, and reciter preferences.
- Tafsir content previously requested by the user, together with source and cache time.
- Adhkar favorite selections and optional morning/evening reminder choices.

Application backup is disabled. Noor does not request access to contacts, precise or approximate
location, photos, files, camera, or microphone. On Android 13 and later, notification permission is
requested only when the user enables an adhkar reminder. Reminder work is scheduled locally with
WorkManager; no reminder preference is sent to a server.

## Network requests

The complete Quran text, chapter/page metadata, and curated adhkar collection are bundled in the
application and work offline. Adhkar search, favorites, counters, tasbih, and reminder scheduling do
not require a network connection. Network access occurs only when the user:

1. Streams recitation audio from the selected audio provider.
2. Requests tafsir that is not already cached locally.
3. Opens a source website from the legal-notices screen.

Those external providers receive the normal technical information required for an HTTPS request,
which can include IP address, request time, requested resource, and user-agent information. Their
handling of that information is governed by their own policies. Noor does not combine that network
information with an account or advertising identifier.

## Sharing and sale

Noor does not sell personal data. Noor does not send user bookmarks, reading history, preferences,
or cached tafsir to the developer or to advertising/analytics services.

## Retention and deletion

Bookmarks, adhkar favorites, reminder choices, and preferences remain until the user changes them,
clears application storage, or uninstalls Noor. Cached tafsir is eligible for automatic deletion after 90 days and can also be removed by clearing application storage or uninstalling the application.

## Children

Noor does not knowingly collect personal information from children and contains no account,
advertising, social, or user-generated-content features.

## Publisher information required before publication

The Google Play listing must host this policy at a public HTTPS URL and add the legal publisher
name and support/privacy contact address. Those publisher-specific values are intentionally not
invented in the source repository.

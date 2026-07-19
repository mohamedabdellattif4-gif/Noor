# Security policy

## Supported versions

Security fixes are applied to the latest release branch and the current development branch.

## Reporting a vulnerability

Do not open a public issue for a suspected vulnerability. Send a private report to the publisher's
security contact after it is configured for distribution. Include the affected version, Android
version, reproduction steps, impact, and any proof-of-concept material.

Until a publisher address is selected, keep reports private within the project team. Never include
Play signing keys, keystore passwords, provider credentials, or user data in an issue or log.

## Security boundaries

Noor has no account system and stores only Quran content, non-sensitive preferences, bookmarks, and
cached public tafsir. Network access is restricted to fixed HTTPS content providers. See
`docs/SECURITY_REVIEW.md` for the reviewed threat model and remaining external release gates.

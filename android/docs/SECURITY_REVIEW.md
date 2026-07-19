# Security review

## Threat surface

Noor has no account, WebView, user-generated content, file picker, camera, microphone, contacts,
location, advertising SDK, analytics SDK, or embedded credential. Network access is limited to
user-initiated public recitation and tafsir HTTPS requests.

## Findings and remediation

| Finding | Severity | Resolution |
|---|---:|---|
| Cleartext network traffic could be accepted | High | Disable cleartext in both the manifest and network-security configuration |
| Tafsir transport could follow redirects or consume an unbounded response | High | Disable redirects, enforce timeouts and JSON, and cap raw response bytes at 1 MiB |
| Media playback needs an exported service but must not expose arbitrary app commands | High | Export only the Media3 `MediaSessionService` endpoint required for system media controllers; keep the default Media3 trust/command policy and expose no custom commands |
| Unnecessary permissions increase privacy and attack surface | High | Keep the manifest limited to Internet, notification opt-in, wake lock, and the two media-playback foreground-service permissions required by implemented features |
| User data could enter Android cloud/device backup | Medium | Disable backup explicitly |
| URL construction accepted unchecked positions | Medium | Validate chapter/verse inputs and use fixed HTTPS hosts |
| FTS input could inject operators or pathological complexity | High | Normalize and quote terms, then enforce character, token, token-length and result bounds |
| Destructive migration could erase user data | Medium | Forbid destructive fallback and register explicit migrations |
| Secrets could be committed | Medium | Scan source for likely secret assignments; the app requires no API key |
| Cached data or bookmarks could be exposed through exported components | Medium | Export only the launcher activity and the Media3 playback service required for system media controls; keep the startup provider non-exported and expose no content provider or custom media commands |
| Signing secrets could be partial, missing, committed, or referenced through a symlink | High | Require all signing variables together, compare canonical/resolved paths, reject repository-contained keystores, and verify the final AAB signature |
| Dependency and Kotlin vulnerabilities could escape source regex checks | Medium | Add CodeQL Java/Kotlin analysis, Dependency Review, Dependabot, and Gradle dependency-graph submission |
| Production builds could expose shell profiling | Medium | Put `profileable` only in the Benchmark and non-minified profile-generation source-set manifests; Release remains non-profileable |

## Media-service decision

The playback service is intentionally `android:exported="true"` because Media3 background playback
uses an exported `MediaSessionService` so trusted system controllers, Bluetooth and notification
controls can connect. The service publishes only the Media3 session action. It does not publish a
content library or custom command surface. Media3 distinguishes controllers trusted by the user or
system; Noor relies on that standard session policy instead of inventing an incompatible private
media protocol.

## Deliberate choices

Certificate pinning is not used. Noor consumes public content from independently operated services;
pinning adds outage and certificate-rotation risk without protecting a credential or private API.
The app instead uses platform TLS, system trust anchors, HTTPS-only policy, fixed hosts, bounded
responses and no redirect following for tafsir.

Bookmarks and preferences are not application-encrypted because they are non-secret data inside the
Android application sandbox and the requirements include no identity or high-sensitivity threat
model. Application-level encryption would require product decisions for key recovery, device
transfer and data-loss behavior.

## Release and supply-chain controls

`tools/release_preflight.py` enforces JDK/SDK/version/signing requirements before a release.
`tools/release_gate.sh` requires a complete external keystore configuration and rejects a final AAB
whose signature cannot be verified. CI runs CodeQL, dependency review, weekly dependency updates,
and Gradle dependency submission. Wrapper downloads remain checksum-pinned and CI validates the
resulting Wrapper JAR.

## Re-review result

The source review found no remaining known repository-fixable high-severity security issue. CI and
Android instrumentation gates are configured, but they could not be executed in the generation
environment because the Android SDK and Gradle Wrapper JAR are unavailable there. Signed-artifact
analysis, device testing, provider terms/privacy confirmation, and Play Console declarations remain
external release gates.

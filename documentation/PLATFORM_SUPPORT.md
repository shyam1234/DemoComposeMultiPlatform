# Platform Support Matrix

Verified against official JetBrains / Kotlin documentation (as of late 2025 / 2026 docs):

- [KMP supported platforms](https://kotlinlang.org/docs/multiplatform/supported-platforms.html)
- [Compose Multiplatform compatibility](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html)
- [Android TV quality](https://developer.android.com/develop/adaptive-apps/quality-guidelines/tv-app-quality)
- [Designing for tvOS](https://developer.apple.com/design/human-interface-guidelines/designing-for-tvos)

## Support matrix

| Platform | KMP logic | Shared CMP UI | Native UI | Player | Demo status |
|----------|-----------|---------------|-----------|--------|-------------|
| Android Mobile | Stable | Stable CMP | — | Media3 ExoPlayer | Primary target |
| Android Tablet | Stable | Stable CMP (adaptive) | — | Media3 | Primary target |
| Android TV / STB | Stable | Stable CMP + TV UX | Optional Leanback bits | Media3 | Primary target |
| iPhone | Stable | Stable CMP | — | AVPlayer | Verified on device (Xcode + signing) |
| iPad | Stable | Stable CMP (adaptive) | — | AVPlayer | Supported (same `iosApp`) |
| macOS Desktop | Stable | Stable CMP Desktop | — | AVPlayer / desktop adapter | Primary JVM target |
| Apple TV (tvOS) | KMP **Beta** | **Not official CMP** | **SwiftUI** | AVPlayer | Native UI + shared logic |
| Web / Wasm (`wasmJs`) | Beta / Experimental | CMP Web (`ComposeViewport`) | — | HTML5 `<video>` + `hls.js` | **Verified** (`apps/webApp`, GitHub Pages) |
| watchOS | Beta | No | — | — | **Out of scope** |

## Important disclosures

### Compose Multiplatform does not officially support tvOS UI

CMP stable UI targets are **Android, iOS, Desktop (JVM), and Web (Beta)**.  
tvOS appears under **KMP** as **Beta**, but there is **no official Compose Multiplatform UI tier for tvOS**.

**Decision:** Apple TV uses **SwiftUI** for presentation and consumes shared Kotlin for catalogs, watchlist rules, and player contracts. We do **not** rely on community CMP-tvOS forks for this production-oriented architecture.

### Android TV is not “phone UI scaled up”

Android TV gets:

- D-pad / remote focus management
- Clear focus indicators
- Larger type and 10-foot UI spacing
- Row-first browsing
- No touch-only interactions

Implementation lives primarily in CMP with TV-specific components (`platform/androidtv` + design tokens), not a separate non-Compose stack.

### Environment limitations on this machine (documented)

| Tooling | Observed | Impact |
|---------|----------|--------|
| JDK 17 (Homebrew) | Available | Android / Desktop builds |
| Android SDK | Available (API 34–36) | Android app builds |
| Full Xcode | Required for iOS | `apps/iosApp` + `platform/ios`; CLT alone is not enough |
| Apple Silicon vs Intel | Host may vary | Device `iosArm64` OK; Apple Silicon sim OK; **Intel `x86_64` sim not supported** (no Compose `iosX64`) |
| Device trust | Personal Team | Enable Developer Mode; Trust developer after first install |

Anything not executed in CI/local is marked **unverified** in test reports — never claimed as passing.

## UX expectations by platform

| Platform | Navigation | Input | Layout |
|----------|------------|-------|--------|
| Phone | Bottom bar | Touch | Single column |
| Tablet | Nav rail / adaptive | Touch | Multi-column shelves |
| Android TV | Top tabs / side nav | D-pad | Landscape shelves, focus |
| iPhone / iPad | Tab / stack | Touch | Safe areas, adaptive |
| macOS | Side nav / top bar | Mouse + keyboard | Wide window layouts |
| Web (`wasmJs`) | Responsive (top bar >= 840dp, bottom bar < 840dp) | Mouse + keyboard / touch | Wide OTT shelves or mobile column |
| Apple TV | Tab bar / focus engine | Siri Remote | SwiftUI TV layouts |

## Official links to keep nearby

- Compose platforms: https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html
- KMP architecture: https://kotlinlang.org/docs/multiplatform/multiplatform-architecture.html
- Android TV quality: https://developer.android.com/develop/adaptive-apps/quality-guidelines/tv-app-quality
- tvOS HIG: https://developer.apple.com/design/human-interface-guidelines/designing-for-tvos

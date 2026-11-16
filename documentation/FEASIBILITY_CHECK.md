# Compose Multiplatform (CMP) Feasibility & Production Readiness Check

This document defines the **technical feasibility criteria, benchmark suite, and Go / No-Go scorecard** required to build full confidence before committing **Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP)** to production.

It combines general CMP production benchmarks with the current state observed in this POC (`Kotlin 2.4.21`, `CMP 1.11.1`, `Ktor 3.1.3`, `Koin 4.1.0`, `SQLDelight 2.4.1`, `Coil 3.2.0`, `Media3 1.7.1`).

---

## Executive Decision Framework

Before moving beyond POC, every pillar below should reach **Pass** or **Accepted Mitigation** (with a documented native fallback):

| Rating | Meaning | Action |
|--------|---------|--------|
| **PASS** | Verified on real target hardware against numeric thresholds | Proceed with CMP |
| **CONDITIONAL** | Known limitation with a clean native boundary (`expect/actual` or native shell) | Proceed with documented boundary |
| **BLOCKER** | Fails SLA (frame drops, crash rate, DRM, binary size, CI velocity) with no viable workaround | Re-evaluate CMP for that surface |

### Current POC Baseline (Snapshot)

| Pillar | Current POC Status | Gap to Production Confidence |
|--------|--------------------|------------------------------|
| **UI Support (CMP)** | Verified on Android, Android TV shell, Desktop JVM, and iOS device (`OttShared` static framework); tvOS scoped to SwiftUI | Frame pacing / scroll benchmarks on low-end Android & iOS, D-pad focus QA on TV, `RemoteImage` on Desktop (`Box` stub today), accessibility & IME edge cases |
| **Network Support** | Ktor 3.1.3 wired (`OkHttp` Android, `Darwin` iOS, `CIO` Desktop) + `kotlinx.serialization` + `ktor-client-mock` tests | Auth token refresh, TLS cert pinning, HTTP disk cache, offline/reachability, retry/backoff policies |
| **Database Support** | **SQLDelight 2.4.1 implemented** in `:core:database` (`AndroidSqliteDriver`, `NativeSqliteDriver`, `JdbcSqliteDriver`); offline catalog cache (29 categories / 320 channels verified on Android `SM-A076B` & `iPhone 13`) + `PersistentWatchlistRepository` + JVM tests (`SqlDelightDataSourcesTest`, `IptvCatalogRepositoryOfflineTest`) | Schema migration tests (`v1 -> v2`), 10k-row batch benchmarks, SQLCipher encryption (if needed), and secure Keystore/Keychain storage for auth/DRM tokens |
| **Playback & Hardware** | `PlayerEngine` contract with Media3 (Android) & `AVPlayer` (iOS); `FakePlayerEngine` on Desktop/tests | DRM (Widevine / FairPlay), PiP, background audio & lock-screen controls, Chromecast / AirPlay |
| **Observability & Ops** | `commonTest` runs on JVM (`desktopTest`) | Crash symbolication (iOS dSYM + K/N traces), startup/memory/IPA size budgets, CI macOS build times |

---

## 1. UI Support Feasibility (Compose Multiplatform)

### 1.1 Target Support Matrix & Boundaries

Verify official JetBrains stability tiers for every product surface before committing shared UI (see [PLATFORM_SUPPORT.md](PLATFORM_SUPPORT.md)):

- **Android Mobile / Tablet**: Stable CMP. Verify adaptive layouts (`WindowSizeClass`), foldables, and edge-to-edge insets.
- **Android TV / STB**: Stable CMP, but requires TV-first UX (D-pad focus traversal, 10-foot typography, overscan margins, Leanback launcher).
- **iPhone / iPad (`iosArm64`, `iosSimulatorArm64`)**: Stable CMP via Skia/Metal. Note that CMP 1.11+ drops `iosX64` (Intel simulators are unsupported).
- **macOS Desktop (JVM)**: Stable CMP Desktop. Note that in this POC, `core/ui/.../RemoteImage.desktop.kt` is currently a text placeholder rather than Coil-backed.
- **Apple TV (`tvOS`)**: **No official CMP UI support** (KMP is Beta). Keep UI in **SwiftUI** consuming shared KMP domain/data/player modules ([ADR-002](ARCHITECTURE_DECISIONS.md)).
- **Web (Wasm) / watchOS**: Out of scope / Beta—do not include in production UI estimates without separate spikes.

### 1.2 UI Benchmarks & Edge Cases to Test

| Area | What to Benchmark / Validate | Target / Pass Criteria |
|------|------------------------------|------------------------|
| **Scroll & Animation Fluidity** | Nested `LazyColumn` + `LazyRow` carousels (e.g., 20+ shelves, 30+ cards/row with remote images) on mid-tier Android and iPhone | `< 1%` janky frames (>16.6ms on 60Hz, >8.3ms on 120Hz ProMotion); zero skipped frames during fling |
| **Platform Interop (`UIKitView` / `AndroidView`)** | Embedding native video surfaces (`AVPlayerLayer`, `PlayerView`), WebViews, or ads inside CMP layouts; touch/focus routing over native views | Zero z-order clipping, rotation glitches, or gesture conflicts between Compose and native subviews |
| **iOS Native Feel & System Integration** | Predictive/edge swipe-back gesture, UITextField/IME keyboard insets, copy/paste context menus, Dynamic Type scaling, notch/Dynamic Island safe areas | Matches iOS HIG expectations; keyboard never obscures focused inputs |
| **Android TV D-Pad & Focus** | Directional focus movement across rows/columns, focus memory when returning from Details/Player, long-press key events | 100% reachable via remote; visible focus ring on every interactive node |
| **Navigation & State Restoration** | `jetbrains-navigation-compose` deep links (universal links / app links), backstack preservation, Android process death (`SavedStateHandle`), iOS background restoration | Zero lost navigation arguments or crashed restorations after OS process kill |
| **Resources & Localization** | `compose.components.resources` images, custom fonts, pluralized strings, RTL layout mirroring (Arabic/Hebrew) | All assets resolve across Android/iOS/Desktop; zero layout overflow in RTL or 200% font scale |
| **Accessibility** | TalkBack (Android), VoiceOver (iOS), focus order, semantic labels, minimum touch targets (48dp / 44pt) | Screen readers navigate full core journeys without unlabeled nodes or focus traps |
| **Image Pipeline Parity** | Coil 3 (`coil-network-okhttp` on Android, `coil-network-ktor` on iOS, plus Desktop wiring), disk/memory cache limits, WebP/AVIF/SVG decoding | Memory cap enforced; fast thumbnail recycling without OOM on low-RAM devices |

---

## 2. Network Support Feasibility (Ktor & Serialization)

This POC already uses **Ktor 3.1.3** (`OkHttp` on Android, `Darwin` on iOS, `CIO` on Desktop) with `kotlinx.serialization.json`. Before production sign-off, verify the following real-world networking requirements across **all engines**:

### 2.1 Engine & Protocol Parity

| Capability | Android (`OkHttp`) | Apple (`Darwin`) | Desktop (`CIO`) | Verification Required |
|------------|-------------------|------------------|-----------------|----------------------|
| **HTTP/2 & HTTP/3** | HTTP/2 default | HTTP/2 + HTTP/3 (`NSURLSession`) | HTTP/1.1 & HTTP/2 | Verify multiplexed API latency & connection reuse on each target |
| **TLS & Certificate Pinning** | `CertificatePinner` | `NSURLSession` challenge delegate | Custom `TrustManager` | Implement and test cert pinning + pin rotation failure handling on both Android & iOS |
| **Corporate Proxy / VPN / IPv6** | System proxy aware | System PAC / VPN aware | Needs explicit config check | Test behind Charles/Proxyman, IPv6-only NAT64 networks (App Store requirement), and VPN |

### 2.2 Production Network Benchmarks & Features

- **Authentication & Token Refresh**:
  - Single-flight token refresh (`Mutex` / Ktor `Auth` plugin) when parallel API calls receive `401 Unauthorized`.
  - Verify refresh works cleanly when the app wakes from background on iOS (`Darwin` background task constraints).
- **Offline, Reachability & Resilience**:
  - Cross-platform network reachability observer (`ConnectivityManager` on Android, `NWPathMonitor` on iOS) exposed as `Flow<NetworkStatus>` to repositories.
  - Retry with exponential backoff + jitter for idempotent `GET` calls; circuit-breaker behavior for upstream outages.
- **HTTP Caching & Bandwidth**:
  - Verify whether Ktor `HttpCache` / `NSURLCache` / `OkHttp Cache` behaves consistently for `ETag` and `Cache-Control` headers, or standardize caching in the repository/DB layer.
- **Payload Parsing Throughput**:
  - Benchmark `kotlinx.serialization` decoding of large catalog/EPG JSON payloads (e.g., 5–10 MB responses) off the main thread (`Dispatchers.Default`).
  - Ensure `ignoreUnknownKeys = true` and `coerceInputValues = true` protect against backend schema additions.
- **Background Transfers**:
  - For offline video downloads or large asset sync, Ktor in-process requests are insufficient when iOS suspends the app—plan native `WorkManager` (Android) and `URLSessionConfiguration.background` (iOS).

---

## 3. Database & Local Persistence Support Feasibility

> **Status in this POC:** `:core:database` is now implemented with **SQLDelight 2.4.1** (`AndroidSqliteDriver`, `NativeSqliteDriver`, `JdbcSqliteDriver`), providing `PersistentWatchlistRepository` and offline catalog caching (`IptvCatalogRepository` with 6h TTL + offline fallback), verified on physical Android (`SM-A076B`), iPhone (`iPhone 13`), and JVM tests. Note: on Kotlin/Native iOS, use `kotlin.time.Clock.System` rather than `kotlinx.datetime.Clock.System` unless `kotlinx-datetime` linkage is aligned. Production hardening below (migrations, 10k-row throughput, encryption, and Keystore/Keychain) still needs formal benchmarking.

### 3.1 Candidate Storage Technologies

Evaluate and lock in both **structured persistence** and **secure key-value storage**:

| Layer | Candidate A | Candidate B | Recommendation Criteria |
|-------|-------------|-------------|-------------------------|
| **Relational DB** | **SQLDelight 2.x** (SQL-first, mature KMP across Android/iOS/Desktop/tvOS, `coroutines-extensions` `Flow`) | **AndroidX Room KMP (2.7+)** (DAO/annotation-first, KSP, `androidx.sqlite:sqlite-bundled` for cross-platform SQLite parity) | Benchmark schema migration ergonomics, KSP/Gradle build speed, SQLite version parity, and team familiarity |
| **Preferences** | **AndroidX DataStore KMP** (`PreferencesDataStore` / Proto) | **Multiplatform Settings** (`SharedPreferences` / `NSUserDefaults`) | Prefer DataStore for async corruption-safe reads/writes; avoid synchronous `NSUserDefaults` on hot paths |
| **Secure Storage** | **Platform Keystore / Keychain** (`expect/actual`: Android Keystore + Encrypted storage, Apple Keychain Services) | — | **Mandatory** for auth tokens, refresh tokens, user IDs, and DRM offline license keys |

### 3.2 Database Benchmarks & Edge Cases to Cover

1. **Migration Safety & Zero-Data-Loss Upgrades**:
   - Write automated schema migration tests (v1 -> v2 -> v3) running in CI on JVM, Android, and iOS.
   - Verify foreign-key enforcement (`PRAGMA foreign_keys = ON`) and WAL mode (`PRAGMA journal_mode = WAL`) across drivers.
2. **Large Dataset Query & Write Throughput**:
   - Benchmark batch upsert of **10,000+ catalog/EPG records** inside a single transaction and time reactive `Flow` query emissions.
   - Confirm zero main-thread disk I/O (`StrictMode` enabled on Android debug builds; Instruments file I/O check on iOS).
3. **iOS / Kotlin-Native Concurrency & Backgrounding**:
   - Test concurrent reads (UI observing `Flow`) + writes (background sync) under Kotlin/Native's memory model without `SQLITE_BUSY` lock errors.
   - Verify iOS `NSFileProtection` attributes so background syncs or silent pushes don't fail while the device is locked.
4. **Encryption at Rest (If Required by Compliance/DRM)**:
   - Spike SQLCipher integration cost across Android NDK (`android-database-sqlcipher`) and iOS CocoaPods/XCFramework linking, plus binary size impact.
5. **Backup & Eviction Hygiene**:
   - Exclude large cache DBs from iOS iCloud Backup (`NSURLIsExcludedFromBackupKey`) and Android Auto Backup (`full-backup-content`), while preserving user watchlist/settings as intended.

---

## 4. Additional Benchmarks Required for Full Production Confidence

Beyond UI, Network, and Database, OTT and multiplatform apps commonly fail on the following operational and platform-integration dimensions:

### 4.1 Media Playback, DRM & Platform Services (OTT-Specific)

- **DRM Parity**: Widevine L1/L3 (`Media3` on Android) vs FairPlay Streaming (`AVContentKeySession` + `AVPlayer` on iOS/macOS/tvOS) behind `PlayerEngine` (`core/player-api`).
- **System Playback Integration**:
  - Picture-in-Picture (PiP) handoff from Compose screen to native PiP window and back.
  - Background audio continuation + lock-screen / Control Center metadata & scrubbing (`MediaSession` vs `MPNowPlayingInfoCenter` / `MPRemoteCommandCenter`).
  - Casting (`CastContext` / Google Cast on Android; AirPlay route picker on iOS).
- **Codec & Stream Edge Cases**: HLS/DASH adaptive bitrate switching, audio track / subtitle selection (WebVTT, CEA-608/708), and rapid channel zapping latency (time-to-first-frame).

### 4.2 App Performance, Memory & Binary Size

| Metric | How to Measure | Suggested Production Threshold |
|--------|----------------|-------------------------------|
| **Cold Startup Time** | Android Macrobenchmark (`StartupTimingMetric`); iOS `os_signpost` / Instruments App Launch | `< 1.5s` on mid-tier hardware (including Koin init + Skia warm-up) |
| **App Binary / Download Size** | Android App Bundle (AAB) per-ABI size; iOS App Store thinned IPA size (`OttShared.framework` + Skia) | Delta over native baseline `< 15–20 MB` compressed |
| **Idle & Peak Memory** | Android Profiler / `dumpsys meminfo`; Xcode Memory Gauge & Allocations | Stable under scrolling; no monotonic growth after opening/closing Player 20x |
| **Recomposition & CPU** | Compose Compiler metrics/reports (`strongSkipping`, skippable composables) + Layout Inspector | Zero unstable domain models causing full-screen recompositions |

### 4.3 Swift / iOS Interop & Lifecycle Ergonomics

Since iOS hosts CMP inside `UIViewController` and **tvOS uses SwiftUI** against shared KMP:

- **Kotlin -> Swift API Ergonomics**: Evaluate **SKIE** or **KMP-NativeCoroutines** for consuming Kotlin `StateFlow`, `suspend` functions (cancellation support), and `sealed class` exhaustive `switch` statements in SwiftUI.
- **Lifecycle & Memory Leaks**: Verify `ViewModel` clearing (`ViewModelStoreOwner`) when dismissing an iOS `UIViewController` or navigating away, and confirm zero retain cycles between Swift closures and Kotlin objects.
- **Xcode Build & Debug Loop**: Measure clean and incremental time of `./gradlew :apps:shared:embedAndSignAppleFrameworkForXcode` during active iOS development.

### 4.4 Third-Party SDK Ecosystem

Audit every required non-core SDK for KMP readiness or `expect/actual` wrapper effort:

- **Crash Reporting & Symbolication**: Sentry KMP or Firebase Crashlytics (verify Kotlin/Native crash stack traces + iOS dSYM upload in CI gives readable Kotlin file/line numbers).
- **Analytics & Feature Flags**: Unified analytics wrapper (`expect/actual` over Firebase/Amplitude/Segment).
- **Push Notifications & Deep Linking**: FCM (Android) + APNs (iOS) routing into `jetbrains-navigation-compose`.
- **Monetization / Ads / Auth**: Google Play Billing vs StoreKit 2, IMA Ad SDK (native player overlays), SSO / Apple Sign-In / Google Sign-In.

### 4.5 Testing, CI/CD & Team Scalability

- **Test Execution**:
  - Domain, ViewModel, Mappers, and `PlayerController` tests run fast on JVM (`commonTest` via `./gradlew :core:domain:desktopTest`).
  - Add automated **UI tests**: Compose UI tests on Android/Desktop + `XCUITest` on iOS/tvOS.
  - Add **Koin DI graph verification** (`koin-test` `verify()`) in CI so missing bindings fail at build time, not runtime.
- **CI Runner Topology & Cost**:
  - Android & Desktop unit/assemble jobs run on Linux runners.
  - iOS / tvOS framework linking, Xcode archiving, and simulator tests require **macOS CI runners**—benchmark runner minutes per PR.
- **Upgrade & Lockstep Risk**:
  - Kotlin, Compose Multiplatform, AGP, and Xcode updates are tightly coupled. Verify rollback/upgrade cadence and avoid unmaintained community KMP libraries in the critical path.

---

## 5. Feasibility Scorecard & Sign-Off Checklist

Use this scorecard to track PoC validation before greenlighting production development:

| # | Benchmark Area | Target / Acceptance Criterion | Status | Evidence / Notes |
|---|----------------|------------------------------|--------|------------------|
| 1 | **CMP UI — Mobile & Tablet** | Smooth 60/120fps scrolling, safe areas, IME, orientation/adaptive layout on physical Android & iPhone/iPad | `[ ] Pending` | POC runs on Android & iOS; needs formal frame-drop profiling |
| 2 | **CMP UI — Android TV** | 100% D-pad navigation, focus restoration, Leanback compliance on TV hardware/emulator | `[ ] Pending` | `androidTvApp` shell exists; needs D-pad QA |
| 3 | **CMP UI — Desktop (macOS)** | `RemoteImage` implemented (replace stub), keyboard/mouse UX, window resizing | `[ ] Pending` | `RemoteImage.desktop.kt` is currently a placeholder |
| 4 | **Native UI Boundary (tvOS)** | SwiftUI consumes shared KMP `Flow`/use cases cleanly (SKIE/bridge verified) | `[ ] Pending` | Documented in ADR-002; needs SwiftUI spike |
| 5 | **Networking (Ktor)** | Auth refresh, TLS pinning, offline/reachability, error mapping, JSON throughput verified on OkHttp + Darwin | `[ ] Pending` | Basic Ktor M3U/JSON + mock tests exist |
| 6 | **Database & Migrations** | SQLDelight or Room KMP benchmarked (10k writes, reactive `Flow`, v1->v2 migration test) on Android + iOS | `[~] Partial` | `:core:database` (SQLDelight 2.4.1) implemented & verified offline on Android (`SM-A076B`) + `iPhone 13` + JVM tests; migration & 10k-row stress benchmarks remain |
| 7 | **Secure Storage** | Keystore (Android) + Keychain (iOS/tvOS) wrapper tested across app upgrades & reinstalls | `[ ] Not Started` | Required for auth/session tokens |
| 8 | **Media & DRM** | Widevine + FairPlay, PiP, background playback, and `UIKitView`/ExoPlayer surface lifecycle leak-free | `[ ] Pending` | Clear-stream Media3/AVPlayer verified in POC |
| 9 | **Startup, RAM & Binary Size** | Cold start `< 1.5s`, size delta `< 20 MB`, zero leaks in 20x player open/close stress test | `[ ] Pending` | Needs Macrobenchmark + Xcode Instruments run |
| 10 | **Crash Symbolication & CI** | Readable K/N crash stacks in Crashlytics/Sentry; PR CI pipeline `< 15 min` including iOS build | `[ ] Pending` | Only local JVM tests executed so far |

---

## 6. Recommended Validation Plan (2-Week Confidence Sprint)

To close the remaining gaps above with minimal wasted effort:

1. **Days 1–3 — Database & Secure Storage Spike**:
   - Add a `core/database` module using **SQLDelight** or **Room KMP** (implementing `WatchlistRepository` + offline catalog cache) plus a Keystore/Keychain token store. Run migration and 10k-row batch benchmarks on both Android and iOS.
2. **Days 4–6 — Production Networking Hardening**:
   - Add auth interceptor with concurrent `401` refresh test, `NWPathMonitor` / `ConnectivityManager` reachability flow, and TLS pinning test on both `OkHttp` and `Darwin`.
3. **Days 7–9 — UI, Desktop Image & Performance Profiling**:
   - Wire Coil 3 in `RemoteImage.desktop.kt`, run Android Macrobenchmark (startup + scroll frame timing) and Xcode Instruments (Core Animation, Allocations, IPA size), and test RTL + Dynamic Type + TalkBack/VoiceOver on core screens.
4. **Days 10–12 — OTT Playback, Crash Symbolication & tvOS Bridge**:
   - Validate PiP + background controls + DRM spike on `Media3` and `AVPlayer`, verify Kotlin/Native dSYM symbolication, and run a minimal SwiftUI screen consuming shared KMP `StateFlow` (testing SKIE if needed).
5. **Days 13–14 — Go / No-Go Review**:
   - Fill out Section 5 with real numbers from physical devices and sign off on target-by-target CMP adoption.

# OttPlatform — Compose Multiplatform OTT Demo

Modular, production-oriented **OTT streaming demo** built with **Kotlin Multiplatform** and **Compose Multiplatform (CMP)**.

Shared business logic and shared UI power Android (phone, tablet, TV), iOS, macOS Desktop, and **Web (`wasmJs`)**. **Apple TV uses SwiftUI** for UI (CMP does not officially support tvOS) while reusing Kotlin domain/data/player contracts.

## Start here — architecture docs

| Document | What you’ll learn |
|----------|-------------------|
| **[ARCHITECTURE.md](documentation/architecture/ARCHITECTURE.md)** | System overview, Clean Architecture, CMP strategy, Mermaid diagrams |
| [MODULE_STRUCTURE.md](documentation/MODULE_STRUCTURE.md) | Folders, Gradle modules, responsibilities |
| [DEPENDENCY_RULES.md](documentation/DEPENDENCY_RULES.md) | Allowed / forbidden module edges |
| [PLATFORM_SUPPORT.md](documentation/PLATFORM_SUPPORT.md) | Official support matrix & disclosures |
| [PLAYER_ARCHITECTURE.md](documentation/PLAYER_ARCHITECTURE.md) | `PlayerEngine`, Media3 / AVPlayer adapters |
| [FEATURE_EXTRACTION_GUIDE.md](documentation/FEATURE_EXTRACTION_GUIDE.md) | Enable / disable / extract features safely |
| [TESTING_GUIDE.md](documentation/TESTING_GUIDE.md) | Unit, UI, and platform test strategy |
| [BUILD_AND_RUN.md](documentation/BUILD_AND_RUN.md) | JDK, Gradle, run commands |
| [ARCHITECTURE_DECISIONS.md](documentation/ARCHITECTURE_DECISIONS.md) | ADRs and trade-offs |
| [FEASIBILITY_CHECK.md](documentation/FEASIBILITY_CHECK.md) | CMP UI, network, database & production readiness benchmarks |

## Architecture at a glance

```mermaid
flowchart TB
    subgraph CMP["Compose Multiplatform UI"]
        Phone["Android Mobile/Tablet"]
        ATV["Android TV"]
        iOS["iPhone/iPad"]
        Mac["macOS Desktop"]
        Web["Web (wasmJs)"]
    end

    subgraph Native["Native UI"]
        tvOS["Apple TV — SwiftUI"]
    end

    subgraph Shared["Shared Kotlin"]
        Domain["Domain + Use cases"]
        Data["Data + Offline SQLite cache"]
        Database["core/database (SQLDelight)"]
        Player["PlayerEngine contract"]
    end

    Phone --> Shared
    ATV --> Shared
    iOS --> Shared
    Mac --> Shared
    Web --> Shared
    tvOS --> Shared
```

## Design pillars

1. **Clean Architecture** — UI → domain ← data; domain stays platform-free  
2. **Feature modules** — `features/<name>/api` + `impl` for independent extraction  
3. **CMP first** — shared UI on officially supported targets  
4. **Native playback** — Media3 on Android, AVPlayer on Apple platforms  
5. **Testable by default** — fakes + `commonTest` without devices  

## Independence checks

| Check | Intent |
|-------|--------|
| Remove Search feature | Home / Player must still build |
| Replace ExoPlayer binding | Player UI unchanged |
| Run domain tests on JVM | No Android/iOS device required |

## Tech stack

- Kotlin **2.4.21** · Compose Multiplatform **1.11.1** (AGP 8.10 / compileSdk 36)
- Coroutines · Serialization · Ktor · Koin
- **SQLDelight 2.4.1** (`:core:database` — Android, iOS, Desktop SQLite drivers) + `localStorage` persistence on Web (`wasmJs`) for offline catalog caching & persistent watchlist
- Media3 ExoPlayer (Android) · AVPlayer (iOS) · HTML5 `<video>` + `hls.js` (`platform/web`) · FakePlayerEngine (desktop/tests)
- **iptv-org** free category M3U + categories JSON

## Demo data: free IPTV API

Live catalog from **iptv-org** (no key):

- Categories: `https://iptv-org.github.io/api/categories.json`
- Shelves: `https://iptv-org.github.io/iptv/categories/{news|movies|sports|…}.m3u`

See [IPTV_DATA_SOURCE.md](documentation/IPTV_DATA_SOURCE.md).

## Run

```bash
export JAVA_HOME=/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew :apps:androidApp:assembleDebug
./gradlew :apps:androidTvApp:assembleDebug
./gradlew :apps:desktopApp:run
./gradlew :apps:webApp:wasmJsBrowserDistribution
```

**Web (`wasmJs`):** serves at `http://localhost:8080/` locally and deploys to GitHub Pages via `.github/workflows/deploy-web.yml` (`https://shyam1234.github.io/DemoComposeMultiPlatform/`).

**iOS:** open `apps/iosApp/iosApp.xcodeproj` in full Xcode, set a Development Team, run on a **physical device** (Intel simulators are not supported). Details: [BUILD_AND_RUN.md](documentation/BUILD_AND_RUN.md) · [apps/iosApp/README.md](apps/iosApp/README.md).

## Unit tests (executed)

```bash
./gradlew :core:database:desktopTest :core:data:desktopTest :core:domain:desktopTest :core:player-api:desktopTest
```

**Passed on this machine:** Search/Home use cases, M3U parser, SQLDelight catalog & watchlist persistence, offline fallback repository, and PlayerController.

## Status

Android phone/tablet/TV + Desktop + **Web (`wasmJs`)** + **iOS device** (CMP + AVPlayer + SQLDelight offline cache) are verified. Shared UI (splash, responsive top/bottom nav, hero/shelves, search, details, watchlist, player) runs on Android, iPhone, and Web (`apps/webApp` via `OttShared` / `apps/shared`), with 29 categories and 320 channels cached offline (`ott_platform.db` on native, `localStorage` on Web). Apple TV remains SwiftUI + shared KMP (not CMP UI).

## Official references

- [Compose Multiplatform platforms](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html)
- [KMP architecture](https://kotlinlang.org/docs/multiplatform/multiplatform-architecture.html)
- [Android TV quality](https://developer.android.com/develop/adaptive-apps/quality-guidelines/tv-app-quality)
- [tvOS HIG](https://developer.apple.com/design/human-interface-guidelines/designing-for-tvos)

# Build and Run

## Prerequisites

| Tool | Recommended | Notes |
|------|-------------|-------|
| JDK | **17** | Set `JAVA_HOME` to JDK 17 (not newer-only hosts if AGP requires 17) |
| Android Studio / SDK | API 26+ compile 36 | `ANDROID_HOME` set |
| Xcode | Latest stable | Required for iOS / tvOS (full Xcode, not only CLT) |
| Gradle | Wrapper in repo | Prefer `./gradlew` |

### JDK tip (this environment)

```bash
export JAVA_HOME=/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

`gradle.properties` may also pin `org.gradle.java.home` for local machines.

## Stack versions (catalog)

Managed in `gradle/libs.versions.toml`:

- Kotlin **2.4.21**
- Compose Multiplatform **1.11.1**
- AGP **8.10.1**
- Koin **4.1.0**
- Ktor **3.1.3**
- SQLDelight **2.4.1** (`:core:database`)
- Media3 **1.7.1**

## Sync / assemble

```bash
./gradlew projects
./gradlew :apps:androidApp:assembleDebug
./gradlew :apps:androidTvApp:assembleDebug
./gradlew :apps:desktopApp:compileKotlinDesktop
./gradlew :apps:webApp:wasmJsBrowserDistribution
```

## Run targets

### Android phone / tablet

```bash
./gradlew :apps:androidApp:installDebug
adb shell am start -n com.ott.platform.android/.MainActivity
```

Or run `apps/androidApp` from Android Studio.

### Android TV

```bash
./gradlew :apps:androidTvApp:installDebug
```

Use an Android TV emulator or device. Verify D-pad focus, not touch-only flows.

### macOS Desktop (CMP)

```bash
./gradlew :apps:desktopApp:run
```

### Web (`wasmJs` — Compose Multiplatform Web)

Build the production Wasm bundle or start the webpack dev server:

```bash
# Production static bundle (output: apps/webApp/build/dist/wasmJs/productionExecutable)
./gradlew :apps:webApp:wasmJsBrowserDistribution

# Serve locally on http://localhost:8080/
python3 -m http.server 8080 --directory apps/webApp/build/dist/wasmJs/productionExecutable

# Or webpack dev server:
./gradlew :apps:webApp:wasmJsBrowserDevelopmentRun
```

The GitHub Actions workflow `.github/workflows/deploy-web.yml` deploys `productionExecutable` to GitHub Pages (`https://shyam1234.github.io/DemoComposeMultiPlatform/`). Offline catalog caching and watchlist persistence on Web use `window.localStorage`, and playback uses HTML5 `<video>` + `hls.js`.

### iOS

Requires **full Xcode** (not Command Line Tools only):

```bash
sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
```

1. Open `apps/iosApp/iosApp.xcodeproj`.
2. Sign in with an Apple ID (Xcode → Settings → Accounts) and set `TEAM_ID` in `apps/iosApp/Configuration/Config.xcconfig` (or Signing & Capabilities).
3. On the device: enable **Developer Mode**, then after install **Trust** the developer under VPN & Device Management.
4. Select a **physical iPhone/iPad** (or Apple Silicon simulator) → Run.  
   **Do not** use Intel `x86_64` simulators — Compose 1.11+ has no `iosX64` target.

See also [apps/iosApp/README.md](../apps/iosApp/README.md).

The Xcode “Compile Kotlin Framework” phase runs:

```bash
./gradlew :apps:shared:embedAndSignAppleFrameworkForXcode
```

Prebuild frameworks from the repo root:

```bash
./gradlew :apps:shared:linkDebugFrameworkIosArm64
./gradlew :apps:shared:linkDebugFrameworkIosSimulatorArm64   # Apple Silicon hosts
```

Shared UI matches Android phone (`OttApp(isTv = false)`); playback uses `AvPlayerEngine` in `platform/ios`, and offline catalog/watchlist persistence uses SQLDelight (`NativeSqliteDriver`, linked with `-lsqlite3` in `apps/shared` and `apps/iosApp`).

### Apple TV

1. Open `apps/appleTvApp` in Xcode.
2. Link/consume the shared KMP framework exposing domain/data/player APIs.
3. Run on tvOS simulator/device.

**Note:** UI is SwiftUI; CMP is not used for tvOS screens.

## Tests

```bash
./gradlew :core:domain:allTests
./gradlew :core:data:allTests
./gradlew test
```

See [TESTING_GUIDE.md](TESTING_GUIDE.md).

## Demo content

Live dynamic OTT catalog from **Play-CMS** (`https://cms.play-cms.com`) — internal auth, dynamic menus, layouts, rails, and HLS/trailer playback URLs. See [API.md](API.md) for full endpoint definitions and role ownership.

## Troubleshooting

| Issue | Mitigation |
|-------|------------|
| Wrong Java version | Force JDK 17 via `JAVA_HOME` / `org.gradle.java.home` |
| iOS build fails with CLT only | Install full Xcode and `xcode-select -s /Applications/Xcode.app/Contents/Developer` |
| Signing requires a development team | Add Apple ID in Xcode Accounts; set `TEAM_ID` / Automatic signing |
| `Unknown iOS simulator arch: x86_64` | Use a physical device or Apple Silicon simulator — not Intel simulators |
| Developer Mode disabled | iPhone Settings → Privacy & Security → Developer Mode |
| App installs but won’t launch (Security / signature) | Settings → General → VPN & Device Management → Trust developer |
| Missing `CFBundleIdentifier` | Ensure `apps/iosApp/iosApp/Info.plist` has CFBundle keys (already in repo) |
| TV app looks like phone UI | Run `androidTvApp`, not `androidApp` |
| Player black screen on demo | Confirm stream URL / Media3 HLS (Android) or AVPlayer (iOS) wiring |

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
- Compose Multiplatform **1.12.2**
- AGP **8.10.1**
- Koin **4.1.0**
- Ktor **3.1.3**
- Media3 **1.7.1**

## Sync / assemble

```bash
./gradlew projects
./gradlew :apps:androidApp:assembleDebug
./gradlew :apps:androidTvApp:assembleDebug
./gradlew :apps:desktopApp:compileKotlinDesktop
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

### iOS

Requires **full Xcode** (`xcode-select -s /Applications/Xcode.app/Contents/Developer`).

1. Open `apps/iosApp/iosApp.xcodeproj`.
2. Set your Development Team in Signing (or `TEAM_ID` in `apps/iosApp/Configuration/Config.xcconfig`).
3. Select an iPhone/iPad simulator or device → Run.

The Xcode “Compile Kotlin Framework” phase runs:

```bash
./gradlew :apps:shared:embedAndSignAppleFrameworkForXcode
```

Prebuild frameworks from the repo root (Apple Silicon / arm64 simulator — Compose 1.11+ has no `iosX64`):

```bash
./gradlew :apps:shared:linkDebugFrameworkIosSimulatorArm64
./gradlew :apps:shared:linkDebugFrameworkIosArm64
```

Shared UI is the same phone layout as Android (`OttApp(isTv = false)`); playback uses `AvPlayerEngine`.

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

The app uses **mock repositories / sample catalog** data. No backend, DRM license server, or paid streaming service is required.

## Troubleshooting

| Issue | Mitigation |
|-------|------------|
| Wrong Java version | Force JDK 17 via `JAVA_HOME` / `org.gradle.java.home` |
| iOS build fails with CLT only | Install full Xcode and select it with `xcode-select` |
| TV app looks like phone UI | Run `androidTvApp`, not `androidApp` |
| Player black screen on demo | Confirm sample media URL / fake engine wiring for the target |

# iOS app (Compose Multiplatform)

Hosts shared `OttApp` via `OttShared.framework` (AVPlayer + Darwin Ktor).

## Requirements

- Full **Xcode** (not only Command Line Tools)
- `sudo xcode-select -s /Applications/Xcode.app/Contents/Developer`
- JDK **17** (`JAVA_HOME`)
- Apple ID signed into Xcode (**Settings → Accounts**) with a Development Team
- Physical **iPhone/iPad** for Intel Mac hosts (see below)

## Signing

1. Set `TEAM_ID` in `Configuration/Config.xcconfig` (or Signing & Capabilities in Xcode).
2. `CODE_SIGN_STYLE = Automatic` is already enabled in the project.
3. On the device:
   - **Settings → Privacy & Security → Developer Mode → On** (restart if asked)
   - After first install: **Settings → General → VPN & Device Management → Trust** your developer

## Run

1. Open `iosApp.xcodeproj` in Xcode.
2. Select your **physical iPhone/iPad** (not an Intel x86_64 simulator).
3. Build & Run — the “Compile Kotlin Framework” phase runs:

```bash
./gradlew :apps:shared:embedAndSignAppleFrameworkForXcode
```

CLI install/launch (after a successful device build):

```bash
xcrun devicectl device install app --device <UDID> \
  ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/Debug-iphoneos/iosApp.app
xcrun devicectl device process launch --device <UDID> com.ott.platform.ios
```

## Architectures

| Host / destination | Target | Notes |
|--------------------|--------|-------|
| Physical iPhone/iPad | `iosArm64` | Supported |
| Apple Silicon simulator | `iosSimulatorArm64` | Supported |
| Intel Mac simulator (`x86_64`) | `iosX64` | **Not supported** — Compose 1.11+ dropped `iosX64` |

Prebuild frameworks from the repo root:

```bash
./gradlew :apps:shared:linkDebugFrameworkIosArm64
./gradlew :apps:shared:linkDebugFrameworkIosSimulatorArm64   # Apple Silicon only
```

## What you get

Same phone UI as Android (`OttApp(isTv = false)`): splash, bottom nav, hero, shelves, player chrome. Playback uses `AvPlayerEngine` (`platform/ios`).

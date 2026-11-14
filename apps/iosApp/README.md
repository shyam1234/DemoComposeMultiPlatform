# iOS app (Compose Multiplatform)

Hosts shared `OttApp` via `OttShared.framework` (AVPlayer + Darwin Ktor).

## Requirements

- Full **Xcode** (not only Command Line Tools)
- `xcode-select -s /Applications/Xcode.app/Contents/Developer`
- JDK 17

## Run

1. Open `iosApp.xcodeproj` in Xcode.
2. Select an iPhone simulator or device.
3. Build & Run (framework is built by the “Compile Kotlin Framework” script phase).

From repo root you can prebuild frameworks (Apple Silicon recommended; Compose no longer ships `iosX64`):

```bash
./gradlew :apps:shared:linkDebugFrameworkIosSimulatorArm64
./gradlew :apps:shared:linkDebugFrameworkIosArm64
```

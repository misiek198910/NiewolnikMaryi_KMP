# Run Instructions

SlaveOfMary is a Kotlin Multiplatform (KMP) project targeting Android and iOS,
sharing UI (Compose Multiplatform) and business logic from the `shared` module.

## Requirements

- Android Studio (latest stable, with the Kotlin Multiplatform plugin) or IntelliJ IDEA
- JDK 17+
- Xcode (latest stable) — only needed for iOS, macOS only
- An `ANDROID_HOME` / Android SDK set up (Android Studio configures this automatically)

## Android

Open the project root in Android Studio and run the `androidApp` configuration, or
use Gradle directly:

```
./gradlew :androidApp:assembleDebug   # build the debug APK
./gradlew :androidApp:installDebug    # build and install on a connected device/emulator
```

- Application ID: `mivs.niewolnik_maryi`
- Module: `androidApp`

## iOS

Open [`iosApp/iosApp.xcodeproj`](./iosApp/iosApp.xcodeproj) in Xcode, select the
`iosApp` scheme and a simulator/device, then Run (⌘R). Xcode builds the shared Kotlin
code (via the `embedAndSignAppleFrameworkForXcode` Gradle task, wired into the Xcode
build phases) before compiling the Swift/SwiftUI sources.

To build headlessly from the command line:

```
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator \
  -configuration Debug -destination 'generic/platform=iOS Simulator' \
  CODE_SIGNING_ALLOWED=NO build
```

If `xcode-select -p` points at the Command Line Tools instead of a full Xcode
installation, prefix the command above (and any Gradle task that links the shared
Kotlin/Native framework, e.g. `:shared:linkReleaseFrameworkIosArm64`) with:

```
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
```

The built app lands under
`~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/Debug-iphonesimulator/SlaveOfMary.app`.

## Tests

```
./gradlew :shared:testAndroidHostTest       # shared-module unit tests on the JVM
./gradlew :shared:iosSimulatorArm64Test     # shared-module tests on an iOS simulator
```

## Project layout

- [`/androidApp`](./androidApp) — Android application entry point.
- [`/iosApp`](./iosApp/iosApp) — iOS application entry point (SwiftUI host for the
  Compose Multiplatform UI).
- [`/shared`](./shared/src) — code shared across platforms (`commonMain`), plus
  platform-specific implementations (`androidMain`, `iosMain`).

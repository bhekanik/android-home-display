# The starter builds and runs on the TB-X505F

Checked on 14 October 2026 against app source `4c34af1cd7c9872e7fc8d781a25a02cdd2d7c70b`.

A clean Git clone built on macOS with the Android Studio JDK and SDK 35:

```sh
./gradlew --no-daemon --max-workers=4 testDebugUnitTest assembleDebug lintDebug
```

All five unit tests passed. Debug assembly and Android lint passed. Lint reported 14 warnings, including English-only UI strings, newer available toolchain versions, legacy full-screen APIs and intentionally enabled WebView JavaScript. These checks do not establish compatibility with every Android version or device.

On a Lenovo Tab M10 TB-X505F running Android 10, the starter opened its setup dialog with no saved address, accepted an HTTPS address, rendered the page and retained the address after a force-stop/restart. It was installed alongside another launcher; the default Home app was not changed in this test. The original app from which this starter was extracted had already been used as the Home app on that device.

The Home Assistant YAML parsed successfully. Its six card configurations rendered through Home Assistant 2026.10.1's card helpers after the four example entity IDs were mapped to existing entities. No error cards appeared. This check did not operate the light or thermostat.

Windows builds, other tablets, release signing and sustained unattended operation remain untested. There is no prebuilt release APK or automated release pipeline.

# Android Home Display

Turn an Android tablet into a full-screen browser for a home dashboard. Enter the dashboard URL once, sign in on the tablet, and optionally make this app the default Home app.

This is the reusable launcher from my Lenovo Tab M10 build, inspired by [Wes Bos's photo-frame project](https://github.com/wesbos/photo-frame-dashboard). The included [Home Assistant template](home-assistant/README.md) gives you a clock, weather, calendar and room controls using built-in cards. You can also point the app at your own web page.

The app targets Android 10 and newer. The original launcher and this starter were tested on a Lenovo Tab M10 TB-X505F running Android 10. First-launch setup, page loading and saved-address persistence after an app restart passed on that device. The public version has a separate application ID and asks for an address on first launch; it has no preset server, credentials or household configuration. Long-term unattended operation and other tablet models have not been established.

## Build the launcher

Install Android Studio, Android SDK Platform 35 and SDK Build Tools 35.0.0 through its SDK Manager. Use JDK 17 or a compatible newer JDK. Open this folder in Android Studio and let Gradle sync, or use the terminal:

```sh
git clone https://github.com/bhekanik/android-home-display.git
cd android-home-display
./gradlew --no-daemon --max-workers=4 testDebugUnitTest assembleDebug lintDebug
```

On Windows, use `gradlew.bat`. For a terminal build, set `JAVA_HOME` to your JDK and `ANDROID_HOME` to your Android SDK, or set `sdk.dir` in a local `local.properties` file. Do not commit local paths or signing keys.

The project uses Gradle 8.14.3, Android Gradle Plugin 8.13.0 and Java 17 source compatibility. The wrapper verifies the Gradle distribution checksum. The APK is `app/build/outputs/apk/debug/app-debug.apk`. This is a development build, signed by your local debug key, with WebView debugging enabled. No prebuilt APK is supplied.

## Install over USB

Enable Developer options and USB debugging on the tablet. On the Lenovo, tap Build number repeatedly in Settings, then enable USB debugging under Developer options. Connect a data-capable Micro-USB cable and approve the computer on the tablet.

```sh
adb devices -l
adb -s YOUR_DEVICE_SERIAL install -r app/build/outputs/apk/debug/app-debug.apk
adb -s YOUR_DEVICE_SERIAL shell am start -n com.bhekanik.homedisplay/.WallDisplayActivity
```

Replace `YOUR_DEVICE_SERIAL` with the intended device from the first command. If it is absent, try a different data cable and USB port. If it says `unauthorized`, unlock the tablet and approve the debugging prompt. Android's [device setup guide](https://developer.android.com/studio/run/device) covers platform-specific drivers and permissions.

The first launch opens Display settings. Enter an HTTP or HTTPS address reachable from the tablet. A computer's `localhost` is not reachable as `localhost` from the tablet. For Home Assistant, use the address and dashboard path that already work in the tablet's browser. Leave passwords out of the URL and sign in through the page.

![First-launch settings with an empty dashboard address and overnight dimming enabled](docs/images/setup.png)

Hold Volume Up to reopen Display settings. Choose **Choose Home app** and select **Home Display** if you want the Android Home button to return to it. You can use the app without changing the default launcher. This is a personal launcher, not a locked kiosk: Android Settings and the system UI remain accessible.

After installation the computer is not needed to run the app. The tablet still needs Wi-Fi access to its dashboard server. A page served from a laptop will stop being available when that laptop sleeps. Leave USB debugging disabled when you no longer need it; the app has no network debugging or remote administration service.

## The screen stays awake while the app is visible

The app uses Android's `FLAG_KEEP_SCREEN_ON`. It does not keep the display awake while another app is in the foreground, override the power button, bypass the lock screen or unlock the tablet after a reboot.

The overnight dimming option is enabled by default. It uses the tablet's local time, dims to 8% from 22:00 to 07:00, and otherwise uses 45% brightness. Touching the app restores 45% brightness for two minutes. These values are fixed defaults in `NightHours.java` and `WallDisplayActivity.java`; the settings dialog toggles the schedule. A dimmed screen stays visible and the page keeps running.

Orientation is fixed to landscape. If Android's own screensaver takes over while charging, disable it in system Display settings. This launcher does not include a moving screensaver; that belongs to the page you choose to display.

## Page failures recover without reinstalling

The app keeps normal WebView cookies and site storage across restarts and APK updates. Uninstalling it or clearing app data removes its settings and session.

Failed main-page loads show an error panel with retry and settings buttons. Network/server errors retry with backoff from two seconds to one minute. A restored network triggers another attempt. Most HTTP client errors wait for a manual retry, while 408, 429 and server errors retry automatically. HTTPS certificate errors are rejected. A crashed WebView renderer is replaced.

```sh
adb -s YOUR_DEVICE_SERIAL shell logcat -d -s WallDisplay:I AndroidRuntime:E
```

Logs report load and recovery events without recording the dashboard URL. Recovery of data inside a loaded page, such as a disconnected WebSocket, remains that page's responsibility.

HTTP is permitted for local dashboards. Use HTTPS with a valid certificate when available; HTTP does not encrypt the session on the network. Only load pages you trust. WebView has JavaScript and site storage enabled, but file/content access and third-party cookies are disabled. The app requests internet and network-state permissions only, has no credential JavaScript bridge, and disables Android app backup. It has no camera, microphone, motion detection or native notification bridge.

## Return to the normal tablet launcher

Hold Volume Up, choose **Choose Home app**, and select the tablet's original launcher. Keep the original launcher installed. Use **Android settings** from the same dialog when needed. There is no root, bootloader unlock, firmware flash or device-owner enrolment to undo.

To update an APK while retaining its settings, use `adb install -r` with an APK signed by the same key. An installation built on a different computer may have a different debug key and fail with a signature mismatch. Do not clear data to solve that unless you are prepared to sign in and configure it again.

Before mounting a tablet, trial it on its intended power supply, check Wi-Fi at the intended location, and verify sleep/wake, overnight dimming and reconnection. Check the battery and charging port physically. The launcher does not manage charging or make an ageing battery safe for unattended use.

## Licence

App source and templates are MIT licensed. Gradle wrapper files retain their Apache 2.0 licence; see [third-party notices](THIRD_PARTY_NOTICES.md).

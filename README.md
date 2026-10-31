# Friction

[![Build](https://github.com/divyangdodiya/friction-launcher/actions/workflows/android.yml/badge.svg)](https://github.com/divyangdodiya/friction-launcher/actions/workflows/android.yml)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-black.svg)](LICENSE)
![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-black.svg)

A minimal Android launcher that makes distracting apps slow and awkward to reach.

Most digital wellbeing tools block apps and get switched off. Friction takes a different approach: everything still works, but the habitual path to a time sink gets longer, plainer and more deliberate, while the apps you actually need stay one tap away.

## Features

**Home screen**
- Clock, date and a count of distraction apps opened today
- Hard reminders in a distressed type: *GRIND. NO EXCUSES. / PUT THE FOOD DOWN. / CHUG WATER. NOW. / GET UP. WALK.*
- Up to 5 apps of your choice, one tap away (your dialer, SMS app and WhatsApp by default)
- Swipe up for the menu, swipe down for notifications

**Menu**
- Every other app in a plain text grid, reshuffled on every visit so muscle memory never forms
- Search matches only the **full** app name, with no autocomplete
- Distraction apps carry a small dot; long press any app to mark it

**The gate**
- Opening a distraction starts a countdown (5 to 60 seconds) that restarts if you leave
- You must write why you are opening it, at least three words
- The big button is *Never mind*; *Open anyway* stays small

**Friction against yourself**
- Shortening the wait, turning off the question, showing icons, removing a distraction or pinning one to home all require typing *I choose distraction over focus*

**Privacy**
- No internet permission, no analytics, no accounts
- The only permission is `EXPAND_STATUS_BAR`, used for the swipe-down gesture
- Settings and a daily counter are stored on the device and excluded from backups; your typed reasons are never saved

## Install

1. Download the latest `friction-*.apk` from [Releases](https://github.com/divyangdodiya/friction-launcher/releases/latest) on your phone.
2. Allow your browser to install unknown apps when prompted, then install.
3. Press the home button and choose **Friction** as the default home app, or go to *Settings > Apps > Default apps > Home app*.

New releases install over the old one and keep your settings.

## Building

Requirements: JDK 17 and the Android SDK (Android Studio Ladybug or newer works out of the box).

```sh
git clone https://github.com/divyangdodiya/friction-launcher.git
cd friction-launcher
./gradlew testReleaseUnitTest assembleRelease
```

Local builds are signed with your Android debug key. Android rejects updates signed with a different key, so uninstall the official build before installing your own.

## Architecture

Single module, layered by package, with dependencies pointing inward:

```
domain/   Pure Kotlin: models, rules, repository interfaces, use cases. Unit tested.
data/     Android implementations: LauncherApps, DataStore, system services.
di/       Hilt modules binding data implementations to domain interfaces.
ui/       Jetpack Compose screens, each with a ViewModel exposing StateFlow UI state.
```

- Kotlin, Jetpack Compose, Material 3
- MVVM with unidirectional data flow
- Hilt for dependency injection
- Preferences DataStore for settings and usage
- Navigation Compose with type-safe routes
- `LauncherApps` with a package change callback for the app list

Rules that decide behavior (what counts as lowering friction, exact name matching, the home screen limit) live in `domain/` as plain Kotlin and are covered by tests in `app/src/test`.

## Releases and signing

Every push to `main` runs tests and lint, then publishes a signed APK as a GitHub release. The release key lives only in repository secrets:

| Secret | Value |
| --- | --- |
| `SIGNING_KEYSTORE_BASE64` | `base64 -w0 release.jks` |
| `SIGNING_STORE_PASSWORD` | Keystore password |
| `SIGNING_KEY_ALIAS` | Key alias |
| `SIGNING_KEY_PASSWORD` | Key password |

Without these secrets (for example, on pull requests from forks) CI still builds and tests with the debug key and skips publishing.

## Limits

A launcher cannot block apps opened from notifications, the recents screen or another launcher, and you can always switch launchers in system settings. Friction raises the cost of the habitual path; it does not lock the phone.

## Contributing

Ideas, bug reports and pull requests are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) first. Security issues: see [SECURITY.md](SECURITY.md).

## License

Copyright 2026 Divyang Dodiya

Licensed under the [Apache License, Version 2.0](LICENSE).

The reminder font, [Rubik Distressed](https://fonts.google.com/specimen/Rubik+Distressed), is licensed under the SIL Open Font License 1.1; see [licenses/](licenses/).

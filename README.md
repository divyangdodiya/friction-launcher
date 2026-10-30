# Friction

A minimal Android launcher that makes distracting apps slow and awkward to reach.

## What it does

- **Home screen:** time, date, today's distraction count, four hard reminders, and up to 5 apps you choose (Phone, Messages and WhatsApp by default). Distractions placed on home still go through the gate.
- **Gestures:** swipe up anywhere on home to slide the menu open, swipe down to pull the notification shade.
- **Menu:** every other app in a text grid that is shuffled on every visit. Search only matches the **full** app name, so there is no autocomplete to lean on. Distraction apps carry a small dot.
- **Gate:** opening a distraction app shows a countdown (5 to 60 seconds) and asks why you are opening it (at least three words). Leaving the screen restarts the wait.
- **Distraction list:** common browsers, video and social apps are pre-marked. Long press any app in the menu to add it.
- **Lowering friction costs effort:** shortening the wait, turning off the question, showing icons, removing an app from the list, or putting a distraction on the home screen all require typing *I choose distraction over focus*.
- Monochrome, text only by default (optional grayscale icons), no animations.

## Install

1. On your phone, open the repo's **Releases** page and download the latest `friction-*.apk`.
2. Allow your browser to install unknown apps when prompted, then install.
3. Press the home button and choose **Friction** as the default home app (or Settings > Apps > Default apps > Home app).

Every push to `main` builds and publishes a new release. New builds install over the old one and keep your settings.

## Architecture

Single module, layered by package, with dependencies pointing inward:

```
domain/   Pure Kotlin. Models, rules, repository interfaces, use cases. Unit tested.
data/     Android implementations: LauncherApps, DataStore, intents.
di/       Hilt modules binding data implementations to domain interfaces.
ui/       Jetpack Compose screens, each with a ViewModel exposing StateFlow UI state.
```

- Kotlin, Jetpack Compose, Material 3
- MVVM with unidirectional data flow
- Hilt for dependency injection
- Preferences DataStore for settings and usage
- Navigation Compose with type-safe routes
- `LauncherApps` API with a package change callback for the app list

## Build locally

Open the project in Android Studio and run, or:

```
./gradlew testReleaseUnitTest assembleRelease
```

## Signing

Builds are signed with the committed `keystore/dev.keystore` so every APK installs as an update. To use a private key instead, add these repository secrets and CI picks them up:

| Secret | Value |
| --- | --- |
| `SIGNING_KEYSTORE_BASE64` | `base64 -w0 your.keystore` |
| `SIGNING_STORE_PASSWORD` | Keystore password |
| `SIGNING_KEY_ALIAS` | Key alias |
| `SIGNING_KEY_PASSWORD` | Key password |

Switching keys requires uninstalling the old build once.

## Limits

A launcher cannot block apps opened from notifications, the recents screen, or another launcher. Friction raises the cost of the habitual path, it does not lock the phone.

## Credits

Reminder font: [Rubik Distressed](https://fonts.google.com/specimen/Rubik+Distressed), SIL Open Font License 1.1 (see `licenses/`).

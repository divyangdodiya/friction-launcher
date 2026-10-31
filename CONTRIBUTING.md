# Contributing to Friction

Thanks for helping. Friction is small on purpose, so a few guidelines keep it that way.

## Before you start

- **Bugs:** open an issue with your device, Android version and steps to reproduce.
- **Features:** open an issue to discuss it first. Friction's goal is to make distractions harder to reach without getting in the way of what matters, so features that add color, rewards, streak animations or engagement loops are unlikely to be accepted.
- **Small fixes** (typos, clear bugs) can go straight to a pull request.

## Development

Requirements: JDK 17 and the Android SDK.

```sh
./gradlew testDebugUnitTest lintRelease assembleRelease
```

All three must pass; CI runs the same commands on every pull request.

## Code guidelines

- Follow the existing layering: business rules in `domain/` (plain Kotlin, no Android imports), platform code in `data/`, screens and ViewModels in `ui/`.
- Anything that changes how much friction the app applies belongs in `SettingChange.reducesFriction` and needs a unit test.
- Keep the app offline. Pull requests that add the `INTERNET` permission, analytics or tracking will not be merged.
- User-facing text goes in `strings.xml`.
- Formatting follows `.editorconfig` (official Kotlin style, 120 columns, trailing commas).

## Pull requests

- Keep each pull request focused on one change.
- Describe what changed and why, and add screenshots for UI changes.
- By submitting a pull request you agree that your contribution is licensed under the Apache License 2.0.

# Security Policy

## Reporting a vulnerability

Please do not open a public issue for security problems. Use GitHub's private reporting instead: go to the **Security** tab of this repository and choose **Report a vulnerability**.

Include what you found, how to reproduce it and the affected version. You can expect a first response within a week.

## Scope

Friction has no network access and stores only its own settings on the device. Reports about the following are especially welcome:

- Ways for another app to change Friction's settings or bypass the gate through an exported component
- Anything that causes Friction to read or expose data beyond the list of installed apps
- Problems with the release signing or build pipeline

## Verifying a release

Official APKs are published only on this repository's Releases page and are signed with the same key every time. Android refuses to install an update signed with a different key, so an APK from anywhere else will not update an official install.

# NEON TOK — Android prototype

A local-first Android Studio project written in Kotlin + Jetpack Compose.

## Requirements
- Android Studio (current stable)
- Android SDK Platform 35
- JDK 17
- Internet on first Gradle sync to download dependencies

## Run on your phone
1. Unzip the project.
2. Open Android Studio → **Open** → select the `NeonTokAndroid` folder.
3. Wait for Gradle sync. If asked, install Android SDK 35.
4. Connect an Android phone with USB debugging enabled, or create an emulator.
5. Press **Run ▶**.

## Build an APK
Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
The debug APK should appear at:
`app/build/outputs/apk/debug/app-debug.apk`

## Prototype functionality
- Import videos using Android's system picker.
- Vertical swipe feed for imported local clips.
- Looping playback, a simple like toggle, and saving a chosen local clip into `Movies/NEONTOK`.
- Three accent themes.
- Local privacy/settings toggles.
- No ad SDKs are included.

## Current limitations
- This is not a modified TikTok APK and does not sign in to TikTok.
- There is no remote recommendation feed, comments backend, cloud upload, or account system yet.
- Privacy toggles are UI preferences in this prototype; they do not yet persist after closing the app.
- Saving copies the selected local media. It does not remove watermarks or bypass access controls on third-party services.
- The prototype currently treats imported videos as local feed items. Network video URLs are not implemented.

## Build the APK without Android Studio (GitHub Actions)
1. Create a GitHub repository and upload the contents of this folder to it (the files inside `NeonTokAndroid`, not the parent folder).
2. Open the repository's **Actions** tab and select **Build NEON TOK APK**.
3. Press **Run workflow**. Wait for the workflow to finish successfully.
4. Open the completed run and download the artifact named **NEON-TOK-debug-APK**. Unzip it to get `app-debug.apk`.

This produces a debug APK for testing, not a Play Store release-signed APK. Never install APKs from sources you do not trust.

# University Manual App

## Build (Android Studio)
1. Open the project in Android Studio.
2. Wait for Gradle sync to finish.
3. Run **Build > Make Project** or click the **Run** button to install on an emulator/device.

## Build (Command Line)
```bash
./gradlew clean assembleDebug
```

The debug APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

> Note: If you see an Android Studio warning about **16 KB alignment**, it is related to
> native libraries used by dependencies. The PDF viewer now uses the platform
> `PdfRenderer` API (no native PDF libs). This project is configured to ship only ARM ABIs
> to avoid x86_64 native library alignment issues. If you are using an emulator, pick an
> **ARM64** system image (x86_64 emulators will not run this build).

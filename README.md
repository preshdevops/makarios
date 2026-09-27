# Makarios

Makarios is a native Android application for personal biblical affirmations, widgets, and social sharing, built with Jetpack Compose and Material 3.

## Overview

Makarios allows users to write personal declarations, match them with scripture using an offline on-device neural model, and display them on their home screen, lock screen, or export them to social media.

Key capabilities:
- Affirmation Creator: Write a personal declaration and match it with a relevant Bible verse offline using an embedded ONNX Runtime model.
- Home Screen Widgets: Native Jetpack Glance widgets in multiple grid sizes (2x2, 4x2, 4x3, and 4x4) with typography tailored for each size.
- Lock Screen Wallpapers: Full-resolution 9:20 wallpapers formatted to clear clock and notification areas.
- Social Share Engine: Native image rendering tailored for Instagram Stories (9:16), Instagram Posts (1:1), Snapchat (9:16), X cards (16:9), and WhatsApp Status (4:5).
- Scheduled Notifications: Customizable daily reminder times using exact system alarms.
- Cloud Sync & Guest Access: Firebase Authentication supporting optional email sign-up/sign-in with cloud backup, or instant guest access without an account.

## Architecture and Tech Stack

The application uses an offline-first architecture with optional cloud synchronization.

- Language: Kotlin 2.1.10
- UI: Jetpack Compose (BOM 2025.02.00) and Material 3
- Home Screen Widgets: Jetpack Glance 1.1.1
- Local ML Engine: Microsoft ONNX Runtime Mobile 1.20.0 (offline embedding similarity search)
- Authentication and Backend: Firebase Auth and Cloud Firestore (BOM 33.10.0)
- Image Loading: Coil Compose 2.7.0
- Build System: Gradle 8.11.1 and Android Gradle Plugin 8.8.2
- Minimum SDK: Android 8.0 (API 26)
- Target SDK: Android 15 (API 35)

## Project Structure

```
android/
  app/src/main/
    assets/
      bible_embeddings.bin   # Quantized offline scripture embeddings
      model.onnx             # MiniLM transformer model for on-device inference
    java/com/makarios/app/
      data/                  # Repositories, Affirmation models, AuthManager
      ml/                    # ONNX Runtime vector search engine
      ui/
        components/          # Reusable Compose components
        screens/             # Home, Library, Create, Saved, Profile, WidgetStudio
        theme/               # Typography, color tokens, and theme definitions
      util/                  # WallpaperRenderer, ShareHelper, ReminderManager
      widget/                # Jetpack Glance widget implementation
    res/                     # Fonts, drawables, and XML resources
```

## Typography and Design

The design focuses on editorial typography and calm visual hierarchy:
- Display and Declarations: Cormorant Garamond
- Interface and Body Text: Work Sans
- Color Tokens: Porcelain background, Espresso text, Terracotta accent, Sunlit Gold, and Morning Sage

## Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Android SDK 35

### Building the Project

Clone the repository:
```bash
git clone https://github.com/preshdevops/makarios.git
cd makarios/android
```

Build the debug APK:
```bash
./gradlew assembleDebug
```
(On Windows PowerShell: `.\gradlew.bat assembleDebug`)

Install on a connected device or emulator:
```bash
./gradlew installDebug
```

## Continuous Integration

The repository runs automated builds on GitHub Actions:
- android-ci.yml: Runs on push and pull requests to main, compiling the debug APK and uploading artifacts.
- release.yml: Builds release packages upon tag creation.

## License

This project is licensed under the Apache License 2.0. See the LICENSE file for details.

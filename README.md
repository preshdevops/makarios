# Makarios · Biblical Affirmations & Sacred Declarations

[![Android CI](https://github.com/preshdevops/makarios/actions/workflows/android-ci.yml/badge.svg)](https://github.com/preshdevops/makarios/actions/workflows/android-ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2025.02-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Expressive-795548.svg)](https://m3.material.io)
[![ONNX Runtime](https://img.shields.io/badge/ONNX%20Runtime-Mobile%201.20-005CED.svg)](https://onnxruntime.ai)

> *“Speak truth over your life · Grounded in scripture.”*  
> **Gospel. Tech. Precious.**

**Makarios** is a bespoke, publication-grade Christian affirmation Android application built with modern Jetpack Compose and Material 3. It bridges timeless liturgical restraint with modern editorial design, empowering believers to write personal biblical affirmations, match them dynamically with God's Word using on-device neural AI, and carry them everywhere—as native home screen widgets, lock screen wallpapers, and tailored social media graphics.

---

## ✨ Key Features

### 1. ✍️ Affirmation Creator & Neural Scripture Matching
- **Personal Declaration Authoring:** Write what your heart needs to declare in your own authentic words.
- **On-Device Semantic AI:** Powered by an embedded **Microsoft ONNX Runtime** neural model that matches declarations with canonical Bible verses completely offline—with zero cloud latency, zero API costs, and total user privacy.
- **Mandatory Scripture Grounding:** Every single affirmation—curated or user-authored—is permanently paired with its foundation in God's Word.

### 2. 🎨 Bespoke Social Media Sharing Engine
Tailored, pixel-perfect visual layouts rendered natively on-device across 6 distinct aspect ratios:
- **Instagram Story (`9:16`):** Dedicated safe zones for system headers and reply bars, delicate hairline framing, and illuminated scripture plaque.
- **Instagram Post (`1:1`):** Archival museum card with double hairline architectural border and corner diamond nodes.
- **Snapchat Story (`9:16`):** Minimalist frosted lens container with translucent depth and radiant contrast.
- **X / Twitter Card (`16:9` Landscape):** Broadsheet pull-quote layout with left terracotta accent bar and monumental quotation mark.
- **WhatsApp Status (`4:5`):** High-contrast, compression-proof devotional blessing layout.
- **Lock Screen Wallpaper (`9:20`):** Full-bleed wallpaper reserving top 28% clearance for system clock, date, and notifications.

### 3. 📱 Native Jetpack Glance Home & Lock Screen Widgets
- **Glance Modern App Widgets:** Native Android home screen widgets rendering live scripture, Cormorant Garamond quotes, and category badges directly on the phone screen.
- **Widget Studio:** Live phone frame simulator in the Explore tab allowing users to toggle between Home Screen and Lock Screen views.
- **Direct System Pinning:** One-tap widget pinning via `AppWidgetManager.requestPinAppWidget` and direct lock screen wallpaper setting via `WallpaperManager`.

### 4. ⏰ Sacred Time Reminders
- **Tactile Time Picker:** Custom Material 3 sacred dialog with tactile hour/minute number spinners and AM/PM pill toggle.
- **Sacred Presets:** Instant scheduling for *Dawn* (6:30 AM), *Morning* (8:30 AM), *Midday* (12:30 PM), *Evening* (8:30 PM), and *Night* (10:00 PM).
- **Exact Alarms:** Powered by `AlarmManager` and `WorkManager` for guaranteed delivery even in Android Doze mode.

### 5. 📖 Curated Category Library & Saved Vault
- Categories organized by spiritual seasons: *Identity*, *Peace*, *Strength*, *Purpose*, *Courage*, *Joy*, *Provision*, and *Confidence*.
- Interactive category filtering, full-text scripture search, and instant bookmarking with reactive UI updates.

---

## 🏛️ Architecture & Tech Stack

Makarios is architected as a **100% local-first, zero-friction** native Android application:

```
android/
 ├── app/src/main/
 │    ├── assets/
 │    │    ├── bible_embeddings.bin   # Quantized semantic scripture embeddings
 │    │    └── model.onnx             # On-device MiniLM transformer model
 │    ├── java/com/makarios/app/
 │    │    ├── data/                  # Repository, models, Affirmation data source
 │    │    ├── ml/                    # ONNX Runtime local vector search engine
 │    │    ├── ui/
 │    │    │    ├── components/       # SocialShareSheet, SacredTimePickerDialog, Cards
 │    │    │    ├── screens/          # Home, Library, Create, Saved, Profile, WidgetStudio
 │    │    │    └── theme/            # Color tokens, Typography, Theme definition
 │    │    ├── util/                  # WallpaperRenderer, ShareHelper, ReminderManager
 │    │    └── widget/                # Jetpack Glance widget implementation
 │    └── res/                        # Vector brand icons, drawables, fonts
 └── gradle/libs.versions.toml        # Version catalog
```

| Layer | Technology |
|---|---|
| **Language** | Kotlin 2.1.10 (JVM Target 17) |
| **UI Framework** | Jetpack Compose (BOM 2025.02.00) + Material 3 |
| **Widgets** | Jetpack Glance 1.1.1 (`glance-appwidget`, `glance-material3`) |
| **Image Loading** | Coil 2.7.0 for Compose |
| **On-Device AI** | Microsoft ONNX Runtime Android 1.20.0 |
| **Architecture** | Unidirectional Data Flow (UDF), State-driven Compose |
| **Build System** | Android Gradle Plugin 8.8.2 + Gradle 8.11.1 |

---

## 🎨 Design System & Craft Guidelines

Makarios follows strict editorial craft guidelines adhering to the `/impeccable` design standard:

- **Typography:**
  - *Display & Quotes:* **Cormorant Garamond** (Editorial serif, medium/regular/italic)
  - *Interface & Labels:* **Work Sans** (Clean, humanist geometric sans-serif)
- **Palette:**
  - `Porcelain` (`#FAF7F2`) — Primary warm linen ground
  - `Espresso` (`#2C2622`) — Deep charcoal umber ink for high-contrast legibility
  - `Terracotta` (`#A85842`) — Earthy clay brand accent
  - `Sunlit Gold` (`#D4A038`) — Radiant morning amber accent
  - `Morning Sage` (`#607768`) — Quiet eucalyptus secondary accent
- **No AI-Slop Design:** No purple/indigo gradient accents, no arbitrary glows, no emoji bullet points, no kicker eyebrows over headings, and no colored card borders.

---

## 🛠️ Getting Started

### Prerequisites
- **Android Studio:** Ladybug (2024.2.1) or Meerkat+
- **JDK:** OpenJDK 17
- **Android SDK:** Compile SDK 35, Min SDK 26 (Android 8.0 Oreo+)

### Build & Run Locally

1. **Clone the repository:**
   ```bash
   git clone https://github.com/preshdevops/makarios.git
   cd makarios
   ```

2. **Compile the Debug APK:**
   ```bash
   cd android
   ./gradlew assembleDebug
   ```
   *(On Windows PowerShell, run `.\gradlew.bat assembleDebug`)*

3. **Install on connected device or emulator:**
   ```bash
   ./gradlew installDebug
   ```

---

## 🚀 CI/CD & Automated Releases

The repository includes complete GitHub Actions workflows:

- **Continuous Integration (`.github/workflows/android-ci.yml`):**
  - Runs on every `push` and `pull_request` to `main`.
  - Sets up JDK 17, compiles the debug APK, verifies Kotlin compilation, and uploads the built APK artifact (`makarios-debug-apk`).
- **Release Automation (`.github/workflows/release.yml`):**
  - Automatically triggers on git version tags (e.g. `git tag v1.0.0 && git push origin v1.0.0`) or via manual dispatch.
  - Builds the release binary and publishes an official GitHub Release with signed artifacts attached.

---

## 📄 License

Makarios is released under the **Apache License 2.0**. See [LICENSE](LICENSE) for details.

---

<p align="center">
  Crafted with intention · <b>Gospel. Tech. Precious.</b>
</p>

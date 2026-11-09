<div align="center">

# 🍡 Mochiku (餅ク)
### The Sensual Manga Reader — A True Fork of Komikku with Mochy-Music Aesthetics & Recap Gimmicks

[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](/LICENSE)
[![Platform: Android](https://img.shields.io/badge/Platform-Android%208.0+-green.svg?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

**Mochiku (餅ク)** is an honest, open-source community fork of [Komikku](https://github.com/komikku-app/komikku) (which is based on TachiyomiSY & Mihon).

> [!IMPORTANT]
> **📜 Honest Upstream Attribution & Code Provenance**  
> We want to be 100% transparent: **Mochiku is a fork of Komikku**. We did not build the core manga engine from scratch.  
> All underlying manga parsers, extension sandboxes, tracker synchronization (AniList/MAL), download managers, and reader rendering engines belong to the brilliant upstream developers and contributors of [Komikku](https://github.com/komikku-app/komikku), [TachiyomiSY](https://github.com/jobobby04/TachiyomiSY), and [Mihon](https://github.com/mihonapp/mihon).  
> Mochiku's purpose is to provide a customized community edition featuring a pure OLED dark UI inspired by [Mochy-Music](https://github.com/realidkroo/Mochy-Music), a floating capsule navigation bar, and the interactive Manga Story Recap.  
> Please star and support the upstream repository at **[github.com/komikku-app/komikku](https://github.com/komikku-app/komikku)**!

</div>

---

## 🌟 What Makes Mochiku Unique

### 1. 🎨 Mochy-Music Pure OLED Design (No Gradients)
- **True OLED Black**: Deep `#000000` canvas with zero gray-wash backlight bleed.
- **Frosted Dark Acrylic**: `#121214` glass cards with subtle `#222226` borders.
- **Monochrome Typography**: `#FFFFFF` / `#E0E0E0` primary text and `#8E8E8E` neutral subtitles.
- **Strictly No Colorful Gradients**: Clean, minimalist, and focused on artwork and content.

### 2. 🎴 The Manga Recap Gimmick & Otaku Passport (Built into Stats)
- **Monthly & Yearly Range Selector**: Seamlessly inspect reading stats across `November 2026` or the full year `2026`.
- **Signature Barcode Unlock Card**: Requires 10 chapters read this month to unlock, displaying a live progress bar and authentic Code128 barcode trigger.
- **24-Hour Timeline Distribution Bar Chart**: Exact 00:00 to 23:00 timeline bars mapping your hourly reading habits and identifying your peak window (e.g. *01:00 AM Night Owl*).
- **7-Day Weekday Tempo Bar Chart**: Visualizes reading volume from Monday through Sunday.
- **3D Otaku Passport / Reader ID**: Brushed titanium metallic ID card with official ID number (`#MKU-2026-8492`), certified reader tier, and stats strip.
- **Vintage Thermal Paper Manga Receipt**: Itemized convenience store receipt with chapters, page tally, and duration.
- **Fullscreen Story Rewind**: Story-style slides with smooth counters and milestone celebrations.

### 3. 📖 100% of Komikku's Native Android Functionality Preserved
- **Browse & Sources**: Full compatibility with all Tachiyomi/Mihon/Komikku extension sources (MangaDex, MangaHere, Asura, Flame, Local Source, etc.).
- **Library**: Custom categories (`Reading`, `Plan to Read`, `Completed`, `Dropped`), display modes, and sorting.
- **Updates & History**: Chronological update feeds with one-tap downloads, and exact reading history with session duration.
- **Advanced Reader Engine**:
  - Webtoon continuous vertical scroll
  - Right-to-Left (Manga) and Left-to-Right (Comic) paged modes
  - Dual-Page Spread view
  - Color filters: Inverted (OLED Night Mode), Grayscale (E-Ink), Warm Sepia
  - Whitespace border cropping & volume key navigation
- **Trackers & Sync**: 2-way sync with AniList, MyAnimeList, Kitsu, and Shikimori.
- **Downloads & Offline**: Background download queue and offline storage.
- **Backup & Restore**: Full JSON/protobuf library backup and restoration.

---

## 🏗️ Project Architecture

Mochiku maintains Komikku's multi-module Android architecture:

```
├── app/                  # Android application & Jetpack Compose UI
│   ├── src/main/java/eu/kanade/presentation/
│   │   ├── theme/colorscheme/MochikuColorScheme.kt    # Mochy OLED theme
│   │   └── more/stats/components/MochikuRecapSection.kt # Manga Recap Gimmick
├── core/                 # Core utilities and archive handling
├── data/                 # Database, room, and repository layers
├── domain/               # Domain models, interactor use-cases, AppTheme
├── source-api/           # Manga source and extension interfaces
├── source-local/         # Local manga file reader
├── i18n/                 # Multilingual localization resources
└── build.gradle.kts      # Gradle multi-project build configuration
```

---

## 🛠️ Building From Source

### Prerequisites
- Android Studio Ladybug / Meerkat or higher
- JDK 17 (OpenJDK 17 recommended)
- Android SDK Platform 35

### Build Commands
```bash
# Clone the repository
git clone https://github.com/sky4302/mochiku.git
cd mochiku

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

The compiled APK will be located in `app/build/outputs/apk/`.

---

## 📄 License

Mochiku is licensed under the [Apache License 2.0](LICENSE) honoring the upstream licenses of Komikku, TachiyomiSY, and Mihon.

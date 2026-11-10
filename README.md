<div align="center">

<img src="mochiku.png" width="128" height="128" alt="Mochiku Logo" />

# Mochiku (餅ク)
### Community Fork of Komikku — Mochy-Music Aesthetics, Reading Identity, Continue-Reading Bar & Story Recap

[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](/LICENSE)
[![Platform: Android](https://img.shields.io/badge/Platform-Android%208.0+-green.svg?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Latest Release](https://img.shields.io/github/v/release/sky4302/mochiku?label=Release)](https://github.com/sky4302/mochiku/releases/latest)

**Mochiku (餅ク)** is an honest, open-source community fork of [Komikku](https://github.com/komikku-app/komikku) (built upon TachiyomiSY and Mihon).

> [!IMPORTANT]
> **Honest Upstream Attribution & Code Provenance**  
> We want to be 100% transparent: **Mochiku is a fork of Komikku**. We did not build the core manga engine from scratch.  
> All underlying manga parsers, extension sandboxes, tracker synchronization (AniList/MAL), download managers, and reader rendering engines belong to the brilliant upstream developers and contributors of [Komikku](https://github.com/komikku-app/komikku), [TachiyomiSY](https://github.com/jobobby04/TachiyomiSY), and [Mihon](https://github.com/mihonapp/mihon).  
> Mochiku focuses on tactile reading ergonomics and personal reading identity: a pure OLED dark UI inspired by [Mochy-Music](https://github.com/realidkroo/Mochy-Music), a gesture-driven floating capsule navigation bar, a re-read-aware Continue-Reading capsule, 100% on-device Passport Stamps, an interactive 3D Reader ID Card, and a shareable 8-slide Story Recap.  
> Please star and support the upstream repository at **[github.com/komikku-app/komikku](https://github.com/komikku-app/komikku)**!

</div>

---

## What Makes Mochiku Unique

### 1. Mochy-Music Pure OLED Design & Theme-Adaptive Custom UI
- **True OLED Black (`MochikuColorScheme`)**: Pure `#000000` canvas with neutral `#101012` / `#18181A` containers, subtle `#1E1E22` / `#26262A` borders, `#E0E0E0` / `#8E8E8E` typography, and zero colorful gradients.
- **Full Material Theme Adaptation**: Unlike static custom overlays, Mochiku's Reader ID Card, floating capsules, 8-slide Story Recap, Passport Stamp grid, Thermal Receipt, and 9:16 Share Cards dynamically derive every surface, border, and text color from `MaterialTheme.colorScheme` — adapting cleanly across Mochy OLED, light themes, and Monet/Material You palettes.

### 2. Floating Capsule Navigation & Smart Continue-Reading Bar
- **Gesture-Driven Capsule Bottom Bar**: Replaces the standard docked bottom bar with a floating pill featuring continuous horizontal slide-to-switch gestures across tabs, a spring-animated active pill, press/scale bounce, and subtle haptic feedback.
- **Floating Continue-Reading Capsule**: Optional quick-resume bar (`Settings -> Appearance -> Show Continue-Reading bar`) anchored above the navigation pill on Library, Updates, and Browse. Displays the latest cover, series title, target chapter, and 1-indexed saved page, with swipe-to-dismiss per session.
- **Re-Read Support Without Resetting Read Checkmarks**: Most manga readers lose track of progress if you re-read an already-completed series unless you erase read checkmarks. Mochiku tracks active re-read sessions in `HistoryPreferences`, showing your current mid-chapter page on the capsule, jumping straight back to that exact `resumePage` when tapped, and automatically advancing to `Ch. N+1 · Page 1` when you finish a chapter (or hiding after the final chapter) while keeping all read checkmarks untouched.

### 3. On-Device Reader Identity & 3D Flip Passport Card
- **100% Offline Profile**: Configure your username, local avatar photo, and generated `PROFILE USR#<number>` passport ID during first-launch onboarding or anytime from the **More** header and **Statistics** screen — stored strictly on-device with zero accounts or telemetry.
- **3D Flip ID Card & Code128 Laser Scanner**: Tap the Passport Card to flip in 3D perspective between the front badge (avatar, ID, join date, and up to 3 latest unlocked mini-stamps) and the rear **Code128 barcode**. Tapping the back triggers an animated laser sweep and `VERIFIED` badge to launch the Story Recap, paired with a live verification progress bar (10 chapters milestone).

### 4. Interactive 8-Slide Story Recap & Vintage Thermal Receipt
- **Month / Year Range Selector**: Toggle between the current calendar month and full year inside **Statistics** to review live reading duration, chapter counts, and top read series.
- **8-Slide Fullscreen Story Rewind**: Staggered build-in animations across 8 slides covering your overall reading journey, real calendar month progress, top series carousel, favorite series spotlight, cover artwork, animated chapter count-up, sliding thermal paper receipt, and minimalist conclusion ID card with recent mini-stamps.
- **Vintage Thermal Paper Receipt**: Serrated convenience-store receipt dialog summarizing top read titles, reading hours, and chapter totals.

### 5. Offline 9:16 Recap Image Sharing & Privacy Mode
- **4 Theme-Adaptive 9:16 Story Cards**: Preview and export **Thermal Receipt**, **Reader ID**, **Barcode**, and **Passport Stamps** cards directly to Android's share sheet via offline `rememberGraphicsLayer` PNG rendering.
- **One-Tap Privacy Masking**: Enable *"Hide my name and titles"* before exporting to anonymize your display name (`Reader`), omit your profile photo, mask card/barcode numbers, and replace series titles with `Series 1`, `Series 2`, and so on.

### 6. Sticky Passport Stamps Milestone System
- **8 On-Device Achievement Stamps**: Unlock ink-style badges across reading volume (`First Page`, `100 Chapters`, `1000 Chapters`), streaks (`7-Day Streak`, `30-Day Streak`), reading habits (`Night Owl` between 00:00–04:59, `Marathon` with 30+ chapters in a day), and library completion (`Finisher` for completed series of 5+ chapters).
- **Slam Animations, Sticky Persistence & Cross-UI Sync**: Newly earned milestones trigger a full-screen stamp slam overlay inside **Statistics**, appear in a 3-column deterministic-tilt ink grid with detail progress sheets, and persist unlock timestamps and observed active dates across history clears. Mini-stamps automatically sync onto the Reader ID Card, **More** tab header, and Story Recap finale.

### 7. Cache Self-Healing, Optimized Builds & In-App Updates
- **Self-Healing `ChapterCache`**: Verified disk writes, journal synchronization, and automatic retry recovery in `ChapterCache` and reader page loaders to prevent `chapter_disk_cache` `ENOENT` read crashes.
- **Seamless Upgrades & Performance**: Built with R8 shrinking, Baseline Profiles, a permanent release signing key for in-place upgrades, circular emblem branding, and an unconditional in-app update checker linked to `sky4302/mochiku` GitHub Releases.

### 8. 100% of Komikku & Mihon's Core Functionality Preserved
- **Browse & Sources**: Full compatibility with Tachiyomi / Mihon / Komikku extension sources and local archives.
- **Library**: Custom categories, tags, display modes, sorting, and library filters.
- **Updates & History**: Chronological chapter updates, background downloads, and per-chapter reading history.
- **Advanced Reader Engine**: Webtoon continuous vertical scroll, RTL/LTR/Vertical paged modes, dual-page spread view, color filters, border cropping, and volume-key navigation.
- **Trackers & Backups**: Two-way sync with AniList, MyAnimeList, Kitsu, Shikimori, Bangumi, MangaUpdates, Komga, Kavita, and Suwayomi, plus local backup/restore.

---

## Downloads

Pre-built signed APKs are published on [**GitHub Releases**](https://github.com/sky4302/mochiku/releases/latest):

- **`Mochiku-Universal.apk`** — Works on all Android devices (recommended if unsure)
- **`Mochiku-arm64-v8a.apk`** — Modern 64-bit Android phones
- **`Mochiku-armeabi-v7a.apk`** — Older 32-bit Android devices
- **`Mochiku-x86_64.apk`** — PC emulators / WSA / Chromebooks

---

## Project Architecture

Mochiku preserves Komikku's multi-module Android Clean Architecture while cleanly scoping custom features:

```
├── app/                              # Android application & Jetpack Compose UI
│   └── src/main/java/eu/kanade/
│       ├── presentation/
│       │   ├── home/                 # Floating ContinueReadingBar capsule
│       │   ├── more/
│       │   │   ├── onboarding/       # ProfileStep onboarding setup
│       │   │   ├── profile/          # MochikuIdCard, StoryRecap, ProfileHeader/Store
│       │   │   └── stats/components/ # RecapSection, PassportStampsSection, ShareCards/Preview
│       │   └── theme/colorscheme/    # MochikuColorScheme (Mochy-Music pure OLED)
│       └── tachiyomi/ui/home/        # HomeScreen + gesture-driven MochikuCapsuleBottomBar
├── core/                             # Core utilities and archive handling
├── data/                             # SQLDelight database and repositories (getAllHistory)
├── domain/                           # Domain models & interactors (GetContinueReading, GetPassportStamps, HistoryPreferences)
├── source-api/                       # Manga source and extension interfaces
├── source-local/                     # Local manga file reader
├── i18n/ & i18n-kmk/                 # Multilingual strings and custom Mochiku/Komikku strings (KMR)
└── build.gradle.kts                  # Gradle multi-project build configuration
```

---

## Building From Source

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

## License

Mochiku is licensed under the [Apache License 2.0](LICENSE) honoring the upstream licenses of Komikku, TachiyomiSY, and Mihon.

# BlockIT (FocusLock) 📱🔒

> **A minimalist, distraction-free Android focus lock engineered for deep work and digital wellbeing.** Inspired by Nothing OS monochrome typography with vibrant Accent Orange highlights.

---

## 🌟 Overview

**BlockIT** is an uncompromising focus and digital detox tool designed to protect your attention. When a focus session begins, BlockIT deploys an impenetrable, multi-layer distraction barrier that keeps non-essential applications out of reach while ensuring critical utilities (phone calls, emergency parachutes, and pre-whitelisted productivity apps) remain accessible.

---

## ✨ Key Features

### 1. Dual-State Lock Screen
- **Lock Screen 1 (Active HUD)**:
  - High-visibility digital countdown timer and seconds cards with custom pixel-font rendering.
  - **Rotating Preview Widget**: Repurposed widget card that auto-cycles between:
    - **Clock Widget**: Smooth analog clock canvas, digital time readout, date display, and 1-tap redirect to the system Clock app.
    - **Spotify Widget**: Interactive media player with **Previous (`⏮`)**, **Play / Pause (`▶ / ❚❚`)**, and **Next (`⏭`)** controls via `AudioManager`, plus direct Spotify launching.
    - Manual navigation dots with touch cooldown logic.
  - **Allowed Apps Dock**: Quick-launch dock accommodating up to 6 permitted applications with dashed empty indicators and live app icons.
  - **Slide-to-Exit Track**: Physical spring-damped slider with haptic feedback at 75% threshold completion.
  - **Emergency Parachute System**: Configurable safety-valve escape hatches to exit sessions in urgent scenarios.
  - **Emergency Dialer**: Direct access to the phone dialer; full pass-through for incoming and ongoing phone calls.

- **Lock Screen 2 (Ambient Screensaver / AOD)**:
  - Activates after 10 seconds of touch inactivity.
  - Fades away all non-timing controls into deep OLED black.
  - Continuously floats remaining time digits across a 2-axis sinusoidal path (±20dp X, ±28dp Y) to prevent screen burn-in.
  - **Anti-Accidental Tap Shield**: A single blind tap cleanly awakens the screen to Lock Screen 1 without misfiring on underlying apps or slider controls.

---

### 2. Standalone Scheduling Routines
- **Dedicated Bag Association**:
  - **Routine 1** ➔ **Bag 01**
  - **Routine 2** ➔ **Bag 02**
  - **Routine 3** ➔ **Bag 03**
- **100% Standalone Execution**: Routines never displace or reorder when start times or active days are modified.
- **Precision Rulers**: Hardware refresh rate continuous ruler pickers for exact start and duration times.
- **Vertical ON/OFF Switch**: Custom dual-state toggle with high-contrast `AccentOrange` (`#F55314`) state indication.

---

### 3. App Bags System
- 3 distinct bags (e.g., *Deep Work*, *Reading*, *Light Tasks*).
- 6-slot dock per bag with an intuitive bottom sheet app picker to customize allowed packages.
- Pixel-art bag previews and parachute balance controls.

---

### 4. Guided Permissions Setup Wizard
- A dedicated 5-step onboarding and reconfigurable wizard matching the design language:
  1. **Notifications (`POST_NOTIFICATIONS`)**: Persistent foreground service notifications.
  2. **Accessibility Service (`FocusAccessibilityService`)**: Window inspection, distraction barrier triggering, and status bar pull-down interception.
  3. **Display Over Other Apps (`SYSTEM_ALERT_WINDOW`)**: Instant full-screen barrier overlay.
  4. **Battery Optimization**: Exempts BlockIT from OS Doze and aggressive background killing.
  5. **Usage Access (`PACKAGE_USAGE_STATS`)**: Analytics tracking and focus duration insights.
- Live `Lifecycle.Event.ON_RESUME` detection updates permission state the instant the user returns from Android settings.

---

### 5. Stats & Daily Progress
- Weekly focus bar charts.
- Interactive rotary gauge showing today's completed focus duration.
- Detailed session audit history and streak tracking.
- Completion **DONE** celebration screen with pixel-art graphics and stats sharing.

---

## 🛠️ Architecture & Tech Stack

```
FocusLock/
├── app/
│   ├── src/main/java/com/focuslock/app/
│   │   ├── data/
│   │   │   ├── database/        # Room Database, Converters, DAOs (BagDao, RoutineDao, SessionDao)
│   │   │   ├── model/           # Data models (InstalledApp, RoutineEntity, etc.)
│   │   │   └── preferences/     # AppPreferences (SharedPreferences & StateFlow wrappers)
│   │   ├── service/             # FocusAccessibilityService, FocusLockService, Alarm & Boot Receivers
│   │   ├── ui/
│   │   │   ├── navigation/      # AppNavigation, Screen routes, HorizontalPager integration
│   │   │   ├── screens/
│   │   │   │   ├── home/        # HomeScreen, FocusDial, SlideToStart, VerticalRulerPicker
│   │   │   │   ├── bags/        # BagsScreen, AppPickerSheet, BagPixelArtView
│   │   │   │   ├── routines/    # RoutinesScreen, RoutineEditorDialog
│   │   │   │   ├── stats/       # StatsScreen, RotaryGaugeView
│   │   │   │   ├── lock/        # LockScreenActivity, LockScreenContent, DoneScreenContent
│   │   │   │   ├── permissions/ # PermissionsWizardScreen
│   │   │   │   └── settings/    # SettingsScreen (AOD toggle, permissions deep-links)
│   │   │   └── theme/           # Color tokens, typography, Theme definitions
│   │   └── util/                # PermissionUtils, System helpers
```

- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose with Material 3
- **Local Persistence**: Room Database + Kotlin Coroutines & Flow
- **Background Enforcement**: Android Foreground Service + AccessibilityService API + BroadcastReceivers + AlarmManager
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) with KSP

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (or newer)
- **JDK 17** or **JDK 21**
- Android SDK with minimum API 26 (Android 8.0) and Target API 34/35

### Building & Installing

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/BlockIT.git
   cd BlockIT/FocusLock
   ```

2. **Assemble Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Install on connected device or emulator**:
   ```bash
   ./gradlew installDebug
   ```
   *or via ADB directly:*
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Grant Required Permissions**:
   - Launch BlockIT on the device.
   - Follow the **Permissions Setup Wizard** to enable Notification, Accessibility, Overlay, and Battery Optimization permissions.

---

## 🔒 Security & Privacy

BlockIT is privacy-first:
- **Zero Internet Telemetry**: BlockIT does not harvest, transmit, or monetize your usage data.
- **Local Storage Only**: All routines, session histories, and bag assignments are stored purely in your local on-device SQLite database.
- **Accessibility Service Usage**: Strictly used to detect blacklisted foreground package transitions and dismiss distracting system overlays during an active focus session.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

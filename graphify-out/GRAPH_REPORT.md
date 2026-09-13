# Graph Report - FocusLock  (2026-09-13)

## Corpus Check
- Corpus is ~28,468 words - fits in a single context window. You may not need a graph.

## Summary
- 376 nodes · 758 edges · 24 communities (17 shown, 2 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 6 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- App Preferences & Parachutes
- Bag Data Access
- Home Dial & Controls
- Logo & UI Brand Components
- Routine Scheduling DAO
- Room Focus Database
- Accessibility Service Enforcement
- Session History DAO
- Foreground Lock Service
- Navigation & Wizard UI
- Main Activity Lifecycle
- Bags UI & App Picker
- System Permission Utils
- Navigation Destination Routes
- Watchdog Alarm Receiver
- System Boot Receiver
- Done Screen Pixel Art
- Room Type Converters
- Gradle Wrapper Script

## God Nodes (most connected - your core abstractions)
1. `AppPreferences` - 48 edges
2. `BagEntity` - 31 edges
3. `FocusAccessibilityService` - 23 edges
4. `FocusLockApp` - 21 edges
5. `RoutineEntity` - 21 edges
6. `BagsViewModel` - 20 edges
7. `FocusLockService` - 19 edges
8. `LockScreenContent()` - 14 edges
9. `SessionEntity` - 13 edges
10. `RoutinesViewModel` - 13 edges

## Surprising Connections (you probably didn't know these)
- `RoutineEditorDialog()` --references--> `BagEntity`  [EXTRACTED]
  app/src/main/java/com/focuslock/app/ui/screens/routines/RoutineEditorDialog.kt → app/src/main/java/com/focuslock/app/data/database/entities/BagEntity.kt
- `TopHeaderBar()` --calls--> `BlockLogoView()`  [INFERRED]
  app/src/main/java/com/focuslock/app/ui/screens/home/components/TopHeaderBar.kt → app/src/main/java/com/focuslock/app/ui/screens/home/components/BlockLogoView.kt
- `DoneScreenContent()` --calls--> `DonePixelArtView()`  [INFERRED]
  app/src/main/java/com/focuslock/app/ui/screens/lock/components/DoneScreenContent.kt → app/src/main/java/com/focuslock/app/ui/screens/lock/components/DonePixelArtView.kt
- `FocusLockApp` --calls--> `AppPreferences`  [EXTRACTED]
  app/src/main/java/com/focuslock/app/FocusLockApp.kt → app/src/main/java/com/focuslock/app/data/preferences/AppPreferences.kt
- `RoutinesViewModel` --references--> `BagEntity`  [EXTRACTED]
  app/src/main/java/com/focuslock/app/ui/screens/routines/RoutinesViewModel.kt → app/src/main/java/com/focuslock/app/data/database/entities/BagEntity.kt

## Import Cycles
- None detected.

## Communities (24 total, 2 thin omitted)

### Community 0 - "App Preferences & Parachutes"
Cohesion: 0.06
Nodes (3): AppPreferences, StateFlow, SharedPreferences

### Community 1 - "Bag Data Access"
Cohesion: 0.10
Nodes (9): BagDao, Flow, BagEntity, BagsViewModel, AndroidViewModel, StateFlow, HomeViewModel, AndroidViewModel (+1 more)

### Community 2 - "Home Dial & Controls"
Cohesion: 0.11
Nodes (28): FocusDial(), Modifier, Modifier, SlideToStart(), DigitalTimerDisplay(), Color, Dp, Modifier (+20 more)

### Community 3 - "Logo & UI Brand Components"
Cohesion: 0.14
Nodes (25): Activity, BlockLogoView(), Color, Dp, Modifier, DoneScreenContent(), Context, shareSessionStats() (+17 more)

### Community 4 - "Routine Scheduling DAO"
Cohesion: 0.13
Nodes (7): Flow, RoutineDao, RoutineEntity, RoutineScheduler, AndroidViewModel, StateFlow, RoutinesViewModel

### Community 5 - "Room Focus Database"
Cohesion: 0.13
Nodes (13): FocusDatabase, FocusDatabaseCallback, Context, FocusLockApp, BroadcastReceiver, Context, Intent, RoutineAlarmReceiver (+5 more)

### Community 6 - "Accessibility Service Enforcement"
Cohesion: 0.20
Nodes (4): AccessibilityEvent, AccessibilityService, FocusAccessibilityService, Intent

### Community 7 - "Session History DAO"
Cohesion: 0.17
Nodes (6): Flow, SessionDao, SessionEntity, AndroidViewModel, StateFlow, StatsViewModel

### Community 8 - "Foreground Lock Service"
Cohesion: 0.21
Nodes (7): FocusLockService, Context, Intent, IBinder, Job, Notification, Service

### Community 9 - "Navigation & Wizard UI"
Cohesion: 0.22
Nodes (16): AppNavigation(), MainPagerScreen(), Color, Dp, ImageVector, Modifier, PermissionsWizardScreen(), StepContentCard() (+8 more)

### Community 10 - "Main Activity Lifecycle"
Cohesion: 0.21
Nodes (8): android, Bundle, ComponentActivity, MainActivity, Bundle, ComponentActivity, LockScreenActivity, FocusLockTheme()

### Community 11 - "Bags UI & App Picker"
Cohesion: 0.23
Nodes (11): InstalledApp, AppPickerSheet(), AppSlotItem(), BagsScreen(), FallbackSlotIcon(), Modifier, SlotOptionsModalSheet(), Dp (+3 more)

### Community 13 - "Navigation Destination Routes"
Cohesion: 0.25
Nodes (7): Bags, Home, PermissionsWizard, Routines, Screen, Settings, Stats

### Community 14 - "Watchdog Alarm Receiver"
Cohesion: 0.52
Nodes (4): BroadcastReceiver, Context, Intent, LockWatchdogReceiver

### Community 15 - "System Boot Receiver"
Cohesion: 0.53
Nodes (4): BootReceiver, BroadcastReceiver, Context, Intent

### Community 16 - "Done Screen Pixel Art"
Cohesion: 0.70
Nodes (4): DonePixelArtView(), Color, Dp, Modifier

### Community 18 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **6 isolated node(s):** `Home`, `Bags`, `Routines`, `Stats`, `Settings` (+1 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 60 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FocusLockApp` connect `Room Focus Database` to `App Preferences & Parachutes`, `Bag Data Access`, `Logo & UI Brand Components`, `Routine Scheduling DAO`, `Accessibility Service Enforcement`, `Session History DAO`, `Foreground Lock Service`, `Navigation & Wizard UI`, `Watchdog Alarm Receiver`, `System Boot Receiver`?**
  _High betweenness centrality (0.352) - this node is a cross-community bridge._
- **Why does `AppPreferences` connect `App Preferences & Parachutes` to `Room Focus Database`?**
  _High betweenness centrality (0.211) - this node is a cross-community bridge._
- **Why does `BagEntity` connect `Bag Data Access` to `Home Dial & Controls`, `Logo & UI Brand Components`, `Routine Scheduling DAO`, `Room Focus Database`, `Bags UI & App Picker`?**
  _High betweenness centrality (0.164) - this node is a cross-community bridge._
- **What connects `Home`, `Bags`, `Routines` to the rest of the system?**
  _6 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `App Preferences & Parachutes` be split into smaller, more focused modules?**
  _Cohesion score 0.06105457909343201 - nodes in this community are weakly interconnected._
- **Should `Bag Data Access` be split into smaller, more focused modules?**
  _Cohesion score 0.10121457489878542 - nodes in this community are weakly interconnected._
- **Should `Home Dial & Controls` be split into smaller, more focused modules?**
  _Cohesion score 0.10953058321479374 - nodes in this community are weakly interconnected._
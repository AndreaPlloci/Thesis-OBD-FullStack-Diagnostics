# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build and install on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run a single test class
./gradlew test --tests "com.andreaplloci.thesisobdapp.ExampleUnitTest"

# Lint
./gradlew lint

# Clean
./gradlew clean
```

## Architecture Overview

**Jarvis Hub** is a single-activity Android app (Jetpack Compose) for automotive OBD-II diagnostics via Bluetooth. It scans vehicle parameters and sends diagnostic reports to an n8n webhook backend.

### Screen Flow (wizard-style)
```
ConnectionScreen → SelectionScreen → ReadingScreen → DiagnosticScreen → SuccessScreen
```
All navigation state lives in `ui/AppNavigation.kt`, which is a single Composable holding shared state (connected device, OBD data, vehicle specs) and rendering the appropriate screen based on a `currentScreen` enum.

### Key Components

- **`ObdManager.kt`** — Core Bluetooth RFCOMM socket manager. Sends AT initialization commands (`AT Z`, `AT E0`, `AT S0`, `AT SP 5`) then reads PIDs via polling. Uses 150ms delays between commands for Bosch ECU compatibility. Protocol 5 is hardcoded for Alfa Romeo 159 compatibility.
- **`Models.kt`** — All data classes: `ObdData` (sensor readings), `MaintenanceWork`, `ReportRequest` (full payload).
- **`data/CarDatabase.kt`** — Static lookup tables for supported vehicles (Audi A3, Alfa Romeo 159/Giulia). Cascading brand → model → year → engine dropdowns are driven from here.
- **`repository/ReportRepository.kt`** — Sends the final `ReportRequest` via Retrofit to the n8n webhook.
- **`N8nApiService.kt` + `RetrofitConfig.kt`** — Retrofit setup targeting `https://jarvis.tail34577a.ts.net/`.

### OBD PIDs Read
- `0105` — Coolant temperature
- `0104` — Engine load
- `0110` — Mass Air Flow (MAF)
- `0123` — Fuel rail pressure
- `AT RV` — Battery voltage
- `03` — DTCs (Diagnostic Trouble Codes)

### Permissions Required
Runtime: `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `ACCESS_FINE_LOCATION` — handled in `ui/components/PermissionHandler.kt`.

### Tech Stack
- Jetpack Compose + Navigation (no NavController — manual state-driven navigation)
- Kotlin Coroutines for async Bluetooth I/O
- Retrofit 2 + OkHttp (60s timeout) + Gson
- `obd-java-api 1.0` (pires library) for OBD command objects
- Version catalog at `gradle/libs.versions.toml` (AGP 9.1, Kotlin 2.2.10, Compose BOM 2024.09.00)

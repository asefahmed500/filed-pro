# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FieldForce Pro is a field workforce management application with an Android client (`:app`) and Ktor backend (`:server`). The app enables executives to check in/out, manage tasks, track visits, and handle file records (expenses, PODs, incidents, timesheets) with offline-first sync.

## Build & Run Commands

```bash
# Backend (Ktor/Netty on port 8080)
./gradlew :server:run

# Android app
./gradlew :app:assembleDebug                 # Build debug APK
./gradlew :app:installDebug                  # Install on connected device

# Testing
./gradlew test                               # All unit tests
./gradlew :app:test                          # Android unit tests (JUnit4 + Robolectric + Roborazzi)
./gradlew :app:connectedCheck                # Instrumented tests (requires emulator/device)
./gradlew :app:test --tests "ClassName"      # Run specific test class

# Typecheck
./gradlew compileKotlin                      # Typecheck all modules
./gradlew lint                               # Android Lint
```

## Architecture

### Multi-module Kotlin/Gradle Monorepo
- **`:app`** — Android app (Jetpack Compose, Room, Retrofit, Coil)
- **`:server`** — Ktor backend (Netty, PostgreSQL, Exposed ORM)
- **Gradle version catalog**: `gradle/libs.versions.toml`

### Entry Points
- **Android**: `MainActivity.kt` → `FieldForceApp()` composable
- **Server**: `ServerMain.kt` → `Application.module()`

### Data Layer (Offline-first)
- **Room local DB** mirrors PostgreSQL for offline capability
- **`FieldForceRepository`** (`Repository.kt`): tries remote first via Retrofit, falls back to local Room DB
- **Offline queue** buffers mutations when offline, syncs via `POST /api/sync`
- **Retrofit BASE_URL**: `http://10.0.2.2:8080/` (Android emulator → host loopback)

### Database
- **Server**: PostgreSQL + Exposed ORM. Default: `postgres`/`postgres` @ `jdbc:postgresql://localhost:5432/fieldforce`
- Override via env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD` (configured in `server/src/main/resources/application.conf`)
- Tables auto-created + seed data on first launch via `SchemaUtils.create()`

### UI Architecture
- **All screens in one file**: `FieldForceApp.kt` (~1334 lines) contains `DashboardView`, `TasksAndVisitsView`, `FileTrackingCenterView`, `InteractiveSimulationMapView`, `PrdScopeTodoView`
- **No DI framework**: Manual `object` singletons (`DatabaseFactory`, `FieldForceDatabase`, `FieldForceRepository`) and `AndroidViewModel`
- **State**: `FieldForceViewModel` manages UI state

### Auth & Roles
- **No passwords**: Login = email lookup in `WelcomeViews.kt`. Any email matching a seed user works.
- **Fast login**: Hardcoded IDs `admin_1`, `manager_1`, `exec_1`, `exec_2` (see user switcher in `FieldForceApp.kt`)
- **Roles**: `ADMIN`, `MANAGER`, `EXECUTIVE`

## Entity Types

- **User**: id, email, name, role (ADMIN/MANAGER/EXECUTIVE), phone, photoUri, reportingManagerId, workZone (name, lat, lng, radius)
- **Attendance**: employeeId, checkInTime, checkOutTime, checkInLat/Lng, checkInSelfieUri, notes, tasksCompleted, expenses
- **Task**: title, description, priority (HIGH/MEDIUM/LOW), dueDate, location, status, assignedTo, proof (photo, signature), managerFeedback
- **Visit**: executiveId, customerName, address, checkInTime, checkOutTime, notes, signature, photo, reportPdf
- **FileRecord**: fileName, category (EXPENSE/POD/INCIDENT/TIMESHEET), fileUri, uploadedBy, timestamp, location, tags, amount, status
- **Notification**: userId, title, description, timestamp, isRead

## Gotchas

- **Room DB version 1**: No migration support. Schema changes require uninstall/reinstall of app.
- **Theme**: Always forces **light mode** (`MyApplicationTheme` ignores `darkTheme` parameter)
- **GPS dependencies**: Commented out in `app/build.gradle.kts` but declared in `libs.versions.toml` (play-services-location, accompanist-permissions)
- **Camera dependencies**: Commented out in `app/build.gradle.kts` but declared in catalog
- **Keystore**: `debug.keystore` committed as `debug.keystore.base64`. For local dev, remove `signingConfig = signingConfigs.getByName("debugConfig")` from `app/build.gradle.kts`
- **Gemini API**: `.env` file only used for `GEMINI_API_KEY` by Secrets Gradle plugin; app doesn't currently use Gemini
- **Server DB**: `SchemaUtils.create()` drops-and-recreates tables — not safe for production; use explicit migrations
- **GPS simulation**: In-app slider for testing geofencing without real location

## Environment Setup

Create `.env` from `.env.example`:
```
GEMINI_API_KEY=your_key_here
```

Ensure PostgreSQL is running on localhost:5432 before starting the server.

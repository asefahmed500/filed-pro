# FieldForce Pro — AGENTS.md

## Project structure
- **Not a web/Node.js project.** Multi-module Kotlin/Gradle monorepo.
- `:app` — Android app (Jetpack Compose, Room, Retrofit, Coil). Entry: `MainActivity.kt` → `FieldForceApp()` composable.
- `:server` — Ktor backend (Netty, PostgreSQL, Exposed ORM). Entry: `ServerMainKt.module`.
- Gradle version catalog: `gradle/libs.versions.toml`.

## Commands
```bash
./gradlew :server:run                    # Start Ktor backend (port 8080)
./gradlew :app:assembleDebug             # Build debug APK
./gradlew test                           # All unit tests
./gradlew :app:test                      # Android unit tests only (JUnit4 + Robolectric + Roborazzi)
./gradlew :app:connectedCheck            # Instrumented tests (needs emulator/device)
./gradlew lint                           # Android Lint
./gradlew compileKotlin                  # Typecheck all modules
```

## Database (server)
Default: `postgres`/`postgres` @ `jdbc:postgresql://localhost:5432/fieldforce`
Override via env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD` (defined in `server/src/main/resources/application.conf`).
Tables auto-created + seed data on first launch.

## Auth (non-obvious)
- **No passwords.** Login = email lookup (`WelcomeViews.kt`). Any email matching a seed user works.
- Fast login via hardcoded IDs: `admin_1`, `manager_1`, `exec_1`, `exec_2` (see user switcher in `FieldForceApp.kt`).
- 3 roles: `ADMIN`, `MANAGER`, `EXECUTIVE`.

## Architecture quirks
- **All UI in one file:** `FieldForceApp.kt` (~1334 lines). All screens (`DashboardView`, `TasksAndVisitsView`, `FileTrackingCenterView`, `InteractiveSimulationMapView`, `PrdScopeTodoView`) are called from this file. No separate screen files.
- **Offline-first:** Room local DB mirrors PostgreSQL. `Repository.kt` tries remote first, falls back to local. Offline queue buffers mutations, syncs via `POST /api/sync`.
- **Retrofit BASE_URL** hardcoded to `http://10.0.2.2:8080/` (Android emulator → host loopback).
- **No DI framework.** Manual `object` singletons (`DatabaseFactory`, `FieldForceDatabase`, `FieldForceRepository`) and `AndroidViewModel`.
- **GPS simulation slider** in the app for testing geofencing without real location.

## Gotchas
- `debug.keystore` is committed as `debug.keystore.base64` (decode before use). For local dev, remove `signingConfig = signingConfigs.getByName("debugConfig")` from `app/build.gradle.kts` as README says.
- Theme always forces **light mode** (`MyApplicationTheme` ignores `darkTheme` parameter).
- Room DB **version 1, no migration support.** Schema changes require uninstall/reinstall.
- GPS/camera dependencies are commented out in `app/build.gradle.kts` but declared in `libs.versions.toml`.
- Ktor server uses `SchemaUtils.create()` — **drops-and-recreates is not safe** for production; use explicit migrations for production.
- `.env` file (from `.env.example`) only used for `GEMINI_API_KEY` by the Secrets Gradle plugin — the app itself doesn't currently use Gemini.
- No CI/CD, no Docker, no pre-commit hooks configured.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FieldForce Pro is a field workforce management application with an Android client (`:app`) and Ktor backend (`:server`). The app enables executives to check in/out, manage tasks, track visits, and handle file records (expenses, PODs, incidents, timesheets) with offline-first sync.

**Recent Refactoring (May 2026)**: The codebase has been refactored to follow Clean Architecture principles. New code uses `data/local/`, `domain/`, and `presentation/` packages, while legacy code remains in `data/` and `ui/` packages.

## Build & Run Commands

```bash
# Backend (Ktor/Netty on port 8081)
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

### Clean Architecture Package Structure (New - May 2026)

```
com.example/
├── data/
│   ├── local/                    # Room database (NEW - refactored)
│   │   ├── entity/              # Room entities: UserEntity, AttendanceEntity, etc.
│   │   ├── dao/                 # Room DAOs: UserDao, AttendanceDao, etc.
│   │   └── database/            # FieldForceDatabase
│   ├── FieldForceApiService.kt  # Retrofit API client
│   ├── Repository.kt            # Data repository with offline sync (legacy)
│   ├── SyncQueue.kt             # Offline sync queue entity
│   └── SyncQueueDao.kt          # Sync queue DAO
├── domain/                     # Domain layer (NEW - refactored)
│   ├── model/                   # Domain models: User, Task, Attendance, etc.
│   │   └── Result.kt            # Error handling wrapper
│   └── usecase/                 # Business logic: LoginWithEmailUseCase, etc.
├── presentation/               # Presentation layer (NEW - refactored)
│   ├── component/common/        # Reusable UI components
│   │   ├── button/             # PrimaryButton, SecondaryButton, etc.
│   │   └── input/              # FfTextField, FfNumberField, etc.
│   ├── navigation/             # Type-safe navigation routes
│   └── ui/state/               # UiState, UiEvent, UiEffect contracts
├── di/                        # Dependency injection (NEW - refactored)
│   └── AppContainer.kt         # Manual DI container
└── ui/                        # Legacy UI (being migrated)
    ├── FieldForceApp.kt        # Main app with all screens (~1300 lines)
    ├── FieldForceViews.kt     # Reusable UI components
    ├── WelcomeViews.kt         # Login/welcome screens
    ├── FieldForceViewModel.kt # State management (monolithic)
    └── PrdScopeView.kt        # PRD documentation view
```

**Migration Note**: When adding new features, use the Clean Architecture packages (`domain/`, `data/local/`, `presentation/`). Legacy `ui/` code will be migrated incrementally.

### Data Layer (Offline-first)
- **Room local DB** mirrors PostgreSQL for offline capability
- **`FieldForceRepository`** (`Repository.kt`): tries remote first via Retrofit, falls back to local Room DB
- **Offline queue** (`SyncQueueEntity`) buffers mutations when offline, syncs via `POST /api/sync`
- **Retrofit BASE_URL**: `http://10.0.2.2:8081/` (Android emulator → host loopback)
- **Use Cases** (NEW): `LoginWithEmailUseCase`, `CheckInUseCase`, `CheckOutUseCase` encapsulate business logic

### Database
- **Server**: PostgreSQL + Exposed ORM. Default: `postgres`/`postgres` @ `jdbc:postgresql://localhost:5432/fieldforce`
- Override via env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD` (configured in `server/src/main/resources/application.conf`)
- Tables auto-created + seed data on first launch via `SchemaUtils.create()`
- **Local DB**: Room database version 3 with fallback migration

### UI Architecture
- **Legacy**: `FieldForceViewModel` (monolithic, ~627 lines) manages all UI state
- **New**: `AppContainer` provides ViewModels with dependency injection
- **State**: Expose as `StateFlow<T>` for UI, `SharedFlow` for one-time events
- **Result Type**: Use `Result<T>` sealed class for consistent error handling (Success, Error, Loading)

### Auth & Roles
- **No passwords**: Login = email lookup in `WelcomeViews.kt`. Any email matching a seed user works.
- **Fast login**: Hardcoded IDs `admin_1`, `manager_1`, `exec_1`, `exec_2` (see user switcher in `FieldForceApp.kt`)
- **Roles**: `ADMIN`, `MANAGER`, `EXECUTIVE` (enums in `User.Role`)
- **Domain Models**: Use `User.fromEntity()` to convert Room entities to domain models

## Entity Types

### Domain Models (NEW - in `domain/model/`)
- **User**: id, email, name, role (enum), phone, photoUri, reportingManagerId, workZone (data class with lat/lng/radius)
- **Attendance**: employeeId, checkInTime, checkOutTime, checkInLocation, checkInSelfieUri, notes, expenses, geofence status
- **Task**: title, description, priority (enum: HIGH/MEDIUM/LOW), dueDate, location, status (enum: PENDING/IN_PROGRESS/COMPLETED/REJECTED)
- **Visit**: executiveId, customerName, address, checkInTime, checkOutTime, notes, signature, photo, reportPdf
- **FileRecord**: fileName, category (enum: EXPENSE/POD/INCIDENT/TIMESHEET), fileUri, uploadedBy, amount, status (enum)
- **Notification**: userId, title, description, timestamp, isRead (with factory methods)

### Room Entities (in `data/local/entity/`)
- **UserEntity**, **AttendanceEntity**, **TaskEntity**, **VisitEntity**, **FileRecordEntity**, **NotificationEntity**, **SyncQueueEntity**
- Use domain model extensions like `User.fromEntity()` to convert

## Gotchas

- **Room DB version 3**: New structure after refactoring. Uses `fallbackToDestructiveMigration()` for development.
- **Two Entity Types**: Room entities in `data/local/entity/`, domain models in `domain/model/`. Always use domain models in business logic.
- **Theme**: Always forces **light mode** (`MyApplicationTheme` ignores `darkTheme` parameter)
- **GPS dependencies**: Commented out in `app/build.gradle.kts` but declared in `libs.versions.toml` (play-services-location, accompanist-permissions)
- **Camera dependencies**: Commented out in `app/build.gradle.kts` but declared in catalog
- **Keystore**: `debug.keystore` committed as `debug.keystore.base64`. For local dev, remove `signingConfig = signingConfigs.getByName("debugConfig")` from `app/build.gradle.kts`
- **Gemini API**: `.env` file only used for `GEMINI_API_KEY` by Secrets Gradle plugin; app doesn't currently use Gemini
- **Server DB**: `SchemaUtils.create()` drops-and-recreates tables — not safe for production; use explicit migrations
- **GPS simulation**: In-app slider for testing geofencing without real location
- **API Port**: Server runs on port 8081 (8080 occupied by EnterpriseDB)

## Environment Setup

Create `.env` from `.env.example`:
```
GEMINI_API_KEY=your_key_here
```

Ensure PostgreSQL is running on localhost:5432 before starting the server.

## Using Use Cases (NEW Pattern)

When adding new features, use the use case pattern:

```kotlin
// 1. Create domain model in domain/model/
// 2. Create use case in domain/usecase/
class MyFeatureUseCase(
    private val dao: SomeDao,
    private val notificationDao: NotificationDao
) {
    suspend operator fun invoke(params): Result<MyModel> {
        // Business logic here
    }
}

// 3. Inject in AppContainer
val myFeatureUseCase by lazy { MyFeatureUseCase(dao, notificationDao) }

// 4. Use in ViewModel
val result = myFeatureUseCase(params)
when (result) {
    is Result.Success -> // handle success
    is Result.Error -> // handle error
    is Result.Loading -> // show loading
}
```

## Error Handling (NEW Pattern)

Use the `Result<T>` sealed class for all operations:

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: UiError) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

sealed class UiError {
    data class Network(val message: String) : UiError()
    data class Validation(val message: String, val field: String?) : UiError()
    data class Auth(val message: String) : UiError()
    // ...
}
```

## Common Patterns

### Creating a new screen:
1. Create domain models in `domain/model/`
2. Create Room entities and DAOs in `data/local/`
3. Create use cases in `domain/usecase/`
4. Create ViewModels with `AppContainer` injection
5. Create reusable components in `presentation/component/common/`
6. Add navigation route in `presentation/navigation/Routes.kt`

### Adding offline sync:
1. Entity must extend or map to `SyncQueueEntity`
2. Use `enqueueForSync()` in repository
3. Call `processPendingSyncItems()` when connection restored

### Role-based UI:
Use `user.isAdmin()`, `user.isManager()`, `user.isExecutive()` instead of string comparison.

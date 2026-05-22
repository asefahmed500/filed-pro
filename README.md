# FieldForce Pro

<div align="center">
  <h3>🚀 Enterprise Field Operations Management System</h3>
  <p>A comprehensive mobile workforce management solution with real-time tracking, task management, and team oversight.</p>
</div>

---

## 📱 Overview

FieldForce Pro is a field workforce management application built with:
- **Android Client**: Jetpack Compose, Material Design 3, Room, Retrofit
- **Ktor Backend**: PostgreSQL, Exposed ORM, RESTful API
- **Real-time Features**: GPS tracking, offline-first architecture, push notifications

### Key Features

#### For Field Executives
- ✅ GPS-verified check-in/check-out with selfie verification
- ✅ Task management with priority levels and status tracking
- ✅ Customer visit logging with digital signatures
- ✅ Expense and document submission (PODs, receipts, incident reports)
- ✅ Offline mode with automatic sync

#### For Managers
- ✅ Real-time team overview and location tracking
- ✅ Task assignment and workflow management
- ✅ Approval system for expenses and documents
- ✅ Performance monitoring and reporting

#### For Administrators
- ✅ KPI dashboards and analytics
- ✅ User management and role configuration
- ✅ System-wide monitoring and controls
- ✅ Advanced reporting and data export

---

## 🏗️ Architecture

### Multi-Module Structure
```
fieldforce-pro/
├── app/                    # Android application module
│   └── src/main/
│       ├── java/com/example/
│       │   ├── data/      # Data layer (Room, Retrofit)
│       │   ├── ui/        # Compose UI components
│       │   └── MainActivity.kt
├── server/                 # Ktor backend module
│   └── src/main/kotlin/com/example/server/
│       ├── database/      # Exposed ORM, PostgreSQL
│       └── routes/        # API endpoints
├── gradle/                 # Gradle wrapper and configuration
├── build.gradle.kts        # Root build file
└── settings.gradle.kts     # Project settings
```

### Technology Stack

**Android**
- Kotlin 2.2.10
- Jetpack Compose 1.9.0
- Material 3 Design System
- Room Database 2.7.0
- Retrofit 2.12.0
- Coil 3.0.4
- Coroutines 1.10.2

**Server**
- Kotlin 2.2.10
- Ktor 3.0.3
- PostgreSQL (via HikariCP)
- Exposed ORM 0.48.0

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- PostgreSQL 14 or later
- Android SDK API 24+

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/yourusername/fieldforce-pro.git
cd fieldforce-pro

# Build the project
./gradlew build

# Run the server (in a separate terminal)
cd server
../gradlew :server:run

# Build and install the Android app
./gradlew :app:installDebug
```

### Environment Setup

1. **Database Setup**
   ```bash
   # Create PostgreSQL database
   createdb fieldforce

   # Or use Docker
   docker run --name fieldforce-db \
     -e POSTGRES_PASSWORD=postgres \
     -e POSTGRES_DB=fieldforce \
     -p 5432:5432 \
     -d postgres:14
   ```

2. **Configuration**
   - Copy `.env.example` to `.env`
   - Configure database connection in `server/src/main/resources/application.conf`
   - For Android emulator, server is accessible at `http://10.0.2.2:8080/`
   - For physical device, replace with your local IP address

3. **Generate Debug Keystore** (if needed)
   ```bash
   base64 -d debug.keystore.base64 > debug.keystore
   ```

---

## 📱 Usage

### First Launch

1. **Quick Login** - Use one of the demo accounts:
   - **Admin**: admin@fieldforce.pro (Arthur Pendragon)
   - **Manager**: morgan.lefay@fieldforce.pro (Morgan LeFay)
   - **Executive**: lancelot.dulac@fieldforce.pro (Lancelot DuLac)

2. **Complete Onboarding** - Follow the role-specific setup guide

3. **Start Working** - Check in, view tasks, submit updates

### Key Workflows

**Check-In/Check-Out**
1. Tap "Check In" from dashboard
2. Allow GPS permission
3. Take verification selfie
4. Add shift note
5. Confirm

**Task Management**
1. View assigned tasks from Tasks tab
2. Tap task for details
3. Update status (Pending → In Progress → Completed)
4. Add proof photos and signatures

**File Submission**
1. Tap "+" button in Files tab
2. Select category (Expense, POD, Incident, Timesheet)
3. Capture/upload document
4. Add details and submit

---

## 🔧 Development

### Build Commands

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew :app:assembleDebug

# Build release APK
./gradlew :app:assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run lint
./gradlew lint

# Typecheck only
./gradlew compileKotlin
```

### Project Structure

**Data Layer** (`app/src/main/java/com/example/data/`)
- `FieldForceDatabase.kt` - Room database
- `FieldForceRepository.kt` - Data repository with offline sync
- `FieldForceApiService.kt` - Retrofit API service
- `Entities.kt` - Room entities
- `DAOs.kt` - Data Access Objects
- `TypeAliases.kt` - Domain type aliases

**UI Layer** (`app/src/main/java/com/example/ui/`)
- `FieldForceApp.kt` - Main app navigation
- `FieldForceViews.kt` - Screen implementations
- `WelcomeViews.kt` - Login/signup screens
- `FieldForceViewModel.kt` - State management
- `design/` - Design system components
- `components/` - Reusable UI components
- `onboarding/` - Onboarding flow

**Server Layer** (`server/src/main/kotlin/com/example/server/`)
- `ServerMain.kt` - Ktor server entry
- `routes/Routes.kt` - API route definitions
- `database/DatabaseFactory.kt` - Database initialization
- `database/Schemas.kt` - Exposed table definitions

### Design System

The app uses a comprehensive design system defined in `ui/design/AppDesign.kt`:
- **Colors**: Based on OKLCH color space for perceptual uniformity
- **Typography**: Consistent font sizes and weights
- **Spacing**: 4dp grid system
- **Components**: Reusable cards, buttons, badges

---

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Screenshot Tests
```bash
./gradlew recordRoborazziDebugRoborazziStandalone
./gradlew verifyRoborazziDebugRoborazziStandalone
```

---

## 📊 Server API

### Base URL
- Emulator: `http://10.0.2.2:8080/`
- Device: `http://YOUR_LOCAL_IP:8080/`

### Endpoints

**Users**
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create/update user

**Attendance**
- `GET /api/attendance` - List all attendance
- `GET /api/attendance/employee/{employeeId}` - Get employee attendance
- `GET /api/attendance/employee/{employeeId}/active` - Get active attendance
- `POST /api/attendance` - Check in/out

**Tasks**
- `GET /api/tasks` - List all tasks
- `GET /api/tasks/employee/{employeeId}` - Get employee tasks
- `POST /api/tasks` - Create/update task
- `DELETE /api/tasks/{id}` - Delete task

**Files**
- `GET /api/files` - List all files
- `POST /api/files` - Upload file
- `DELETE /api/files/{id}` - Delete file

**Sync**
- `POST /api/sync` - Sync offline changes

---

## 🔄 Offline Architecture

The app uses an offline-first architecture:

1. **Local Database**: Room database mirrors PostgreSQL
2. **Offline Queue**: Changes are queued when offline
3. **Auto Sync**: Changes sync when connection restored
4. **Conflict Resolution**: Server state takes precedence

---

## 🎨 UI/UX Features

- **Material Design 3**: Modern, consistent interface
- **Dark Mode**: Automatic theme switching
- **Responsive Layouts**: Adapts to different screen sizes
- **Accessibility**: Proper labels, focus order, contrast ratios
- **Animations**: Smooth transitions and feedback
- **Loading States**: Clear progress indicators
- **Error Handling**: User-friendly error messages
- **Empty States**: Helpful messages when no data

---

## 📝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Code Style**: Follow Kotlin coding conventions
2. **Testing**: Add tests for new features
3. **Documentation**: Update relevant documentation
4. **Commits**: Use clear commit messages

### Development Workflow

1. Create feature branch: `git checkout -b feature/my-feature`
2. Make changes and test
3. Submit pull request
4. Address review feedback
5. Merge to main

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 🙏 Acknowledgments

- **Jetpack Compose** - Modern UI toolkit
- **Material Design 3** - Design system
- **Ktor** - Asynchronous framework
- **Exposed ORM** - SQL framework
- **Coil** - Image loading

---

## 📞 Support

For issues and questions:
- Create an issue on GitHub
- Check documentation in `/docs` folder
- Review CLAUDE.md for development guidance

---

<div align="center">
  <p>Built with ❤️ for field workforce management</p>
  <p>© 2026 FieldForce Pro. All rights reserved.</p>
</div>

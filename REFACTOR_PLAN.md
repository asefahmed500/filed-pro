# FieldForce Pro - Android Kotlin Refactoring Plan

## Executive Summary

**Current State:** Monolithic structure with single ViewModel, all entities/DAOs in single files, no domain layer, minimal separation of concerns.

**Target State:** Clean Architecture with feature-based modules, reusable components, proper state management, and production-ready code.

---

## Phase 1: Code Restructuring (Foundation)

### 1.1 Feature-Based Package Structure
**Current:** `com.example.data`, `com.example.ui`

**Target:**
```
com.example/
├── data/
│   ├── local/
│   │   ├── entity/
│   │   │   ├── UserEntity.kt
│   │   │   ├── AttendanceEntity.kt
│   │   │   └── ...
│   │   ├── dao/
│   │   │   ├── UserDao.kt
│   │   │   ├── AttendanceDao.kt
│   │   │   └── ...
│   │   └── database/
│   │       └── FieldForceDatabase.kt
│   ├── remote/
│   │   ├── dto/
│   │   │   ├── UserDto.kt
│   │   │   └── ...
│   │   ├── api/
│   │   │   └── FieldForceApiService.kt
│   │   └── interceptor/
│   │       └── AuthInterceptor.kt
│   └── repository/
│       ├── UserRepository.kt
│       ├── AttendanceRepository.kt
│       └── ...
├── domain/
│   ├── model/
│   │   ├── User.kt
│   │   ├── Attendance.kt
│   │   └── ...
│   ├── usecase/
│   │   ├── LoginUseCase.kt
│   │   ├── CheckInUseCase.kt
│   │   └── ...
│   └── result/
│       └── Result.kt
├── presentation/
│   ├── feature/
│   │   ├── auth/
│   │   │   ├── login/
│   │   │   │   ├── LoginScreen.kt
│   │   │   │   ├── LoginViewModel.kt
│   │   │   │   └── LoginContract.kt
│   │   ├── dashboard/
│   │   │   ├── DashboardScreen.kt
│   │   │   ├── DashboardViewModel.kt
│   │   │   └── DashboardContract.kt
│   │   ├── tasks/
│   │   │   ├── TasksScreen.kt
│   │   │   ├── TasksViewModel.kt
│   │   │   └── TasksContract.kt
│   │   ├── attendance/
│   │   │   ├── AttendanceScreen.kt
│   │   │   ├── AttendanceViewModel.kt
│   │   │   └── AttendanceContract.kt
│   │   └── files/
│   │       ├── FilesScreen.kt
│   │       ├── FilesViewModel.kt
│   │       └── FilesContract.kt
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Routes.kt
│   ├── component/
│   │   ├── common/
│   │   │   ├── button/
│   │   │   │   ├── PrimaryButton.kt
│   │   │   │   └── SecondaryButton.kt
│   │   │   ├── card/
│   │   │   │   ├── UserCard.kt
│   │   │   │   └── TaskCard.kt
│   │   │   ├── input/
│   │   │   │   ├── FfTextField.kt
│   │   │   │   └── FfOutlinedTextField.kt
│   │   │   ├── dialog/
│   │   │   │   ├── FfAlertDialog.kt
│   │   │   │   └── FfBottomSheet.kt
│   │   │   �── avatar/
│   │   │   │   └── UserAvatar.kt
│   │   │   └── loading/
│   │   │       └── LoadingIndicator.kt
│   │   └── theme/
│   │       ├── color/
│   │       ├── typography/
│   │       ├── shape/
│   │       └── dimen/
│   └── ui/
│       ├── state/
│       │   └── UiState.kt
│       └── event/
│           └── UiEvent.kt
├── di/
│   ├── AppModule.kt
│   ├── RepositoryModule.kt
│   └── UseCaseModule.kt
└── util/
    ├── extension/
    │   ├── FlowExt.kt
    │   ├── ContextExt.kt
    │   └── ViewExt.kt
    └── formatter/
        ├── DateFormatter.kt
        └── NumberFormatter.kt
```

### 1.2 Split Entities.kt
- Extract each entity to separate file
- Add proper documentation
- Use proper type aliases

### 1.3 Split DAOs.kt
- Extract each DAO to separate file
- Add proper documentation
- Organize by domain

---

## Phase 2: Domain Layer Implementation

### 2.1 Create Domain Models
- Separate domain models from Room entities
- Add proper validation
- Use value classes where appropriate

### 2.2 Create Use Cases
Extract business logic from ViewModel into use cases:
- `LoginWithEmailUseCase`
- `LoginWithIdUseCase`
- `SignUpUseCase`
- `CheckInUseCase`
- `CheckOutUseCase`
- `AssignTaskUseCase`
- `UpdateTaskStatusUseCase`
- `StartVisitUseCase`
- `EndVisitUseCase`
- `SubmitFileUseCase`
- `ApproveFileUseCase`
- `RejectFileUseCase`
- `RefreshDataUseCase`
- `ToggleOnlineModeUseCase`

### 2.3 Create Result Wrapper
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: UiError) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

sealed class UiError {
    data class Network(val message: String) : UiError()
    data class Validation(val message: String) : UiError()
    data class Auth(val message: String) : UiError()
    data class Unknown(val message: String) : UiError()
}
```

---

## Phase 3: UI Layer Refactoring

### 3.1 Create Screen Contracts
Each screen will have a contract with:
- `UiState` sealed class
- `UiEvent` sealed class
- `UiEffect` sealed class (one-time events)

Example:
```kotlin
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

sealed class LoginUiEvent {
    data class EmailChanged(val email: String) : LoginUiEvent()
    data class PasswordChanged(val password: String) : LoginUiEvent()
    object LoginClicked : LoginUiEvent()
}

sealed class LoginUiEffect {
    data class ShowToast(val message: String) : LoginUiEffect()
    object NavigateToDashboard : LoginUiEffect()
}
```

### 3.2 Split FieldForceViewModel
Create separate ViewModels per feature:
- `AuthViewModel`
- `DashboardViewModel`
- `TasksViewModel`
- `AttendanceViewModel`
- `FilesViewModel`
- `MapViewModel`

### 3.3 Create Reusable Components
Extract common UI elements:
- **Buttons**: `PrimaryButton`, `SecondaryButton`, `IconButton`
- **Cards**: `UserCard`, `TaskCard`, `AttendanceCard`, `FileCard`
- **Inputs**: `FfTextField`, `FfOutlinedTextField`, `FfDropdown`
- **Dialogs**: `FfAlertDialog`, `FfBottomSheet`, `FfDatePickerDialog`
- **Avatars**: `UserAvatar`, `RoleAvatar`
- **Loading**: `LoadingIndicator`, `PullToRefresh`
- **Empty States**: `EmptyState`, `ErrorState`
- **Status Indicators**: `StatusBadge`, `PriorityBadge`

---

## Phase 4: Navigation Refactoring

### 4.1 Type-Safe Navigation
```kotlin
sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Dashboard : Screen("dashboard/{roleId}") {
        fun createRoute(roleId: String) = "dashboard/$roleId"
    }
    object Tasks : Screen("tasks")
    object Attendance : Screen("attendance")
    object Files : Screen("files")
    object Map : Screen("map")
}
```

### 4.2 Navigation Manager
Create a central navigation manager to handle all navigation logic

---

## Phase 5: Resource Centralization

### 5.1 strings.xml
Move all hardcoded strings to resources:
- Screen titles
- Button labels
- Error messages
- Validation messages
- Content descriptions
- Hint text

### 5.2 dimens.xml
Standardize all dimensions:
- Spacing values (small, medium, large, xlarge)
- Text sizes
- Icon sizes
- Border widths
- Corner radius values

### 5.3 colors.xml
Define semantic colors:
- Primary, secondary, tertiary
- Success, warning, error, info
- Surface variants
- Text colors

---

## Phase 6: Dependency Injection

### 6.1 Manual DI Module
Since we're not using Hilt/Dagger:
- Create `AppContainer` class
- Provide all dependencies
- Handle ViewModel creation

---

## Phase 7: Testing Infrastructure

### 7.1 Test Structure
```
test/
├── domain/
│   └── usecase/
│       └── LoginUseCaseTest.kt
├── presentation/
│   └── viewmodel/
│       └── LoginViewModelTest.kt
└── util/
    └── DateFormatterTest.kt
```

### 7.2 Test Utilities
- `MainDispatcherRule`
- `TestDatabaseFactory`
- `FakeApiService`
- `TestObservers`

---

## Phase 8: Build & Validation

### 8.1 Lint Fixes
- Address all warnings
- Suppress with justification where needed

### 8.2 Compilation
- Ensure zero compilation errors
- Verify all modules link correctly

### 8.3 Manual Testing
- Verify all user flows
- Test offline sync
- Test role-based access

---

## Execution Order

1. **Phase 1** - Restructure packages (foundation)
2. **Phase 2** - Create domain layer
3. **Phase 3** - Refactor UI layer
4. **Phase 4** - Fix navigation
5. **Phase 5** - Centralize resources
6. **Phase 6** - Add DI
7. **Phase 7** - Add tests
8. **Phase 8** - Final validation

---

## Critical Success Factors

✅ Maintain functionality throughout refactoring
✅ Build after each phase
✅ Run tests after each phase
✅ Document breaking changes
✅ Keep commits atomic and reversible

---

## Files to Create (New): ~80
## Files to Modify: ~20
## Files to Delete: ~5

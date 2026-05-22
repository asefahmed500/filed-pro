# FieldForce Pro - Testing Guide

## Overview

This guide provides comprehensive testing instructions for the FieldForce Pro Android application to ensure all features work correctly before deployment.

## Prerequisites

### Environment Setup
1. **Android Studio**: Latest stable version
2. **Java Development Kit (JDK)**: Version 11 or higher
3. **PostgreSQL Database**: Running on localhost:5432
4. **Android Device/Emulator**: API 24 (Android 7.0) or higher

### Server Setup
```bash
# Start the PostgreSQL database
# Ensure PostgreSQL is running on localhost:5432

# Start the Ktor server
./gradlew :server:run

# Verify server is running
curl http://localhost:8080/api/health
```

### Database Setup
```sql
-- Verify database connection
-- Default credentials: postgres/postgres @ localhost:5432/fieldforce

-- Check if tables are created
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public';
```

## Unit Testing

### Run All Unit Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests "FieldForceViewModelTest"
./gradlew test --tests "RepositoryTest"
./gradlew test --tests "FieldForceDatabaseTest"
```

### Run Tests for Specific Module
```bash
./gradlew :app:test
./gradlew :server:test
```

### Test Coverage
```bash
./gradlew testDebugUnitTest jacocoTestReport
# Report generated at: app/build/reports/jacoco/testDebugUnitTest/html/index.html
```

## Integration Testing

### Run Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Run Specific Instrumented Test
```bash
./gradlew connectedAndroidTest --tests "FieldForceInstrumentedTest"
```

### Run UI Tests
```bash
./gradlew connectedAndroidTest --tests "FieldForceUITest"
```

## Manual Testing Checklist

### 1. Authentication & User Management

#### Login Screen
- [ ] Welcome screen displays correctly
- [ ] Quick login cards work for Admin, Manager, Executive roles
- [ ] Email login works with valid email
- [ ] Email login shows error for invalid email
- [ ] Sign-up form validation works
- [ ] Sign-up creates new user successfully
- [ ] Role-based navigation works correctly

#### User Switcher
- [ ] User switcher dialog opens correctly
- [ ] All user profiles display with correct information
- [ ] Switching between users works smoothly
- [ ] User data updates correctly after switching

### 2. Admin Dashboard

#### KPI Dashboard
- [ ] Dashboard loads without errors
- [ ] All KPI cards display correct data
- [ ] Executive count is accurate
- [ ] Active attendance count is correct
- [ ] Task completion rate is calculated correctly
- [ ] Total expenses display correctly

#### Task Management
- [ ] Task list displays all tasks
- [ ] Task status badges show correct colors
- [ ] Priority badges display correctly
- [ ] Filter tasks by status works
- [ ] Sort tasks by priority works
- [ ] Task details dialog opens correctly

#### User Management
- [ ] User list displays all users
- [ ] Create new user dialog works
- [ ] User role badges display correctly
- [ ] User information is accurate

### 3. Manager Dashboard

#### Team Overview
- [ ] Team member list displays correctly
- [ ] Team member status indicators work
- [ ] Team performance metrics display accurately

#### Task Assignment
- [ ] Assign task dialog opens correctly
- [ ] Executive selection works
- [ ] Priority selection works
- [ ] Task assignment succeeds
- [ ] Notification is sent to assigned executive

#### Document Approval
- [ ] Pending documents list displays correctly
- [ ] Document details show accurately
- [ ] Approve action works correctly
- [ ] Reject action works correctly
- [ ] Rejection reason dialog works
- [ ] Status updates reflect immediately

### 4. Executive Dashboard

#### Check-In/Check-Out
- [ ] Check-in FAB displays when not checked in
- [ ] Check-in dialog opens correctly
- [ ] Selfie selection works
- [ ] Location verification works
- [ ] Geofence status displays correctly
- [ ] Check-in succeeds
- [ ] Active attendance indicator displays
- [ ] Check-out FAB displays when checked in
- [ ] Check-out dialog opens correctly
- [ ] Task completion input works
- [ ] Expenses input works
- [ ] Check-out succeeds

#### Task Management
- [ ] Task list displays assigned tasks
- [ ] Task status displays correctly
- [ ] Start task action works
- [ ] Complete task action works
- [ ] Proof photo upload works
- [ ] Signature capture works
- [ ] Task status updates correctly

#### File Submission
- [ ] File submission FAB displays
- [ ] File submission dialog opens correctly
- [ ] Category selection works
- [ ] Amount input works for expenses
- [ ] Photo selection works
- [ ] File submission succeeds
- [ ] Pending files list displays correctly

### 5. Common Features

#### Offline Mode
- [ ] Offline toggle works correctly
- [ ] Offline banner displays when offline
- [ ] Data queues correctly when offline
- [ ] Sync occurs when coming back online
- [ ] Sync progress displays correctly
- [ ] Offline operations work smoothly

#### Notifications
- [ ] Notification badge displays correct count
- [ ] Notification center opens correctly
- [ ] Notifications display in chronological order
- [ ] Read/unread status displays correctly
- [ ] Mark all as read works
- [ ] Notifications are created for actions

#### Map View
- [ ] Map displays correctly
- [ ] Executive markers display correctly
- [ ] Location indicators work
- [ ] GPS simulation slider works
- [ ] Geofence boundaries display
- [ ] Zoom and pan work correctly

#### Loading States
- [ ] Loading indicators display during async operations
- [ ] Loading states don't block UI unnecessarily
- [ ] Loading completes successfully
- [ ] Loading can be cancelled if needed

#### Error States
- [ ] Error messages display correctly
- [ ] Error messages are user-friendly
- [ ] Error recovery works correctly
- [ ] Network errors are handled gracefully
- [ ] Validation errors display correctly

#### Empty States
- [ ] Empty state displays when no data
- [ ] Empty state messages are helpful
- [ ] Empty state icons display correctly
- [ ] Empty states suggest actions

## Performance Testing

### Load Testing
```bash
# Test with large datasets
# - Create 1000+ tasks
# - Create 100+ users
# - Create 500+ attendance records
# - Verify UI remains responsive
```

### Memory Testing
```bash
# Monitor memory usage
# - Open Android Studio Profiler
# - Monitor memory allocation
# - Check for memory leaks
# - Verify garbage collection works
```

### Battery Testing
```bash
# Test battery impact
# - Use app for 1 hour
# - Monitor battery consumption
# - Check for excessive wake locks
# - Verify location optimization
```

## Network Testing

### Different Network Conditions
- [ ] Works on WiFi
- [ ] Works on 4G/5G
- [ ] Works on 3G
- [ ] Handles poor connectivity
- [ ] Handles network switching
- [ ] Handles airplane mode

### Server Testing
- [ ] Works when server is up
- [ ] Handles server downtime gracefully
- [ ] Handles server errors correctly
- [ ] Retry logic works
- [ ] Timeout handling works

## Compatibility Testing

### Android Versions
- [ ] Android 7.0 (API 24) - Minimum
- [ ] Android 8.0 (API 26)
- [ ] Android 9.0 (API 28)
- [ ] Android 10 (API 29)
- [ ] Android 11 (API 30)
- [ ] Android 12 (API 31)
- [ ] Android 13 (API 33)
- [ ] Android 14 (API 34)

### Screen Sizes
- [ ] Small screens (4.0" - 4.6")
- [ ] Normal screens (4.7" - 5.5")
- [ ] Large screens (5.6" - 6.5")
- [ ] Extra large screens (6.6"+)
- [ ] Tablets (7"+)

### Orientations
- [ ] Portrait mode works correctly
- [ ] Landscape mode works correctly
- [ ] Orientation changes don't cause crashes
- [ ] State is preserved during rotation

## Accessibility Testing

### Screen Reader
- [ ] TalkBack announces all important elements
- [ ] Content descriptions are meaningful
- [ ] Focus order is logical
- [ ] Actions are announced clearly

### Touch Targets
- [ ] All interactive elements are 48dp x 48dp minimum
- [ ] Buttons are easily tappable
- [ ] Touch targets don't overlap

### Color Contrast
- [ ] Text contrast ratio meets WCAG AA (4.5:1)
- [ ] Important elements have sufficient contrast
- [ ] Color is not the only indicator

## Security Testing

### Data Protection
- [ ] Sensitive data is not logged
- [ ] Database is properly secured
- [ ] Network traffic uses HTTPS
- [ ] API keys are protected

### Authentication
- [ ] Login works correctly
- [ ] Logout works completely
- [ ] Sessions expire properly
- [ ] Invalid credentials are rejected

## Regression Testing

### Core Functionality
- [ ] All previously working features still work
- [ ] No new bugs introduced
- [ ] Performance hasn't degraded
- [ ] UI/UX hasn't regressed

## Test Reports

### Bug Reporting Format
```
Bug Title: [Short description]
Severity: [Critical/High/Medium/Low]
Priority: [P1/P2/P3/P4]
Environment: [Device, OS version, App version]
Steps to Reproduce:
1. [Step 1]
2. [Step 2]
3. [Step 3]

Expected Result: [What should happen]
Actual Result: [What actually happens]
Attachments: [Screenshots, logs, etc.]
```

### Test Summary Template
```
Test Date: [Date]
Tester: [Name]
Build Version: [Version]
Tests Executed: [Number]
Tests Passed: [Number]
Tests Failed: [Number]
Pass Rate: [Percentage]

Critical Issues: [Number]
High Issues: [Number]
Medium Issues: [Number]
Low Issues: [Number]

Overall Status: [Pass/Fail]
Notes: [Additional information]
```

## Continuous Testing

### Automated Testing Pipeline
```bash
# Run on every commit
./gradlew test

# Run before merging
./gradlew test connectedAndroidTest

# Run before release
./gradlew test connectedAndroidTest lint
```

## Conclusion

Follow this comprehensive testing guide to ensure the FieldForce Pro application is production-ready. Regular testing and quality assurance will help deliver a reliable and user-friendly experience.

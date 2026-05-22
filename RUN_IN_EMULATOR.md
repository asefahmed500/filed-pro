# Running FieldForce Pro in Android Emulator

## Quick Start (Android Studio Method)

Since Android Studio is running, use this method:

### Step 1: Open Project in Android Studio
1. File → Open → Select `G:\filed-pro`
2. Wait for Gradle sync to complete

### Step 2: Start Emulator
1. Click **Device Manager** icon in toolbar
2. Select your emulator
3. Click **Play** button to start

### Step 3: Run App
1. Select your app module (should be `app`)
2. Click green **Run** button (▶)
3. Select your emulator
4. App will install and launch automatically

## Alternative: Terminal Method

Find your Android Studio installation and use its embedded JDK:

```bash
# Find Android Studio path
# Usually: C:\Users\YourName\AppData\Local\Android\Sdk

# Set JAVA_HOME to Android Studio's JDK
set JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
set PATH=%JAVA_HOME%\bin;%PATH%

# Then run Gradle
cd G:\filed-pro
.\gradlew :app:assembleDebug

# Install via ADB (if emulator is running)
.\gradlew :app:installDebug
```

## Troubleshooting

### Issue: "JAVA_HOME is not set"
**Solution**: Run from Android Studio instead - it handles this automatically

### Issue: Emulator not found
**Solution**:
1. Open Device Manager in Android Studio
2. Create a new virtual device
3. Start it before running the app

### Issue: Gradle sync fails
**Solution**:
1. File → Invalidate Caches → Invalidate and Restart
2. Reopen the project

## Recommended Emulator Settings

- **Device**: Pixel 6 or Pixel 7
- **System Image**: Android 13 (API 33) or higher
- **RAM**: At least 2048 MB
- **VM Heap**: 512 MB
- **Internal Storage**: 2048 MB

## Quick Verification

Once app is running, verify:
- [ ] Welcome screen displays
- [ ] Quick login cards appear
- [ ] Can login as Admin/Manager/Executive
- [ ] Dashboard loads correctly
- [ ] No crashes or errors

## Tips

1. **First launch** may take longer (Gradle builds dependencies)
2. **Subsequent launches** will be much faster
3. **Hot reload** works for Compose changes (Ctrl+F10)
4. **Logcat** shows app logs for debugging

Enjoy testing FieldForce Pro! 🚀

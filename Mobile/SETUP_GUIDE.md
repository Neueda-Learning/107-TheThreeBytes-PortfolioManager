# HSBC Portfolio Manager - Android App Setup Guide

## 🚀 Quick Start Guide

### Prerequisites

Before you begin, ensure you have the following installed:

1. **Android Studio** (Hedgehog 2023.1.1 or newer)
   - Download from: https://developer.android.com/studio
   
2. **Java Development Kit (JDK) 17**
   - Bundled with Android Studio or download separately
   
3. **Android SDK**
   - Android SDK 34 (API Level 34)
   - Minimum SDK 26 (API Level 26)

### Backend Setup

1. **Start the Backend Server**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
   
   The backend should be running on `http://localhost:8080`

2. **Verify Backend is Running**
   ```bash
   curl http://localhost:8080/api/portfolio-items
   ```

### Mobile App Setup

#### Step 1: Open the Project

1. Launch Android Studio
2. Click "Open" and navigate to the `mobile` folder
3. Wait for Gradle sync to complete (this may take a few minutes)

#### Step 2: Configure Build

The app is pre-configured to connect to the backend. The API URL is set in `app/build.gradle`:
```
buildConfigField "String", "API_BASE_URL", "\"http://10.0.2.2:8080/api/\""
```

- `10.0.2.2` is the Android emulator's alias for `localhost`
- If using a physical device, change this to your computer's IP address (e.g., "http://192.168.1.100:8080/api/")

#### Step 3: Sync Gradle

If Gradle hasn't synced automatically:
1. Click "File" → "Sync Project with Gradle Files"
2. Wait for sync to complete

#### Step 4: Run the App

**Option A: Using Android Emulator**
1. Click "Device Manager" in Android Studio
2. Create a new virtual device (recommended: Pixel 5, API 34)
3. Click the green "Run" button (▶) or press Shift+F10
4. Select your emulator device

**Option B: Using Physical Device**
1. Enable Developer Options on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
2. Enable USB Debugging in Developer Options
3. Connect device via USB
4. Click "Run" and select your device
5. Update API URL to use your computer's IP address

### Troubleshooting

#### Problem: "Unable to connect to backend"
**Solution:**
- Ensure backend is running on port 8080
- For emulator: Use `10.0.2.2:8080`
- For physical device: 
  1. Find your computer's IP: `ipconfig` (Windows) or `ifconfig` (Mac/Linux)
  2. Update API_BASE_URL in `app/build.gradle`
  3. Ensure device and computer are on the same network
  4. Check firewall settings

#### Problem: "Gradle sync failed"
**Solution:**
- Check internet connection
- Click "File" → "Invalidate Caches / Restart"
- Delete `.gradle` folder and sync again

#### Problem: "SDK not found"
**Solution:**
- Open SDK Manager (Tools → SDK Manager)
- Install Android SDK 34
- Install Android SDK Build-Tools 34

#### Problem: Build errors about missing dependencies
**Solution:**
- Ensure `settings.gradle` has correct repositories
- Click "Build" → "Clean Project"
- Click "Build" → "Rebuild Project"

### Testing the App

1. **Dashboard Screen**
   - Should display portfolio summary
   - Try filtering by asset type
   - Refresh button should reload data

2. **Holdings Screen**
   - Tap "View Holdings" or FAB (+)
   - Add a new investment
   - Edit and delete existing holdings

### Features Overview

#### 🎨 UI Components
- **Material 3 Design**: Modern, beautiful interface
- **HSBC Brand Colors**: Professional red theme
- **Smooth Animations**: Polished user experience
- **Responsive Layout**: Works on various screen sizes

#### 💼 Functionality
- View portfolio dashboard with analytics
- Add new investment holdings
- Edit existing holdings
- Delete holdings with confirmation
- Filter by asset type (Stock, Bond, Crypto)
- Real-time data synchronization

### API Configuration for Different Environments

#### Development (Emulator)
```kotlin
buildConfigField "String", "API_BASE_URL", "\"http://10.0.2.2:8080/api/\""
```

#### Development (Physical Device - Same Network)
```kotlin
buildConfigField "String", "API_BASE_URL", "\"http://192.168.1.100:8080/api/\""
```

#### Production
```kotlin
buildConfigField "String", "API_BASE_URL", "\"https://api.yourproduction.com/api/\""
```

### Building Release APK

1. Click "Build" → "Generate Signed Bundle / APK"
2. Select "APK"
3. Create or use existing keystore
4. Choose "release" build variant
5. Build completes in `app/release/app-release.apk`

### Project Architecture

```
Mobile App Architecture (MVVM)
├── Data Layer
│   ├── API Service (Retrofit)
│   ├── Models (Data classes)
│   └── Repository (Business logic)
├── UI Layer
│   ├── ViewModels (State management)
│   ├── Screens (Compose UI)
│   └── Theme (Material 3)
└── Navigation
    └── Compose Navigation
```

### Tech Stack Details

- **Jetpack Compose**: Modern declarative UI
- **Material 3**: Latest Material Design
- **Kotlin Coroutines**: Asynchronous programming
- **StateFlow**: Reactive state management
- **Retrofit**: Type-safe HTTP client
- **Gson**: JSON serialization
- **OkHttp**: HTTP client with interceptors

### Next Steps

1. **Customize Theme**: Edit colors in `ui/theme/Color.kt`
2. **Add Features**: Extend functionality in respective layers
3. **Add Charts**: Integrate MPAndroidChart for visualizations
4. **Add Offline Support**: Implement Room database
5. **Add Authentication**: Integrate user login

### Support

For issues or questions:
1. Check logs in Logcat (Android Studio)
2. Review API responses in Network Inspector
3. Verify backend endpoints are accessible
4. Check device/emulator connectivity

### Performance Tips

- Use release builds for performance testing
- Enable R8 minification for smaller APK
- Use ProGuard rules for optimization
- Test on various devices and API levels

---

## 📱 Screenshot References

The app includes:
- ✅ Beautiful gradient dashboard cards
- ✅ Color-coded asset type indicators
- ✅ Smooth screen transitions
- ✅ Material 3 components throughout
- ✅ Intuitive add/edit/delete dialogs
- ✅ Professional data visualization

Enjoy building with the Portfolio Manager! 🚀


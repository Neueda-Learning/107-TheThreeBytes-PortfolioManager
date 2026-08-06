# Portfolio Manager Mobile App - Quick Reference

## 🎯 What You Get

A fully functional Android app with:

### ✨ Features
- **Beautiful Dashboard**: Portfolio overview with real-time analytics
- **Holdings Management**: Add, edit, delete investments
- **Asset Filtering**: Filter by Stocks, Bonds, or Crypto
- **Material 3 Design**: Modern, professional UI
- **HSBC Branding**: Corporate red theme
- **Smooth Animations**: Polished user experience

### 🏗️ Architecture
- **MVVM Pattern**: Clean separation of concerns
- **Jetpack Compose**: Modern declarative UI
- **Kotlin Coroutines**: Async operations
- **Retrofit**: API communication
- **StateFlow**: Reactive state management

### 📱 Screens

#### 1. Dashboard Screen
- Total portfolio value with gradient card
- Gain/Loss indicators with colors
- Asset allocation breakdown
- Holdings list preview
- Filter chips for asset types
- Pull to refresh

#### 2. Holdings Screen
- Complete list of investments
- Add new holdings with FAB
- Edit existing items
- Delete with confirmation
- Form validation
- Success/Error messages

### 🎨 UI Highlights

**Colors:**
- Primary: HSBC Red (#DB0011)
- Secondary: Blue (#1E88E5)
- Accents: Green, Orange, Purple, Teal
- Beautiful gradients and shadows

**Components:**
- Gradient summary cards
- Color-coded asset type icons
- Smooth dialogs and alerts
- Responsive layouts
- Material 3 cards and buttons

### 🔌 API Integration

Connects to backend endpoints:
- GET `/api/dashboard` - Dashboard data
- GET `/api/portfolio-items` - List holdings
- POST `/api/portfolio-items` - Create holding
- PUT `/api/portfolio-items/{id}` - Update holding
- DELETE `/api/portfolio-items/{id}` - Delete holding

### 🚀 Getting Started

1. **Open in Android Studio**
   ```
   File → Open → Select 'mobile' folder
   ```

2. **Wait for Gradle Sync**
   (First time may take 5-10 minutes)

3. **Configure Backend URL**
   - Emulator: Uses `10.0.2.2:8080` (pre-configured)
   - Physical device: Update IP in `app/build.gradle`

4. **Run the App**
   - Click Run button (▶)
   - Select device/emulator
   - Wait for build and install

### 📋 Requirements

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34
- Minimum device: Android 8.0 (API 26)
- Backend running on port 8080

### 🛠️ Customization

**Change Colors:**
Edit `ui/theme/Color.kt`

**Change API URL:**
Edit `app/build.gradle` → `API_BASE_URL`

**Add Features:**
Follow MVVM pattern in respective layers

### 💡 Tips

- Use emulator for quick testing
- Check Logcat for debugging
- Test on multiple screen sizes
- Use release builds for performance testing

### 🐛 Common Issues

**Can't connect to backend?**
→ Check backend is running
→ Verify URL configuration
→ Check firewall settings

**Build fails?**
→ Sync Gradle files
→ Clean and rebuild project
→ Check internet connection

**Emulator slow?**
→ Enable hardware acceleration
→ Use x86 system images
→ Allocate more RAM in AVD settings

### 📚 File Structure

```
mobile/
├── app/
│   ├── src/main/
│   │   ├── java/com/hsbc/portfoliomanager/
│   │   │   ├── data/           # API & Models
│   │   │   ├── ui/             # Screens & Theme
│   │   │   └── MainActivity.kt
│   │   ├── res/                # Resources
│   │   └── AndroidManifest.xml
│   └── build.gradle            # App config
├── build.gradle                # Project config
├── settings.gradle             # Project settings
└── README.md                   # Documentation
```

### 🎓 Learning Resources

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3](https://m3.material.io/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Android Development](https://developer.android.com/)

---

**Happy Coding! 🚀**


# Portfolio Manager - Android Mobile App

A modern Android application for managing investment portfolios, built with Jetpack Compose and Material 3 design.

## Features

- 📊 **Dashboard**: View comprehensive portfolio overview with real-time analytics
- 💼 **Holdings Management**: Add, edit, and delete investment holdings
- 📈 **Asset Allocation**: Visual breakdown of portfolio by asset type
- 🎨 **Material 3 Design**: Beautiful, modern UI with smooth animations
- 🔄 **Real-time Sync**: Connects to backend API for data persistence

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit + OkHttp
- **Async**: Kotlin Coroutines + Flow
- **Material Design**: Material 3
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## Project Structure

```
app/
├── data/
│   ├── api/           # API service definitions
│   ├── model/         # Data models
│   └── repository/    # Repository layer
├── ui/
│   ├── navigation/    # Navigation setup
│   ├── screens/       # UI screens
│   ├── theme/         # Material theme
│   └── viewmodel/     # ViewModels
└── MainActivity.kt
```

## Setup

1. **Prerequisites**
   - Android Studio Hedgehog (2023.1.1) or newer
   - JDK 17
   - Android SDK 34

2. **Backend Configuration**
   - Ensure the backend server is running on `http://localhost:8080`
   - For Android emulator, the app uses `10.0.2.2` to access localhost
   - Update `API_BASE_URL` in `app/build.gradle` if needed

3. **Build and Run**
   ```bash
   # Open project in Android Studio
   # Sync Gradle
   # Run on emulator or physical device
   ```

## Key Features Details

### Dashboard Screen
- Portfolio summary with total value and gain/loss
- Asset type filtering (Stocks, Bonds, Crypto)
- Asset allocation breakdown
- Holdings list with detailed information

### Holdings Management Screen
- Add new investments with validation
- Edit existing holdings
- Delete with confirmation
- Real-time updates

### Design Highlights
- HSBC brand colors (Red primary theme)
- Smooth animations and transitions
- Responsive layouts for different screen sizes
- Material 3 components
- Dark/Light theme support

## API Endpoints Used

- `GET /api/dashboard` - Fetch dashboard data
- `GET /api/portfolio-items` - List all holdings
- `POST /api/portfolio-items` - Create new holding
- `PUT /api/portfolio-items/{id}` - Update holding
- `DELETE /api/portfolio-items/{id}` - Delete holding

## Screenshots

The app features:
- Beautiful gradient cards
- Color-coded asset types
- Intuitive navigation
- Smooth animations
- Professional data visualization

## Future Enhancements

- Charts and graphs integration
- Offline mode with caching
- Push notifications for price alerts
- Biometric authentication
- Export portfolio reports
- Multi-currency support

## License

This project is part of the HSBC Portfolio Management System.


# London Live Bus Journey Tracker

A real-time London bus tracking Android app built with Jetpack Compose, Clean Architecture, and the TfL Unified API.

## Features

- 🔍 Search for bus stops and stations
- 🚌 View live bus arrival times
- 📍 Track bus position in real-time using "Virtual GPS"
- 🗺️ Google Maps integration
- 🎨 Modern Material 3 design

## Architecture

The app follows **Clean Architecture** with three layers:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  (Screens, ViewModels, UI State, Compose Components)        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│  (Use Cases, Repository Interfaces, Domain Models)          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                             │
│  (Repository Implementations, API Service, DTOs, Mappers)   │
└─────────────────────────────────────────────────────────────┘
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp + Kotlinx Serialization
- **Navigation**: Navigation Compose
- **Maps**: Google Maps Compose
- **Async**: Kotlin Coroutines + Flow

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/london-live-bus-journey-tracker.git
cd london-live-bus-journey-tracker
```

### 2. Configure API Keys

Copy the template and add your API keys:

```bash
cp local.properties.template local.properties
```

Edit `local.properties`:

```properties
# TfL API Key - https://api-portal.tfl.gov.uk/
TFL_API_KEY=your_tfl_api_key_here

# Google Maps API Key - https://console.cloud.google.com/
MAPS_API_KEY=your_google_maps_api_key_here
```

### 3. Get TfL API Key

1. Go to [TfL API Portal](https://api-portal.tfl.gov.uk/)
2. Create an account
3. Register a new application
4. Copy the **Primary Key**

### 4. Get Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or select existing)
3. Enable **Maps SDK for Android**
4. Go to Credentials → Create Credentials → API Key
5. Restrict the key:
    - Application restrictions: Android apps
    - Add your app's package name and SHA-1 certificate

### 5. Build and Run

```bash
./gradlew assembleDebug
```

Or open in Android Studio and run.

## Project Structure

```
app/src/main/java/com/example/london_live_bus_journey_tracker/
├── data/
│   ├── remote/
│   │   ├── api/           # Retrofit API service
│   │   └── dto/           # Data Transfer Objects
│   ├── repository/        # Repository implementations
│   └── mapper/            # DTO to Domain mappers
├── domain/
│   ├── model/             # Domain models
│   ├── repository/        # Repository interfaces
│   ├── usecase/           # Business logic use cases
│   └── common/            # Common utilities (Result)
├── presentation/
│   ├── screens/           # Compose screens + ViewModels
│   ├── components/        # Reusable UI components
│   └── navigation/        # Navigation graph
├── di/                    # Hilt dependency injection modules
└── ui/theme/              # Material theme definitions
```

## Virtual GPS Feature

Since the TfL API doesn't provide real-time bus GPS coordinates, we implement "Virtual GPS":

1. Get live arrival predictions for a bus line
2. Find the prediction for the specific vehicle
3. Match the approaching stop to the route sequence
4. Use the stop's coordinates as the bus position
5. Display the next stop information

## API Endpoints Used

| Endpoint | Description |
|----------|-------------|
| `GET /StopPoint/Search/{query}` | Search for stops |
| `GET /Journey/JourneyResults/{from}/to/{to}` | Plan a journey |
| `GET /Line/{lineId}/Arrivals` | Get live arrivals |
| `GET /Line/{lineId}/Route/Sequence/{direction}` | Get route stops |

## License

MIT License - see [LICENSE](LICENSE) for details.

## Acknowledgments

- [Transport for London](https://tfl.gov.uk/) for the Unified API
- [Google Maps Platform](https://developers.google.com/maps) for mapping services
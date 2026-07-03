# CORSYNC-Movil / Sakura — Refactoring Summary

## How to Change the API URL

Edit `API_BASE_URL` and `SIGNALR_HUB_URL` in the `.env` file (project root), then rebuild:

```bash
# .env
API_BASE_URL=http://nueva-url/api/
SIGNALR_HUB_URL=http://nueva-url/hub/aura
```

The values are read at Gradle sync time by `app/build.gradle.kts` via `java.util.Properties` and injected as `BuildConfig` fields.

---

## Architecture Overview

### Layer structure

```
app/src/main/java/com/sakura/aura/
├── data/
│   ├── mapper/         ← DTO ↔ Domain mapping (AuthMapper, UserMapper, ReadingMapper, ChallengeMapper)
│   ├── model/
│   │   ├── request/    ← Retrofit request DTOs (LoginRequest, RegisterRequest, …)
│   │   └── response/   ← Retrofit response DTOs (AuthResponse, UserResponse, …)
│   ├── remote/         ← Network: ApiService, RetrofitClient, SignalRService, AppConfig, ApiEndpoints, …
│   └── repository/     ← Repository impls (call API + map to domain)
├── domain/
│   ├── model/          ← 12 domain models (UserProfile, AuthToken, Reading, Challenge, …)
│   ├── repository/     ← Repository interfaces (use domain models only)
│   ├── usecase/        ← 9 use cases (thin wrappers with business logic)
│   └── util/           ← AuraMapper (color mapping, stress labels)
├── ui/
│   ├── auth/           ← AuthScreen + AuthViewModel
│   ├── home/           ← HomeScreen + HomeViewModel
│   ├── history/        ← HistoryScreen + AuraDetailSheet + HistoryViewModel
│   ├── challenges/     ← ChallengesScreen + ChallengesViewModel
│   └── profile/        ← ProfileScreen + ProfileViewModel
├── di/                 ← Hilt modules (AppModule, NetworkModule)
└── navigation/         ← BottomNavBar + NavGraph
```

### DDD decoupling

- **Domain layer** depends on **nothing** from `data/`. It defines its own models (e.g. `UserProfile`, `AuthToken`) and repository interfaces.
- **Data layer** depends on domain (repository impls implement domain interfaces, mappers convert DTOs → domain models).
- **UI layer** depends on domain only (uses domain models in `State` and in Compose). No DTO leaks.

### Key decisions

| Decision | Rationale |
|----------|-----------|
| `.env` parsed at build time, not runtime | Simple, no extra library, values available as `BuildConfig` fields |
| `AuthRepository.logout()` accepts raw strings | Avoids forcing ViewModels to construct a fake `AuthToken` just for logout |
| `LogoutUseCase` reads tokens from `TokenManager` | ViewModels don't need to inject `TokenManager` |
| `ScanAuraUseCase` wraps `SignalRService` | UI gets clean domain `Telemetry` flow; `TelemetryResponse` stays internal to `SignalRService` |
| `AuraMapper` in `domain/util/` | Single source of truth for aura color / stress color / stress label; removed 3 duplicated copies from UI |

### How to add a new feature

1. Add endpoint constant in `data/remote/ApiEndpoints.kt`
2. Add Retrofit method in `data/remote/ApiService.kt`
3. Add request/response DTO in `data/model/request/` or `data/model/response/`
4. Add domain model in `domain/model/`
5. Add mapper in `data/mapper/`
6. Add/update repository interface in `domain/repository/`
7. Implement repository in `data/repository/`
8. Add use case in `domain/usecase/` (if needed)
9. Use in ViewModel / UI

---

## Files changed

### New files (created)
- `domain/model/AuthToken.kt`, `Challenge.kt`, `LoginCredentials.kt`, `Medal.kt`, `NewReadingData.kt`, `ProfileUpdateData.kt`, `Reading.kt`, `ReadingSummary.kt`, `RegistrationData.kt`, `Telemetry.kt`, `UserProfile.kt`, `UserStats.kt`
- `data/mapper/AuthMapper.kt`, `ChallengeMapper.kt`, `ReadingMapper.kt`, `UserMapper.kt`
- `domain/util/AuraMapper.kt`
- `domain/usecase/GetChallengesUseCase.kt`, `GetReadingsUseCase.kt`, `GetUserProfileUseCase.kt`, `GetUserStatsUseCase.kt`, `LoginUseCase.kt`, `LogoutUseCase.kt`, `RegisterUseCase.kt`, `ScanAuraUseCase.kt`, `UpdateProfileUseCase.kt`
- `.env`

### Modified files
- `app/build.gradle.kts` — reads `.env`, sets `buildConfigField`
- `data/remote/AppConfig.kt` — wraps `BuildConfig` fields
- `data/remote/ApiEndpoints.kt` — centralized endpoint constants
- `data/remote/ApiService.kt` — uses `ApiEndpoints`
- `data/remote/RetrofitClient.kt` — uses `AppConfig`
- `data/remote/SignalRService.kt` — uses `AppConfig`
- `data/remote/TokenManager.kt` — uses `AppConfig`
- `data/remote/AuthRecoveryInterceptor.kt` — uses `AppConfig` + `ApiEndpoints`
- `data/repository/*Impl.kt` — 4 files, rewritten to use mappers
- `domain/repository/*.kt` — 4 interfaces, updated to domain types
- `ui/**/ — 5 screens + 5 ViewModels, updated to domain models
- `navigation/NavGraph.kt` — updated imports
- `di/NetworkModule.kt` — updated imports
- `app/src/test/**/*.kt` — 5 test files
- 18 files with Git merge-conflict markers cleaned

### Deleted files
- `domain/repository/TelemetryRepository.kt` (empty)

---

## Running tests

```bash
./gradlew testDebugUnitTest
```

All 9 unit tests pass (5 ViewModel tests + 4 repository tests).

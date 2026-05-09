# NewsApp

A modern Android news reader: aggregated feed, search, bookmarks, and onboarding. Built with Kotlin and Jetpack Compose following clean architecture.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose 1.4.3 + Material 3 |
| DI | Dagger Hilt 2.45 |
| HTTP | Retrofit 2.9.0 + Gson |
| Database | Room 2.5.2 |
| Pagination | Paging 3.1.1 |
| Image loading | Coil 2.4.0 |
| Preferences | DataStore 1.0.0 |
| Navigation | Navigation Compose 2.6.0 |
| Build | Gradle 8.0.2 KTS, minSdk 24, targetSdk 34 |

## Key Directories

```
app/src/main/java/com/biprangshu/newsapp/
├── data/           # Retrofit API, Room DB, repository implementations
│   ├── local/      # NewsDao, NewsDataBase, NewsTypeConverter
│   ├── remote/     # NewsApi, paging sources, response models
│   └── repository/ # NewsRespositoryImplementation
├── domain/         # Interfaces, models, use cases — no Android deps
│   ├── model/      # Article, Source (domain models)
│   ├── repository/ # NewsRepository interface
│   └── usecases/   # One class per operation; NewsUseCases/AppEntryUseCases facades
├── di/             # AppModule.kt — all Hilt @Provides bindings
├── navigation/     # NavGraph.kt (root graph), Route.kt (sealed routes)
├── news_navigator/ # NewsNavigator (bottom-tab host), NewsBottomNavigation
├── details/        # Details feature: screen, VM, state, event, top bar
├── bookmark/       # Bookmark feature: screen, VM, state
└── ui/theme/       # Color, Type, Theme
```

Root-level screens/components (Home, Search, Onboarding, shared composables) live flat in the package root.

## Build & Test Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build (minification enabled)
./gradlew assembleRelease

# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests
./gradlew connectedAndroidTest

# Clean
./gradlew clean
```

## Entry Points

- `MainActivity.kt` — single activity, `@AndroidEntryPoint`, hosts the root `NavGraph`
- `NewsApplication.kt` — `@HiltAndroidApp`, app-level Hilt setup
- `MainViewModel.kt` — reads `AppEntryUseCases.readAppEntry` to decide start destination (onboarding vs. main feed)
- `di/AppModule.kt` — all singleton bindings; start here to understand the dependency graph

## Known Issues

- API key is hardcoded in `data/remote/NewsApi.kt` — should be moved to `BuildConfig` or secrets
- `NewsRespositoryImplementation.kt` has a typo in the filename ("Resp" instead of "Rep"); don't rename without updating all imports
- No test files currently exist in the project

## Additional Documentation

Check these when working on the relevant area:

| Topic | File |
|---|---|
| Architecture patterns, MVVM conventions, event/state pattern | `.claude/docs/architectural_patterns.md` |

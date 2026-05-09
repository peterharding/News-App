# News App — CLAUDE.md

## Module Structure

Single-module app (`:app`). All source lives under:
`app/src/main/java/com/biprangshu/newsapp/`

Top-level packages:
- `data/` — Room, Retrofit, repository implementations
- `domain/` — models, repository interfaces, use cases
- `di/` — Hilt module
- `navigation/` — routes and nav graph
- `news_navigator/` — bottom-nav container
- `bookmark/`, `details/`, `HomeScreen`, `SearchScreen`, `OnBoardingScreen` — feature packages/files at the root level

## Architecture

Clean Architecture with three layers:

**Domain** (innermost, no Android deps)
- Entities: `domain/model/Article.kt`, `domain/model/Source.kt`
- Repository contracts: `domain/repository/NewsRepository.kt`
- Use cases: one class per operation in `domain/usecases/`, aggregated into `NewsUseCases.kt:1`

**Data** (implements domain interfaces)
- `data/repository/NewsRespositoryImplementation.kt` — implements `NewsRepository`
- `data/local/` — Room DAO (`NewsDao.kt`) and database (`NewsDataBase.kt`)
- `data/remote/` — Retrofit API (`NewsApi.kt`) and paging sources

**Presentation**
- One `*ViewModel` + one `*Screen` composable (+ optional `*State`/`*Event`) per feature
- ViewModels hold a single `mutableStateOf(…State())` and expose it as a val; screens are stateless

## Dependency Injection

Framework: Dagger Hilt.

- `NewsApplication.kt:1` — `@HiltAndroidApp`, initialises the DI graph
- `MainActivity.kt:1` — `@AndroidEntryPoint`, entry point for activity-scoped injection
- `di/AppModule.kt:35` — `@Module @InstallIn(SingletonComponent::class)`, all bindings are `@Singleton`

What `AppModule` provides (all singletons): `LocalUserManager`, `AppEntryUseCases`, `NewsApi` (Retrofit), `NewsRepository`, `NewsUseCases`, `NewsDataBase`, `NewsDao`.

ViewModels use `@HiltViewModel` + `@Inject constructor`.

## Navigation

- `navigation/Route.kt:1` — sealed class defining all route objects: `OnBoardingScreen`, `HomeScreen`, `SearchScreen`, `BookMarkScreen`, `DetailsScreen`, plus nested-graph wrappers
- `navigation/NavGraph.kt:1` — root `NavGraph` composable; branches on `startDestination` into `AppStartNavigation` (onboarding) or `NewsNavigation` (main)
- `news_navigator/NewsNavigator.kt:1` — bottom-nav shell; hosts Home / Search / Bookmark tabs; article details open inside this graph
- Article passed to details via `savedStateHandle` (`NewsNavigator.kt:166`)
- `MainViewModel.kt:1` reads `AppEntryUseCases.readAppEntry` to determine start destination; called from `MainActivity.kt:50`

## Bookmarks Feature — Layer-by-Layer Entry Points

| Layer | File | Key symbol |
|---|---|---|
| Screen | `bookmark/BookMarkScreen.kt:25` | `BookMarkScreen(state, navigateToDetails)` |
| ViewModel | `bookmark/BookMarkViewModel.kt:1` | `BookMarkViewModel` — collects `selectArticles` flow into state |
| State | `bookmark/BookmarkState.kt:1` | `BookmarkState(articles)` |
| Use case | `domain/usecases/SelectArticles.kt:1` | `SelectArticles.invoke(): Flow<List<Article>>` |
| Repo interface | `domain/repository/NewsRepository.kt:18` | `selectArticles()` |
| Repo impl | `data/repository/NewsRespositoryImplementation.kt:52` | delegates to `newsDao.getArticles()` |
| DAO | `data/local/NewsDao.kt:21` | `getArticles(): Flow<List<Article>>` |

Other bookmark-relevant use cases (upsert/delete/select-one) live alongside `SelectArticles` in `domain/usecases/` and are wired in `DetailsViewModel.kt`.

## Naming Conventions

| Artefact | Convention | Example |
|---|---|---|
| Screen composable | `<Name>Screen.kt`, function `<Name>Screen` | `BookMarkScreen.kt` |
| ViewModel | `<Name>ViewModel.kt` | `BookMarkViewModel.kt` |
| State | `<Name>State.kt`, data class | `BookmarkState.kt` |
| Event (sealed) | `<Name>Event.kt` | `SearchEvent.kt` |
| Use case | verb-noun, one class per file, `operator fun invoke()` | `UpsertArticle.kt` |
| UseCase aggregator | `<Feature>UseCases.kt`, data class | `NewsUseCases.kt` |
| Repo interface | `<Domain>Repository.kt` in `domain/repository/` | `NewsRepository.kt` |
| Repo impl | `<Domain>RepositoryImplementation.kt` in `data/repository/` | `NewsRespositoryImplementation.kt` |
| DAO | `<Entity>Dao.kt` | `NewsDao.kt` |
| DB | `<Entity>DataBase.kt` | `NewsDataBase.kt` |

Note: the existing repository implementation file has a typo (`Respository`) — match that spelling only when referencing that specific file; new files should use the correct spelling.

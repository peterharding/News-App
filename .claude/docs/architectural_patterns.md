# Architectural Patterns

## 1. Clean Architecture (3 layers)

The app enforces strict layer separation:

- **Presentation** — ViewModels, Compose screens, state/event classes
- **Domain** — use cases, repository interfaces, models (`domain/`). Zero Android framework imports.
- **Data** — Retrofit, Room, DataStore implementations (`data/`). Implements domain interfaces.

Dependencies flow inward: data → domain ← presentation. Never import `data/` from a screen or ViewModel directly.

**Reference:** `domain/repository/NewsRepository.kt` (interface) vs `data/repository/NewsRespositoryImplementation.kt` (impl).

---

## 2. MVVM + Compose State

Each screen owns exactly one ViewModel. Two state patterns are used:

**MutableState** (simple, synchronous screens):
```
SearchViewModel: _state = mutableStateOf(SearchState()); val state: State<SearchState>
```
- `SearchViewModel.kt`, `OnBoardingViewModel.kt`

**StateFlow** (async/coroutine-heavy screens):
```
DetailsViewModel: _state = MutableStateFlow(DetailsState()); val state = _state.asStateFlow()
```
- `DetailsViewModel.kt`, `BookMarkViewModel.kt`, `HomeViewModel.kt`

State is always an immutable data class. Update via `_state.value = _state.value.copy(...)` or `_state.update { }`.

---

## 3. Event-Driven UI Communication

Screens never call ViewModel methods directly — they dispatch sealed class events. This makes the ViewModel's public API explicit and testable.

Pattern:
```
Screen -> viewModel.onEvent(SomeEvent.Foo) -> ViewModel.onEvent() dispatches
```

Event classes: `SearchEvent.kt`, `DetailsEvent.kt`, `OnBoardingEvent.kt`.

Each ViewModel has a single `fun onEvent(event: XEvent)` entry point that dispatches to private methods.

---

## 4. Use Case Facade Pattern

Each business operation is one class with a single `operator fun invoke(...)`. Use cases are grouped into facade data classes for clean injection.

```
NewsUseCases(getNews, searchNews, upsertArticle, deleteArticle, selectArticles, selectArticle)
AppEntryUseCases(saveAppEntry, readAppEntry)
```

**Reference:** `domain/usecases/NewsUseCases.kt`, `domain/usecases/AppEntryUseCases.kt`, `di/AppModule.kt`.

ViewModels receive the facade, not individual use cases: `@Inject constructor(private val newsUseCases: NewsUseCases)`.

---

## 5. Repository Pattern

`NewsRepository` (domain interface) is the only seam between domain and data. The implementation in `data/repository/` depends on `NewsApi` and `NewsDao`, both injected via Hilt.

Return types follow this convention:
- Paginated remote data → `Flow<PagingData<T>>` via `Pager { PagingSource }`
- Local reactive queries → `Flow<List<T>>` from Room
- One-shot writes → `suspend fun` returning Unit

**Reference:** `domain/repository/NewsRepository.kt:1`, `data/repository/NewsRespositoryImplementation.kt`.

---

## 6. Paging 3 Integration

Two `PagingSource` subclasses handle pagination:
- `NewsPagingSource` — top-headlines feed by sources
- `SearchNewsPagingSource` — search query pagination

Both call `newsApi.GetNews(page = params.key ?: 1, ...)` and return `LoadResult.Page` / `LoadResult.Error`.

The repository wraps them in `Pager(PagingConfig(pageSize = 10)) { PagingSource(...) }.flow`.

Compose collects paging data with `collectAsLazyPagingItems()`. `ArticlesList` is overloaded for both `List<Article>` (bookmarks) and `LazyPagingItems<Article>` (feed/search).

**Reference:** `data/remote/NewsPagingSource.kt`, `HomeScreen.kt`, `SearchScreen.kt`.

---

## 7. Dagger Hilt Dependency Injection

All bindings live in `di/AppModule.kt` (`@Module @InstallIn(SingletonComponent::class)`).

Annotations used consistently:
- `@HiltAndroidApp` — `NewsApplication`
- `@AndroidEntryPoint` — `MainActivity`
- `@HiltViewModel` + `@Inject constructor` — all ViewModels
- `@Provides @Singleton` — all module bindings

New singletons should be added to `AppModule.kt` as `@Provides @Singleton` functions. New ViewModels need `@HiltViewModel`.

---

## 8. Navigation Structure

Two-level navigation:

**Level 1 — Root NavGraph (`navigation/NavGraph.kt`):**
- `AppStartNavigation` graph → `OnBoardingScreen`
- `NewsNavigation` graph → `NewsNavigatorScreen`
- Start destination decided by `MainViewModel` reading DataStore entry

**Level 2 — NewsNavigator (`news_navigator/NewsNavigator.kt`):**
- Nested `NavHost` with bottom-bar routes: Home, Search, Details, Bookmark
- `Route` is a sealed class of objects/data classes in `navigation/Route.kt`

**Article passing between screens:** via `savedStateHandle`:
```kotlin
navController.currentBackStackEntry?.savedStateHandle?.set("article", article)
// retrieved in DetailsViewModel via savedStateHandle.get<Article>("article")
```

---

## 9. Side-Effect Pattern (Transient UI Messages)

Toasts and one-shot UI events are not stored in state — they use a nullable `var sideEffect` property on the ViewModel (backed by `mutableStateOf`):

```
DetailsViewModel.sideEffect: String?  // set on upsert/delete
```

The screen observes it, shows the Toast, then calls `viewModel.onEvent(DetailsEvent.RemoveSideEffect)` to null it out.

**Reference:** `details/DetailsViewModel.kt`, `news_navigator/NewsNavigator.kt`.

---

## 10. Feature Package Convention

Self-contained features get their own package with four files:
```
feature/
├── FeatureScreen.kt      — @Composable screen
├── FeatureViewModel.kt   — @HiltViewModel
├── FeatureState.kt       — immutable data class
└── FeatureEvent.kt       — sealed class
```

Deviations: `details/` also has `DetailsTopBar.kt` (large isolated composable). The `news_navigator/` package is a structural feature (host + bottom bar), not a screen.

Shared composables (ArticleCard, ArticlesList, SearchBar, ShimmerEffect, EmptyScreen, NewsButton) live in the package root since they're reused across multiple features.

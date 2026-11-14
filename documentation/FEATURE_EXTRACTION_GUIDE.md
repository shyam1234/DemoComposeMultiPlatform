# Feature Extraction Guide

Features are designed to be **enabled, disabled, extracted, or replaced** without rewriting unrelated modules.

## Feature shape

```
features/<name>/
├── api/     # Public contracts only
└── impl/    # UI, ViewModels, Koin module, private details
```

### `api` should contain

- Navigation routes / destination keys
- Public models other features may need
- Ports/interfaces (e.g. `WatchlistPort`)
- Stable documentation of the feature boundary

### `impl` should contain

- Compose screens
- ViewModels
- Feature Koin module
- Private mappers / UI models
- Feature-specific resources

### `impl` must not be depended on by

- Other feature `impl` modules
- Other feature `api` modules
- `core/*` modules

Only **`apps/shared`** (and thin app shells) should depend on `impl`.

## How to disable a feature (in-app)

Example: disable **Search**.

1. Remove `searchModule` from Koin registration in `apps/shared`.
2. Remove Search destinations from the navigation graph contributor.
3. Remove Search entry points (e.g. bottom-bar item / TV menu item).
4. Keep `:features:search:api` only if another feature still references its routes/ports; otherwise remove that dependency too.

Home and Player should continue to compile.

## How to extract a feature into another app

Example: extract **Watchlist** as a library.

1. Publish/consume `:features:watchlist:api` and `:features:watchlist:impl`.
2. Ensure `impl` only depends on allowed cores (`domain`, `ui`, `designsystem`, `navigation`, maybe `player-api` if needed).
3. In the new app, register `watchlistModule` and provide required ports (auth user id, content repository).
4. Bind platform dependencies the same way (DI), do not hardcode Application singletons inside the feature.

## How to replace an implementation

Example: replace mock search with a remote search service.

1. Keep `features/search/api` unchanged if possible.
2. Swap repository binding in DI (`SearchRepository` → new impl in `core/data` or a new data module).
3. Leave Search UI/ViewModel intact if the domain contract is stable.

Example: replace Media3 with another Android engine.

1. Implement `PlayerEngine` in `platform/android`.
2. Change Koin binding of `PlayerEngine`.
3. Do not change `features/player/impl` UI.

## Safe removal checklist

- [ ] No other `features/*/impl` Gradle dependency on this `impl`
- [ ] Navigation graph no longer references the feature’s destinations
- [ ] Koin modules no longer load the feature module
- [ ] Design system has no feature-specific hard dependency (tokens stay generic)
- [ ] Docs updated (`MODULE_STRUCTURE.md`, this guide)

## Anti-patterns that break extraction

| Anti-pattern | Why it hurts |
|--------------|--------------|
| HomeImpl → SearchImpl | Cannot delete Search alone |
| Domain imports Compose | Cannot reuse logic on tvOS SwiftUI |
| Global mutable feature flags in UI | Hidden coupling |
| Player UI imports ExoPlayer | Cannot swap engine |
| “Utils” module dumping all features | Fake modularity |

## Independence validation (architecture acceptance)

| Check | Pass criteria |
|-------|---------------|
| Remove Search | Home + Player still build |
| Replace ExoPlayer binding | Player UI module unchanged |
| Run domain tests on JVM | No Android/iOS device needed |

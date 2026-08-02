# BattleBucks — Real-Time Leaderboard

Android assignment for a live leaderboard, similar to what you’d ship in a mobile gaming product.

Focus is architecture, correct ranking under live updates, and lifecycle/performance — not visual polish.

---

## How to run

1. Open the project in Android Studio (Ladybug / Koala or newer).
2. Let Gradle sync finish.
3. Run on an API 26+ emulator or device.

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
./gradlew :app:testDebugUnitTest
```

Package: `com.battlebucks.app`

---

## What this app does

Two independent pieces:

1. **Game Engine** — generates score events (simulates a match backend)
2. **Leaderboard** — consumes those events, ranks players, pushes UI updates

The UI shows a live list of 25 players. Scores only go up. Ranking uses competition rules (ties share a rank, next rank skips).

---

## Module responsibilities

### Module 1 — Score Generator / Game Engine

`data/engine/GameEngine.kt`

- Owns the player list
- Emits `ScoreUpdate` on a Flow every 500–2000ms
- Picks a random player each tick
- Score only increases
- Pure Kotlin — no Android, no UI

Treat this as “gameplay → score events”.

### Module 2 — Leaderboard (consumer)

`data/repository/LeaderboardRepositoryImpl.kt`  
`utils/RankingCalculator.kt`

- Listens to engine updates
- Keeps latest player state
- Applies ranking rules
- Exposes ranked list as `StateFlow`
- Does **not** generate scores

---

## Architecture overview

```
GameEngine  →  LeaderboardRepository  →  UseCases  →  ViewModel  →  Fragment
                      ↑
               RankingCalculator
```

| Layer | Role |
|-------|------|
| Engine | Produce score events |
| Repository | Own leaderboard state |
| RankingCalculator | Rank math (dense ranking) |
| Use cases | Thin entry points for VM |
| ViewModel | Coordinate flows — no ranking |
| UI | Render + DiffUtil animations |

**MVVM + thin Clean Architecture.** Hilt for DI. Coroutines / Flow / StateFlow throughout.

Ranking does **not** live in the ViewModel or Fragment. That keeps the math unit-testable and reusable if we later swap the engine for a WebSocket feed.

### Ranking rules

| Score | Rank |
|------:|-----:|
| 100 | 1 |
| 90 | 2 |
| 90 | 2 |
| 80 | 4 |

Same score → same rank. Next different score skips. Username is a stable tie-break for list order.

### UI choice: XML

XML + ViewBinding + RecyclerView `ListAdapter`.

Why not Compose for this task:
- DiffUtil payloads give cheap partial row updates under high churn
- Assignment cares about live-update performance more than UI fashion
- ViewBinding + ListAdapter is still the production default on a lot of gaming codebases

Visual effect: score scale pulse (1.0 → 1.08 → 1.0) and a short highlight on the row that scored.

---

## Performance & lifecycle

### Avoiding UI jank

- Engine / ranking run off the main thread (`Dispatchers.Default` app scope)
- UI only collects with `repeatOnLifecycle(STARTED)` + `collectLatest`
- List updates go through DiffUtil — never `notifyDataSetChanged()`
- Payload path animates score without full rebinds when possible

### Rotation

ViewModel survives config change. `stateIn(WhileSubscribed)` keeps the last list so the screen comes back without an empty flash.

### Background

`onStop` stops the engine and cancels collection. No score loop burning CPU while the user is elsewhere. `onStart` resumes it.

### Memory

- Fragment clears the RecyclerView adapter in `onDestroyView`
- App-scoped job is cancelled on stop
- No static Activity / View references

### Scaling

| Size | Approach |
|------|----------|
| ~25–100 | Current design is fine (full re-rank each tick) |
| ~1K | Incremental updates / sorted structure; throttle UI to ~10–15 fps; consider paging visible window |
| ~100K | Don’t keep everyone in memory on device. Server ranks; client gets a window (top N + me). Diff only that window. |

---

## Why the modules are split this way

The engine shouldn’t know about ranks. The leaderboard shouldn’t invent scores. That split mirrors a real game: match service emits events, leaderboard service consumes them.

If tomorrow scores come from a socket instead of `GameEngine`, the repository interface stays the same. UI doesn’t care.

### Where ranking lives

`RankingCalculator` — pure function, no Android.

Why there:
- Easy to unit test
- Not buried in a ViewModel “because it’s convenient”
- One place to change if product tweaks tie rules

### Trade-offs I accepted

1. **Full re-rank every event** — simple and correct for 25 players; not ideal at huge scale.
2. **Highlight last scorer until next event** — readable UX, not a timed fade.
3. **In-memory only** — no Room / network; assignment is about live architecture, not persistence.
4. **XML over Compose** — DiffUtil control under churn mattered more here than declarative UI.
5. **Engine paused off-screen** — saves battery; means you miss updates while backgrounded (fine for a demo; production would buffer or reconnect).

---

## Code review (if a mid-level wrote this)

| # | Comment | Type | Why |
|---|---------|------|-----|
| 1 | Ranking must not sit only in the ViewModel | Must Fix | Business rule belongs in a testable domain/util layer |
| 2 | Don’t drive “live” updates with a UI timer | Must Fix | Fake real-time; couples UI to data generation |
| 3 | Use DiffUtil / ListAdapter, never blanket `notifyDataSetChanged()` | Must Fix | Causes flicker and dropped frames under live churn |
| 4 | Collect with `repeatOnLifecycle`, not bare `lifecycleScope` forever | Must Fix | Avoids leaks and wasted work off-screen |
| 5 | Cancel / pause the engine when the screen stops | Improvement | Battery and background CPU |
| 6 | Add unit tests for tie / skip rank cases | Improvement | Ranking bugs are silent and ugly in prod |
| 7 | Payload-based score animation instead of full rebind | Improvement | Smoother list under frequent updates |
| 8 | Extract player count / tick interval to config | Tech Debt | Hard-coded 25 / 500–2000ms will get copy-pasted |

---

## Shipping in 7 days

### Non-negotiable

- Correct dense ranking
- Engine / leaderboard separation
- Smooth list updates (DiffUtil)
- Lifecycle-safe collection
- Basic ranking unit tests

### Cut / defer

- Fancy empty / shimmer states
- Search / filter
- Persistence
- Anti-cheat
- Compose migration
- CI polish beyond “tests pass locally”

### Work split

| Person | Owns |
|--------|------|
| Junior | XML layouts, adapter binding, basic styling |
| Mid | GameEngine Flow, repository wiring, DiffUtil |
| Lead (me) | Ranking rules, architecture boundaries, review, lifecycle/perf, README / decisions |

---

## Optional extras done

- Unit tests for `RankingCalculator` (ties, skip ranks, highlight)

### Production ideas (not implemented)

- **CI:** ktlint + detekt + unit tests on PR
- **Anti-cheat:** server-authoritative scores, signed events, rate limits, anomaly detection on sudden jumps
- **Prod readiness:** WebSocket source, reconnect/backoff, top-N + “my rank” window, analytics on update lag

---

## What I’d improve with more time

- Incremental ranking structure instead of full sort
- Server-backed feed behind the same repository interface
- Instrumentation test for “score update → row animates”
- detekt / ktlint in CI
- Clear highlight timeout instead of “until next scorer”

---

## Project layout

```
app/src/main/java/com/battlebucks/app/
├── data/engine          → GameEngine
├── data/repository      → LeaderboardRepositoryImpl
├── domain/model
├── domain/repository
├── domain/usecase
├── presentation/leaderboard
│   ├── adapter
│   ├── ui
│   └── viewmodel
├── di
└── utils                → RankingCalculator
```

---

## Stack

Kotlin · XML + ViewBinding · Material 3 · MVVM + Clean · Hilt · Coroutines / Flow / StateFlow · RecyclerView ListAdapter

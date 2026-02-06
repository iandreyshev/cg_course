# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**CGLabs** — Android educational project for a Computer Graphics course. 4 lab modules, Kotlin + Jetpack Compose UI + OpenGL ES 3.0 for 3D.

- **Kotlin:** 2.0.0 | **AGP:** 8.5.0 | **Gradle:** 8.10.2
- **SDK:** min 28, target/compile 35 | **JVM target:** 11
- **Deps:** centralized in `gradle/libs.versions.toml`

## Commands

```bash
./gradlew build                  # Build all
./gradlew assembleDebug          # Debug APK
./gradlew installDebug           # Install on device/emulator
./gradlew :lab3:build            # Build single module (:core, :lab1-4)
./gradlew test                   # All tests
./gradlew :core:testDebugUnitTest --tests "ru.iandreyshev.core.SomeTest"  # Single test
```

No lint/detekt/ktlint configured.

## Module Structure

Modules live in `modules/` but are remapped in `settings.gradle.kts` so Gradle uses `:core`, `:lab1`, etc.

```
app/           → ru.iandreyshev.cglabs    # Navigation, menu, wires labs together
modules/core/  → ru.iandreyshev.core      # BaseViewModel, OpenGL utils, theme, shared code
modules/lab1/  → ru.iandreyshev.cglab1    # 2D Canvas (Initials, Bresenham, House, Hangman)
modules/lab2/  → ru.iandreyshev.cglab2    # 2D graphics (ImageViewer, Alchemy, StoryEditor)
modules/lab3/  → ru.iandreyshev.cglab3    # OpenGL intro (Triangle, Bezier, Asteroids)
modules/lab4/  → ru.iandreyshev.cglab4    # 3D objects (Cube draft, Stellated dodecahedron)
```

`modules/libs/ui/` and `modules/libs/utils/` exist but are currently empty.

The `core` module uses `api()` dependencies — all lab modules get Compose, Material3, Lifecycle, etc. transitively through `:core`.

## Architecture

### MVVM: BaseViewModel<TState, TEvent>

All ViewModels extend `core/BaseViewModel`. Key API:
- `state: State<TState>` — Compose State for automatic recomposition
- `events: Flow<TEvent>` — one-shot events via buffered Channel
- `stateValue: TState` — read current state in ViewModel
- `updateState { copy(...) }` — modify state (receiver is current state)
- `emitEvent(event)` — send one-time event to UI

### Navigation

Type-safe navigation via Kotlinx Serialization. Routes defined as `@Serializable object` in `app/.../navigation/Screens.kt`. Navigation graph in `MainNavHost.kt` is split into builder functions per lab: `buildLab1Navigation()`, `buildLab2Navigation()`, etc.

### Package Organization (per lab feature)

```
{feature}/
├── presentation/    # ViewModel, State data class
├── domain/          # Models, business logic
└── ui/              # Compose screens
    └── openGL/      # GLSurfaceView + Renderer (labs 3-4)
```

### OpenGL Pattern (Labs 3-4)

Shaders in `src/main/res/raw/` with convention `{name}_vert.vert` / `{name}_frag.frag`.

Core utilities:
- `createProgramGLES30(resources, @RawRes vertRes, @RawRes fragRes)` — compile & link shader program
- `Resources.loadShader(type, @RawRes resId)` — load single shader
- `handleErrorsGLES30()` — log OpenGL errors (tag: "OpenGL")
- `Color.floatArray()` — Compose Color → `floatArrayOf(r, g, b, a)` for OpenGL

Each OpenGL screen has a custom `GLSurfaceView` subclass and a `GLSurfaceView.Renderer` implementation.

### Menu System

DSL in `app/.../menu/MenuDSL.kt`:
```kotlin
MenuScreen(navController) {
    lab(1, "Title") {
        task("Name", "Description", Lab1.Route)
    }
}
```

### Adding a New Task

1. Add `@Serializable object` route in `Screens.kt`
2. Create screen composable in the lab module
3. Add `composable<Route> { Screen() }` in the lab's builder function in `MainNavHost.kt`
4. Add `task(...)` entry in `buildMenuNavigation()`

### Adding a New Lab Module

1. Create `modules/lab{N}/` with `build.gradle.kts` (copy from existing lab)
2. Add to `settings.gradle.kts`: `include(":lab{N}")` + `project(":lab{N}").projectDir = file("modules/lab{N}")`
3. Add `implementation(project(":lab{N}"))` in `app/build.gradle.kts`
4. Create `buildLab{N}Navigation()` function in `MainNavHost.kt`

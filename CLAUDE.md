# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**CGLabs** is a multi-module Android educational project demonstrating Computer Graphics concepts through 4 laboratory modules. The project uses Kotlin with Jetpack Compose for UI and OpenGL ES 3.0 for 3D graphics.

- **Language:** Kotlin 2.0.0
- **Min SDK:** 28 (Android 9.0) - Required for OpenGL features
- **Target SDK:** 35
- **Build System:** Gradle 8.5.0 with version catalog (`gradle/libs.versions.toml`)

## Common Commands

### Build and Run
```bash
# Build all modules
./gradlew build

# Clean build
./gradlew clean build

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug
```

### Build Individual Modules
```bash
./gradlew :lab1:build
./gradlew :lab2:build
./gradlew :lab3:build
./gradlew :lab4:build
./gradlew :core:build
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :app:test

# Run a single test class
./gradlew :core:testDebugUnitTest --tests "ru.iandreyshev.core.SomeTest"
```

### Dependency Management
All dependencies are centralized in `gradle/libs.versions.toml`. When adding dependencies:
1. Add version to `[versions]` section
2. Add library to `[libraries]` section
3. Reference in module's `build.gradle.kts` as `libs.libraryName`

## Architecture Overview

### Module Structure

```
CGLabs/
├── app/                  # Application module - navigation and menu
└── modules/
    ├── core/             # Shared utilities and base classes
    ├── lab1/             # 2D Canvas graphics basics
    ├── lab2/             # 2D computer graphics programming
    ├── lab3/             # OpenGL graphics fundamentals
    ├── lab4/             # 3D object visualization
    └── libs/
        ├── ui/           # Reusable Compose components
        └── utils/        # Common utilities
```

Note: Modules are mapped via `settings.gradle.kts` so Gradle references use `:core`, `:lab1`, etc.

**Module Namespaces:**
- `app`: `ru.iandreyshev.cglabs`
- `core`: `ru.iandreyshev.core`
- `lab1-4`: `ru.iandreyshev.cglab{N}` (e.g., `ru.iandreyshev.cglab1`)

### Key Architectural Patterns

#### 1. MVVM with State/Event Pattern
All ViewModels extend `BaseViewModel<State, Event>` from the `core` module:

```kotlin
abstract class BaseViewModel<TState, TEvent>(initialState: TState) : ViewModel() {
    val state: State<TState>              // Compose State
    val events: Flow<TEvent>               // Channel-based event flow

    protected fun updateState(modifier: TState.() -> TState)
    protected fun emitEvent(event: TEvent)
}
```

**Usage:**
- State is exposed as Compose `State<T>` for automatic recomposition
- Events are emitted through `Channel` and exposed as `Flow`
- Always use `updateState {}` to modify state
- Never mutate state directly

#### 2. Navigation Architecture
Uses type-safe navigation with Kotlinx Serialization:

```kotlin
// Define route objects as @Serializable
object Lab1 {
    @Serializable object Initials
    @Serializable object House
}

// Navigation graph in MainNavHost.kt
NavHost(navController, Menu) {
    composable<Lab1.Initials> { InitialsScreen() }
}
```

All lab routes are defined in `app/src/main/java/ru/iandreyshev/cglabs/navigation/Screens.kt`

#### 3. OpenGL Rendering Pattern (Labs 3-4)

**Shader Management:**
- Vertex and fragment shaders are stored in `src/main/res/raw/`
- Use `createProgramGLES30(resources, vertRes, fragRes)` from core module
- Naming convention: `{object}_vert.vert` and `{object}_frag.frag`

**Renderer Structure:**
```kotlin
class MyGLRenderer : GLSurfaceView.Renderer {
    private var program: Int = 0

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        program = createProgramGLES30(resources, R.raw.vert, R.raw.frag)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }
}
```

**Error Handling:**
Use `handleErrorsGLES30()` from core module to check for OpenGL errors during development.

### Package Organization Within Labs

Each lab follows clean architecture with layer separation:

```
cglab{N}/
├── {feature}/
│   ├── presentation/     # ViewModels and State classes
│   ├── domain/           # Business logic and models
│   └── ui/               # Compose screens and renderers
│       ├── {Feature}Screen.kt
│       └── openGL/       # OpenGL renderers (Labs 3-4)
└── res/
    └── raw/              # Shader files (.vert, .frag)
```

## Lab-Specific Information

### Lab 1: 2D Canvas Basics
- Uses Android Canvas API for drawing
- Features: Initials animation, Bresenham's circle, House drawing, Hangman game
- No OpenGL - pure Canvas rendering

### Lab 2: 2D Computer Graphics
- Canvas-based with bitmap operations
- Features: Image viewer, Alchemy craft game, Story editor
- Includes sound effects via `SoundPlayer`
- Uses custom `ElementsStore` for game state management

### Lab 3: OpenGL Fundamentals
- First introduction to OpenGL ES 3.0
- **Asteroids Game:** Complex state machine with game phases (Start/Playing/GameOver)
  - State management in `asteroids/presentation/gameState/` directory
  - Multiple renderers: Ship, Enemy, Bullet, Star, Particle
- **Bezier Curves:** Both Canvas and OpenGL implementations
- All shaders in `res/raw/` directory

### Lab 4: 3D Visualization
- 3D object rendering with transformations
- Currently implements "третья звездчатая форма додекаэдра" (stellated dodecahedron)
- Uses projection and view matrices for 3D transformations

## Important Utilities (Core Module)

### OpenGL Utilities
- `createProgramGLES30(resources, vertRes, fragRes)` - Create shader program
- `handleErrorsGLES30()` - Check for OpenGL errors
- `Resources.loadShader(type, resId)` - Load shader from raw resource

### Color Management
- `Color.kt` provides `Color.toFloatArray()` extension for OpenGL
- Converts Compose Color to RGBA float array `[r, g, b, a]`

### Math Utilities
- `Math.kt` - Common math operations
- `OffsetExt.kt` - Compose Offset extensions

## Menu System

The main menu uses a DSL defined in `MenuDSL.kt`:

```kotlin
MenuScreen(navController) {
    lab(1, "Title") {
        task("Name", "Description", Lab1.SomeScreen)
    }
}
```

When adding new tasks:
1. Define route in `Screens.kt`
2. Create screen composable in lab module
3. Register in `MainNavHost.kt` navigation graph
4. Add to menu in `buildMenuNavigation()` function

## Working with Dependencies

### Adding New Lab Module
1. Create module directory: `mkdir modules/lab{N}`
2. Add to `settings.gradle.kts`:
   - `include(":lab{N}")`
   - `project(":lab{N}").projectDir = file("modules/lab{N}")`
3. Create `build.gradle.kts` following existing lab patterns
4. Add dependency in `app/build.gradle.kts`: `implementation(project(":lab{N}"))`
5. Update `MainNavHost.kt` with navigation routes

### Common Dependencies
- OpenGL: Already included in Android SDK
- Compose: Use BOM version from version catalog
- Navigation: `libs.androidx.navigation.compose`
- Lifecycle: `libs.androidx.lifecycle.runtime.ktx` and `viewmodel-compose`

## Debugging Tips

### OpenGL Issues
- Call `handleErrorsGLES30()` after OpenGL operations during debugging
- Check shader compilation errors in logcat (tag: "OpenGL")
- Ensure shaders are in `res/raw/` and referenced correctly

### Navigation Issues
- Verify route objects are `@Serializable`
- Check if route is registered in `MainNavHost.kt`
- For navigation with results, use `SavedStateHandle` (see Lab2 Alchemy craft example)

### ViewModel State Issues
- Always use `updateState {}` - never mutate state directly
- Access current state via `stateValue` property in ViewModel
- For one-time events, use `emitEvent()` and collect `events` Flow in UI

## Code Style Notes

- Package structure: `presentation/`, `domain/`, `ui/` separation
- ViewModels go in `presentation/` package
- Compose screens go in `ui/` package
- Business logic and models go in `domain/` package
- Use trailing commas in multiline parameter lists
- OpenGL renderers typically in `ui/openGL/` subpackage

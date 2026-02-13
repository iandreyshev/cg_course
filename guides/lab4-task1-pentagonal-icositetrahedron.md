# Пентагональный икоситетраэдр — пошаговый гайд

Реализуем визуализацию **пентагонального икоситетраэдра** — каталановского тела, двойственного к курносому кубу (38 вершин, 24 пятиугольных грани, 60 рёбер). Каждый шаг добавляет одну фичу. После каждого шага можно собрать проект и увидеть результат.

| Шаг | Что добавляем | Результат |
|-----|--------------|-----------|
| 1 | Статическая фигура на экране | Разноцветный многогранник, без взаимодействия |
| 2 | Вращение пальцем (drag) | Палец вращает фигуру |
| 3 | Инерция (fling) | Фигура продолжает вращаться после свайпа |
| 4 | Масштабирование (pinch) | Два пальца увеличивают/уменьшают |
| 5 | Освещение | Грани затеняются в зависимости от угла к свету |
| 6 | Прозрачность | Видно грани за гранями |

Структура файлов, которые мы создадим:

```
modules/lab4/src/main/java/ru/iandreyshev/cglab4/
└── pentagonalicositetrahedron/
    ├── presentation/
    │   ├── PentagonalIcositetrahedronState.kt
    │   └── PentagonalIcositetrahedronViewModel.kt
    └── ui/
        ├── PentagonalIcositetrahedronScreen.kt
        ├── PentagonalIcositetrahedronGLSurfaceView.kt
        ├── PentagonalIcositetrahedronGLRenderer.kt
        └── PentagonalIcositetrahedronRenderer.kt

modules/lab4/src/main/res/raw/
    ├── pent_vert.vert   (добавим на шаге 5)
    └── pent_frag.frag   (добавим на шаге 5)
```

---

# Шаг 1. Рисуем статическую фигуру

**Цель:** на экране появляется разноцветный многогранник. Никакого взаимодействия — просто фигура.

## 1.1. Навигация — подключаем экран к приложению

Прежде чем писать код фигуры, подключим маршрут, чтобы можно было открыть экран из меню.

**Файл: `app/.../navigation/Screens.kt`** — добавь маршрут в `Lab4`:

```kotlin
object Lab4 {
    @Serializable
    object Figure

    @Serializable
    object PentagonalIcositetrahedron
}
```

**Файл: `app/.../navigation/MainNavHost.kt`** — добавь импорт, composable и пункт меню:

```kotlin
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui.PentagonalIcositetrahedronScreen
```

В `buildLab4Navigation`:

```kotlin
private fun NavGraphBuilder.buildLab4Navigation(context: Context) {
    composable<Lab4.Figure> {
        FigureScreen()
    }
    composable<Lab4.PentagonalIcositetrahedron> {
        PentagonalIcositetrahedronScreen()
    }
}
```

В `buildMenuNavigation`, внутри `lab(4, ...)`:

```kotlin
task(
    "Пентагональный икоситетраэдр",
    "24 пятиугольных грани, 38 вершин, 60 рёбер",
    Lab4.PentagonalIcositetrahedron
)
```

## 1.2. State — состояние фигуры (минимальная версия)

Создай файл `presentation/PentagonalIcositetrahedronState.kt`:

```kotlin
package ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation

import android.opengl.Matrix

data class PentagonalIcositetrahedronState(
    val rotationMatrix: FloatArray = FloatArray(16).apply {
        Matrix.setIdentityM(this, 0)
    },
    val scale: Float = 0.5f,
)
```

Пока только два поля:
- `rotationMatrix` — матрица поворота 4×4 (16 float). Единичная матрица = нет поворота. На этом шаге не меняется, но нужна для отрисовки. На шаге 2.5 будем накапливать в ней повороты от drag.
- `scale` — масштаб. Начальное значение 0.5 (фигура в половину размера, чтобы помещалась на экране).

## 1.3. ViewModel (минимальная версия)

Создай файл `presentation/PentagonalIcositetrahedronViewModel.kt`:

```kotlin
package ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation

import ru.iandreyshev.core.BaseViewModel

class PentagonalIcositetrahedronViewModel : BaseViewModel<PentagonalIcositetrahedronState, Any>(
    initialState = PentagonalIcositetrahedronState()
)
```

Пустой ViewModel — просто хранит состояние. Методы добавим позже.

`BaseViewModel` из модуля `:core` даёт:
- `state: State<TState>` — Compose-State для подписки
- `stateValue: TState` — текущее значение
- `updateState { copy(...) }` — обновить состояние

## 1.4. Screen (минимальная версия — без жестов)

Создай файл `ui/PentagonalIcositetrahedronScreen.kt`:

```kotlin
package ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation.PentagonalIcositetrahedronViewModel

@Composable
fun PentagonalIcositetrahedronScreen(
    viewModel: PentagonalIcositetrahedronViewModel = viewModel { PentagonalIcositetrahedronViewModel() }
) {
    val state by viewModel.state

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            PentagonalIcositetrahedronGLSurfaceView(it)
        },
        update = { view ->
            view.updateState(state)
        }
    )
}
```

`AndroidView` — мост между Compose и классическими Android View:
- `factory` — создаёт GLSurfaceView один раз при первой композиции.
- `update` — вызывается при каждой рекомпозиции (когда `state` меняется). Передаёт актуальное состояние в GL-поверхность.

Жестов пока нет — просто показываем фигуру.

## 1.5. GLSurfaceView — обёртка OpenGL-поверхности

Создай файл `ui/PentagonalIcositetrahedronGLSurfaceView.kt`:

```kotlin
package ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui

import android.content.Context
import android.opengl.GLSurfaceView
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation.PentagonalIcositetrahedronState

class PentagonalIcositetrahedronGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val _renderer: PentagonalIcositetrahedronGLRenderer

    init {
        setEGLContextClientVersion(3)
        _renderer = PentagonalIcositetrahedronGLRenderer(resources)
        setRenderer(_renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun updateState(state: PentagonalIcositetrahedronState) {
        _renderer.updateState(state)
        requestRender()
    }
}
```

- `setEGLContextClientVersion(3)` — используем OpenGL ES 3.0.
- `setRenderer(...)` — система будет вызывать его `onSurfaceCreated`, `onSurfaceChanged`, `onDrawFrame`.
- `renderMode = RENDERMODE_WHEN_DIRTY` — кадр рисуется только при вызове `requestRender()`. Анимации пока нет, поэтому непрерывный режим не нужен.
- `updateState(...)` — мост от Compose к OpenGL.

## 1.6. GLRenderer — настройка OpenGL-окружения

Создай файл `ui/PentagonalIcositetrahedronGLRenderer.kt`:

```kotlin
package ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation.PentagonalIcositetrahedronState
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PentagonalIcositetrahedronGLRenderer(
    private val resources: Resources
) : GLSurfaceView.Renderer {

    private val _projectionMatrix = FloatArray(16)
    private val _viewMatrix = FloatArray(16)

    private lateinit var _drawable: PentagonalIcositetrahedronRenderer

    @Volatile
    private var _state = PentagonalIcositetrahedronState()

    init {
        Matrix.setLookAtM(
            _viewMatrix, 0,
            0f, 0f, 5f,   // eye — позиция камеры
            0f, 0f, 0f,   // center — куда смотрим
            0f, 1f, 0f,   // up — вектор «вверх»
        )
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES30.glClearColor(0.02f, 0.02f, 0.05f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        _drawable = PentagonalIcositetrahedronRenderer(resources)
    }

    override fun onDrawFrame(unused: GL10) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        _drawable.draw(_state, _viewMatrix, _projectionMatrix)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(_projectionMatrix, 0, 45f, aspect, 0.1f, 100f)
    }

    fun updateState(state: PentagonalIcositetrahedronState) {
        _state = state
    }
}
```

Здесь три колбэка OpenGL:

**Матрица вида (View Matrix)** — камера на расстоянии 5 по оси Z, смотрит в начало координат. Фигура находится в начале координат — камера видит её «в лоб».

**`onSurfaceCreated`:**
- `glClearColor(0.02, 0.02, 0.05, 1.0)` — тёмно-синий фон.
- `glEnable(GL_DEPTH_TEST)` — тест глубины: ближние грани перекрывают дальние. Без этого фигура будет выглядеть «вывернутой».
- Создаём `PentagonalIcositetrahedronRenderer` — он компилирует шейдеры и генерирует геометрию.

**`onDrawFrame`** — вызывается каждый кадр. Очищаем буферы и рисуем фигуру.

**`onSurfaceChanged`** — вызывается при создании/повороте экрана:
- `glViewport` — область вывода на весь экран.
- `perspectiveM` — перспективная проекция: 45° угол обзора, ближняя плоскость 0.1, дальняя 100.

**`@Volatile`** у `_state` — состояние пишется из UI-потока (через `updateState`), читается из GL-потока (в `onDrawFrame`). Без `@Volatile` GL-поток может видеть устаревшую версию `_state` из-за кэширования в регистрах процессора. `@Volatile` гарантирует, что запись из одного потока сразу видна другому.

> **Замечание:** параметры `unused: GL10` и `config: EGLConfig` — наследие OpenGL ES 1.x API. В ES 3.0 они не используются, поэтому названы `unused`. Android Studio может сгенерировать их с другими именами (`p0`, `p1`) и nullable-типами (`GL10?`, `EGLConfig?`) — это нормально, оба варианта работают.

## 1.7. Renderer — генерация геометрии и отрисовка

Это ключевой файл — самый большой в проекте. Создай `ui/PentagonalIcositetrahedronRenderer.kt`.

> **Важно:** ниже код файла разбит на несколько блоков с пояснениями, но все блоки — части **одного класса** `PentagonalIcositetrahedronRenderer`. Собери их в один файл в порядке: объявление класса → `init` → `generateGeometry()` → `cross()` → поля буферов → `draw()`.

### Теория: как OpenGL рисует 3D-объекты

OpenGL умеет рисовать только **треугольники**. Чтобы нарисовать многогранник:
1. Задаём координаты вершин (38 точек в 3D).
2. Описываем грани (24 пятиугольника — какие вершины соединяются).
3. Разбиваем каждый пятиугольник на треугольники (GPU не знает, что такое пятиугольник).
4. Упаковываем данные в нативные буферы и отправляем в GPU.
5. GPU пропускает данные через **шейдеры** (маленькие программы) и рисует пиксели.

Конвейер рендеринга:

```
Координаты вершин → [Вершинный шейдер: позиция на экране] → [Растеризация] → [Фрагментный шейдер: цвет пикселя] → Экран
```

### Шейдеры

На этом шаге переиспользуем шейдеры куба (`cube_vert.vert` / `cube_frag.frag`). Они уже есть в проекте и подходят для любой цветной геометрии без освещения — принимают позицию и цвет вершины, умножают на MVP-матрицу. Собственные шейдеры с поддержкой нормалей и освещения добавим на шаге 5.

**Вершинный шейдер** — обрабатывает каждую вершину:
```glsl
uniform mat4 uMVPMatrix;      // Матрица Model-View-Projection (одна на все вершины)
attribute vec4 vPosition;      // Позиция вершины (своя для каждой)
attribute vec4 vColor;         // Цвет вершины
varying vec4 fColor;           // Передаём цвет во фрагментный шейдер

void main() {
    gl_Position = uMVPMatrix * vPosition;  // Позиция → экранные координаты
    fColor = vColor;                       // Пробрасываем цвет
}
```

**Фрагментный шейдер** — задаёт цвет каждого пикселя:
```glsl
precision mediump float;
varying vec4 fColor;

void main() {
    gl_FragColor = fColor;     // Красим пиксель цветом вершины
}
```

Разница между `attribute` и `uniform`:
- **attribute** — данные для каждой вершины отдельно (позиция, цвет)
- **uniform** — данные одинаковые для всех вершин (матрица MVP)

### Код Renderer

```kotlin
package ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.Matrix
import ru.iandreyshev.cglab4.R
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation.PentagonalIcositetrahedronState
import ru.iandreyshev.core.createProgramGLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

private const val COORDS_PER_VERTEX = 3    // x, y, z
private const val COLORS_PER_VERTEX = 4    // r, g, b, a

class PentagonalIcositetrahedronRenderer(res: Resources) {

    private val vertices: FloatArray
    private val colors: FloatArray
    private val vertexCount: Int

    init {
        val (verts, cols) = generateGeometry()
        vertices = verts
        colors = cols
        vertexCount = vertices.size / COORDS_PER_VERTEX
    }
```

### Генерация геометрии — 38 вершин многогранника

Координаты берутся из канонических координат DMcCooey (математик Дэвид Маккуи вычислил точные координаты для каталановских тел). Четыре константы, из комбинаций которых строятся все 38 вершин:

```kotlin
    private fun generateGeometry(): Pair<FloatArray, FloatArray> {
        val C0 = 0.2187966430f
        val C1 = 0.7401837414f
        val C2 = 1.0236561781f
        val C3 = 1.3614101519f

        // 38 вершин, три группы:
        val polyVerts = arrayOf(
            // V0-V5: осевые — лежат на осях координат
            floatArrayOf( C3,   0f,   0f),   // V0  +X
            floatArrayOf(-C3,   0f,   0f),   // V1  -X
            floatArrayOf(  0f,  C3,   0f),   // V2  +Y
            floatArrayOf(  0f, -C3,   0f),   // V3  -Y
            floatArrayOf(  0f,   0f,  C3),   // V4  +Z
            floatArrayOf(  0f,   0f, -C3),   // V5  -Z

            // V6-V13: «кубические» — как вершины куба со стороной 2*C1
            floatArrayOf( C1,  C1,  C1),     // V6
            floatArrayOf( C1,  C1, -C1),     // V7
            floatArrayOf( C1, -C1,  C1),     // V8
            floatArrayOf( C1, -C1, -C1),     // V9
            floatArrayOf(-C1,  C1,  C1),     // V10
            floatArrayOf(-C1,  C1, -C1),     // V11
            floatArrayOf(-C1, -C1,  C1),     // V12
            floatArrayOf(-C1, -C1, -C1),     // V13

            // V14-V37: остальные — комбинации C0, C1, C2 с перестановками
            floatArrayOf( C0,  C2,  C1),     // V14
            floatArrayOf( C0, -C2, -C1),     // V15
            floatArrayOf(-C0,  C2, -C1),     // V16
            floatArrayOf(-C0, -C2,  C1),     // V17
            floatArrayOf( C2,  C1,  C0),     // V18
            floatArrayOf( C2, -C1, -C0),     // V19
            floatArrayOf(-C2,  C1, -C0),     // V20
            floatArrayOf(-C2, -C1,  C0),     // V21
            floatArrayOf( C1,  C0,  C2),     // V22
            floatArrayOf( C1, -C0, -C2),     // V23
            floatArrayOf(-C1,  C0, -C2),     // V24
            floatArrayOf(-C1, -C0,  C2),     // V25
            floatArrayOf( C0,  C1, -C2),     // V26
            floatArrayOf( C0, -C1,  C2),     // V27
            floatArrayOf(-C0,  C1,  C2),     // V28
            floatArrayOf(-C0, -C1, -C2),     // V29
            floatArrayOf( C1,  C2, -C0),     // V30
            floatArrayOf( C1, -C2,  C0),     // V31
            floatArrayOf(-C1,  C2,  C0),     // V32
            floatArrayOf(-C1, -C2, -C0),     // V33
            floatArrayOf( C2,  C0, -C1),     // V34
            floatArrayOf( C2, -C0,  C1),     // V35
            floatArrayOf(-C2,  C0,  C1),     // V36
            floatArrayOf(-C2, -C0, -C1),     // V37
        )
```

### 24 пятиугольных грани

Каждая грань — 5 индексов из массива `polyVerts`:

```kotlin
        val faces = arrayOf(
            intArrayOf(15, 29,  5, 23,  9),
            intArrayOf(33, 21,  1, 37, 13),
            intArrayOf(21, 33,  3, 17, 12),
            intArrayOf(29, 15,  3, 33, 13),
            intArrayOf(19, 31,  3, 15,  9),
            intArrayOf(31, 19,  0, 35,  8),
            intArrayOf(17, 27,  4, 25, 12),
            intArrayOf(35, 22,  4, 27,  8),
            intArrayOf(27, 17,  3, 31,  8),
            intArrayOf(37, 24,  5, 29, 13),
            intArrayOf(24, 37,  1, 20, 11),
            intArrayOf(23, 34,  0, 19,  9),
            intArrayOf(34, 23,  5, 26,  7),
            intArrayOf(25, 36,  1, 21, 12),
            intArrayOf(32, 20,  1, 36, 10),
            intArrayOf(30, 18,  0, 34,  7),
            intArrayOf(16, 26,  5, 24, 11),
            intArrayOf(20, 32,  2, 16, 11),
            intArrayOf(26, 16,  2, 30,  7),
            intArrayOf(36, 25,  4, 28, 10),
            intArrayOf(28, 14,  2, 32, 10),
            intArrayOf(22, 35,  0, 18,  6),
            intArrayOf(18, 30,  2, 14,  6),
            intArrayOf(14, 28,  4, 22,  6),
        )
```

### Палитра — 24 цвета, по одному на грань

```kotlin
        val faceColorPalette = arrayOf(
            floatArrayOf(0.95f, 0.25f, 0.20f, 1.0f),
            floatArrayOf(0.20f, 0.80f, 0.30f, 1.0f),
            floatArrayOf(0.20f, 0.40f, 0.95f, 1.0f),
            floatArrayOf(0.95f, 0.85f, 0.20f, 1.0f),
            floatArrayOf(0.85f, 0.25f, 0.85f, 1.0f),
            floatArrayOf(0.20f, 0.85f, 0.85f, 1.0f),
            floatArrayOf(0.95f, 0.55f, 0.15f, 1.0f),
            floatArrayOf(0.55f, 0.20f, 0.90f, 1.0f),
            floatArrayOf(0.25f, 0.65f, 0.25f, 1.0f),
            floatArrayOf(0.85f, 0.75f, 0.30f, 1.0f),
            floatArrayOf(0.25f, 0.75f, 0.75f, 1.0f),
            floatArrayOf(0.85f, 0.30f, 0.55f, 1.0f),
            floatArrayOf(0.90f, 0.45f, 0.15f, 1.0f),
            floatArrayOf(0.40f, 0.15f, 0.85f, 1.0f),
            floatArrayOf(0.15f, 0.85f, 0.45f, 1.0f),
            floatArrayOf(0.85f, 0.85f, 0.50f, 1.0f),
            floatArrayOf(0.50f, 0.85f, 0.85f, 1.0f),
            floatArrayOf(0.85f, 0.50f, 0.85f, 1.0f),
            floatArrayOf(0.70f, 0.30f, 0.25f, 1.0f),
            floatArrayOf(0.30f, 0.65f, 0.30f, 1.0f),
            floatArrayOf(0.60f, 0.45f, 0.80f, 1.0f),
            floatArrayOf(0.80f, 0.60f, 0.40f, 1.0f),
            floatArrayOf(0.45f, 0.70f, 0.55f, 1.0f),
            floatArrayOf(0.75f, 0.40f, 0.40f, 1.0f),
        )
```

Формат: `(R, G, B, A)`, от 0.0 до 1.0. A = 1.0 — непрозрачный.

### Веерная триангуляция

OpenGL не умеет рисовать пятиугольники — только треугольники. Разбиваем каждый пятиугольник на треугольники методом **«веера от центроида»** (fan triangulation).

**Что такое центроид?**

Центроид — это геометрический центр грани, «центр тяжести» многоугольника. Для пятиугольника с вершинами V0...V4 центроид C вычисляется как среднее арифметическое координат всех вершин:

```
C.x = (V0.x + V1.x + V2.x + V3.x + V4.x) / 5
C.y = (V0.y + V1.y + V2.y + V3.y + V4.y) / 5
C.z = (V0.z + V1.z + V2.z + V3.z + V4.z) / 5
```

В коде это строки:
```kotlin
val cx = verts.map { it[0] }.average().toFloat()
val cy = verts.map { it[1] }.average().toFloat()
val cz = verts.map { it[2] }.average().toFloat()
```

Центроид всегда лежит внутри выпуклого многоугольника — это важно, потому что из него можно провести лучи ко всем рёбрам, и ни один не выйдет за пределы грани.

**Как работает веер?**

Из центроида строим 5 треугольников, каждый соединяет центроид с двумя соседними вершинами:

```
         V0
        /|  \
       / | t0 \
      /  |      \
    V4   C------V1       C = центроид
      \  |      /        t0 = (C, V0, V1)
       \ | t1 /          t1 = (C, V1, V2)
        \|  /             t2 = (C, V2, V3)
         V3----V2         t3 = (C, V3, V4)
                          t4 = (C, V4, V0)
```

В коде — цикл по рёбрам грани:
```kotlin
for (i in 0 until 5) {
    val a = floatArrayOf(cx, cy, cz)   // центроид
    val b = verts[i]                    // текущая вершина
    val c = verts[(i + 1) % 5]         // следующая (% 5 замыкает: после V4 идёт V0)
    // → треугольник (a, b, c)
}
```

**Почему не от вершины?** Можно было бы разбить от одной из вершин (V0→V1→V2, V0→V2→V3, V0→V3→V4 = 3 треугольника), но для неправильных пятиугольников (как у нашего многогранника) треугольники получились бы сильно вытянутыми. Веер от центроида даёт более равномерные треугольники — освещение на шаге 5 будет выглядеть лучше.

Итого: 24 грани x 5 треугольников = **120 треугольников = 360 вершин**.

### Проверка нормалей

При разбиении некоторые треугольники могут оказаться «вывернутыми» (нормаль смотрит внутрь). Проверяем через **векторное произведение** (даёт нормаль) и **скалярное произведение** (проверяет направление):

- `dot(нормаль, вектор_к_центру) >= 0` → нормаль наружу, порядок вершин верный
- `dot < 0` → меняем B и C местами, нормаль развернётся

Это работает, потому что фигура центрирована в (0,0,0) — вектор к центру треугольника всегда «наружу».

```kotlin
        // Сюда собираем координаты всех треугольников (x,y,z для каждой вершины)
        val faceVertices = mutableListOf<Float>()
        // Сюда — цвета (r,g,b,a для каждой вершины)
        val faceColors = mutableListOf<Float>()

        faces.forEachIndexed { faceIndex, face ->
            val color = faceColorPalette[faceIndex]
            // face = [15, 29, 5, 23, 9] — индексы 5 вершин пятиугольника
            // verts = массив из 5 координат (floatArrayOf(x, y, z) каждая)
            val verts = face.map { polyVerts[it] }

            // Центроид грани — среднее координат 5 вершин (см. раздел выше)
            val cx = verts.map { it[0] }.average().toFloat()
            val cy = verts.map { it[1] }.average().toFloat()
            val cz = verts.map { it[2] }.average().toFloat()

            // Строим 5 треугольников веером от центроида
            for (i in 0 until 5) {
                val a = floatArrayOf(cx, cy, cz)   // вершина A = центроид
                val b = verts[i]                    // вершина B = текущая вершина грани
                val c = verts[(i + 1) % 5]         // вершина C = следующая вершина

                // === Проверка: нормаль смотрит наружу или внутрь? ===

                // Шаг 1: Строим два вектора-ребра треугольника из точки A
                //   AB = B - A (от центроида к текущей вершине)
                //   AC = C - A (от центроида к следующей вершине)
                val ab = floatArrayOf(b[0] - a[0], b[1] - a[1], b[2] - a[2])
                val ac = floatArrayOf(c[0] - a[0], c[1] - a[1], c[2] - a[2])

                // Шаг 2: Векторное произведение AB × AC даёт вектор нормали —
                // перпендикуляр к плоскости треугольника.
                // Направление нормали зависит от порядка вершин (правило правой руки):
                //   A→B→C по часовой   → нормаль внутрь
                //   A→B→C против часовой → нормаль наружу
                // Мы пока не знаем, какой порядок получился — проверим ниже.
                val normal = cross(ab, ac)

                // Шаг 3: Находим центр треугольника (не путать с центроидом грани! —
                // это среднее 3 вершин именно этого треугольника).
                // Фигура стоит в начале координат (0,0,0), поэтому вектор
                // от (0,0,0) к центру треугольника (tcx, tcy, tcz) направлен НАРУЖУ
                // от центра фигуры.
                val tcx = (a[0] + b[0] + c[0]) / 3f
                val tcy = (a[1] + b[1] + c[1]) / 3f
                val tcz = (a[2] + b[2] + c[2]) / 3f

                // Шаг 4: Скалярное произведение нормали и вектора «наружу».
                // dot > 0 → угол между ними < 90° → нормаль смотрит наружу ✓
                // dot < 0 → угол > 90° → нормаль смотрит внутрь ✗ → нужно перевернуть
                val dot = normal[0] * tcx + normal[1] * tcy + normal[2] * tcz

                if (dot >= 0) {
                    // Порядок A→B→C уже даёт нормаль наружу — записываем как есть
                    faceVertices.addAll(a.toList())
                    faceVertices.addAll(b.toList())
                    faceVertices.addAll(c.toList())
                } else {
                    // Нормаль смотрит внутрь — меняем B и C местами.
                    // Это разворачивает обход вершин (был по часовой → стал против),
                    // и нормаль (AB × AC) меняет направление на противоположное.
                    faceVertices.addAll(a.toList())
                    faceVertices.addAll(c.toList())
                    faceVertices.addAll(b.toList())
                }

                // Все 3 вершины треугольника получают одинаковый цвет грани
                faceColors.addAll(color.toList())
                faceColors.addAll(color.toList())
                faceColors.addAll(color.toList())
            }
        }

        return Pair(faceVertices.toFloatArray(), faceColors.toFloatArray())
    }

    // Векторное произведение двух 3D-векторов.
    // Результат — вектор, перпендикулярный обоим входным (нормаль к плоскости).
    // Формула: (a.y*b.z - a.z*b.y, a.z*b.x - a.x*b.z, a.x*b.y - a.y*b.x)
    private fun cross(a: FloatArray, b: FloatArray) = floatArrayOf(
        a[1] * b[2] - a[2] * b[1],
        a[2] * b[0] - a[0] * b[2],
        a[0] * b[1] - a[1] * b[0]
    )
```

### Буферы — мост CPU → GPU

OpenGL не читает Kotlin-массивы. Нужно скопировать данные в **нативную память** через `FloatBuffer`.

**Почему нельзя передать обычный массив?**

Kotlin-код работает на JVM. Когда ты создаёшь `FloatArray(360)`, JVM выделяет память под этот массив и сама решает, где именно в оперативной памяти он будет лежать. Более того, **сборщик мусора** (GC) может в любой момент переместить массив на другой адрес — это часть нормальной работы JVM, для Kotlin-кода это незаметно.

Но OpenGL — это не Kotlin-библиотека. Это C-код, который общается с GPU через **прямые адреса в памяти**: «прочитай 360 float'ов начиная с адреса 0x7F3A00». Если GC переместит массив в момент чтения — GPU прочитает мусор.

**Решение:** `ByteBuffer.allocateDirect()` просит операционную систему выделить блок памяти напрямую, в обход JVM. Этот блок:
- Лежит по **фиксированному адресу** — GC его не трогает и не перемещает
- Доступен и для JVM (через `FloatBuffer`), и для нативного кода (через указатель)
- Нужно вручную указать размер в байтах (JVM за тебя это не сделает)

```
FloatArray          →  память под управлением JVM  →  GC может переместить  →  ✗ OpenGL не может читать
allocateDirect()    →  память от ОС напрямую       →  адрес фиксирован      →  ✓ OpenGL читает безопасно
```

```kotlin
    private var _program: Int = createProgramGLES30(res, R.raw.cube_vert, R.raw.cube_frag)

    private val _modelMatrix = FloatArray(16)
    private val _viewModelMatrix = FloatArray(16)
    private val _mvpMatrix = FloatArray(16)

    private var _mvpMatrixHandle: Int = 0
    private var _positionHandle: Int = 0
    private var _colorHandle: Int = 0

    private val _vertexBuffer: FloatBuffer = ByteBuffer
        // Выделяем нативную память нужного размера (количество float × 4 байта на float)
        .allocateDirect(vertices.size * Float.SIZE_BYTES)
        // Порядок байтов: little-endian или big-endian — зависит от платформы.
        // nativeOrder() ставит тот, который понимает GPU данного устройства.
        .order(ByteOrder.nativeOrder())
        // Оборачиваем сырые байты в FloatBuffer — удобно записывать float'ы
        .asFloatBuffer()
        .apply {
            put(vertices)   // копируем данные из Kotlin-массива в нативную память
            position(0)     // сбрасываем курсор на начало — GPU будет читать отсюда
        }

    private val _colorBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(colors.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(colors); position(0) }
```

Цепочка вызовов — всегда одна и та же для любого буфера: `allocateDirect` → `order` → `asFloatBuffer` → `put` → `position(0)`. Меняется только массив-источник и размер.

### Метод draw — отрисовка кадра

```kotlin
    fun draw(
        state: PentagonalIcositetrahedronState,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
    ) {
        // 1. Model-матрица: масштаб + поворот
        //    На шаге 1 rotationMatrix — единичная матрица, фигура стоит на месте.
        //    После шага 2.5 rotationMatrix будет накапливать повороты от drag.
        Matrix.setIdentityM(_modelMatrix, 0)
        Matrix.scaleM(_modelMatrix, 0, state.scale, state.scale, state.scale)
        val scaledModel = _modelMatrix.copyOf()
        Matrix.multiplyMM(_modelMatrix, 0, scaledModel, 0, state.rotationMatrix, 0)

        // 2. MVP = Projection x View x Model
        Matrix.multiplyMM(_viewModelMatrix, 0, viewMatrix, 0, _modelMatrix, 0)
        Matrix.multiplyMM(_mvpMatrix, 0, projectionMatrix, 0, _viewModelMatrix, 0)

        // 3. Активируем шейдер
        GLES30.glUseProgram(_program)

        // 4. Передаём данные в шейдер
        _positionHandle = GLES30.glGetAttribLocation(_program, "vPosition")
        GLES30.glEnableVertexAttribArray(_positionHandle)
        GLES30.glVertexAttribPointer(
            _positionHandle, COORDS_PER_VERTEX, GLES30.GL_FLOAT,
            false, COORDS_PER_VERTEX * Float.SIZE_BYTES, _vertexBuffer,
        )

        _colorHandle = GLES30.glGetAttribLocation(_program, "vColor")
        GLES30.glEnableVertexAttribArray(_colorHandle)
        GLES30.glVertexAttribPointer(
            _colorHandle, COLORS_PER_VERTEX, GLES30.GL_FLOAT,
            false, COLORS_PER_VERTEX * Float.SIZE_BYTES, _colorBuffer,
        )

        _mvpMatrixHandle = GLES30.glGetUniformLocation(_program, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(_mvpMatrixHandle, 1, false, _mvpMatrix, 0)

        // 5. Рисуем
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        GLES30.glDisableVertexAttribArray(_positionHandle)
        GLES30.glDisableVertexAttribArray(_colorHandle)
    }
}
```

Три матрицы по 16 чисел (4x4):
- **Model** — как расположен объект (масштаб, поворот). Пока `rotationMatrix` — единичная матрица, фигура стоит на месте.
- **View** — где камера.
- **Projection** — перспектива (далёкое — меньше).

## 1.8. Проверка

```bash
./gradlew assembleDebug
```

Запусти — на экране статичный разноцветный многогранник. Не вращается, не масштабируется. Это нормально!

---

# Шаг 2. Добавляем вращение (drag)

**Цель:** провёл пальцем — фигура повернулась.

## 2.1. ViewModel — добавляем `onDrag`

В `PentagonalIcositetrahedronViewModel` добавь:

```kotlin
import androidx.compose.ui.geometry.Offset

// Внутри класса:
fun onDrag(dragAmount: Offset) {
    updateState {
        copy(rotation = rotation + dragAmount)
    }
}
```

При каждом событии drag прибавляем смещение пальца к текущему повороту. `updateState { copy(...) }` — идиоматический способ обновить State.

## 2.2. Screen — добавляем обработку жеста одним пальцем

Замени содержимое `PentagonalIcositetrahedronScreen`:

```kotlin
package ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation.PentagonalIcositetrahedronViewModel

@Composable
fun PentagonalIcositetrahedronScreen(
    viewModel: PentagonalIcositetrahedronViewModel = viewModel { PentagonalIcositetrahedronViewModel() }
) {
    val state by viewModel.state

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown().consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }

                        if (pressed.isEmpty()) break

                        val change = pressed[0]
                        val dragAmount = change.positionChange()
                        if (dragAmount != Offset.Zero) {
                            viewModel.onDrag(dragAmount)
                            change.consume()
                        }
                    }
                }
            },
        factory = { PentagonalIcositetrahedronGLSurfaceView(it) },
        update = { view -> view.updateState(state) }
    )
}
```

`awaitEachGesture` обрабатывает каждый жест от нажатия до отпускания. Внутри цикл:
- `awaitPointerEvent()` — ждём любое событие указателя (движение, нажатие, отпускание). Возвращает `PointerEvent` со списком `changes` — по одному на каждый палец.
- `event.changes.filter { it.pressed }` — оставляем только пальцы, которые сейчас прижаты к экрану. Если список пуст — все пальцы отпущены, выходим из цикла.
- `positionChange()` — смещение пальца (в пикселях) с предыдущего события. Передаём во ViewModel для обновления угла поворота.
- `change.consume()` — помечаем событие как обработанное, чтобы другие обработчики жестов его не перехватили.

## 2.3. Проверка

Запусти — теперь палец вращает фигуру. Но после отпускания она останавливается мгновенно.

---

# Шаг 2.5. Улучшаем вращение — матрица вместо углов

**Цель:** фигура вращается «за пальцем» естественно, вне зависимости от текущей ориентации.

### Проблема текущего подхода

Сейчас в `draw()` вращение строится так:

```kotlin
Matrix.rotateM(_modelMatrix, 0, state.rotation.y, 1f, 0f, 0f)  // вокруг мировой X
Matrix.rotateM(_modelMatrix, 0, state.rotation.x, 0f, 1f, 0f)  // вокруг мировой Y
```

`rotation` — это **накопленные углы**, и каждый кадр матрица строится заново из них вокруг **фиксированных мировых осей** X и Y. Если фигура уже повёрнута на 90° — свайп вправо всё равно вращает вокруг мировой Y, а не вокруг той оси, которая сейчас выглядит как «вправо» на экране.

```
Мировая Y (↑)                      Мировая Y (↑)
    |                                    |
    |   свайп →                          |   свайп →
    ■   вращение вокруг Y ✓              ◆   всё ещё вокруг Y ✗
 начальное положение              фигура повёрнута на 90°
                                  (хочется вокруг другой оси)
```

### Идея: инкрементальное вращение

Вместо «запомнить углы → каждый кадр строить матрицу с нуля» будем **накапливать саму матрицу вращения**. Каждый drag-дельта — это маленький поворот, который домножается на текущую матрицу:

```
Было:    rotation += dragDelta  →  матрица = rotateX(rotation.y) × rotateY(rotation.x)
Стало:   rotationMatrix = rotateX(deltaY) × rotateY(deltaX) × rotationMatrix
```

Ключевое отличие: маленький поворот `rotateX(deltaY) × rotateY(deltaX)` применяется **перед** текущим поворотом, то есть в системе координат экрана (камеры), а не объекта. Поэтому свайп вправо всегда выглядит как поворот вправо.

### Изменения

**1. State** — заменяем `rotation: Offset` на `rotationMatrix: FloatArray`:

```kotlin
data class PentagonalIcositetrahedronState(
    val rotationMatrix: FloatArray = FloatArray(16).apply {
        android.opengl.Matrix.setIdentityM(this, 0)
    },
    val scale: Float = 0.5f,
)
```

Единичная матрица = нет поворота. `FloatArray(16)` — матрица 4×4, формат OpenGL.

> **Примечание:** `FloatArray` не имеет структурного `equals`/`hashCode`, поэтому `data class` не будет корректно сравнивать состояния по значению. Для нашего случая это не проблема — Compose подписан на `State<T>`, и каждый `updateState { copy(...) }` создаёт новый объект, что вызывает рекомпозицию.

**2. ViewModel** — `onDrag` строит маленький поворот и домножает:

```kotlin
fun onDrag(dragAmount: Offset) {
    updateState {
        val tempMatrix = FloatArray(16)
        val resultMatrix = FloatArray(16)

        // Маленький поворот из текущего drag-дельты
        Matrix.setIdentityM(tempMatrix, 0)
        Matrix.rotateM(tempMatrix, 0, dragAmount.y, 1f, 0f, 0f)  // deltaY → вокруг X
        Matrix.rotateM(tempMatrix, 0, dragAmount.x, 0f, 1f, 0f)  // deltaX → вокруг Y

        // Домножаем: новый_поворот × текущий_поворот
        Matrix.multiplyMM(resultMatrix, 0, tempMatrix, 0, rotationMatrix, 0)

        copy(rotationMatrix = resultMatrix)
    }
}
```

Порядок умножения `tempMatrix × rotationMatrix` важен: сначала применяется `rotationMatrix` (текущий накопленный поворот), потом `tempMatrix` (новый маленький поворот в экранных координатах). Если поменять местами — drag будет вращать в координатах объекта, а не экрана.

**3. Renderer (`draw`)** — вместо двух `rotateM` просто умножаем на матрицу из state:

```kotlin
    // Было:
    Matrix.setIdentityM(_modelMatrix, 0)
    Matrix.scaleM(_modelMatrix, 0, state.scale, state.scale, state.scale)
    Matrix.rotateM(_modelMatrix, 0, state.rotation.y, 1f, 0f, 0f)
    Matrix.rotateM(_modelMatrix, 0, state.rotation.x, 0f, 1f, 0f)

    // Стало:
    Matrix.setIdentityM(_modelMatrix, 0)
    Matrix.scaleM(_modelMatrix, 0, state.scale, state.scale, state.scale)
    val scaledModel = _modelMatrix.copyOf()
    Matrix.multiplyMM(_modelMatrix, 0, scaledModel, 0, state.rotationMatrix, 0)
```

`copyOf()` нужен потому что `multiplyMM` не может писать результат в тот же массив, который читает (undefined behavior). Создаём копию, умножаем `копия × rotationMatrix` → результат в `_modelMatrix`.

### Влияние на следующие шаги

- **Шаг 3 (fling):** `velocity` тоже должен работать через матрицу — каждый тик анимации строит маленький поворот из `velocity` и домножает `rotationMatrix`, аналогично `onDrag`.
- **Шаг 4 (pinch):** не затронут — `scale` остаётся отдельным полем.

---

# Шаг 3. Добавляем инерцию (fling)

**Цель:** после быстрого свайпа фигура продолжает вращаться и плавно замедляется.

## 3.1. State — добавляем скорость

В `PentagonalIcositetrahedronState` добавь поле `velocity`:

```kotlin
data class PentagonalIcositetrahedronState(
    val rotationMatrix: FloatArray = FloatArray(16).apply {
        android.opengl.Matrix.setIdentityM(this, 0)
    },
    val velocity: Offset = Offset.Zero,
    val scale: Float = 0.5f,
)
```

## 3.2. ViewModel — добавляем fling-анимацию

Замени содержимое `PentagonalIcositetrahedronViewModel`:

```kotlin
package ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation

import android.opengl.Matrix
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.iandreyshev.core.BaseViewModel
import kotlin.math.abs

class PentagonalIcositetrahedronViewModel : BaseViewModel<PentagonalIcositetrahedronState, Any>(
    initialState = PentagonalIcositetrahedronState()
) {
    private var flingJob: Job? = null
    private val friction = 0.95f
    private val minVelocity = 0.1f

    fun onDrag(dragAmount: Offset) {
        flingJob?.cancel()
        updateState {
            copy(rotationMatrix = applyRotationDelta(rotationMatrix, dragAmount))
        }
    }

    fun onFling(velocity: Offset) {
        val scaledVelocity = velocity / 50f

        flingJob?.cancel()
        flingJob = viewModelScope.launch {
            updateState { copy(velocity = scaledVelocity) }

            while (true) {
                val currentVelocity = stateValue.velocity

                if (abs(currentVelocity.x) < minVelocity && abs(currentVelocity.y) < minVelocity) {
                    updateState { copy(velocity = Offset.Zero) }
                    break
                }

                updateState {
                    copy(
                        rotationMatrix = applyRotationDelta(
                            rotationMatrix,
                            Offset(currentVelocity.x, currentVelocity.y)
                        ),
                        velocity = currentVelocity * friction
                    )
                }

                delay(16)
            }
        }
    }

    private fun applyRotationDelta(currentMatrix: FloatArray, delta: Offset): FloatArray {
        val tempMatrix = FloatArray(16)
        val resultMatrix = FloatArray(16)

        Matrix.setIdentityM(tempMatrix, 0)
        Matrix.rotateM(tempMatrix, 0, delta.y, 1f, 0f, 0f)
        Matrix.rotateM(tempMatrix, 0, delta.x, 0f, 1f, 0f)

        Matrix.multiplyMM(resultMatrix, 0, tempMatrix, 0, currentMatrix, 0)
        return resultMatrix
    }
}
```

Что нового:
- **`applyRotationDelta`** — вынесли логику из шага 2.5 в отдельный метод. Строит маленький поворот из дельты и домножает на текущую матрицу. Используется и в `onDrag`, и в `onFling` — одна и та же механика вращения.
- **`flingJob`** — ссылка на корутину анимации. `cancel()` останавливает предыдущую анимацию (когда палец снова касается экрана).
- **`onDrag`** теперь отменяет fling — пользователь «перехватывает» вращение.
- **`onFling`** — запускает корутину, которая каждые 16 мс (~60 FPS):
  - Применяет `velocity` как дельту поворота через `applyRotationDelta`
  - Умножает скорость на `friction` (0.95) — затухание
  - Останавливается при скорости ниже `minVelocity`
- `velocity / 50f` — `VelocityTracker` возвращает скорость в пикселях/секунду (порядка тысяч). Делим на 50, чтобы получить разумное смещение за кадр. Число подобрано эмпирически: меньше — медленнее инерция, больше — резче. Можешь подстроить под свои ощущения.
- `friction = 0.95f` — коэффициент затухания. Каждый кадр скорость умножается на 0.95 → через ~60 кадров (~1 сек) скорость упадёт до ~5% от начальной. Если поставить 0.98 — затухание медленнее (плавнее), 0.90 — быстрее (резче).

## 3.3. GLSurfaceView — переключаем на непрерывный рендеринг

В `PentagonalIcositetrahedronGLSurfaceView` замени:

```kotlin
renderMode = RENDERMODE_WHEN_DIRTY
```

на:

```kotlin
renderMode = RENDERMODE_CONTINUOUSLY
```

Нужно для плавной анимации fling — кадры рисуются непрерывно (~60 FPS), даже когда палец не касается экрана. Это расходует больше батареи, чем `RENDERMODE_WHEN_DIRTY`, но для анимации инерции необходимо: ViewModel обновляет `state` каждые 16 мс, и GLSurfaceView должен перерисовывать кадр без явного `requestRender()` на каждый тик.

## 3.4. Screen — добавляем VelocityTracker

Обновляем обработку жестов в `PentagonalIcositetrahedronScreen`. Нужно добавить `VelocityTracker` — он отслеживает скорость пальца для расчёта fling при отпускании.

Добавь импорт:

```kotlin
import androidx.compose.ui.input.pointer.util.VelocityTracker
```

Замени блок `.pointerInput(Unit) { ... }`:

```kotlin
.pointerInput(Unit) {
    awaitEachGesture {
        val velocityTracker = VelocityTracker()

        awaitFirstDown().consume()

        while (true) {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }

            if (pressed.isEmpty()) {
                val velocity = velocityTracker.calculateVelocity()
                viewModel.onFling(Offset(velocity.x, velocity.y))
                break
            }

            val change = pressed[0]
            velocityTracker.addPosition(change.uptimeMillis, change.position)

            val dragAmount = change.positionChange()
            if (dragAmount != Offset.Zero) {
                viewModel.onDrag(dragAmount)
                change.consume()
            }
        }
    }
}
```

Что нового:
- `VelocityTracker()` создаётся для каждого жеста.
- `addPosition(...)` — записываем позицию пальца на каждое событие.
- При отпускании: `calculateVelocity()` → `onFling(...)`.

## 3.5. Проверка

Свайпни фигуру — она продолжит вращаться и плавно замедлится. Коснись пальцем — анимация остановится, можно снова вращать.

---

# Шаг 4. Добавляем масштабирование (pinch)

**Цель:** два пальца увеличивают/уменьшают фигуру.

## 4.1. ViewModel — добавляем `onScale`

В `PentagonalIcositetrahedronViewModel` добавь метод:

```kotlin
fun onScale(scaleFactor: Float) {
    updateState {
        copy(scale = (scale * scaleFactor).coerceIn(0.1f, 5f))
    }
}
```

`scaleFactor > 1` = пальцы разводятся = увеличение. `< 1` = сведение = уменьшение. `coerceIn(0.1, 5.0)` — ограничиваем диапазон.

## 4.2. Screen — добавляем pinch-обработку

Добавь импорт:

```kotlin
import kotlin.math.sqrt
```

Замени блок `.pointerInput(Unit) { ... }` финальной версией с поддержкой pinch:

```kotlin
.pointerInput(Unit) {
    awaitEachGesture {
        val velocityTracker = VelocityTracker()
        var previousDistance = 0f
        var isPinching = false

        awaitFirstDown().consume()

        while (true) {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }

            if (pressed.isEmpty()) {
                if (!isPinching) {
                    val velocity = velocityTracker.calculateVelocity()
                    viewModel.onFling(Offset(velocity.x, velocity.y))
                }
                break
            }

            if (pressed.size >= 2) {
                isPinching = true
                val p1 = pressed[0].position
                val p2 = pressed[1].position
                val dx = p1.x - p2.x
                val dy = p1.y - p2.y
                val distance = sqrt(dx * dx + dy * dy)

                if (previousDistance > 0f) {
                    val scaleFactor = distance / previousDistance
                    viewModel.onScale(scaleFactor)
                }
                previousDistance = distance
                event.changes.forEach { it.consume() }
            } else if (!isPinching) {
                val change = pressed[0]
                velocityTracker.addPosition(change.uptimeMillis, change.position)

                val dragAmount = change.positionChange()
                if (dragAmount != Offset.Zero) {
                    viewModel.onDrag(dragAmount)
                    change.consume()
                }
            }
        }
    }
}
```

Логика трёх сценариев:

1. **Один палец + не было пинча** → drag (вращение) + запись в VelocityTracker
2. **Два+ пальца** → вычисляем евклидово расстояние между двумя пальцами (`sqrt(dx² + dy²)`). `scaleFactor = distance / previousDistance`: если пальцы разводятся — `scaleFactor > 1` (увеличение), сводятся — `< 1` (уменьшение). `previousDistance > 0f` — пропускаем первый кадр пинча (нет предыдущего расстояния для сравнения).
3. **Отпускание** → если не было пинча — fling, иначе просто выходим (fling после пинча неуместен)

`isPinching` — флаг: если пользователь начал пинч (коснулся двумя пальцами), то после отпускания fling не запускаем. Без этого флага отпускание второго пальца после пинча вызвало бы резкий fling от последнего движения.

## 4.3. Проверка

Два пальца увеличивают/уменьшают фигуру. Один палец вращает. Быстрый свайп — инерция. Всё вместе!

---

# Шаг 5. Добавляем освещение

**Цель:** грани затеняются в зависимости от угла к источнику света. Фигура выглядит объёмной.

Сейчас все грани одинаково яркие — фигура выглядит плоско. Добавим **диффузное освещение** (модель Ламберта): чем больше угол между гранью и направлением света, тем темнее грань.

## 5.1. Создаём новые шейдеры

Текущие шейдеры (`cube_vert`/`cube_frag`) не поддерживают нормали. Создадим новую пару.

Создай файл `src/main/res/raw/pent_vert.vert`:

```glsl
uniform mat4 uMVPMatrix;
uniform mat4 uModelMatrix;

attribute vec4 vPosition;
attribute vec4 vColor;
attribute vec3 vNormal;

varying vec4 fColor;
varying vec3 fNormal;

void main() {
    gl_Position = uMVPMatrix * vPosition;
    fColor = vColor;
    // Трансформируем нормаль из пространства модели в мировое пространство
    fNormal = mat3(uModelMatrix) * vNormal;
}
```

Что нового по сравнению с `cube_vert`:
- `uModelMatrix` — матрица модели (без View и Projection). Нужна для трансформации нормалей.
- `vNormal` — нормаль вершины (направление «наружу» от грани).
- `mat3(uModelMatrix) * vNormal` — поворачиваем нормаль вместе с объектом. `mat3(...)` берёт верхнюю 3x3 подматрицу (без сдвига).

Создай файл `src/main/res/raw/pent_frag.frag`:

```glsl
precision mediump float;

uniform vec3 uLightDirection;

varying vec4 fColor;
varying vec3 fNormal;

void main() {
    vec3 normal = normalize(fNormal);
    vec3 lightDir = normalize(uLightDirection);

    // Диффузное освещение: яркость = cos(угол между нормалью и светом)
    float diffuse = max(dot(normal, lightDir), 0.0);

    // ambient (0.3) — минимальная яркость, чтобы тени не были полностью чёрными
    float brightness = 0.3 + 0.7 * diffuse;

    gl_FragColor = vec4(fColor.rgb * brightness, fColor.a);
}
```

Формула Ламберта: `brightness = ambient + (1 - ambient) * max(dot(N, L), 0)`.
- `N` — нормаль грани.
- `L` — направление к свету.
- `dot(N, L)` = косинус угла между ними. Грань, смотрящая прямо на свет, получает максимальную яркость. Грань, повёрнутая боком — минимальную.
- `max(..., 0)` — грани, повёрнутые от света, получают только ambient.
- `0.3` (ambient) — минимальная яркость, чтобы теневая сторона не была абсолютно чёрной.

## 5.2. Renderer — добавляем буфер нормалей

В `PentagonalIcositetrahedronRenderer` нужно:

**1. Генерировать нормали при триангуляции.** Добавляем третий массив в `generateGeometry`. Возвращаем `Triple` вместо `Pair`:

```kotlin
    private val normals: FloatArray

    init {
        val (verts, cols, norms) = generateGeometry()
        vertices = verts
        colors = cols
        normals = norms
        vertexCount = vertices.size / COORDS_PER_VERTEX
    }
```

Изменяем сигнатуру и тело `generateGeometry`:

```kotlin
    private fun generateGeometry(): Triple<FloatArray, FloatArray, FloatArray> {
        // ... (polyVerts, faces, faceColorPalette — без изменений) ...

        val faceVertices = mutableListOf<Float>()
        val faceColors = mutableListOf<Float>()
        val faceNormals = mutableListOf<Float>()

        faces.forEachIndexed { faceIndex, face ->
            val color = faceColorPalette[faceIndex]
            val verts = face.map { polyVerts[it] }

            val cx = verts.map { it[0] }.average().toFloat()
            val cy = verts.map { it[1] }.average().toFloat()
            val cz = verts.map { it[2] }.average().toFloat()

            for (i in 0 until 5) {
                val a = floatArrayOf(cx, cy, cz)
                val b = verts[i]
                val c = verts[(i + 1) % 5]

                val ab = floatArrayOf(b[0] - a[0], b[1] - a[1], b[2] - a[2])
                val ac = floatArrayOf(c[0] - a[0], c[1] - a[1], c[2] - a[2])
                val normal = cross(ab, ac)

                val tcx = (a[0] + b[0] + c[0]) / 3f
                val tcy = (a[1] + b[1] + c[1]) / 3f
                val tcz = (a[2] + b[2] + c[2]) / 3f
                val dot = normal[0] * tcx + normal[1] * tcy + normal[2] * tcz

                // Определяем итоговую нормаль (наружу)
                val outNormal = if (dot >= 0) normal
                    else floatArrayOf(-normal[0], -normal[1], -normal[2])

                if (dot >= 0) {
                    faceVertices.addAll(a.toList())
                    faceVertices.addAll(b.toList())
                    faceVertices.addAll(c.toList())
                } else {
                    faceVertices.addAll(a.toList())
                    faceVertices.addAll(c.toList())
                    faceVertices.addAll(b.toList())
                }

                // Нормаль одинаковая для всех 3 вершин треугольника
                repeat(3) { faceNormals.addAll(outNormal.toList()) }

                faceColors.addAll(color.toList())
                faceColors.addAll(color.toList())
                faceColors.addAll(color.toList())
            }
        }

        return Triple(
            faceVertices.toFloatArray(),
            faceColors.toFloatArray(),
            faceNormals.toFloatArray()
        )
    }
```

Что нового: сохраняем нормаль каждого треугольника (`outNormal`). Если `dot < 0`, инвертируем нормаль (чтобы она смотрела наружу). Все 3 вершины треугольника получают одну и ту же нормаль — грань плоская.

**2. Добавляем буфер нормалей и новые хэндлы:**

```kotlin
    private val NORMALS_PER_VERTEX = 3  // x, y, z компоненты нормали

    // Меняем шейдеры: cube_* → pent_* (новые поддерживают нормали и освещение)
    private var _program: Int = createProgramGLES30(res, R.raw.pent_vert, R.raw.pent_frag)

    // Новые хэндлы
    private var _modelMatrixHandle: Int = 0
    private var _normalHandle: Int = 0
    private var _lightDirHandle: Int = 0

    private val _normalBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(normals.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(normals); position(0) }
```

Обрати внимание: `_program` теперь использует `R.raw.pent_vert` и `R.raw.pent_frag` вместо `cube_*`.

**3. Обновляем метод `draw`** — передаём нормали, Model-матрицу и направление света. Вот полная версия `draw` после изменений (новые строки помечены `// NEW`):

```kotlin
    fun draw(
        state: PentagonalIcositetrahedronState,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
    ) {
        // 1. Model-матрица: масштаб + поворот
        Matrix.setIdentityM(_modelMatrix, 0)
        Matrix.scaleM(_modelMatrix, 0, state.scale, state.scale, state.scale)
        val scaledModel = _modelMatrix.copyOf()
        Matrix.multiplyMM(_modelMatrix, 0, scaledModel, 0, state.rotationMatrix, 0)

        // 2. MVP = Projection x View x Model
        Matrix.multiplyMM(_viewModelMatrix, 0, viewMatrix, 0, _modelMatrix, 0)
        Matrix.multiplyMM(_mvpMatrix, 0, projectionMatrix, 0, _viewModelMatrix, 0)

        // 3. Активируем шейдер
        GLES30.glUseProgram(_program)

        // 4. Передаём данные в шейдер
        _positionHandle = GLES30.glGetAttribLocation(_program, "vPosition")
        GLES30.glEnableVertexAttribArray(_positionHandle)
        GLES30.glVertexAttribPointer(
            _positionHandle, COORDS_PER_VERTEX, GLES30.GL_FLOAT,
            false, COORDS_PER_VERTEX * Float.SIZE_BYTES, _vertexBuffer,
        )

        _colorHandle = GLES30.glGetAttribLocation(_program, "vColor")
        GLES30.glEnableVertexAttribArray(_colorHandle)
        GLES30.glVertexAttribPointer(
            _colorHandle, COLORS_PER_VERTEX, GLES30.GL_FLOAT,
            false, COLORS_PER_VERTEX * Float.SIZE_BYTES, _colorBuffer,
        )

        _mvpMatrixHandle = GLES30.glGetUniformLocation(_program, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(_mvpMatrixHandle, 1, false, _mvpMatrix, 0)

        // NEW: Model-матрица (для трансформации нормалей в мировое пространство)
        _modelMatrixHandle = GLES30.glGetUniformLocation(_program, "uModelMatrix")
        GLES30.glUniformMatrix4fv(_modelMatrixHandle, 1, false, _modelMatrix, 0)

        // NEW: Направление света (сверху-справа-спереди)
        _lightDirHandle = GLES30.glGetUniformLocation(_program, "uLightDirection")
        GLES30.glUniform3f(_lightDirHandle, 0.5f, 1.0f, 0.8f)

        // NEW: Нормали
        _normalHandle = GLES30.glGetAttribLocation(_program, "vNormal")
        GLES30.glEnableVertexAttribArray(_normalHandle)
        GLES30.glVertexAttribPointer(
            _normalHandle, NORMALS_PER_VERTEX, GLES30.GL_FLOAT,
            false, NORMALS_PER_VERTEX * Float.SIZE_BYTES, _normalBuffer,
        )

        // 5. Рисуем
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        GLES30.glDisableVertexAttribArray(_positionHandle)
        GLES30.glDisableVertexAttribArray(_colorHandle)
        GLES30.glDisableVertexAttribArray(_normalHandle)  // NEW
    }
```

Свет направлен как `(0.5, 1.0, 0.8)` — сверху-справа-спереди. Вектор не обязан быть нормализованным — шейдер сам нормализует через `normalize()`. Можешь поэкспериментировать с направлением: `(0, 1, 0)` — строго сверху, `(1, 0, 0)` — справа.

## 5.3. Проверка

Запусти — теперь грани затеняются! Грани, повёрнутые к свету, яркие. Грани в тени — тёмные. Поверни фигуру и увидь, как тени перемещаются.

---

# Шаг 6. Добавляем прозрачность

**Цель:** грани полупрозрачные — видно фигуру «насквозь».

## 6.1. GLRenderer — включаем блендинг

В `PentagonalIcositetrahedronGLRenderer`, в методе `onSurfaceCreated`, после `glEnable(GL_DEPTH_TEST)` добавь:

```kotlin
        // Включаем прозрачность
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
```

`glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)` — стандартная формула смешивания:
`итоговый_цвет = цвет_фрагмента * alpha + цвет_фона * (1 - alpha)`.

## 6.2. Renderer — уменьшаем alpha в палитре

В `generateGeometry`, замени последнее значение (alpha) с `1.0f` на `0.75f` во **всех 24 строках** палитры `faceColorPalette`. Формат каждой строки: `floatArrayOf(R, G, B, A)` — меняем A:

```kotlin
        val faceColorPalette = arrayOf(
            floatArrayOf(0.95f, 0.25f, 0.20f, 0.75f),  // было 1.0f → стало 0.75f
            floatArrayOf(0.20f, 0.80f, 0.30f, 0.75f),
            floatArrayOf(0.20f, 0.40f, 0.95f, 0.75f),
            // ... и так для всех остальных 21 строки
        )
```

`0.75` — 75% непрозрачности. Попробуй разные значения: `0.5` — сильно прозрачный, `0.9` — едва заметно. Можно использовать Find & Replace в IDE: заменить `, 1.0f)` на `, 0.75f)` внутри палитры.

## 6.3. GLRenderer — отключаем запись в буфер глубины для прозрачных граней

С включённым depth test есть проблема: ближняя полупрозрачная грань запишется в буфер глубины, и дальняя грань не отрисуется (хотя должна быть видна сквозь неё).

Простое решение — отключить **запись** в буфер глубины (чтение оставить):

В `onSurfaceCreated`, после включения блендинга:

```kotlin
        // Depth test читает, но не пишет — чтобы дальние грани просвечивали
        GLES30.glDepthMask(false)
```

**Компромисс:** без записи в буфер глубины порядок отрисовки граней зависит от порядка в массиве вершин, а не от расстояния до камеры. Иногда дальняя грань отрисуется поверх ближней, и смешивание цветов будет неточным. Визуально это проявляется как лёгкие артефакты при вращении. Для учебного проекта это приемлемо. Идеальное решение — сортировка граней по расстоянию до камеры каждый кадр (Order-Independent Transparency), но это значительно сложнее.

## 6.4. Проверка

Запусти — фигура полупрозрачная! Видно грани за гранями. Повращай и пронаблюдай эффект.

---

# Что ты узнал

| Шаг | Тема | Ключевая концепция |
|-----|------|-------------------|
| 1 | OpenGL Pipeline | Вершины → шейдеры → буферы → `glDrawArrays` → пиксели |
| 1 | Триангуляция | Разбиение пятиугольников на треугольники веером от центроида |
| 1 | Нормали | Проверка направления через cross product + dot product |
| 1 | MVP-матрица | Model (объект) x View (камера) x Projection (перспектива) |
| 2 | Compose + OpenGL | `AndroidView` встраивает GLSurfaceView, `pointerInput` ловит жесты |
| 3 | Анимация | Корутина с `delay(16)`, friction-затухание, cancel при перехвате |
| 4 | Multitouch | Определение pinch через расстояние между двумя пальцами |
| 5 | Диффузное освещение | Модель Ламберта: яркость = cos(угол между нормалью и светом) |
| 6 | Прозрачность | `glEnable(GL_BLEND)` + alpha-канал в цветах |

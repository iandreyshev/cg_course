# Лента Мёбиуса — пошаговый гайд

Реализуем визуализацию **ленты Мёбиуса** — неориентируемой поверхности с одной стороной и одним краем. В отличие от многогранника (пентагональный икоситетраэдр), здесь геометрия строится **параметрически** — формулами, а не списком вершин.

| Шаг | Что добавляем | Результат |
|-----|--------------|-----------|
| 1 | Статическая лента на экране | Разноцветная лента Мёбиуса, без взаимодействия |
| 2 | Вращение пальцем (drag) | Палец вращает фигуру |
| 3 | Инерция и масштабирование (fling + pinch) | Лента вращается после свайпа, два пальца масштабируют |
| 4 | Освещение | Поверхность затеняется в зависимости от угла к свету |

> **Про двусторонний рендеринг:** лента Мёбиуса неориентируемая — у неё нет «внутренней» и «внешней» стороны. Поэтому мы **не включаем** `GL_CULL_FACE` на протяжении всего гайда. OpenGL рисует все треугольники с обеих сторон. А в шейдере освещения (шаг 4) используем `abs(dot(N, L))` вместо `max(dot(N, L), 0)`, чтобы обе стороны освещались одинаково.

Структура файлов:

```
modules/lab4/src/main/java/ru/iandreyshev/cglab4/
└── mobiusstrip/
    ├── presentation/
    │   ├── MobiusStripState.kt
    │   └── MobiusStripViewModel.kt
    └── ui/
        ├── MobiusStripScreen.kt
        ├── MobiusStripGLSurfaceView.kt
        ├── MobiusStripGLRenderer.kt
        └── MobiusStripRenderer.kt

modules/lab4/src/main/res/raw/
    ├── mobius_vert.vert   (добавим на шаге 4)
    └── mobius_frag.frag   (добавим на шаге 4)
```

---

# Шаг 1. Рисуем статическую ленту

**Цель:** на экране появляется разноцветная лента Мёбиуса. Никакого взаимодействия.

## 1.1. Навигация — подключаем экран к приложению

**Файл: `app/.../navigation/Screens.kt`** — добавь маршрут в `Lab4`:

```kotlin
object Lab4 {
    // ... существующие маршруты ...

    @Serializable
    object MobiusStrip
}
```

**Файл: `app/.../navigation/MainNavHost.kt`** — добавь импорт и composable:

```kotlin
import ru.iandreyshev.cglab4.mobiusstrip.ui.MobiusStripScreen
```

В `buildLab4Navigation`:

```kotlin
composable<Lab4.MobiusStrip> {
    MobiusStripScreen()
}
```

В `buildMenuNavigation`, внутри `lab(4, ...)`:

```kotlin
task(
    "Лента Мёбиуса",
    "Неориентируемая поверхность с одной стороной",
    Lab4.MobiusStrip
)
```

## 1.2. State — состояние фигуры

Создай файл `presentation/MobiusStripState.kt`:

```kotlin
package ru.iandreyshev.cglab4.mobiusstrip.presentation

import android.opengl.Matrix

data class MobiusStripState(
    val rotationMatrix: FloatArray = FloatArray(16).apply {
        Matrix.setIdentityM(this, 0)
    },
    val scale: Float = 0.7f,
)
```

Масштаб `0.7f` — лента Мёбиуса компактнее многогранника, можно начать крупнее.

## 1.3. ViewModel (минимальная версия)

Создай файл `presentation/MobiusStripViewModel.kt`:

```kotlin
package ru.iandreyshev.cglab4.mobiusstrip.presentation

import ru.iandreyshev.core.BaseViewModel

class MobiusStripViewModel : BaseViewModel<MobiusStripState, Any>(
    initialState = MobiusStripState()
)
```

## 1.4. Screen (минимальная версия — без жестов)

Создай файл `ui/MobiusStripScreen.kt`:

```kotlin
package ru.iandreyshev.cglab4.mobiusstrip.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.iandreyshev.cglab4.mobiusstrip.presentation.MobiusStripViewModel

@Composable
fun MobiusStripScreen(
    viewModel: MobiusStripViewModel = viewModel { MobiusStripViewModel() }
) {
    val state by viewModel.state

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            MobiusStripGLSurfaceView(it)
        },
        update = { view ->
            view.updateState(state)
        }
    )
}
```

## 1.5. GLSurfaceView

Создай файл `ui/MobiusStripGLSurfaceView.kt`:

```kotlin
package ru.iandreyshev.cglab4.mobiusstrip.ui

import android.content.Context
import android.opengl.GLSurfaceView
import ru.iandreyshev.cglab4.mobiusstrip.presentation.MobiusStripState

class MobiusStripGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val _renderer: MobiusStripGLRenderer

    init {
        setEGLContextClientVersion(3)
        _renderer = MobiusStripGLRenderer(resources)
        setRenderer(_renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun updateState(state: MobiusStripState) {
        _renderer.updateState(state)
        requestRender()
    }
}
```

## 1.6. GLRenderer — настройка OpenGL-окружения

Создай файл `ui/MobiusStripGLRenderer.kt`:

```kotlin
package ru.iandreyshev.cglab4.mobiusstrip.ui

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import ru.iandreyshev.cglab4.mobiusstrip.presentation.MobiusStripState
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MobiusStripGLRenderer(
    private val resources: Resources
) : GLSurfaceView.Renderer {

    private val _projectionMatrix = FloatArray(16)
    private val _viewMatrix = FloatArray(16)

    private lateinit var _drawable: MobiusStripRenderer

    @Volatile
    private var _state = MobiusStripState()

    init {
        Matrix.setLookAtM(
            _viewMatrix, 0,
            0f, 0f, 5f,   // eye
            0f, 0f, 0f,   // center
            0f, 1f, 0f,   // up
        )
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES30.glClearColor(0.02f, 0.02f, 0.05f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        // НЕ включаем GL_CULL_FACE — лента Мёбиуса неориентируемая
        _drawable = MobiusStripRenderer(resources)
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

    fun updateState(state: MobiusStripState) {
        _state = state
    }
}
```

## 1.7. Renderer — генерация геометрии и отрисовка

Это ключевой файл. Создай `ui/MobiusStripRenderer.kt`.

### Теория: параметрическая поверхность

В отличие от многогранника, где координаты вершин заданы таблицей, лента Мёбиуса описывается **формулами** с двумя параметрами:

- **u** ∈ [0, 2π] — «длинный» параметр, идёт вдоль ленты по кругу
- **v** ∈ [-0.5, 0.5] — «короткий» параметр, идёт поперёк ленты (от одного края к другому)

Формулы ленты Мёбиуса:

```
x(u, v) = (1 + v · cos(u/2)) · cos(u)
y(u, v) = (1 + v · cos(u/2)) · sin(u)
z(u, v) =      v · sin(u/2)
```

Ключевой трюк — `u/2` в аргументах cos и sin: когда u проходит полный круг (0 → 2π), поперечное сечение поворачивается только на π (180°). Из-за этого лента «перекручивается» на пол-оборота — и становится неориентируемой.

```
     u=0         u=π          u=2π (=0)
   ┌─────┐     ┌─────┐      ┌─────┐
   │  v  │     │ ╲   │      │     │  ← сечение перевёрнуто!
   │  ↕  │     │  ╲  │      │  ↕  │     v теперь идёт в другую
   └─────┘     └──╲──┘      └─────┘     сторону → одна сторона
```

### Тесселяция: сетка треугольников

Чтобы OpenGL нарисовал поверхность, нужно разбить её на треугольники. Стратегия:

1. Делим диапазон u на `uSteps` частей, v на `vSteps` частей
2. Получаем сетку (uSteps+1) × (vSteps+1) вершин
3. Каждую ячейку сетки разбиваем на 2 треугольника

```
v ↑
  │  (i,j+1)──(i+1,j+1)
  │    │  ╲       │
  │    │    ╲     │
  │    │      ╲   │
  │  (i,j)───(i+1,j)
  └──────────────────→ u
```

Каждая ячейка даёт 2 треугольника:
- Треугольник 1: (i,j), (i+1,j), (i+1,j+1)
- Треугольник 2: (i,j), (i+1,j+1), (i,j+1)

Итого: `uSteps × vSteps × 2` треугольников = `uSteps × vSteps × 6` вершин.

Рекомендуемые значения: `uSteps = 80`, `vSteps = 20` — даёт 3200 треугольников. Достаточно гладко и не тормозит.

### Вычисление нормалей

Для освещения (шаг 4) нужны нормали — перпендикуляры к поверхности. Для параметрической поверхности нормаль вычисляется как **векторное произведение частных производных**:

```
∂P/∂u — касательный вектор вдоль u (вычисляем численно)
∂P/∂v — касательный вектор вдоль v (вычисляем численно)
N = (∂P/∂u) × (∂P/∂v) — нормаль
```

Численная производная (конечные разности):
```
∂P/∂u ≈ (P(u + ε, v) - P(u - ε, v)) / (2ε)
```

Здесь используется **центральная разность** — берём значение чуть правее и чуть левее и делим на удвоённый шаг. Это точнее, чем `(P(u+ε) - P(u)) / ε` (односторонняя разность).

> **Важная особенность:** лента Мёбиуса неориентируемая — у неё нет «внутренней» и «внешней» стороны. Нормаль в точке u=0 и нормаль после полного обхода (u=2π) смотрят в **противоположные стороны** в одной и той же точке! Поэтому мы не используем `GL_CULL_FACE` и применяем `abs()` в шейдере освещения.

### Раскраска

Цвет ленты можно задать градиентом от параметра u — плавный переход вдоль ленты:

```kotlin
// Hue-градиент: u ∈ [0, 2π] → hue ∈ [0, 1]
val hue = u / (2 * PI)
// HSV → RGB конвертация, или просто:
val r = (sin(hue * 2π) * 0.5 + 0.5)
val g = (sin(hue * 2π + 2π/3) * 0.5 + 0.5)
val b = (sin(hue * 2π + 4π/3) * 0.5 + 0.5)
```

Это даёт радужный градиент вдоль ленты — красиво подчёркивает перекрут.

### Код Renderer

```kotlin
package ru.iandreyshev.cglab4.mobiusstrip.ui

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.Matrix
import ru.iandreyshev.cglab4.R
import ru.iandreyshev.cglab4.mobiusstrip.presentation.MobiusStripState
import ru.iandreyshev.core.createProgramGLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

private const val COORDS_PER_VERTEX = 3
private const val COLORS_PER_VERTEX = 4

class MobiusStripRenderer(res: Resources) {

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

### Функция вычисления точки на поверхности

```kotlin
    // Вычисляет точку на ленте Мёбиуса по параметрам u и v
    private fun mobiusPoint(u: Float, v: Float): FloatArray {
        val halfU = u / 2f
        val cosU = cos(u)
        val sinU = sin(u)
        val cosHalfU = cos(halfU)
        val sinHalfU = sin(halfU)

        return floatArrayOf(
            (1f + v * cosHalfU) * cosU,
            (1f + v * cosHalfU) * sinU,
            v * sinHalfU
        )
    }
```

Вынесена в отдельный метод, потому что используется и при генерации вершин, и при численном вычислении нормалей.

### Генерация геометрии

```kotlin
    private fun generateGeometry(): Pair<FloatArray, FloatArray> {
        val uSteps = 80   // делений вдоль ленты
        val vSteps = 20   // делений поперёк ленты
        val PI2 = (2.0 * Math.PI).toFloat()

        val faceVertices = mutableListOf<Float>()
        val faceColors = mutableListOf<Float>()

        for (i in 0 until uSteps) {
            for (j in 0 until vSteps) {
                // Параметры для 4 углов ячейки сетки
                val u0 = PI2 * i / uSteps
                val u1 = PI2 * (i + 1) / uSteps
                val v0 = -0.5f + j.toFloat() / vSteps
                val v1 = -0.5f + (j + 1).toFloat() / vSteps

                // 4 вершины ячейки
                val p00 = mobiusPoint(u0, v0)
                val p10 = mobiusPoint(u1, v0)
                val p11 = mobiusPoint(u1, v1)
                val p01 = mobiusPoint(u0, v1)

                // Цвет — градиент по u (радуга вдоль ленты)
                val hue = i.toFloat() / uSteps
                val r = (sin(hue * PI2) * 0.4f + 0.6f)
                val g = (sin(hue * PI2 + PI2 / 3f) * 0.4f + 0.6f)
                val b = (sin(hue * PI2 + 2f * PI2 / 3f) * 0.4f + 0.6f)
                val color = floatArrayOf(r, g, b, 1.0f)

                // Треугольник 1: p00 → p10 → p11
                faceVertices.addAll(p00.toList())
                faceVertices.addAll(p10.toList())
                faceVertices.addAll(p11.toList())

                // Треугольник 2: p00 → p11 → p01
                faceVertices.addAll(p00.toList())
                faceVertices.addAll(p11.toList())
                faceVertices.addAll(p01.toList())

                // Одинаковый цвет для всех 6 вершин ячейки
                repeat(6) { faceColors.addAll(color.toList()) }
            }
        }

        return Pair(faceVertices.toFloatArray(), faceColors.toFloatArray())
    }
```

Обрати внимание: здесь **нет проверки нормалей**, как в многограннике. Для ленты Мёбиуса нет понятия «наружу» — поверхность неориентируемая. Порядок вершин треугольников фиксирован по обходу сетки.

### Буферы и отрисовка

```kotlin
    private var _program: Int = createProgramGLES30(res, R.raw.cube_vert, R.raw.cube_frag)

    private val _modelMatrix = FloatArray(16)
    private val _viewModelMatrix = FloatArray(16)
    private val _mvpMatrix = FloatArray(16)

    private var _mvpMatrixHandle: Int = 0
    private var _positionHandle: Int = 0
    private var _colorHandle: Int = 0

    private val _vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(vertices.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(vertices)
            position(0)
        }

    private val _colorBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(colors.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(colors); position(0) }

    fun draw(
        state: MobiusStripState,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
    ) {
        Matrix.setIdentityM(_modelMatrix, 0)
        Matrix.scaleM(_modelMatrix, 0, state.scale, state.scale, state.scale)
        val scaledModel = _modelMatrix.copyOf()
        Matrix.multiplyMM(_modelMatrix, 0, scaledModel, 0, state.rotationMatrix, 0)

        Matrix.multiplyMM(_viewModelMatrix, 0, viewMatrix, 0, _modelMatrix, 0)
        Matrix.multiplyMM(_mvpMatrix, 0, projectionMatrix, 0, _viewModelMatrix, 0)

        GLES30.glUseProgram(_program)

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

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        GLES30.glDisableVertexAttribArray(_positionHandle)
        GLES30.glDisableVertexAttribArray(_colorHandle)
    }
}
```

На шаге 1 используем шейдеры куба — они подходят для цветной геометрии без освещения. Свои шейдеры с нормалями добавим на шаге 4.

## 1.8. Проверка

```bash
./gradlew assembleDebug
```

Запусти — на экране разноцветная лента Мёбиуса. Не вращается, не масштабируется.

---

# Шаг 2. Добавляем вращение (drag)

**Цель:** провёл пальцем — лента повернулась.

Этот шаг **идентичен** шагу 2 + 2.5 из гайда пентагонального икоситетраэдра. Различия только в именах классов.

## 2.1. ViewModel — добавляем `onDrag`

В `MobiusStripViewModel` добавь:

```kotlin
import android.opengl.Matrix
import androidx.compose.ui.geometry.Offset

// Внутри класса:
fun onDrag(dragAmount: Offset) {
    updateState {
        copy(rotationMatrix = applyRotationDelta(rotationMatrix, dragAmount))
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
```

Инкрементальное вращение через матрицу — свайп вправо всегда выглядит как поворот вправо, независимо от текущей ориентации. Подробное объяснение — в шаге 2.5 гайда пентагонального икоситетраэдра.

## 2.2. Screen — добавляем обработку жеста

Замени содержимое `MobiusStripScreen`:

```kotlin
package ru.iandreyshev.cglab4.mobiusstrip.ui

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
import ru.iandreyshev.cglab4.mobiusstrip.presentation.MobiusStripViewModel

@Composable
fun MobiusStripScreen(
    viewModel: MobiusStripViewModel = viewModel { MobiusStripViewModel() }
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
        factory = { MobiusStripGLSurfaceView(it) },
        update = { view -> view.updateState(state) }
    )
}
```

## 2.3. Проверка

Палец вращает ленту. После отпускания — мгновенная остановка.

---

# Шаг 3. Инерция и масштабирование (fling + pinch)

**Цель:** после быстрого свайпа лента продолжает вращаться. Два пальца увеличивают/уменьшают.

## 3.1. State — добавляем скорость

```kotlin
import androidx.compose.ui.geometry.Offset

data class MobiusStripState(
    val rotationMatrix: FloatArray = FloatArray(16).apply {
        android.opengl.Matrix.setIdentityM(this, 0)
    },
    val velocity: Offset = Offset.Zero,
    val scale: Float = 0.7f,
)
```

## 3.2. ViewModel — полная версия с fling и pinch

```kotlin
package ru.iandreyshev.cglab4.mobiusstrip.presentation

import android.opengl.Matrix
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.iandreyshev.core.BaseViewModel
import kotlin.math.abs

class MobiusStripViewModel : BaseViewModel<MobiusStripState, Any>(
    initialState = MobiusStripState()
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

    fun onScale(scaleFactor: Float) {
        updateState {
            copy(scale = (scale * scaleFactor).coerceIn(0.1f, 5f))
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

## 3.3. GLSurfaceView — непрерывный рендеринг

В `MobiusStripGLSurfaceView` замени `RENDERMODE_WHEN_DIRTY` на `RENDERMODE_CONTINUOUSLY`.

## 3.4. Screen — добавляем VelocityTracker и pinch

Замени блок `.pointerInput(Unit) { ... }` финальной версией:

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

Добавь импорты:

```kotlin
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlin.math.sqrt
```

## 3.5. Проверка

Свайпни — лента продолжит вращаться. Разведи/сведи два пальца — масштабирование.

---

# Шаг 4. Добавляем освещение

**Цель:** поверхность ленты затеняется — видна объёмность.

## 4.1. Создаём новые шейдеры

Лента Мёбиуса **неориентируемая** — нормали на разных участках смотрят в разные стороны. Стандартный Ламберт покажет одну сторону тёмной. Решение — использовать **абсолютное значение** скалярного произведения: `abs(dot(N, L))`. Это освещает поверхность одинаково с обеих сторон.

Создай файл `src/main/res/raw/mobius_vert.vert`:

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
    fNormal = mat3(uModelMatrix) * vNormal;
}
```

Создай файл `src/main/res/raw/mobius_frag.frag`:

```glsl
precision mediump float;

uniform vec3 uLightDirection;

varying vec4 fColor;
varying vec3 fNormal;

void main() {
    vec3 normal = normalize(fNormal);
    vec3 lightDir = normalize(uLightDirection);

    // abs() — освещаем обе стороны одинаково (для неориентируемой поверхности)
    float diffuse = abs(dot(normal, lightDir));

    float brightness = 0.3 + 0.7 * diffuse;

    gl_FragColor = vec4(fColor.rgb * brightness, fColor.a);
}
```

Ключевое отличие от шейдера пентагонального икоситетраэдра: `abs(dot(...))` вместо `max(dot(...), 0.0)`. Для обычного объекта `max` отсекает свет на теневой стороне. Для ленты Мёбиуса нормали «меняют сторону» — с `max` половина ленты была бы всегда тёмной. `abs` решает это.

## 4.2. Renderer — добавляем нормали

Нужно вычислить нормаль в каждой точке поверхности. Для параметрической поверхности нормаль — это векторное произведение частных производных по u и v. Используем **численное дифференцирование** (конечные разности).

**1. Добавляем метод вычисления нормали:**

```kotlin
    private fun mobiusNormal(u: Float, v: Float): FloatArray {
        val eps = 0.001f

        // Частная производная по u (центральная разность)
        val pu1 = mobiusPoint(u + eps, v)
        val pu0 = mobiusPoint(u - eps, v)
        val dPdu = floatArrayOf(
            (pu1[0] - pu0[0]) / (2 * eps),
            (pu1[1] - pu0[1]) / (2 * eps),
            (pu1[2] - pu0[2]) / (2 * eps),
        )

        // Частная производная по v
        val pv1 = mobiusPoint(u, v + eps)
        val pv0 = mobiusPoint(u, v - eps)
        val dPdv = floatArrayOf(
            (pv1[0] - pv0[0]) / (2 * eps),
            (pv1[1] - pv0[1]) / (2 * eps),
            (pv1[2] - pv0[2]) / (2 * eps),
        )

        // Нормаль = dP/du × dP/dv
        return cross(dPdu, dPdv)
    }

    private fun cross(a: FloatArray, b: FloatArray) = floatArrayOf(
        a[1] * b[2] - a[2] * b[1],
        a[2] * b[0] - a[0] * b[2],
        a[0] * b[1] - a[1] * b[0]
    )
```

`eps = 0.001f` — шаг для численной производной. Слишком большой — неточные нормали, слишком маленький — ошибки округления float. `0.001` — хороший баланс.

**2. Добавляем массив нормалей в `init` и `generateGeometry`.**

Меняем `generateGeometry` на `Triple`:

```kotlin
    private val normals: FloatArray

    init {
        val (verts, cols, norms) = generateGeometry()
        vertices = verts
        colors = cols
        normals = norms
        vertexCount = vertices.size / COORDS_PER_VERTEX
    }

    private fun generateGeometry(): Triple<FloatArray, FloatArray, FloatArray> {
        // ... (существующий код генерации) ...
        val faceNormals = mutableListOf<Float>()

        // Внутри двойного цикла, после создания 6 вершин ячейки:
        // Нормали в 4 углах ячейки
        val n00 = mobiusNormal(u0, v0)
        val n10 = mobiusNormal(u1, v0)
        val n11 = mobiusNormal(u1, v1)
        val n01 = mobiusNormal(u0, v1)

        // Треугольник 1: p00 → p10 → p11
        faceNormals.addAll(n00.toList())
        faceNormals.addAll(n10.toList())
        faceNormals.addAll(n11.toList())

        // Треугольник 2: p00 → p11 → p01
        faceNormals.addAll(n00.toList())
        faceNormals.addAll(n11.toList())
        faceNormals.addAll(n01.toList())

        // ...
        return Triple(
            faceVertices.toFloatArray(),
            faceColors.toFloatArray(),
            faceNormals.toFloatArray()
        )
    }
```

Каждая вершина получает свою нормаль (интерполированную) — это даёт **гладкое** затенение (Гуро). В отличие от многогранника, где все вершины треугольника получали одну нормаль (плоское затенение).

**3. Добавляем буфер нормалей и обновляем `draw`:**

```kotlin
    private val NORMALS_PER_VERTEX = 3

    private var _program: Int = createProgramGLES30(res, R.raw.mobius_vert, R.raw.mobius_frag)

    private var _modelMatrixHandle: Int = 0
    private var _normalHandle: Int = 0
    private var _lightDirHandle: Int = 0

    private val _normalBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(normals.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(normals); position(0) }
```

В методе `draw`, после передачи MVP-матрицы, добавь:

```kotlin
        // Model-матрица для трансформации нормалей
        _modelMatrixHandle = GLES30.glGetUniformLocation(_program, "uModelMatrix")
        GLES30.glUniformMatrix4fv(_modelMatrixHandle, 1, false, _modelMatrix, 0)

        // Направление света
        _lightDirHandle = GLES30.glGetUniformLocation(_program, "uLightDirection")
        GLES30.glUniform3f(_lightDirHandle, 0.5f, 1.0f, 0.8f)

        // Нормали
        _normalHandle = GLES30.glGetAttribLocation(_program, "vNormal")
        GLES30.glEnableVertexAttribArray(_normalHandle)
        GLES30.glVertexAttribPointer(
            _normalHandle, NORMALS_PER_VERTEX, GLES30.GL_FLOAT,
            false, NORMALS_PER_VERTEX * Float.SIZE_BYTES, _normalBuffer,
        )
```

И в конце `draw` не забудь:

```kotlin
        GLES30.glDisableVertexAttribArray(_normalHandle)
```

## 4.3. Проверка

Лента выглядит объёмной — видны светлые и тёмные области. Поверни и убедись, что обе стороны освещены (благодаря `abs()` в шейдере). Лента видна с любой стороны, без дыр и артефактов.

### (Опционально) Полупрозрачность

Если хочется добавить прозрачность, замени alpha в палитре цветов (в `generateGeometry`) на, например, `0.85f` и добавь в `onSurfaceCreated`:

```kotlin
GLES30.glEnable(GLES30.GL_BLEND)
GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
```

Для ленты Мёбиуса двухпроходная схема прозрачности (как для выпуклого многогранника) **не работает** — поверхность самопересекается визуально. Простой `GL_BLEND` без сортировки даёт приемлемый результат для тонкой ленты.

---

# Дополнительные улучшения

Ниже — возможные улучшения. Решай сам, что делать.

## 1. Зеркальное освещение (Blinn-Phong)

Добавить к диффузному Ламберту **зеркальный блик** (specular highlight). Это яркое пятно на поверхности, как блик на полированном металле.

Формула Blinn-Phong:
```glsl
vec3 viewDir = normalize(uCameraPosition - fWorldPos);
vec3 halfDir = normalize(lightDir + viewDir);
float spec = pow(max(dot(normal, halfDir), 0.0), shininess);
float brightness = ambient + diffuse_coeff * diffuse + specular_coeff * spec;
```

Что нужно:
- Передать позицию камеры (`uCameraPosition`) и позицию фрагмента (`fWorldPos`) в шейдер
- Добавить `varying vec3 fWorldPos` в вершинный шейдер: `fWorldPos = vec3(uModelMatrix * vPosition)`
- В фрагментном шейдере вычислить half-вектор и specular
- `shininess` — 32 или 64 для умеренного блика

## 2. Точечный источник света

Заменить направленный свет (`uLightDirection`) на точечный (`uLightPosition`). Свет зависит от расстояния (затухание). Подробности — в шаге 7 гайда пентагонального икоситетраэдра.

## 3. Анимация вращения источника света

Источник света медленно вращается вокруг ленты. Тени перемещаются в реальном времени.

Что нужно:
- Добавить `lightAngle: Float` в State
- Корутина в ViewModel каждые 16 мс увеличивает угол
- Позиция источника: `(cos(angle) * R, Y, sin(angle) * R)`

## 4. Настраиваемые параметры через UI

Добавить слайдеры в Compose UI для изменения параметров в реальном времени:
- **Ширина ленты** (`v` от `-width/2` до `+width/2`)
- **Количество полу-поворотов** (обычная лента = 1, тройной перекрут = 3)
- **Детализация** (uSteps, vSteps)

Для количества полу-поворотов — в формуле `u/2` заменить на `u * n / 2`, где n — число полу-поворотов:
```
x(u, v) = (1 + v · cos(n·u/2)) · cos(u)
y(u, v) = (1 + v · cos(n·u/2)) · sin(u)
z(u, v) =      v · sin(n·u/2)
```

При n=1 — обычная лента Мёбиуса, n=2 — полный поворот (обычная лента, не Мёбиус), n=3 — тройной перекрут.

> **Сложность:** при изменении параметров нужно **пересоздавать геометрию** (вершины, нормали, буферы). Это значит, что `MobiusStripRenderer` либо перестраивается целиком, либо нужен метод `rebuild()`.

## 5. Wireframe (каркасный режим)

Рисовать рёбра сетки поверх залитых полигонов. Два способа:

**Способ А (простой):** рисовать дважды — сначала `GL_TRIANGLES`, потом `GL_LINE_STRIP` по тем же вершинам.

**Способ Б (через шейдер):** передавать барицентрические координаты в шейдер и подсвечивать пиксели около рёбер.

## 6. Текстурирование

Наложить текстуру на ленту. Координаты текстуры: `(u / 2π, v + 0.5)`. Интересный эффект: текстура с текстом «перевернётся» после обхода ленты — наглядная демонстрация неориентируемости.

---

# Что ты узнал

| Шаг | Тема | Ключевая концепция |
|-----|------|-------------------|
| 1 | Параметрические поверхности | Геометрия задаётся формулами x(u,v), y(u,v), z(u,v), а не таблицей вершин |
| 1 | Тесселяция | Разбиение непрерывной поверхности на сетку треугольников |
| 2-3 | Взаимодействие | Drag, fling, pinch — идентично любому 3D-объекту |
| 4 | Численное дифференцирование | Нормаль = (∂P/∂u) × (∂P/∂v) через конечные разности |
| 4 | Двустороннее освещение | `abs(dot(N, L))` вместо `max(dot(N, L), 0)` для неориентируемых поверхностей |
| 4 | Неориентируемость | Лента Мёбиуса — нет «внутренней» стороны → нельзя использовать cull face |

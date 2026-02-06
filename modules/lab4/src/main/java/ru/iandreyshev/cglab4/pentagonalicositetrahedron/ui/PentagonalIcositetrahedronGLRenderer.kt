package ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation.PentagonalIcositetrahedronState
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Реализация [GLSurfaceView.Renderer] для пентагонального икоситетраэдра.
 *
 * Отвечает за:
 * - Настройку OpenGL-окружения (цвет фона, тест глубины)
 * - Вычисление матриц вида (view) и проекции (projection)
 * - Делегирование отрисовки геометрии в [PentagonalIcositetrahedronRenderer]
 *
 * Состояние (поворот, масштаб) обновляется из UI-потока через [updateState],
 * а читается в GL-потоке при отрисовке каждого кадра. Поле [_state] помечено
 * как @Volatile для безопасного межпоточного доступа.
 */
class PentagonalIcositetrahedronGLRenderer(
    private val resources: Resources
) : GLSurfaceView.Renderer {

    // Матрица перспективной проекции (вычисляется при изменении размера поверхности)
    private val _projectionMatrix = FloatArray(16)
    // Матрица вида (камера) — задаётся один раз в init
    private val _viewMatrix = FloatArray(16)

    // Объект, рисующий геометрию пентагонального икоситетраэдра
    private lateinit var _drawable: PentagonalIcositetrahedronRenderer

    // Текущее состояние фигуры (поворот, масштаб). @Volatile — безопасный доступ из GL-потока
    @Volatile
    private var _state = PentagonalIcositetrahedronState()

    init {
        // Настраиваем матрицу вида: камера на расстоянии 5 по оси Z, смотрит в начало координат
        // eye = (0, 0, 5), center = (0, 0, 0), up = (0, 1, 0)
        Matrix.setLookAtM(
            _viewMatrix, 0,
            0f, 0f, 5f,   // Позиция камеры
            0f, 0f, 0f,   // Точка, на которую смотрит камера
            0f, 1f, 0f,   // Вектор «вверх»
        )
    }

    /**
     * Вызывается при создании GL-поверхности.
     * Настраиваем цвет очистки экрана (тёмно-синий фон) и включаем тест глубины
     * для корректного отображения граней (ближние перекрывают дальние).
     */
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Устанавливаем цвет фона: почти чёрный с лёгким синим оттенком
        GLES30.glClearColor(0.02f, 0.02f, 0.05f, 1.0f)
        // Включаем тест глубины — грани, находящиеся ближе к камере, перекрывают дальние
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        // Создаём объект-рендерер геометрии (компилирует шейдеры, генерирует вершины)
        _drawable = PentagonalIcositetrahedronRenderer(resources)
    }

    /**
     * Вызывается каждый кадр. Очищаем буферы цвета и глубины,
     * затем рисуем фигуру с текущим состоянием (поворот, масштаб).
     */
    override fun onDrawFrame(unused: GL10) {
        // Очищаем буфер цвета (заливаем фон) и буфер глубины
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        // Рисуем пентагональный икоситетраэдр с текущими матрицами вида и проекции
        _drawable.draw(_state, _viewMatrix, _projectionMatrix)
    }

    /**
     * Вызывается при изменении размера поверхности (например, при повороте экрана).
     * Обновляем viewport и пересчитываем матрицу перспективной проекции.
     */
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        // Устанавливаем область вывода на всю поверхность
        GLES30.glViewport(0, 0, width, height)

        // Вычисляем соотношение сторон для перспективной проекции
        val aspect = width.toFloat() / height.toFloat()
        // Перспективная проекция: угол обзора 45°, ближняя плоскость 0.1, дальняя 100
        Matrix.perspectiveM(_projectionMatrix, 0, 45f, aspect, 0.1f, 100f)
    }

    /**
     * Обновляет состояние фигуры. Вызывается из UI-потока (Compose),
     * поэтому поле _state помечено @Volatile.
     */
    fun updateState(state: PentagonalIcositetrahedronState) {
        _state = state
    }
}

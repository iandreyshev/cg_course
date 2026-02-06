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

// Количество координат на одну вершину (x, y, z)
private const val COORDS_PER_VERTEX = 3
// Количество компонент цвета на одну вершину (r, g, b, a)
private const val COLORS_PER_VERTEX = 4

/**
 * Пентагональный икоситетраэдр (Pentagonal Icositetrahedron).
 *
 * Каталановское тело — двойственное к курносому кубу (snub cube).
 * Свойства многогранника:
 * - 38 вершин
 * - 24 пятиугольных грани (неправильные пятиугольники)
 * - 60 рёбер
 * - Хиральное тело (существует в левой и правой формах, здесь — левая)
 *
 * Для рендеринга каждая пятиугольная грань разбивается на 5 треугольников
 * методом веерной триангуляции от центроида грани, итого 24 × 5 = 120 треугольников.
 *
 * Координаты вершин взяты из канонических координат DMcCooey
 * (описанная сфера радиусом ~1.36).
 */
class PentagonalIcositetrahedronRenderer(res: Resources) {

    // Массив координат всех вершин треугольников (x, y, z для каждой вершины)
    private val vertices: FloatArray
    // Массив цветов для каждой вершины (r, g, b, a)
    private val colors: FloatArray
    // Общее количество вершин для glDrawArrays
    private val vertexCount: Int

    init {
        // Генерируем геометрию: вершины и цвета для всех треугольников
        val (verts, cols) = generatePentagonalIcositetrahedron()
        vertices = verts
        colors = cols
        // Количество вершин = размер массива координат / 3 (x, y, z)
        vertexCount = vertices.size / COORDS_PER_VERTEX
    }

    /**
     * Генерирует массивы вершин и цветов для пентагонального икоситетраэдра.
     *
     * Алгоритм:
     * 1. Задаём 38 вершин многогранника в канонических координатах DMcCooey.
     * 2. Задаём 24 пятиугольных грани как массивы индексов вершин.
     * 3. Для каждой грани:
     *    a) Вычисляем центроид (среднее арифметическое координат вершин грани).
     *    b) Разбиваем пятиугольник на 5 треугольников «веером» от центроида.
     *    c) Для каждого треугольника проверяем направление нормали —
     *       если нормаль направлена внутрь, меняем порядок вершин на противоположный.
     *
     * @return Пара (координаты вершин, цвета вершин)
     */
    private fun generatePentagonalIcositetrahedron(): Pair<FloatArray, FloatArray> {
        // Канонические координаты DMcCooey (описанная сфера радиусом ~1.36)
        // C0..C3 — четыре константы, из которых строятся все 38 вершин
        val C0 = 0.2187966430f
        val C1 = 0.7401837414f
        val C2 = 1.0236561781f
        val C3 = 1.3614101519f

        // 38 вершин многогранника.
        // V0–V5: осевые вершины (лежат на осях координат)
        // V6–V13: «кубические» вершины (все координаты ±C1)
        // V14–V37: остальные вершины (комбинации C0, C1, C2)
        val polyVerts = arrayOf(
            floatArrayOf( C3,   0f,   0f),   // V0  осевая +X
            floatArrayOf(-C3,   0f,   0f),   // V1  осевая -X
            floatArrayOf(  0f,  C3,   0f),   // V2  осевая +Y
            floatArrayOf(  0f, -C3,   0f),   // V3  осевая -Y
            floatArrayOf(  0f,   0f,  C3),   // V4  осевая +Z
            floatArrayOf(  0f,   0f, -C3),   // V5  осевая -Z
            floatArrayOf( C1,  C1,  C1),     // V6  кубическая +++
            floatArrayOf( C1,  C1, -C1),     // V7  кубическая ++-
            floatArrayOf( C1, -C1,  C1),     // V8  кубическая +-+
            floatArrayOf( C1, -C1, -C1),     // V9  кубическая +--
            floatArrayOf(-C1,  C1,  C1),     // V10 кубическая -++
            floatArrayOf(-C1,  C1, -C1),     // V11 кубическая -+-
            floatArrayOf(-C1, -C1,  C1),     // V12 кубическая --+
            floatArrayOf(-C1, -C1, -C1),     // V13 кубическая ---
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

        // 24 пятиугольных грани. Каждая грань задана пятью индексами вершин
        // в порядке обхода против часовой стрелки (CCW) при взгляде снаружи
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

        // Палитра из 24 цветов — по одному уникальному цвету на каждую грань.
        // Формат: (R, G, B, A), значения от 0.0 до 1.0
        val faceColorPalette = arrayOf(
            floatArrayOf(0.95f, 0.25f, 0.20f, 1.0f),  // Красный
            floatArrayOf(0.20f, 0.80f, 0.30f, 1.0f),  // Зелёный
            floatArrayOf(0.20f, 0.40f, 0.95f, 1.0f),  // Синий
            floatArrayOf(0.95f, 0.85f, 0.20f, 1.0f),  // Жёлтый
            floatArrayOf(0.85f, 0.25f, 0.85f, 1.0f),  // Пурпурный
            floatArrayOf(0.20f, 0.85f, 0.85f, 1.0f),  // Голубой
            floatArrayOf(0.95f, 0.55f, 0.15f, 1.0f),  // Оранжевый
            floatArrayOf(0.55f, 0.20f, 0.90f, 1.0f),  // Фиолетовый
            floatArrayOf(0.25f, 0.65f, 0.25f, 1.0f),  // Тёмно-зелёный
            floatArrayOf(0.85f, 0.75f, 0.30f, 1.0f),  // Золотой
            floatArrayOf(0.25f, 0.75f, 0.75f, 1.0f),  // Бирюзовый
            floatArrayOf(0.85f, 0.30f, 0.55f, 1.0f),  // Розовый
            floatArrayOf(0.90f, 0.45f, 0.15f, 1.0f),  // Жжёный оранжевый
            floatArrayOf(0.40f, 0.15f, 0.85f, 1.0f),  // Индиго
            floatArrayOf(0.15f, 0.85f, 0.45f, 1.0f),  // Изумрудный
            floatArrayOf(0.85f, 0.85f, 0.50f, 1.0f),  // Светло-золотой
            floatArrayOf(0.50f, 0.85f, 0.85f, 1.0f),  // Светло-бирюзовый
            floatArrayOf(0.85f, 0.50f, 0.85f, 1.0f),  // Светло-пурпурный
            floatArrayOf(0.70f, 0.30f, 0.25f, 1.0f),  // Тёмно-красный
            floatArrayOf(0.30f, 0.65f, 0.30f, 1.0f),  // Лесной зелёный
            floatArrayOf(0.60f, 0.45f, 0.80f, 1.0f),  // Лавандовый
            floatArrayOf(0.80f, 0.60f, 0.40f, 1.0f),  // Песочный
            floatArrayOf(0.45f, 0.70f, 0.55f, 1.0f),  // Шалфейный
            floatArrayOf(0.75f, 0.40f, 0.40f, 1.0f),  // Лососёвый
        )

        // Списки для накопления координат вершин и цветов всех треугольников
        val faceVertices = mutableListOf<Float>()
        val faceColors = mutableListOf<Float>()

        // Обходим каждую из 24 пятиугольных граней
        faces.forEachIndexed { faceIndex, face ->
            val color = faceColorPalette[faceIndex]
            // Получаем координаты пяти вершин текущей грани
            val verts = face.map { polyVerts[it] }

            // Вычисляем центроид грани — среднее арифметическое координат пяти вершин
            val cx = verts.map { it[0] }.average().toFloat()
            val cy = verts.map { it[1] }.average().toFloat()
            val cz = verts.map { it[2] }.average().toFloat()

            // Веерная триангуляция: разбиваем пятиугольник на 5 треугольников.
            // Каждый треугольник: центроид + i-я вершина + (i+1)-я вершина грани
            for (i in 0 until 5) {
                val a = floatArrayOf(cx, cy, cz)   // Центроид грани
                val b = verts[i]                       // Текущая вершина грани
                val c = verts[(i + 1) % 5]             // Следующая вершина грани (циклически)

                // Проверяем направление нормали треугольника.
                // Вычисляем нормаль через векторное произведение рёбер AB и AC
                val ab = floatArrayOf(b[0] - a[0], b[1] - a[1], b[2] - a[2])
                val ac = floatArrayOf(c[0] - a[0], c[1] - a[1], c[2] - a[2])
                val normal = cross(ab, ac)

                // Центр треугольника — используется для проверки направления нормали
                val tcx = (a[0] + b[0] + c[0]) / 3f
                val tcy = (a[1] + b[1] + c[1]) / 3f
                val tcz = (a[2] + b[2] + c[2]) / 3f

                // Скалярное произведение нормали с вектором от начала координат к центру треугольника.
                // Если dot >= 0, нормаль направлена наружу — порядок вершин верный.
                // Если dot < 0, нормаль направлена внутрь — меняем порядок вершин (b ↔ c).
                val dot = normal[0] * tcx + normal[1] * tcy + normal[2] * tcz

                if (dot >= 0) {
                    // Нормаль направлена наружу — порядок вершин правильный
                    faceVertices.addAll(a.toList())
                    faceVertices.addAll(b.toList())
                    faceVertices.addAll(c.toList())
                } else {
                    // Нормаль направлена внутрь — меняем обход (b и c местами)
                    faceVertices.addAll(a.toList())
                    faceVertices.addAll(c.toList())
                    faceVertices.addAll(b.toList())
                }
                // Все три вершины треугольника получают одинаковый цвет грани
                faceColors.addAll(color.toList())
                faceColors.addAll(color.toList())
                faceColors.addAll(color.toList())
            }
        }

        return Pair(faceVertices.toFloatArray(), faceColors.toFloatArray())
    }

    /**
     * Вычисляет векторное произведение двух трёхмерных векторов.
     * Результат — вектор, перпендикулярный обоим входным векторам (нормаль к плоскости).
     */
    private fun cross(a: FloatArray, b: FloatArray): FloatArray {
        return floatArrayOf(
            a[1] * b[2] - a[2] * b[1],   // x-компонента
            a[2] * b[0] - a[0] * b[2],   // y-компонента
            a[0] * b[1] - a[1] * b[0]    // z-компонента
        )
    }

    // Компилируем и линкуем шейдерную программу OpenGL из ресурсов (вершинный + фрагментный шейдер)
    private var _program: Int = createProgramGLES30(res, R.raw.cube_vert, R.raw.cube_frag)

    // Матрица модели: применяет масштабирование и вращение к объекту
    private val _modelMatrix = FloatArray(16)
    // Промежуточная матрица: результат умножения View × Model
    private val _viewModelMatrix = FloatArray(16)
    // Итоговая MVP-матрица: Projection × View × Model
    private val _mvpMatrix = FloatArray(16)

    // Хэндлы (идентификаторы) uniform-переменных и атрибутов в шейдере
    private var _mvpMatrixHandle: Int = 0   // uniform uMVPMatrix
    private var _positionHandle: Int = 0    // attribute vPosition
    private var _colorHandle: Int = 0       // attribute vColor

    // Буфер вершин: нативная память для передачи координат в GPU
    private val _vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(vertices.size * Float.SIZE_BYTES)  // Выделяем нативную память
        .order(ByteOrder.nativeOrder())                    // Порядок байтов платформы
        .asFloatBuffer()                                   // Интерпретируем как FloatBuffer
        .apply {
            put(vertices)    // Копируем данные вершин
            position(0)      // Сбрасываем позицию чтения на начало
        }

    // Буфер цветов: нативная память для передачи цветов вершин в GPU
    private val _colorBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(colors.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(colors)
            position(0)
        }

    /**
     * Отрисовывает пентагональный икоситетраэдр.
     *
     * Этапы отрисовки:
     * 1. Формируем матрицу модели (Model): масштаб → поворот вокруг X → поворот вокруг Y
     * 2. Вычисляем MVP-матрицу: Projection × View × Model
     * 3. Активируем шейдерную программу
     * 4. Передаём вершины, цвета и MVP-матрицу в шейдер
     * 5. Вызываем glDrawArrays для отрисовки всех треугольников
     *
     * @param state текущее состояние (поворот и масштаб)
     * @param viewMatrix матрица вида (положение камеры)
     * @param projectionMatrix матрица проекции (перспектива)
     */
    fun draw(
        state: PentagonalIcositetrahedronState,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
    ) {
        // Шаг 1: Формируем матрицу модели
        // Начинаем с единичной матрицы
        Matrix.setIdentityM(_modelMatrix, 0)
        // Применяем равномерное масштабирование по всем осям
        Matrix.scaleM(_modelMatrix, 0, state.scale, state.scale, state.scale)
        // Поворот вокруг оси X (горизонтальное вращение пальцем по вертикали)
        Matrix.rotateM(_modelMatrix, 0, state.rotation.y, 1f, 0f, 0f)
        // Поворот вокруг оси Y (горизонтальное вращение пальцем по горизонтали)
        Matrix.rotateM(_modelMatrix, 0, state.rotation.x, 0f, 1f, 0f)

        // Шаг 2: Вычисляем итоговую MVP-матрицу
        // View × Model
        Matrix.multiplyMM(_viewModelMatrix, 0, viewMatrix, 0, _modelMatrix, 0)
        // Projection × (View × Model)
        Matrix.multiplyMM(_mvpMatrix, 0, projectionMatrix, 0, _viewModelMatrix, 0)

        // Шаг 3: Активируем шейдерную программу
        GLES30.glUseProgram(_program)

        // Шаг 4a: Передаём координаты вершин в атрибут vPosition шейдера
        _positionHandle = GLES30.glGetAttribLocation(_program, "vPosition")
        GLES30.glEnableVertexAttribArray(_positionHandle)
        GLES30.glVertexAttribPointer(
            _positionHandle,
            COORDS_PER_VERTEX,                    // 3 компоненты (x, y, z)
            GLES30.GL_FLOAT,                      // Тип данных — float
            false,                                // Не нормализовать
            COORDS_PER_VERTEX * Float.SIZE_BYTES, // Шаг между вершинами в байтах
            _vertexBuffer,                        // Буфер с данными
        )

        // Шаг 4b: Передаём цвета вершин в атрибут vColor шейдера
        _colorHandle = GLES30.glGetAttribLocation(_program, "vColor")
        GLES30.glEnableVertexAttribArray(_colorHandle)
        GLES30.glVertexAttribPointer(
            _colorHandle,
            COLORS_PER_VERTEX,                    // 4 компоненты (r, g, b, a)
            GLES30.GL_FLOAT,
            false,
            COLORS_PER_VERTEX * Float.SIZE_BYTES,
            _colorBuffer,
        )

        // Шаг 4c: Передаём MVP-матрицу в uniform uMVPMatrix шейдера
        _mvpMatrixHandle = GLES30.glGetUniformLocation(_program, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(_mvpMatrixHandle, 1, false, _mvpMatrix, 0)

        // Шаг 5: Рисуем все треугольники одним вызовом
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        // Отключаем массивы атрибутов после отрисовки
        GLES30.glDisableVertexAttribArray(_positionHandle)
        GLES30.glDisableVertexAttribArray(_colorHandle)
    }
}

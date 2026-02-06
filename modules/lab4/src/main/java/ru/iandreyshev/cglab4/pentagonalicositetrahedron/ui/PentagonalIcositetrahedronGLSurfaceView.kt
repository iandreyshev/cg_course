package ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui

import android.content.Context
import android.opengl.GLSurfaceView
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation.PentagonalIcositetrahedronState

/**
 * OpenGL-поверхность для отображения пентагонального икоситетраэдра.
 *
 * Наследуется от [GLSurfaceView] и настраивает OpenGL ES 3.0 контекст.
 * Используется режим непрерывного рендеринга ([RENDERMODE_CONTINUOUSLY]),
 * чтобы анимация инерционного вращения отображалась плавно.
 *
 * Связь с Compose: метод [updateState] вызывается из Compose-экрана
 * при каждой рекомпозиции, передавая актуальное состояние в рендерер.
 */
class PentagonalIcositetrahedronGLSurfaceView(context: Context) : GLSurfaceView(context) {

    // Рендерер, отвечающий за отрисовку кадров OpenGL
    private val _renderer: PentagonalIcositetrahedronGLRenderer

    init {
        // Устанавливаем версию OpenGL ES 3.0
        setEGLContextClientVersion(3)
        // Создаём рендерер и передаём ему доступ к ресурсам (для загрузки шейдеров)
        _renderer = PentagonalIcositetrahedronGLRenderer(resources)
        // Назначаем рендерер для данной GL-поверхности
        setRenderer(_renderer)
        // Непрерывный режим: кадры перерисовываются постоянно (нужно для анимации fling)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    /**
     * Обновляет состояние рендерера (поворот, масштаб, скорость).
     * Вызывается из Compose при изменении состояния ViewModel.
     * После обновления запрашивает перерисовку кадра.
     */
    fun updateState(state: PentagonalIcositetrahedronState) {
        _renderer.updateState(state)
        requestRender()
    }
}

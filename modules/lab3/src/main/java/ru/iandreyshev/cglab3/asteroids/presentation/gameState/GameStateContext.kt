package ru.iandreyshev.cglab3.asteroids.presentation.gameState

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import ru.iandreyshev.cglab3.asteroids.presentation.AsteroidsState
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase
import ru.iandreyshev.cglab3.asteroids.presentation.Sound
import ru.iandreyshev.cglab3.asteroids.presentation.StickInfo

abstract class GameStateContext {
    abstract val state: AsteroidsState

    var stickFieldCenter = Offset.Zero
    var stickFieldControlRadius = 0f
    var worldSize: IntSize? = null

    var stickInfo: StickInfo? = null
    var isFireHandled = true

    var nextEnemySpawnTime = 0L
    var nextStarSpawnTime = 0L

    abstract fun changePhase(phase: GamePhase)
    abstract fun updateState(modifier: AsteroidsState.() -> AsteroidsState)
    abstract fun play(sound: Sound)
}

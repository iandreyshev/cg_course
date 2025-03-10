package ru.iandreyshev.cglab3.asteroids.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.iandreyshev.cglab3.asteroids.domain.AsteroidsConst
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.GAME_OVER
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.PLAYING
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.START
import ru.iandreyshev.cglab3.common.BaseViewModel
import kotlin.math.abs
import kotlin.math.sign

private const val MILLIS_IN_SEC = 1000L
private const val FRAME_RATE = 60L

class AsteroidsViewModel : BaseViewModel<AsteroidsState, Any>(
    initialState = AsteroidsState()
) {
    private var _stickFieldCenter = Offset.Zero
    private var _stickFieldControlRadius = 0f
    private var _worldSize: IntSize? = null

    private var _stickInfo: StickInfo? = null
    private var _isFireHandled = true

    init {
        runGameLoop()
    }

    fun onStart() {
    }

    fun onUpdateWorldSize(size: IntSize) {
        println("update world size: $size")
        _worldSize = size
    }

    fun onDragStart(stickFieldCenter: Offset, stickFieldRadius: Float) {
        _stickFieldCenter = stickFieldCenter
        _stickFieldControlRadius = stickFieldRadius
    }

    fun onDrag(position: Offset) {
        _stickInfo = StickInfo.create(_stickFieldCenter, _stickFieldControlRadius, position)

        updateState {
            copy(stickCenter = _stickInfo?.center)
        }
    }

    fun onDragEnd() {
        _stickInfo = null

        updateState {
            copy(stickCenter = _stickFieldCenter)
        }
    }

    fun onFire() {
        _isFireHandled = false
    }

    fun onRestart() {
    }

    private fun runGameLoop() {
        viewModelScope.launch {
            var lastFrameTime = System.currentTimeMillis()

            while (true) {
                val newFrameTime = System.currentTimeMillis()
                val elapsedTime = (newFrameTime - lastFrameTime) / 1000f
                doFrameTick(elapsedTime)
                lastFrameTime = newFrameTime

                delay(MILLIS_IN_SEC / FRAME_RATE)
            }
        }
    }

    private fun doFrameTick(elapsedTime: Float) = updateState {
        when (stateValue.gamePhase) {
            START -> {
                // TODO: Implement start phase
                this
            }

            PLAYING -> handleCollisions()
                .handleShipMove(elapsedTime)
                .handleShipFire()
                .handleAsteroidsMove()

            GAME_OVER -> {
                // TODO: Implement game over phase
                this
            }
        }
    }

    private fun AsteroidsState.handleCollisions(): AsteroidsState {
        return this
    }

    private fun AsteroidsState.handleShipMove(elapsedTime: Float): AsteroidsState {
        val worldSize = _worldSize ?: return this
        val stickInfo = _stickInfo ?: return this
        var newPosition = ship.pos + stickInfo.normalized * stickInfo.percent * AsteroidsConst.SHIP_SPEED * elapsedTime

        val maxX = worldSize.width / 2
        if (abs(newPosition.x) > maxX) {
            newPosition = newPosition.copy(x = -newPosition.x.sign * maxX)
        }

        val maxY = worldSize.height / 2
        if (abs(newPosition.y) > maxY) {
            newPosition = newPosition.copy(y = -newPosition.y.sign * maxY)
        }

        return copy(
            ship = ship.copy(
                rotation = stickInfo.angle,
                pos = newPosition
            )
        )
    }

    private fun AsteroidsState.handleAsteroidsMove(): AsteroidsState {
        return this
    }

    private fun AsteroidsState.handleBulletsMove(): AsteroidsState {
        return this
    }

    private fun AsteroidsState.handleShipFire(): AsteroidsState {
        if (_isFireHandled) {
            return this
        }

        // TODO: Create bullet
        _isFireHandled = true

        return this
    }

}

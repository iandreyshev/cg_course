package ru.iandreyshev.cglab3.asteroids.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.iandreyshev.cglab3.asteroids.domain.AstConst
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.GAME_OVER
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.PLAYING
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.START
import ru.iandreyshev.cglab3.asteroids.presentation.gameState.GameState
import ru.iandreyshev.cglab3.asteroids.presentation.gameState.GameStateContext
import ru.iandreyshev.cglab3.asteroids.presentation.gameState.states.GameOverGameState
import ru.iandreyshev.cglab3.asteroids.presentation.gameState.states.PlayingGameState
import ru.iandreyshev.cglab3.asteroids.presentation.gameState.states.StartGameState
import ru.iandreyshev.core.BaseViewModel

private const val MILLIS_IN_SEC = 1000L

class AsteroidsViewModel(
    private val soundPlayer: SoundPlayer,
) : BaseViewModel<AsteroidsState, Any>(
    initialState = AsteroidsState()
) {
    private val _context = Context()
    private var _gameState: GameState = StartGameState(_context)

    init {
        viewModelScope.launch {
            var lastFrameTime = System.currentTimeMillis()

            while (true) {
                val newFrameTime = System.currentTimeMillis()
                val elapsedTime = (newFrameTime - lastFrameTime) / 1000f
                doFrameTick(elapsedTime)
                lastFrameTime = newFrameTime

                delay(MILLIS_IN_SEC / AstConst.FRAME_RATE)
            }
        }
    }

    fun onUpdateWorldSize(size: IntSize) {
        _context.worldSize = size
    }

    fun onDragStart(stickFieldCenter: Offset, stickFieldRadius: Float) =
        _gameState.onDragStart(stickFieldCenter, stickFieldRadius)

    fun onDrag(position: Offset) = _gameState.onDrag(position)

    fun onDragEnd() = _gameState.onDragEnd()

    fun onFireClick() = _gameState.onFireClick()

    private fun doFrameTick(elapsedTime: Float) = _gameState.doFrameTick(elapsedTime)

    inner class Context : GameStateContext() {
        override val state: AsteroidsState
            get() = stateValue

        override fun changePhase(phase: GamePhase) =
            this@AsteroidsViewModel.updateState {
                when (phase) {
                    START -> this
                    PLAYING -> {
                        stickInfo = null
                        _gameState = PlayingGameState(_context)
                        toPlayingState()
                    }

                    GAME_OVER -> {
                        _gameState = GameOverGameState(_context)
                        toGameOverState()
                    }
                }
            }

        override fun play(sound: Sound) {
            soundPlayer.play(sound)
        }

        override fun updateState(modifier: AsteroidsState.() -> AsteroidsState) {
            this@AsteroidsViewModel.updateState { stateValue.modifier() }
        }
    }

}

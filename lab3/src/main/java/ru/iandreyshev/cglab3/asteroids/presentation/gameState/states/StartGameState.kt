package ru.iandreyshev.cglab3.asteroids.presentation.gameState.states

import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase
import ru.iandreyshev.cglab3.asteroids.presentation.gameState.GameState
import ru.iandreyshev.cglab3.asteroids.presentation.gameState.GameStateContext

class StartGameState(
    private val context: GameStateContext
) : GameState {

    override fun onFireClick() {
        context.changePhase(GamePhase.PLAYING)
    }

    override fun doFrameTick(elapsedTime: Float) {
        context.updateState {
            handleStarsMove(elapsedTime, context)
                .handleStarsSpawn(context)
        }
    }

}

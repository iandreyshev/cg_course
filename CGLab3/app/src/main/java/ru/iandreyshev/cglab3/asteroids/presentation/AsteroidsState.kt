package ru.iandreyshev.cglab3.asteroids.presentation

import androidx.compose.ui.geometry.Offset
import ru.iandreyshev.cglab3.asteroids.domain.BulletState
import ru.iandreyshev.cglab3.asteroids.domain.EnemyState
import ru.iandreyshev.cglab3.asteroids.domain.ShipState

data class AsteroidsState(
    val phase: GamePhase = GamePhase.START,
    val score: Int = 0,
    val stickCenter: Offset? = null,
    val ship: ShipState? = null,
    val enemies: List<EnemyState> = emptyList(),
    val bullets: List<BulletState> = emptyList(),
    val stars: List<BulletState> = emptyList()
)

enum class GamePhase {
    START,
    PLAYING,
    GAME_OVER;
}

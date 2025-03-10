package ru.iandreyshev.cglab3.asteroids.presentation

import androidx.compose.ui.geometry.Offset
import ru.iandreyshev.cglab3.asteroids.domain.Bullet
import ru.iandreyshev.cglab3.asteroids.domain.Enemy
import ru.iandreyshev.cglab3.asteroids.domain.Explosion
import ru.iandreyshev.cglab3.asteroids.domain.ShipState

data class AsteroidsState(
    val gamePhase: GamePhase = GamePhase.PLAYING,
    val score: Int = 0,
    val stickCenter: Offset? = null,
    val ship: ShipState = ShipState(),
    val enemies: List<Enemy> = emptyList(),
    val bullets: List<Bullet> = emptyList(),
    val explosions: List<Explosion> = emptyList()
)

enum class GamePhase {
    START,
    PLAYING,
    GAME_OVER;
}

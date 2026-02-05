package ru.iandreyshev.cglab3.asteroids.presentation

import androidx.compose.ui.geometry.Offset
import ru.iandreyshev.cglab3.asteroids.domain.BulletState
import ru.iandreyshev.cglab3.asteroids.domain.EnemyState
import ru.iandreyshev.cglab3.asteroids.domain.ParticleState
import ru.iandreyshev.cglab3.asteroids.domain.ShipState
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.GAME_OVER
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.PLAYING

data class AsteroidsState(
    val phase: GamePhase = GamePhase.START,
    val score: Int = 0,
    val stickCenter: Offset? = null,
    val ship: ShipState? = null,
    val enemies: List<EnemyState> = emptyList(),
    val bullets: List<BulletState> = emptyList(),
    val stars: List<BulletState> = emptyList(),
    val particles: List<ParticleState> = emptyList()
) {

    fun toPlayingState() = copy(
        phase = PLAYING,
        score = 0,
        stickCenter = null,
        ship = ShipState(),
        enemies = emptyList(),
        bullets = emptyList(),
        stars = emptyList(),
    )

    fun toGameOverState() = copy(
        phase = GAME_OVER,
        ship = null,
        stickCenter = null,
        enemies = emptyList(),
        bullets = emptyList(),
        particles = emptyList()
    )

}

enum class GamePhase {
    START,
    PLAYING,
    GAME_OVER;
}

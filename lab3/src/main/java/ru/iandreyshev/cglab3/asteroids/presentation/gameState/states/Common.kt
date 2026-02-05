package ru.iandreyshev.cglab3.asteroids.presentation.gameState.states

import androidx.compose.ui.geometry.Offset
import ru.iandreyshev.cglab3.asteroids.domain.BulletState
import ru.iandreyshev.cglab3.asteroids.presentation.AsteroidsState
import ru.iandreyshev.cglab3.asteroids.presentation.gameState.GameStateContext
import ru.iandreyshev.core.normalize
import ru.iandreyshev.core.randomPointOnCircle
import kotlin.math.abs

fun AsteroidsState.handleStarsSpawn(context: GameStateContext): AsteroidsState {
    val worldSize = context.worldSize ?: return this

    val currentTime = System.currentTimeMillis()
    if (context.nextStarSpawnTime > currentTime) {
        return this
    }

    context.nextStarSpawnTime = currentTime + 100

    val newStars = mutableListOf<BulletState>()

    repeat(10) {
        newStars += BulletState(
            position = Offset.Zero,
            direction = randomPointOnCircle(worldSize.height * 2 / 2f).normalize(),
            speed = 800f
        )
    }

    return copy(stars = stars + newStars)
}

fun AsteroidsState.handleStarsMove(elapsedTime: Float, context: GameStateContext): AsteroidsState {
    val worldSize = context.worldSize ?: return this

    return copy(stars = stars.mapNotNull { starState ->
        if (abs(starState.position.x) > worldSize.width ||
            abs(starState.position.y) > worldSize.height
        ) return@mapNotNull null

        val move = starState.direction * starState.speed * elapsedTime
        starState.copy(position = starState.position + move)
    })
}

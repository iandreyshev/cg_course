package ru.iandreyshev.cglab3.asteroids.presentation.gameState.states

import androidx.compose.ui.geometry.Offset
import ru.iandreyshev.cglab3.asteroids.domain.AstConst
import ru.iandreyshev.cglab3.asteroids.domain.BulletState
import ru.iandreyshev.cglab3.asteroids.domain.EnemyState
import ru.iandreyshev.cglab3.asteroids.domain.ParticleState
import ru.iandreyshev.cglab3.asteroids.presentation.AsteroidsState
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.GAME_OVER
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.PLAYING
import ru.iandreyshev.cglab3.asteroids.presentation.Sound
import ru.iandreyshev.cglab3.asteroids.presentation.StickInfo
import ru.iandreyshev.cglab3.asteroids.presentation.gameState.GameState
import ru.iandreyshev.cglab3.asteroids.presentation.gameState.GameStateContext
import ru.iandreyshev.core.circlesIntersect
import ru.iandreyshev.core.degreesToNormalizedVector
import ru.iandreyshev.core.distanceTo
import ru.iandreyshev.core.normalize
import ru.iandreyshev.core.randomPointOnCircle
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.random.Random.Default.nextDouble

class PlayingGameState(
    private val context: GameStateContext
) : GameState {

    override fun onDragStart(stickFieldCenter: Offset, stickFieldRadius: Float) {
        context.stickFieldCenter = stickFieldCenter
        context.stickFieldControlRadius = stickFieldRadius
    }

    override fun onDrag(position: Offset) {
        context.stickInfo = StickInfo.create(context.stickFieldCenter, context.stickFieldControlRadius, position)
        context.updateState {
            copy(stickCenter = context.stickInfo?.center)
        }
    }

    override fun onDragEnd() {
        context.stickInfo = null
        context.updateState {
            copy(stickCenter = context.stickFieldCenter)
        }
    }

    override fun onFireClick() {
        context.play(Sound.FIRE)
        context.isFireHandled = false
    }

    override fun doFrameTick(elapsedTime: Float) {
        context.state.handleCollisions()?.let { state ->
            context.updateState {
                state.handleEnemyHealth()
                    .handleShipMove(elapsedTime)
                    .handleEnemiesMove(elapsedTime)
                    .handleBulletsMove(elapsedTime)
                    .handleParticlesMove(elapsedTime)
                    .handleEnemiesSpawn()
                    .handleBulletsSpawn()
            }
        }
    }


    private fun AsteroidsState.handleCollisions(): AsteroidsState? {
        ship ?: return this

        val newParticles = mutableListOf<ParticleState>()
        var aliveBullets = bullets

        val aliveEnemies = enemies.map { enemy ->
            val enemyRadius = AstConst.Enemy.RADIUS * AstConst.Enemy.SCALE_REGULAR

            if (circlesIntersect(enemy.position, enemyRadius, ship.pos, 30f)) {
                context.play(Sound.GAME_OVER)
                context.changePhase(GAME_OVER)
                return null
            }

            var enemyDamage = 0
            aliveBullets = aliveBullets.mapNotNull { bullet ->
                when {
                    circlesIntersect(enemy.position, enemyRadius, bullet.position, AstConst.Bullet.RADIUS) -> {
                        newParticles += createParticles(bullet.position)
                        context.play(Sound.HIT_ENEMY)
                        ++enemyDamage
                        return@mapNotNull null
                    }

                    else -> return@mapNotNull bullet
                }
            }

            enemy.copy(health = enemy.health - enemyDamage)
        }

        return copy(
            enemies = aliveEnemies,
            bullets = aliveBullets,
            particles = particles + newParticles
        )
    }

    private fun AsteroidsState.handleEnemyHealth(): AsteroidsState {
        var points = 0
        val newEnemies = mutableListOf<EnemyState>()
        val aliveEnemies = enemies.filter { enemy ->
            val isDead = enemy.health <= 0

            if (isDead) {
                if (enemy.level == EnemyState.Level.BOSS) {
                    newEnemies += createEnemiesOnBossKill(enemy)
                    points += AstConst.POINTS_PER_BOSS_ENEMY
                } else {
                    points += AstConst.POINTS_PER_REGULAR_ENEMY
                }
                context.play(Sound.KILL_ENEMY)
            }

            return@filter !isDead
        }

        return copy(
            score = score + points,
            enemies = aliveEnemies + newEnemies
        )
    }

    private fun AsteroidsState.handleShipMove(elapsedTime: Float): AsteroidsState {
        ship ?: return this

        val worldSize = context.worldSize ?: return this
        val stickInfo = context.stickInfo ?: return this
        var newPosition = ship.pos + stickInfo.normalized * stickInfo.percent * AstConst.SHIP_SPEED * elapsedTime

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

    private fun AsteroidsState.handleEnemiesMove(elapsedTime: Float): AsteroidsState {
        val worldSize = context.worldSize ?: return this

        return copy(enemies = enemies.mapNotNull { enemyState ->
            if (abs(enemyState.position.x) > worldSize.width ||
                abs(enemyState.position.y) > worldSize.height
            ) return@mapNotNull null

            val move = enemyState.direction * enemyState.speed * elapsedTime
            enemyState.copy(position = enemyState.position + move)
        })
    }

    private fun AsteroidsState.handleParticlesMove(elapsedTime: Float): AsteroidsState =
        copy(particles = particles.mapNotNull { particleState ->
            if (particleState.position.distanceTo(particleState.startPosition) > AstConst.Particle.MAX_DISTANCE) {
                return@mapNotNull null
            }

            val move = particleState.direction * AstConst.Particle.SPEED * elapsedTime
            particleState.copy(position = particleState.position + move)
        })

    private fun AsteroidsState.handleEnemiesSpawn(): AsteroidsState {
        val worldSize = context.worldSize ?: return this

        val currentTime = System.currentTimeMillis()
        if (context.nextEnemySpawnTime > currentTime) {
            return this
        }

        context.nextEnemySpawnTime = currentTime + when (phase) {
            PLAYING -> (AstConst.Enemy.MIN_SPAWN_PAUSE_MS..AstConst.Enemy.MAX_SPAWN_PAUSE_MS).random()
            else -> AstConst.Enemy.PAUSE_SPAWN_PAUSE_MS
        }

        val spawnPosition = randomPointOnCircle(worldSize.height / 2f)
        val newEnemy = EnemyState.random(
            position = spawnPosition,
            direction = enemyRandomDirection(Offset.Zero - spawnPosition, AstConst.Enemy.DIRECTION_CONE_DEGREE)
        )

        return copy(enemies = enemies + newEnemy)
    }

    private fun AsteroidsState.handleBulletsMove(elapsedTime: Float): AsteroidsState {
        val worldSize = context.worldSize ?: return this

        return copy(bullets = bullets.mapNotNull { bulletState ->
            if (abs(bulletState.position.x) > worldSize.width ||
                abs(bulletState.position.y) > worldSize.height
            ) return@mapNotNull null

            val move = bulletState.direction * bulletState.speed * elapsedTime
            bulletState.copy(position = bulletState.position + move)
        })
    }

    private fun AsteroidsState.handleBulletsSpawn(): AsteroidsState {
        ship ?: return this

        if (context.isFireHandled) {
            return this
        }

        context.isFireHandled = true

        return copy(
            bullets = bullets + BulletState(
                position = ship.pos,
                direction = degreesToNormalizedVector(ship.rotation + 90),
                speed = AstConst.Bullet.SPEED
            )
        )
    }

    private fun createEnemiesOnBossKill(boss: EnemyState) = listOf(
        EnemyState.random(
            position = boss.position,
            level = EnemyState.Level.REGULAR,
            direction = enemyRandomDirection(boss.direction, 120f)
        ),
        EnemyState.random(
            position = boss.position,
            level = EnemyState.Level.REGULAR,
            direction = enemyRandomDirection(boss.direction, 120f)
        )
    )

    private fun createParticles(position: Offset): List<ParticleState> {
        val result = mutableListOf<ParticleState>()
        var degrees = 0f

        while (degrees < 360f) {
            result += ParticleState(
                startPosition = position,
                position = position,
                direction = degreesToNormalizedVector(degrees)
            )
            degrees += AstConst.Particle.SPAWN_DEGREE_OFFSET
        }

        return result
    }

    private fun enemyRandomDirection(direction: Offset, coneDegree: Float): Offset {
        val normalizedDirection = direction.normalize()
        val alpha = atan2(normalizedDirection.y, normalizedDirection.x)

        // Выбираем случайный угол отклонения в диапазоне [-leftDeg, rightDeg]
        val halfCone = (abs(coneDegree) / 2).toDouble()
        val randomOffsetRad = nextDouble(-halfCone, halfCone)
            .let(Math::toRadians)

        // Итоговый угол = угол direction + случайное отклонение
        val finalAngle = alpha + randomOffsetRad

        // Строим новый вектор на основе итогового угла
        val x = cos(finalAngle).toFloat()
        val y = sin(finalAngle).toFloat()

        // Так как cos^2 + sin^2 = 1, вектор (x, y) уже нормализован
        return Offset(x, y)
    }


}
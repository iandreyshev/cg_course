package ru.iandreyshev.cglab3.asteroids.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.iandreyshev.cglab3.asteroids.domain.AstConst
import ru.iandreyshev.cglab3.asteroids.domain.BulletState
import ru.iandreyshev.cglab3.asteroids.domain.EnemyState
import ru.iandreyshev.cglab3.asteroids.domain.ShipState
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.GAME_OVER
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.PLAYING
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.START
import ru.iandreyshev.cglab3.common.BaseViewModel
import ru.iandreyshev.cglab3.common.circlesIntersect
import ru.iandreyshev.cglab3.common.degreesToNormalizedVector
import ru.iandreyshev.cglab3.common.normalize
import ru.iandreyshev.cglab3.common.randomPointOnCircle
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.random.Random.Default.nextDouble

private const val MILLIS_IN_SEC = 1000L

class AsteroidsViewModel : BaseViewModel<AsteroidsState, Any>(
    initialState = AsteroidsState()
) {
    private var _stickFieldCenter = Offset.Zero
    private var _stickFieldControlRadius = 0f
    private var _worldSize: IntSize? = null

    private var _stickInfo: StickInfo? = null
    private var _isFireHandled = true

    private var _nextEnemySpawnTime = 0L
    private var _nextStarSpawnTime = 0L

    init {
        runGameLoop()
    }

    fun onUpdateWorldSize(size: IntSize) {
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

    fun onFireClick() {
        when (stateValue.phase) {
            START,
            GAME_OVER -> {
                updateState {
                    copy(
                        phase = PLAYING,
                        score = 0,
                        ship = ShipState(),
                        enemies = emptyList(),
                        bullets = emptyList(),
                        stars = emptyList(),
                    )
                }
            }

            PLAYING -> {
                _isFireHandled = false
            }
        }
    }

    fun onRestart() {
        onFireClick()
    }

    private fun runGameLoop() {
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

    private fun doFrameTick(elapsedTime: Float) = updateState {
        when (stateValue.phase) {
            START,
            GAME_OVER -> //handleEnemiesMove(elapsedTime)
                handleStarsMove(elapsedTime)
                .handleStarsSpawn()

            PLAYING -> handleCollisions()
                .handleShipMove(elapsedTime)
                .handleEnemiesMove(elapsedTime)
                .handleBulletsMove(elapsedTime)
                .handleEnemiesSpawn()
                .handleBulletsSpawn()
        }
    }

    private fun AsteroidsState.handleCollisions(): AsteroidsState {
        ship ?: return this

        var points = 0
        val newEnemies = mutableListOf<EnemyState>()
        val aliveEnemies = mutableListOf<EnemyState>()
        var aliveBullets = bullets

        enemies.forEach { enemy ->
            val enemyRadius = AstConst.Enemy.RADIUS * AstConst.Enemy.SCALE_LVL_0

            if (circlesIntersect(enemy.position, enemyRadius, ship.pos, 30f)) {
                return copy(
                    phase = GAME_OVER,
                    ship = null,
                    enemies = emptyList(),
                    bullets = emptyList()
                )
            }

            var isEnemyAlive = true
            aliveBullets = aliveBullets.mapNotNull { bullet ->
                when {
                    circlesIntersect(enemy.position, enemyRadius, bullet.position, AstConst.Bullet.RADIUS) -> {
                        if (enemy.level == EnemyState.Level.BOSS) {
                            newEnemies += EnemyState.random(
                                position = enemy.position,
                                level = EnemyState.Level.REGULAR,
                                direction = enemyRandomDirection(enemy.direction)
                            )
                            newEnemies += EnemyState.random(
                                position = enemy.position,
                                level = EnemyState.Level.REGULAR,
                                direction = enemyRandomDirection(enemy.direction)
                            )
                            points += AstConst.POINTS_PER_BOSS_ENEMY
                        } else {
                            points += AstConst.POINTS_PER_REGULAR_ENEMY
                        }

                        isEnemyAlive = false
                        return@mapNotNull null
                    }

                    else -> return@mapNotNull bullet
                }
            }

            if (isEnemyAlive) {
                aliveEnemies += enemy
            }
        }

        return copy(
            score = score + points,
            enemies = aliveEnemies + newEnemies,
            bullets = aliveBullets
        )
    }

    private fun AsteroidsState.handleShipMove(elapsedTime: Float): AsteroidsState {
        ship ?: return this

        val worldSize = _worldSize ?: return this
        val stickInfo = _stickInfo ?: return this
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
        val worldSize = _worldSize ?: return this

        return copy(enemies = enemies.mapNotNull { enemyState ->
            if (abs(enemyState.position.x) > worldSize.width ||
                abs(enemyState.position.y) > worldSize.height
            ) return@mapNotNull null

            val move = enemyState.direction * enemyState.speed * elapsedTime
            enemyState.copy(position = enemyState.position + move)
        })
    }

    private fun AsteroidsState.handleEnemiesSpawn(): AsteroidsState {
        val worldSize = _worldSize ?: return this

        val currentTime = System.currentTimeMillis()
        if (_nextEnemySpawnTime > currentTime) {
            return this
        }

        _nextEnemySpawnTime = currentTime + when (phase) {
            PLAYING -> (AstConst.Enemy.MIN_SPAWN_PAUSE_MS..AstConst.Enemy.MAX_SPAWN_PAUSE_MS).random()
            else -> AstConst.Enemy.PAUSE_SPAWN_PAUSE_MS
        }

        val spawnPosition = randomPointOnCircle(worldSize.height / 2f)
        val newEnemy = EnemyState.random(
            position = spawnPosition,
            direction = enemyRandomDirection(Offset.Zero - spawnPosition)
        )

        return copy(enemies = enemies + newEnemy)
    }

    private fun AsteroidsState.handleStarsSpawn(): AsteroidsState {
        val worldSize = _worldSize ?: return this

        val currentTime = System.currentTimeMillis()
        if (_nextStarSpawnTime > currentTime) {
            return this
        }

        _nextStarSpawnTime = currentTime + 100

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

    private fun AsteroidsState.handleStarsMove(elapsedTime: Float): AsteroidsState {
        val worldSize = _worldSize ?: return this

        return copy(stars = stars.mapNotNull { starState ->
            if (abs(starState.position.x) > worldSize.width ||
                abs(starState.position.y) > worldSize.height
            ) return@mapNotNull null

            val move = starState.direction * starState.speed * elapsedTime
            starState.copy(position = starState.position + move)
        })
    }

    private fun AsteroidsState.handleBulletsMove(elapsedTime: Float): AsteroidsState {
        val worldSize = _worldSize ?: return this

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

        if (_isFireHandled) {
            return this
        }

        _isFireHandled = true

        return copy(
            bullets = bullets + BulletState(
                position = ship.pos,
                direction = degreesToNormalizedVector(ship.rotation + 90),
                speed = AstConst.Bullet.SPEED
            )
        )
    }

    private fun enemyRandomDirection(direction: Offset): Offset {
        val normalizedDirection = direction.normalize()
        val alpha = atan2(normalizedDirection.y, normalizedDirection.x)

        // Выбираем случайный угол отклонения в диапазоне [-leftDeg, rightDeg]
        val coneDegree = AstConst.Enemy.DIRECTION_CONE_DEGREE
        val randomOffsetRad = nextDouble(-coneDegree, coneDegree)
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

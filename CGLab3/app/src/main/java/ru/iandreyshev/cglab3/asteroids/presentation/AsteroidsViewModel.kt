package ru.iandreyshev.cglab3.asteroids.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.iandreyshev.cglab3.asteroids.domain.AstConst
import ru.iandreyshev.cglab3.asteroids.domain.EnemyState
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.GAME_OVER
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.PLAYING
import ru.iandreyshev.cglab3.asteroids.presentation.GamePhase.START
import ru.iandreyshev.cglab3.common.BaseViewModel
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

    private var _nextSpawnTime = 0L

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
        when (stateValue.gamePhase) {
            START,
            GAME_OVER -> {
                updateState { copy(gamePhase = PLAYING) }
            }
            PLAYING -> {
                _isFireHandled = false
            }
        }
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

                delay(MILLIS_IN_SEC / AstConst.FRAME_RATE)
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
                .handleEnemiesMove(elapsedTime)
                .handleBulletsMove(elapsedTime)
                .handleEnemiesSpawn()

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
            if (abs(enemyState.pos.x) + AstConst.Enemy.MAX_RADIUS > worldSize.width ||
                abs(enemyState.pos.y) + AstConst.Enemy.MAX_RADIUS > worldSize.height
            ) return@mapNotNull null

            val move = enemyState.direction * enemyState.speed * elapsedTime
            enemyState.copy(pos = enemyState.pos + move)
        })
    }

    private fun AsteroidsState.handleEnemiesSpawn(): AsteroidsState {
        val currentTime = System.currentTimeMillis()
        if (_nextSpawnTime > currentTime) {
            return this
        }

        val worldSize = _worldSize ?: return this
        _nextSpawnTime = currentTime +
                (AstConst.Enemy.MIN_SPAWN_PAUSE_MS..AstConst.Enemy.MAX_SPAWN_PAUSE_MS).random()

        val spawnPosition = randomPointOnCircle(worldSize.height / 2f)
        val direction = enemyRandomDirection(Offset.Zero - spawnPosition)
        val newEnemy = EnemyState(
            pos = spawnPosition,
            radius = (AstConst.Enemy.MIN_RADIUS..AstConst.Enemy.MAX_RADIUS).random(),
            rotation = (0..360).random().toFloat(),
            speed = (AstConst.Enemy.MIN_SPEED..AstConst.Enemy.MAX_SPEED).random().toFloat(),
            direction = direction
        )

        return copy(enemies = enemies + newEnemy)
    }

    private fun AsteroidsState.handleBulletsMove(elapsedTime: Float): AsteroidsState {
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

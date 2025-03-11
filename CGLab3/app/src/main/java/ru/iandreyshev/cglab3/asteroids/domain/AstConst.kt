package ru.iandreyshev.cglab3.asteroids.domain

object AstConst {
    const val FRAME_RATE = 60L
    const val SHIP_SPEED = 720f

    object Enemy {
        const val MIN_SPAWN_PAUSE_MS = 400L
        const val MAX_SPAWN_PAUSE_MS = 1000L

        const val MIN_RADIUS = 25
        const val MAX_RADIUS = 100

        const val MIN_SPEED = 200
        const val MAX_SPEED = 800

        const val DIRECTION_CONE_DEGREE = 30.0
    }
}

package ru.iandreyshev.cglab3.asteroids.domain

object AstConst {
    const val FRAME_RATE = 60L
    const val SHIP_SPEED = 720f

    const val POINTS_PER_REGULAR_ENEMY = 500
    const val POINTS_PER_BOSS_ENEMY = 1500

    object Enemy {
        const val PAUSE_SPAWN_PAUSE_MS = 250L
        const val MIN_SPAWN_PAUSE_MS = 500L
        const val MAX_SPAWN_PAUSE_MS = 1500L

        const val MIN_SPEED = 100
        const val MAX_SPEED = 400

        const val MIN_ROTATION_SPEED_DEG = 5
        const val MAX_ROTATION_SPEED_DEG = 360

        const val DIRECTION_CONE_DEGREE = 60f

        const val RADIUS = 3f
        const val SCALE_REGULAR = 10f
        const val SCALE_BOSS = 15f

        const val HEALTH_REGULAR = 2
        const val HEALTH_BOSS = 4

        const val SPAWN_BOSS_PROBABILITY_PERCENT = 25
    }

    object Bullet {
        const val SPEED = 1200f
        const val RADIUS = 5f
    }

    object Particle {
        const val SPAWN_DEGREE_OFFSET = 24f
        const val SPEED = 450f
        const val RADIUS = 3f
        const val MAX_DISTANCE = 85f
    }
}

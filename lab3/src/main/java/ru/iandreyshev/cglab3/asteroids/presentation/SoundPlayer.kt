package ru.iandreyshev.cglab3.asteroids.presentation

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import ru.iandreyshev.cglab3.R

enum class Sound {
    FIRE,
    HIT_ENEMY,
    KILL_ENEMY,
    GAME_OVER,
}

class SoundPlayer(
    context: Context
) {

    private val _sounds = mutableMapOf<Sound, Int>()
    private val _soundPool: SoundPool
    private var _lastId: Int? = null

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        _soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(attributes)
            .build()

        _sounds[Sound.FIRE] = _soundPool.load(context, R.raw.sound_fire, 3)
        _sounds[Sound.HIT_ENEMY] = _soundPool.load(context, R.raw.sound_hit, 2)
        _sounds[Sound.KILL_ENEMY] = _soundPool.load(context, R.raw.sound_kill, 1)
        _sounds[Sound.GAME_OVER] = _soundPool.load(context, R.raw.sound_game_over, 1)
    }

    fun play(sound: Sound) {
        _sounds[sound]?.let { id ->
            _lastId?.let {
                _soundPool.stop(it)
            }
            _soundPool.play(id, 1f, 1f, 0, 0, 1f)
            _lastId = id
        }
    }

}

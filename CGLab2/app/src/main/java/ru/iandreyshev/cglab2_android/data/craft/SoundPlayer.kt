package ru.iandreyshev.cglab2_android.data.craft

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import ru.iandreyshev.cglab2_android.R

enum class Sound {
    SUCCESS_CRAFT_1,
    SUCCESS_CRAFT_2,
    BIN_TOSS,
}

class SoundPlayer(
    context: Context
) {

    private val _sounds = mutableMapOf<Sound, Int>()
    private val _soundPool: SoundPool

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        _soundPool = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .build()

        _sounds[Sound.SUCCESS_CRAFT_1] = _soundPool.load(context, R.raw.sound_success, 1)
        _sounds[Sound.SUCCESS_CRAFT_2] = _soundPool.load(context, R.raw.sound_mlg, 1)
        _sounds[Sound.BIN_TOSS] = _soundPool.load(context, R.raw.sound_bin, 1)
    }

    fun play(sound: Sound) {
        _sounds[sound]?.let { id ->
            _soundPool.play(id, 1f, 1f, 0, 0, 1f)
        }
    }

}

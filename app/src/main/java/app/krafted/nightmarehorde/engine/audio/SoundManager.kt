package app.krafted.nightmarehorde.engine.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import app.krafted.nightmarehorde.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<Int, Int>()

    var isMuted: Boolean = false
    var volume: Float = 1.0f

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(15)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds()
    }

    private fun loadSounds() {
        // Weapons
        soundMap[R.raw.sfx_pistol] = soundPool.load(context, R.raw.sfx_pistol, 1)
        soundMap[R.raw.sfx_shotgun] = soundPool.load(context, R.raw.sfx_shotgun, 1)
        soundMap[R.raw.sfx_smg] = soundPool.load(context, R.raw.sfx_smg, 1)
        soundMap[R.raw.sfx_rifle] = soundPool.load(context, R.raw.sfx_rifle, 1)
        soundMap[R.raw.sfx_flamethrower] = soundPool.load(context, R.raw.sfx_flamethrower, 1)
        soundMap[R.raw.sfx_melee] = soundPool.load(context, R.raw.sfx_melee, 1)

        // Entities
        soundMap[R.raw.sfx_zombie_death] = soundPool.load(context, R.raw.sfx_zombie_death, 1)
        soundMap[R.raw.sfx_player_hurt] = soundPool.load(context, R.raw.sfx_player_hurt, 1)
        soundMap[R.raw.sfx_boss_spawn] = soundPool.load(context, R.raw.sfx_boss_spawn, 1)
        soundMap[R.raw.sfx_drone_fire] = soundPool.load(context, R.raw.sfx_drone_fire, 1)

        // Mechanics
        soundMap[R.raw.sfx_pickup] = soundPool.load(context, R.raw.sfx_pickup, 1)
        soundMap[R.raw.sfx_level_up] = soundPool.load(context, R.raw.sfx_level_up, 1)
    }

    fun playSound(soundResId: Int) {
        if (isMuted) return
        val soundId = soundMap[soundResId] ?: return
        soundPool.play(soundId, volume, volume, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}

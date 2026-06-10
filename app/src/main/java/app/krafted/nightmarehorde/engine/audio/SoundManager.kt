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
) : AudioManager {
    // Nullable so the pool can be rebuilt after release(): this is a process-scoped
    // @Singleton that outlives the Activity, so a release() on Activity teardown must
    // not leave the SoundPool permanently dead.
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>()

    override var isMuted: Boolean = false
    override var volume: Float = 1.0f

    init {
        initPool()
    }

    private fun initPool() {
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
        val pool = soundPool ?: return
        // Weapons
        soundMap[R.raw.sfx_pistol] = pool.load(context, R.raw.sfx_pistol, 1)
        soundMap[R.raw.sfx_shotgun] = pool.load(context, R.raw.sfx_shotgun, 1)
        soundMap[R.raw.sfx_smg] = pool.load(context, R.raw.sfx_smg, 1)
        soundMap[R.raw.sfx_rifle] = pool.load(context, R.raw.sfx_rifle, 1)
        soundMap[R.raw.sfx_flamethrower] = pool.load(context, R.raw.sfx_flamethrower, 1)
        soundMap[R.raw.sfx_melee] = pool.load(context, R.raw.sfx_melee, 1)

        // Entities
        soundMap[R.raw.sfx_zombie_death] = pool.load(context, R.raw.sfx_zombie_death, 1)
        soundMap[R.raw.sfx_player_hurt] = pool.load(context, R.raw.sfx_player_hurt, 1)
        soundMap[R.raw.sfx_boss_spawn] = pool.load(context, R.raw.sfx_boss_spawn, 1)
        soundMap[R.raw.sfx_drone_fire] = pool.load(context, R.raw.sfx_drone_fire, 1)

        // Mechanics
        soundMap[R.raw.sfx_pickup] = pool.load(context, R.raw.sfx_pickup, 1)
        soundMap[R.raw.sfx_level_up] = pool.load(context, R.raw.sfx_level_up, 1)
    }

    fun playSound(soundResId: Int) {
        if (isMuted) return
        // Rebuild lazily if the pool was released while this singleton lived on.
        // SoundPool.load is async, so the first play right after a rebuild may be
        // silent; subsequent plays work once loading completes.
        if (soundPool == null) initPool()
        val pool = soundPool ?: return
        val soundId = soundMap[soundResId] ?: return
        pool.play(soundId, volume, volume, 1, 0, 1f)
    }

    override fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }
}

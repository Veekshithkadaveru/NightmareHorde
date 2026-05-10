package app.krafted.nightmarehorde.engine.audio

import android.content.Context
import android.media.MediaPlayer
import app.krafted.nightmarehorde.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentResId: Int = 0

    var isMuted: Boolean = false
        set(value) {
            field = value
            if (value) {
                mediaPlayer?.setVolume(0f, 0f)
            } else {
                mediaPlayer?.setVolume(volume, volume)
            }
        }

    var volume: Float = 0.5f
        set(value) {
            field = value.coerceIn(0f, 1f)
            if (!isMuted) {
                mediaPlayer?.setVolume(field, field)
            }
        }

    fun playMenuMusic() = playMusic(R.raw.bgm_menu)

    fun playGameMusic() = playMusic(R.raw.bgm_game)

    private fun playMusic(resId: Int) {
        // Avoid restarting the same track if it's already playing
        if (resId == currentResId && mediaPlayer?.isPlaying == true) return

        mediaPlayer?.let {
            try { it.stop() } catch (_: IllegalStateException) {}
            it.release()
        }
        mediaPlayer = null

        currentResId = resId
        mediaPlayer = MediaPlayer.create(context, resId)?.apply {
            isLooping = true
            setVolume(if (isMuted) 0f else volume, if (isMuted) 0f else volume)
            start()
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
    }

    fun resume() {
        if (isMuted) return
        mediaPlayer?.let {
            if (!it.isPlaying) it.start()
        }
    }

    fun release() {
        mediaPlayer?.let {
            try { it.stop() } catch (_: IllegalStateException) {}
            it.release()
        }
        mediaPlayer = null
        currentResId = 0
    }
}

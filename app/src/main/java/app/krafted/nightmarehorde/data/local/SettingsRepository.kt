package app.krafted.nightmarehorde.data.local

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val prefs: SharedPreferences
) {
    var musicVolume: Float
        get() = prefs.getFloat(KEY_MUSIC_VOLUME, 70f)
        set(value) = prefs.edit().putFloat(KEY_MUSIC_VOLUME, value).apply()

    var sfxVolume: Float
        get() = prefs.getFloat(KEY_SFX_VOLUME, 80f)
        set(value) = prefs.edit().putFloat(KEY_SFX_VOLUME, value).apply()

    var showFps: Boolean
        get() = prefs.getBoolean(KEY_SHOW_FPS, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_FPS, value).apply()

    var screenShake: Boolean
        get() = prefs.getBoolean(KEY_SCREEN_SHAKE, true)
        set(value) = prefs.edit().putBoolean(KEY_SCREEN_SHAKE, value).apply()

    var performanceMode: Boolean
        get() = prefs.getBoolean(KEY_PERFORMANCE_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_PERFORMANCE_MODE, value).apply()

    companion object {
        private const val KEY_MUSIC_VOLUME = "music_volume"
        private const val KEY_SFX_VOLUME = "sfx_volume"
        private const val KEY_SHOW_FPS = "show_fps"
        private const val KEY_SCREEN_SHAKE = "screen_shake"
        private const val KEY_PERFORMANCE_MODE = "performance_mode"
    }
}

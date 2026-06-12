package app.krafted.nightmarehorde.data.local

import android.content.Context
import android.content.SharedPreferences
import app.krafted.nightmarehorde.engine.input.JoystickMode
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
        get() = prefs.getFloat(KEY_SFX_VOLUME, 50f)
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

    var joystickMode: JoystickMode
        get() = JoystickMode.fromString(prefs.getString(KEY_JOYSTICK_MODE, null))
        set(value) = prefs.edit().putString(KEY_JOYSTICK_MODE, value.name).apply()

    companion object {
        private const val KEY_MUSIC_VOLUME = "music_volume"
        private const val KEY_SFX_VOLUME = "sfx_volume"
        private const val KEY_SHOW_FPS = "show_fps"
        private const val KEY_SCREEN_SHAKE = "screen_shake"
        private const val KEY_PERFORMANCE_MODE = "performance_mode"
        private const val KEY_JOYSTICK_MODE = "joystick_mode"
    }
}

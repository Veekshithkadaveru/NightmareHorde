package app.krafted.nightmarehorde

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.krafted.nightmarehorde.data.local.SettingsRepository
import app.krafted.nightmarehorde.engine.audio.MusicManager
import app.krafted.nightmarehorde.engine.audio.SoundManager
import app.krafted.nightmarehorde.game.data.MapUnlockManager
import app.krafted.nightmarehorde.game.systems.SuppliesManager
import app.krafted.nightmarehorde.ui.navigation.NightmareHordeNavHost
import app.krafted.nightmarehorde.ui.theme.NightmareHordeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var mapUnlockManager: MapUnlockManager
    @Inject lateinit var suppliesManager: SuppliesManager
    @Inject lateinit var soundManager: SoundManager
    @Inject lateinit var musicManager: MusicManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        enableEdgeToEdge()

        // Apply persisted volumes (sliders are 0..100; managers expect 0..1)
        soundManager.volume = (settingsRepository.sfxVolume / 100f).coerceIn(0f, 1f)
        musicManager.volume = (settingsRepository.musicVolume / 100f).coerceIn(0f, 1f)

        setContent {
            NightmareHordeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NightmareHordeNavHost(
                        settingsRepository = settingsRepository,
                        mapUnlockManager = mapUnlockManager,
                        suppliesManager = suppliesManager,
                        soundManager = soundManager,
                        musicManager = musicManager,
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        musicManager.pause()
    }

    override fun onResume() {
        super.onResume()
        musicManager.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        musicManager.release()
        soundManager.release()
    }
}

package app.krafted.nightmarehorde.ui.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.analytics.FirebaseAnalytics
import app.krafted.nightmarehorde.data.local.SettingsRepository
import app.krafted.nightmarehorde.engine.audio.MusicManager
import app.krafted.nightmarehorde.engine.audio.SoundManager
import app.krafted.nightmarehorde.game.data.CharacterClass
import app.krafted.nightmarehorde.game.data.MapUnlockManager
import app.krafted.nightmarehorde.game.systems.SuppliesManager
import app.krafted.nightmarehorde.ui.screens.CharacterSelectScreen
import app.krafted.nightmarehorde.ui.screens.GameOverScreen
import app.krafted.nightmarehorde.ui.screens.GameScreen
import app.krafted.nightmarehorde.ui.screens.MainMenuScreen
import app.krafted.nightmarehorde.ui.screens.MapSelectScreen
import app.krafted.nightmarehorde.ui.screens.SettingsScreen
import app.krafted.nightmarehorde.ui.screens.ShopScreen
import app.krafted.nightmarehorde.ui.screens.SplashScreen

@Composable
fun NightmareHordeNavHost(
    settingsRepository: SettingsRepository,
    mapUnlockManager: MapUnlockManager,
    suppliesManager: SuppliesManager,
    soundManager: SoundManager,
    musicManager: MusicManager,
) {
    val context = LocalContext.current
    val analytics = remember { FirebaseAnalytics.getInstance(context) }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    LaunchedEffect(currentScreen) {
        val screenName = when (currentScreen) {
            is Screen.Splash -> "SplashScreen"
            is Screen.MainMenu -> "MainMenuScreen"
            is Screen.CharacterSelect -> "CharacterSelectScreen"
            is Screen.MapSelect -> "MapSelectScreen"
            is Screen.Game -> "GameScreen"
            is Screen.GameOver -> "GameOverScreen"
            is Screen.Settings -> "SettingsScreen"
            is Screen.Shop -> "ShopScreen"
        }

        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        })

        // Switch background music to match the current screen
        when (currentScreen) {
            is Screen.Game -> musicManager.playGameMusic()
            else -> musicManager.playMenuMusic()
        }
    }

    when (val screen = currentScreen) {
        is Screen.Splash -> SplashScreen(
            onSplashFinished = { currentScreen = Screen.MainMenu }
        )

        is Screen.MainMenu -> MainMenuScreen(
            onPlayClicked = { currentScreen = Screen.CharacterSelect },
            onShopClicked = { currentScreen = Screen.Shop },
            onSettingsClicked = { currentScreen = Screen.Settings }
        )

        is Screen.CharacterSelect -> {
            val stats by suppliesManager.statsFlow.collectAsState()
            CharacterSelectScreen(
                isCharacterUnlocked = { it.unlockChallenge.isCompleted(stats) },
                onCharacterSelected = { characterClass ->
                    currentScreen = Screen.MapSelect(characterClass)
                },
                onBack = { currentScreen = Screen.MainMenu }
            )
        }

        is Screen.MapSelect -> {
            val unlockState by mapUnlockManager.unlockState.collectAsState()
            MapSelectScreen(
                characterClass = screen.characterClass,
                isMapUnlocked = { mapUnlockManager.isUnlocked(it, unlockState) },
                onMapSelected = { mapType ->
                    currentScreen = Screen.Game(screen.characterClass, mapType)
                },
                onBack = { currentScreen = Screen.CharacterSelect }
            )
        }

        is Screen.Game -> GameScreen(
            characterClass = screen.characterClass,
            mapType = screen.mapType,
            onGameOver = { stats -> currentScreen = Screen.GameOver(stats) }
        )

        is Screen.GameOver -> GameOverScreen(
            stats = screen.stats,
            onPlayAgain = { currentScreen = Screen.CharacterSelect },
            onMainMenu = { currentScreen = Screen.MainMenu }
        )

        is Screen.Settings -> SettingsScreen(
            settingsRepository = settingsRepository,
            soundManager = soundManager,
            musicManager = musicManager,
            onBack = { currentScreen = Screen.MainMenu }
        )

        is Screen.Shop -> ShopScreen(
            suppliesManager = suppliesManager,
            onBack = { currentScreen = Screen.MainMenu }
        )
    }
}

package app.krafted.nightmarehorde.ui.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.analytics.FirebaseAnalytics
import app.krafted.nightmarehorde.game.data.CharacterClass
import app.krafted.nightmarehorde.ui.screens.CharacterSelectScreen
import app.krafted.nightmarehorde.ui.screens.GameScreen
import app.krafted.nightmarehorde.ui.screens.MainMenuScreen
import app.krafted.nightmarehorde.ui.screens.MapSelectScreen

@Composable
fun NightmareHordeNavHost() {
    val context = LocalContext.current
    val analytics = remember { FirebaseAnalytics.getInstance(context) }
    
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MainMenu) }

    LaunchedEffect(currentScreen) {
        val screenName = when (currentScreen) {
            is Screen.MainMenu -> "MainMenuScreen"
            is Screen.CharacterSelect -> "CharacterSelectScreen"
            is Screen.MapSelect -> "MapSelectScreen"
            is Screen.Game -> "GameScreen"
            else -> "UnknownScreen"
        }
        
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        })
    }

    when (val screen = currentScreen) {
        is Screen.MainMenu -> MainMenuScreen(
            onPlayClicked = { currentScreen = Screen.CharacterSelect }
        )

        is Screen.CharacterSelect -> CharacterSelectScreen(
            onCharacterSelected = { characterClass ->
                currentScreen = Screen.MapSelect(characterClass)
            },
            onBack = { currentScreen = Screen.MainMenu }
        )

        is Screen.MapSelect -> MapSelectScreen(
            characterClass = screen.characterClass,
            onMapSelected = { mapType ->
                currentScreen = Screen.Game(screen.characterClass, mapType)
            },
            onBack = { currentScreen = Screen.CharacterSelect }
        )

        is Screen.Game -> GameScreen(
            characterClass = screen.characterClass,
            mapType = screen.mapType
        )

        else -> {}
    }
}

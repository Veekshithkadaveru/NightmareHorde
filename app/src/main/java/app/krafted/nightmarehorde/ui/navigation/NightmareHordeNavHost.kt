package app.krafted.nightmarehorde.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.krafted.nightmarehorde.game.data.CharacterClass
import app.krafted.nightmarehorde.ui.screens.CharacterSelectScreen
import app.krafted.nightmarehorde.ui.screens.GameScreen
import app.krafted.nightmarehorde.ui.screens.MainMenuScreen
import app.krafted.nightmarehorde.ui.screens.MapSelectScreen

@Composable
fun NightmareHordeNavHost() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MainMenu) }

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

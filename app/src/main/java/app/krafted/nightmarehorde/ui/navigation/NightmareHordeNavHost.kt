package app.krafted.nightmarehorde.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.krafted.nightmarehorde.game.data.CharacterType
import app.krafted.nightmarehorde.ui.screens.GameScreen
import app.krafted.nightmarehorde.ui.screens.MainMenuScreen

@Composable
fun NightmareHordeNavHost() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MainMenu) }

    when (val screen = currentScreen) {
        is Screen.MainMenu -> MainMenuScreen(
            onPlayClicked = { currentScreen = Screen.Game(CharacterType.CYBERPUNK_DETECTIVE) }
        )

        is Screen.Game -> GameScreen(
            characterType = screen.characterType
        )

        else -> {}
    }
}

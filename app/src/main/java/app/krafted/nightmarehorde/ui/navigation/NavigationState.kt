package app.krafted.nightmarehorde.ui.navigation

import app.krafted.nightmarehorde.game.data.CharacterClass
import app.krafted.nightmarehorde.game.data.CharacterType
import app.krafted.nightmarehorde.game.data.MapType

sealed class Screen {
    data object MainMenu : Screen()
    data object CharacterSelect : Screen()
    data class MapSelect(val characterClass: CharacterClass) : Screen()
    data class Game(val characterClass: CharacterClass, val mapType: MapType) : Screen()
    data class GameOver(val stats: GameOverStats) : Screen()
}

/** Extension to get the CharacterType from a Game screen for convenience. */
val Screen.Game.characterType: CharacterType get() = characterClass.characterType

data class GameOverStats(
    val survivalTimeSec: Float,
    val killCount: Int,
    val levelReached: Int,
    val bossesDefeated: Int,
    val characterType: CharacterType
)

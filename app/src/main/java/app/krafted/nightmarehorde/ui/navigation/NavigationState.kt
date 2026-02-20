package app.krafted.nightmarehorde.ui.navigation

import app.krafted.nightmarehorde.game.data.CharacterType

sealed class Screen {
    data object MainMenu : Screen()
    data object CharacterSelect : Screen()
    data class Game(val characterType: CharacterType) : Screen()
    data class GameOver(val stats: GameOverStats) : Screen()
}

data class GameOverStats(
    val survivalTimeSec: Float,
    val killCount: Int,
    val levelReached: Int,
    val bossesDefeated: Int,
    val characterType: CharacterType
)

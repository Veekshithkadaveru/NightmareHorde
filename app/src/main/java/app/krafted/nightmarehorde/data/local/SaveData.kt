package app.krafted.nightmarehorde.data.local

import kotlinx.serialization.Serializable

@Serializable
data class GameStats(
    val totalKills: Int = 0,
    val totalBossKills: Int = 0,
    val totalPlayTimeSeconds: Long = 0,
    val highestWave: Int = 0,
    val totalRunsPlayed: Int = 0,
    val totalSuppliesEarned: Int = 0,
)

@Serializable
data class SaveData(
    val supplies: Int = 0,
    val permanentUpgrades: Map<String, Int> = emptyMap(),
    val unlockedCharacters: Set<String> = setOf("CYBERPUNK_DETECTIVE"),
    val unlockedMaps: Set<String> = setOf("SUBURBS", "ASHEN_WASTES"),
    val stats: GameStats = GameStats(),
    val version: Int = 1,
    val lastSavedTimestamp: Long = 0,
)

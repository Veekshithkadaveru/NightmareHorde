package app.krafted.nightmarehorde.data.local

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveDataTest {

    private val json = Json { ignoreUnknownKeys = true }

    // --- SaveData defaults ---

    @Test
    fun `SaveData has correct default values`() {
        val saveData = SaveData()

        assertEquals(0, saveData.supplies)
        assertTrue(saveData.permanentUpgrades.isEmpty())
        assertTrue(saveData.unlockedCharacters.contains("CYBERPUNK_DETECTIVE"))
        assertEquals(1, saveData.unlockedCharacters.size)
        assertTrue(saveData.unlockedMaps.contains("SUBURBS"))
        assertTrue(saveData.unlockedMaps.contains("ASHEN_WASTES"))
        assertEquals(2, saveData.unlockedMaps.size)
        assertEquals(GameStats(), saveData.stats)
        assertEquals(1, saveData.version)
        assertEquals(0L, saveData.lastSavedTimestamp)
    }

    // --- SaveData serialization round-trip ---

    @Test
    fun `SaveData serializes and deserializes correctly`() {
        val original = SaveData(
            supplies = 500,
            permanentUpgrades = mapOf("SPEED" to 3, "ARMOR" to 1),
            unlockedCharacters = setOf("CYBERPUNK_DETECTIVE", "WARRIOR"),
            unlockedMaps = setOf("SUBURBS", "ASHEN_WASTES", "VOLCANO"),
            stats = GameStats(
                totalKills = 1000,
                totalBossKills = 5,
                totalPlayTimeSeconds = 3600,
                highestWave = 15,
                totalRunsPlayed = 20,
                totalSuppliesEarned = 2500,
            ),
            version = 1,
            lastSavedTimestamp = 1234567890L,
        )

        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<SaveData>(encoded)

        assertEquals(original, decoded)
    }

    // --- GameStats defaults ---

    @Test
    fun `GameStats has all fields at zero by default`() {
        val stats = GameStats()

        assertEquals(0, stats.totalKills)
        assertEquals(0, stats.totalBossKills)
        assertEquals(0L, stats.totalPlayTimeSeconds)
        assertEquals(0, stats.highestWave)
        assertEquals(0, stats.totalRunsPlayed)
        assertEquals(0, stats.totalSuppliesEarned)
    }

    // --- GameStats serialization round-trip ---

    @Test
    fun `GameStats serializes and deserializes correctly`() {
        val original = GameStats(
            totalKills = 42,
            totalBossKills = 3,
            totalPlayTimeSeconds = 7200,
            highestWave = 10,
            totalRunsPlayed = 8,
            totalSuppliesEarned = 900,
        )

        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<GameStats>(encoded)

        assertEquals(original, decoded)
    }

    // --- Forward compatibility ---

    @Test
    fun `Deserializing JSON with unknown fields succeeds`() {
        val jsonString = """
            {
                "supplies": 100,
                "permanentUpgrades": {},
                "unlockedCharacters": ["CYBERPUNK_DETECTIVE"],
                "unlockedMaps": ["SUBURBS", "ASHEN_WASTES"],
                "stats": {"totalKills": 0, "totalBossKills": 0, "totalPlayTimeSeconds": 0, "highestWave": 0, "totalRunsPlayed": 0, "totalSuppliesEarned": 0},
                "version": 1,
                "lastSavedTimestamp": 0,
                "newFeatureFlag": true,
                "futureData": {"key": "value"}
            }
        """.trimIndent()

        val decoded = json.decodeFromString<SaveData>(jsonString)

        assertEquals(100, decoded.supplies)
        assertEquals(1, decoded.version)
    }

    // --- Version field preserved ---

    @Test
    fun `SaveData with version 2 round-trips correctly`() {
        val original = SaveData(version = 2)

        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<SaveData>(encoded)

        assertEquals(2, decoded.version)
        assertEquals(original, decoded)
    }
}

package app.krafted.nightmarehorde.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaveSystemTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var encryption: SaveEncryption

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        prefs = context.getSharedPreferences("test_save_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        encryption = SaveEncryption()
    }

    @After
    fun tearDown() {
        prefs.edit().clear().commit()
    }

    // --- Encryption Tests ---

    @Test
    fun encryptionRoundTrip() {
        val original = "The quick brown fox jumps over the lazy dog"
        val encrypted = encryption.encrypt(original)
        val decrypted = encryption.decrypt(encrypted)
        assertEquals(original, decrypted)
    }

    @Test
    fun encryptionProducesDifferentCiphertext() {
        val plainText = "same input every time"
        val encrypted1 = encryption.encrypt(plainText)
        val encrypted2 = encryption.encrypt(plainText)
        assertNotEquals(
            "Encrypting the same string twice should produce different ciphertext due to random IV",
            encrypted1,
            encrypted2,
        )
        // Both should still decrypt to the original
        assertEquals(plainText, encryption.decrypt(encrypted1))
        assertEquals(plainText, encryption.decrypt(encrypted2))
    }

    @Test
    fun decryptionOfTamperedDataThrowsSaveCorruptionException() {
        val encrypted = encryption.encrypt("valid data")
        // Tamper with the middle of the ciphertext
        val chars = encrypted.toCharArray()
        val midpoint = chars.size / 2
        chars[midpoint] = if (chars[midpoint] == 'A') 'B' else 'A'
        val tampered = String(chars)

        try {
            encryption.decrypt(tampered)
            fail("Expected SaveCorruptionException when decrypting tampered data")
        } catch (e: SaveCorruptionException) {
            // Expected
        }
    }

    // --- SaveManager Tests ---

    @Test
    fun saveAndLoadRoundTrip() {
        val saveManager = SaveManager(prefs, encryption)

        // Modify currentSave via reflection since the setter is private
        val modifiedSave = saveManager.currentSave.copy(
            supplies = 500,
            permanentUpgrades = mapOf("speed" to 3, "damage" to 2),
            unlockedCharacters = setOf("CYBERPUNK_DETECTIVE", "WARRIOR"),
            stats = GameStats(totalKills = 42, highestWave = 7),
        )
        val field = SaveManager::class.java.getDeclaredField("currentSave")
        field.isAccessible = true
        field.set(saveManager, modifiedSave)

        saveManager.save()

        // Create a new SaveManager with the same prefs and encryption
        val freshManager = SaveManager(prefs, encryption)
        val loaded = freshManager.currentSave

        assertEquals(500, loaded.supplies)
        assertEquals(mapOf("speed" to 3, "damage" to 2), loaded.permanentUpgrades)
        assertEquals(setOf("CYBERPUNK_DETECTIVE", "WARRIOR"), loaded.unlockedCharacters)
        assertEquals(42, loaded.stats.totalKills)
        assertEquals(7, loaded.stats.highestWave)
        // save() sets lastSavedTimestamp, so it should be non-zero
        assertTrue("lastSavedTimestamp should be set after save()", loaded.lastSavedTimestamp > 0)
    }

    @Test
    fun loadWithNoExistingSaveReturnsDefaults() {
        val saveManager = SaveManager(prefs, encryption)
        val defaults = SaveData()

        assertEquals(defaults.supplies, saveManager.currentSave.supplies)
        assertEquals(defaults.permanentUpgrades, saveManager.currentSave.permanentUpgrades)
        assertEquals(defaults.unlockedCharacters, saveManager.currentSave.unlockedCharacters)
        assertEquals(defaults.unlockedMaps, saveManager.currentSave.unlockedMaps)
        assertEquals(defaults.stats, saveManager.currentSave.stats)
        assertEquals(defaults.version, saveManager.currentSave.version)
        assertEquals(defaults.lastSavedTimestamp, saveManager.currentSave.lastSavedTimestamp)
    }

    @Test
    fun resetClearsSave() {
        val saveManager = SaveManager(prefs, encryption)

        // Set up non-default save data
        val modifiedSave = saveManager.currentSave.copy(
            supplies = 999,
            permanentUpgrades = mapOf("armor" to 5),
        )
        val field = SaveManager::class.java.getDeclaredField("currentSave")
        field.isAccessible = true
        field.set(saveManager, modifiedSave)
        saveManager.save()

        // Reset and verify currentSave is back to defaults
        saveManager.reset()
        val defaults = SaveData()
        assertEquals(defaults, saveManager.currentSave)

        // Verify a new SaveManager with the same prefs also loads defaults
        val freshManager = SaveManager(prefs, encryption)
        assertEquals(defaults, freshManager.currentSave)
    }

    @Test
    fun corruptionRecoveryLoadsDefaults() {
        // Write garbage to the SharedPreferences key used by SaveManager
        prefs.edit().putString("encrypted_save_data", "not_valid_encrypted_data!!!").commit()

        // Creating a SaveManager should recover gracefully and return defaults
        val saveManager = SaveManager(prefs, encryption)
        val defaults = SaveData()
        assertEquals(defaults, saveManager.currentSave)
    }
}

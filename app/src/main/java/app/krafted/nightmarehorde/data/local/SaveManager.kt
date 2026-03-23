package app.krafted.nightmarehorde.data.local

import android.content.SharedPreferences
import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveManager @Inject constructor(
    private val prefs: SharedPreferences,
    private val encryption: SaveEncryption,
) {

    private companion object {
        const val TAG = "SaveManager"
        const val KEY_ENCRYPTED_SAVE = "encrypted_save_data"
    }

    private val json = Json { ignoreUnknownKeys = true }

    var currentSave: SaveData = load()
        private set

    fun save() {
        currentSave = currentSave.copy(lastSavedTimestamp = System.currentTimeMillis())
        val serialized = json.encodeToString(SaveData.serializer(), currentSave)
        val encrypted = encryption.encrypt(serialized)
        prefs.edit().putString(KEY_ENCRYPTED_SAVE, encrypted).apply()
    }

    fun load(): SaveData {
        val encrypted = prefs.getString(KEY_ENCRYPTED_SAVE, null) ?: return SaveData()
        return try {
            val decrypted = encryption.decrypt(encrypted)
            json.decodeFromString(SaveData.serializer(), decrypted)
        } catch (e: SaveCorruptionException) {
            Log.w(TAG, "Save data corrupted, starting fresh", e)
            SaveData()
        } catch (e: SerializationException) {
            Log.w(TAG, "Save data corrupted, starting fresh", e)
            SaveData()
        }
    }

    fun reset() {
        prefs.edit().remove(KEY_ENCRYPTED_SAVE).apply()
        currentSave = SaveData()
    }
}

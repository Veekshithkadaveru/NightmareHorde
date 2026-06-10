package app.krafted.nightmarehorde.engine.audio

/**
 * Shared contract for the engine's audio playback managers.
 *
 * [MusicManager] (streamed background music via MediaPlayer) and [SoundManager]
 * (low-latency one-shots via SoundPool) use completely different Android
 * playback backends, so they intentionally do NOT share a base implementation.
 * What they do share is this small volume/mute/lifecycle surface, which lets
 * settings code drive them uniformly.
 */
interface AudioManager {
    /** When true, output is silenced without losing the configured [volume]. */
    var isMuted: Boolean

    /** Playback volume in the range [0, 1]. */
    var volume: Float

    /** Release native audio resources. The manager is unusable afterwards. */
    fun release()
}

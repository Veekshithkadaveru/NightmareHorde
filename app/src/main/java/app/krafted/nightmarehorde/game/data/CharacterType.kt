package app.krafted.nightmarehorde.game.data

/**
 * Defines the playable characters available in the game.
 * Each character specifies its sprite sheet assets, frame dimensions,
 * frame counts, and rendering properties.
 *
 * All spritesheets are horizontal strips where each frame occupies
 * [frameWidth] × [frameHeight] pixels.
 */
enum class CharacterType(
    val displayName: String,
    val idleTextureKey: String,
    val runTextureKey: String,
    val frameWidth: Int,
    val frameHeight: Int,
    val idleFrameCount: Int,
    val runFrameCount: Int,
    val scale: Float,
    val colliderRadius: Float
) {
    CYBERPUNK_DETECTIVE(
        displayName = "Cyberpunk Detective",
        idleTextureKey = "player_idle_sheet",
        runTextureKey = "player_run_sheet",
        frameWidth = 32,
        frameHeight = 32,
        idleFrameCount = 6,
        runFrameCount = 6,
        scale = 3f,
        colliderRadius = 10f
    ),

    TERRIBLE_KNIGHT(
        displayName = "Terrible Knight",
        idleTextureKey = "knight_idle_sheet",
        runTextureKey = "knight_run_sheet",
        frameWidth = 128,
        frameHeight = 96,
        idleFrameCount = 4,
        runFrameCount = 12,
        scale = 1.5f,
        colliderRadius = 20f
    ),

    WEREWOLF(
        displayName = "WereWolf",
        idleTextureKey = "werewolf_idle_sheet",
        runTextureKey = "werewolf_run_sheet",
        frameWidth = 96,
        frameHeight = 76,
        idleFrameCount = 5,
        runFrameCount = 6,
        scale = 1.8f,
        colliderRadius = 18f
    ),

    NIGHTMARE(
        displayName = "Nightmare",
        idleTextureKey = "nightmare_idle_sheet",
        runTextureKey = "nightmare_run_sheet",
        frameWidth = 96,
        frameHeight = 96,
        idleFrameCount = 7,
        runFrameCount = 5,
        scale = 1.5f,
        colliderRadius = 20f
    ),

    HELL_HOUND(
        displayName = "Hell Hound",
        idleTextureKey = "hellhound_idle_sheet",
        runTextureKey = "hellhound_run_sheet",
        frameWidth = 64,
        frameHeight = 48,
        idleFrameCount = 11,
        runFrameCount = 5,
        scale = 2.5f,
        colliderRadius = 14f
    ),

    GHOST(
        displayName = "Ghost",
        idleTextureKey = "ghost_idle_sheet",
        runTextureKey = "ghost_run_sheet",
        frameWidth = 64,
        frameHeight = 80,
        idleFrameCount = 7,
        runFrameCount = 4,
        scale = 2f,
        colliderRadius = 16f
    ),

    OGRE(
        displayName = "Ogre",
        idleTextureKey = "ogre_idle_sheet",
        runTextureKey = "ogre_run_sheet",
        frameWidth = 96,
        frameHeight = 80,
        idleFrameCount = 6,
        runFrameCount = 9,
        scale = 1.5f,
        colliderRadius = 20f
    ),

    DEATH(
        displayName = "Death",
        idleTextureKey = "death_idle_sheet",
        runTextureKey = "death_run_sheet",
        frameWidth = 48,
        frameHeight = 48,
        idleFrameCount = 4,
        runFrameCount = 4,
        scale = 2.5f,
        colliderRadius = 12f
    )
}

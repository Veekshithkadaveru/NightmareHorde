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
        scale = 3.7f,
        colliderRadius = 15f
    ),

    TERRIBLE_KNIGHT(
        displayName = "Terrible Knight",
        idleTextureKey = "knight_idle_sheet",
        runTextureKey = "knight_run_sheet",
        frameWidth = 128,
        frameHeight = 96,
        idleFrameCount = 4,
        runFrameCount = 12,
        scale = 1.24f,
        colliderRadius = 15f
    ),

    SOLDIER(
        displayName = "Soldier",
        idleTextureKey = "soldier_idle_sheet",
        runTextureKey = "soldier_run_sheet",
        frameWidth = 32,
        frameHeight = 32,
        idleFrameCount = 6,
        runFrameCount = 6,
        scale = 3.7f,
        colliderRadius = 15f
    ),

    COMMANDO(
        displayName = "Commando",
        idleTextureKey = "commando_idle_sheet",
        runTextureKey = "commando_run_sheet",
        frameWidth = 32,
        frameHeight = 32,
        idleFrameCount = 6,
        runFrameCount = 6,
        scale = 3.7f,
        colliderRadius = 15f
    ),

    SPACE_MARINE(
        displayName = "Space Marine",
        idleTextureKey = "spacemarine_idle_sheet",
        runTextureKey = "spacemarine_run_sheet",
        frameWidth = 75,
        frameHeight = 48,
        idleFrameCount = 4,
        runFrameCount = 10,
        scale = 2.5f,
        colliderRadius = 15f
    ),

    ENFORCER(
        displayName = "Enforcer",
        idleTextureKey = "enforcer_idle_sheet",
        runTextureKey = "enforcer_run_sheet",
        frameWidth = 64,
        frameHeight = 60,
        idleFrameCount = 2,
        runFrameCount = 6,
        scale = 2f,
        colliderRadius = 15f
    ),

    HUNTER(
        displayName = "Hunter",
        idleTextureKey = "hunter_idle_sheet",
        runTextureKey = "hunter_run_sheet",
        frameWidth = 62,
        frameHeight = 54,
        idleFrameCount = 6,
        runFrameCount = 7,
        scale = 2.2f,
        colliderRadius = 15f
    )
}

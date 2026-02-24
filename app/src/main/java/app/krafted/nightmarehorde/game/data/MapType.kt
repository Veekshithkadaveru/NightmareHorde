package app.krafted.nightmarehorde.game.data

/**
 * Defines the 5 playable map environments.
 * Each map has a unique theme, background, unlock requirement, size, and special feature.
 *
 * Maps are centered at world origin (0, 0). Boundaries run from -halfWidth..-halfWidth
 * on X and -halfHeight..halfHeight on Y.
 */
enum class MapType(
    val displayName: String,
    val description: String,
    /** Drawable texture key for the background (tiled via fillViewport) */
    val backgroundTextureKey: String,
    /** Fallback ARGB color if texture is missing */
    val backgroundColor: Long,
    /** UI accent color for map cards and HUD */
    val accentColor: Long,
    /** Supplies cost to unlock (0 = free) */
    val unlockCost: Int,
    /** Minimum total boss kills required to unlock (0 = none) */
    val requiredBossKills: Int,
    /** Total map width in world units */
    val mapWidth: Float,
    /** Total map height in world units */
    val mapHeight: Float,
    /** Player spawn X (world units, relative to center) */
    val spawnX: Float,
    /** Player spawn Y (world units, relative to center) */
    val spawnY: Float,
    /** Human-readable description of the map's special feature */
    val specialFeature: String
) {
    SUBURBS(
        displayName = "Suburbs",
        description = "Dark urban streets littered with abandoned vehicles and debris.",
        backgroundTextureKey = "background_suburbs",
        backgroundColor = 0xFF1A1A2E,
        accentColor = 0xFF4A90E2,
        unlockCost = 0,
        requiredBossKills = 0,
        mapWidth = 4000f,
        mapHeight = 4000f,
        spawnX = 0f,
        spawnY = 0f,
        specialFeature = "Wrecked cars form impassable cover clusters"
    ),

    MALL(
        displayName = "Mall",
        description = "An abandoned indoor shopping center. Dark corridors between storefronts.",
        backgroundTextureKey = "background_mall",
        backgroundColor = 0xFF2D1B4E,
        accentColor = 0xFFFF9800,
        unlockCost = 500,
        requiredBossKills = 0,
        mapWidth = 4000f,
        mapHeight = 4000f,
        spawnX = 0f,
        spawnY = 0f,
        specialFeature = "Store rooms spawn ammo pickups every 30 seconds"
    ),

    ASHEN_WASTES(
        displayName = "Ashen Wastes",
        description = "A barren, post-apocalyptic wasteland of cracked earth and scattered debris.",
        backgroundTextureKey = "background_ashen_wastes",
        backgroundColor = 0xFF2A2A2A,
        accentColor = 0xFFDD4444,
        unlockCost = 0,
        requiredBossKills = 0,
        mapWidth = 8000f,
        mapHeight = 8000f,
        spawnX = 0f,
        spawnY = 0f,
        specialFeature = "Wide open spaces allow the horde to surround you quickly."
    ),


    MILITARY_BASE(
        displayName = "Military Base",
        description = "An open desert compound with reinforced defensive fortifications.",
        backgroundTextureKey = "background_military",
        backgroundColor = 0xFF2E2A1A,
        accentColor = 0xFFFFD700,
        unlockCost = 1500,
        requiredBossKills = 0,
        mapWidth = 4800f,
        mapHeight = 4800f,
        spawnX = 0f,
        spawnY = 0f,
        specialFeature = "Ammo caches scattered throughout the compound"
    ),

    LAB(
        displayName = "Lab",
        description = "A sci-fi underground bunker with automated laser defense systems.",
        backgroundTextureKey = "background_lab",
        backgroundColor = 0xFF0D1B2A,
        accentColor = 0xFFCC22FF,
        unlockCost = 0,
        requiredBossKills = 5,
        mapWidth = 3600f,
        mapHeight = 3600f,
        spawnX = 0f,
        spawnY = 0f,
        specialFeature = "Laser grid traps deal 5 damage/s to anyone caught inside"
    );

    /** Half-width of the map (map extends ±halfWidth on X axis) */
    val halfWidth: Float get() = mapWidth / 2f

    /** Half-height of the map (map extends ±halfHeight on Y axis) */
    val halfHeight: Float get() = mapHeight / 2f

    /** True if this map is unlocked by default with no cost */
    val isDefaultUnlocked: Boolean get() = unlockCost == 0 && requiredBossKills == 0

    /** Human-readable unlock requirement string for the UI */
    val unlockRequirement: String
        get() = when {
            isDefaultUnlocked -> "Available"
            requiredBossKills > 0 -> "Kill $requiredBossKills bosses total"
            else -> "$unlockCost supplies"
        }
}

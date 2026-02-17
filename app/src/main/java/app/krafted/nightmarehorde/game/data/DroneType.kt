package app.krafted.nightmarehorde.game.data

/**
 * Defines the 4 orbital drone types with base stats.
 * Level scaling: Lv2 = +40% dmg / +20% fire rate, Lv3 = +80% dmg / +40% fire rate + special.
 */
enum class DroneType(
    val displayName: String,
    val baseDamage: Float,
    val baseFireRate: Float,
    val range: Float,
    val projectileSpeed: Float,
    val projectileCount: Int,
    val spreadAngle: Float,
    val maxChainTargets: Int,
    val maxAoeTargets: Int,
    val isTickBased: Boolean,
    val accentColor: Long,
    val textureKey: String
) {
    GUNNER(
        displayName = "Gunner Drone",
        baseDamage = 6f,
        baseFireRate = 4f,
        range = 150f,
        projectileSpeed = 600f,
        projectileCount = 1,
        spreadAngle = 0f,
        maxChainTargets = 0,
        maxAoeTargets = 0,
        isTickBased = false,
        accentColor = 0xFF44AAFF,
        textureKey = "drone_gunner"
    ),

    SCATTER(
        displayName = "Scatter Drone",
        baseDamage = 4f,
        baseFireRate = 1f,
        range = 120f,
        projectileSpeed = 450f,
        projectileCount = 5,
        spreadAngle = 40f,
        maxChainTargets = 0,
        maxAoeTargets = 0,
        isTickBased = false,
        accentColor = 0xFFFFAA00,
        textureKey = "drone_scatter"
    ),

    INFERNO(
        displayName = "Inferno Drone",
        baseDamage = 3f,
        baseFireRate = 2f,
        range = 80f,
        projectileSpeed = 0f,
        projectileCount = 0,
        spreadAngle = 0f,
        maxChainTargets = 0,
        maxAoeTargets = 8,
        isTickBased = true,
        accentColor = 0xFFFF4400,
        textureKey = "drone_inferno"
    ),

    ARC(
        displayName = "Arc Drone",
        baseDamage = 8f,
        baseFireRate = 0.5f,
        range = 180f,
        projectileSpeed = 800f,
        projectileCount = 1,
        spreadAngle = 0f,
        maxChainTargets = 3,
        maxAoeTargets = 0,
        isTickBased = false,
        accentColor = 0xFF8844FF,
        textureKey = "drone_arc"
    );

    fun damageAtLevel(level: Int): Float = when (level) {
        2 -> baseDamage * 1.4f
        3 -> baseDamage * 1.8f
        else -> baseDamage
    }

    fun fireRateAtLevel(level: Int): Float = when (level) {
        2 -> baseFireRate * 1.2f
        3 -> baseFireRate * 1.4f
        else -> baseFireRate
    }

    fun fuelDrainRate(level: Int): Float = when (level) {
        2 -> 0.8f
        3 -> 0.6f
        else -> 1.0f
    }

    fun maxFuel(level: Int): Float = when (level) {
        2 -> 75f
        3 -> 100f
        else -> 60f
    }

    /** Arc Lv3: chain to 5 instead of 3 */
    fun chainTargetsAtLevel(level: Int): Int = when {
        this != ARC -> 0
        level >= 3 -> 5
        else -> maxChainTargets
    }

    /** Inferno Lv3: double burn radius */
    fun effectiveRange(level: Int): Float = when {
        this == INFERNO && level >= 3 -> range * 2f
        else -> range
    }
}

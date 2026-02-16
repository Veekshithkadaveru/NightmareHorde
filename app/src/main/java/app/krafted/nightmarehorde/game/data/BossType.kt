package app.krafted.nightmarehorde.game.data

import app.krafted.nightmarehorde.engine.core.components.AIBehavior

/**
 * Defines the 3 boss types with base stats and attack parameters.
 * HP scales per encounter: Base × (1 + 0.5 × bossNumber)
 */
enum class BossType(
    val displayName: String,
    val assetName: String,
    val baseHealth: Int,
    val damage: Float,
    val moveSpeed: Float,
    val xpReward: Int,
    val colliderRadius: Float,
    val behavior: AIBehavior,
    val frameWidth: Int,
    val frameHeight: Int,
    val frameCount: Int,
    val animationFps: Float,
    val scale: Float,
    /** Accent color used by the boss health bar and effects */
    val accentColor: Long = 0xFFFF4444,
    /**
     * Damage resistance vs multi-hit / penetrating weapons (0.0 = none, 1.0 = immune).
     * Applied in CombatSystem when a penetrating projectile hits a boss.
     * Prevents weapons like the Whip Blade (12 penetrating segments) from melting bosses.
     */
    val multiHitResistance: Float = 0.5f,
    /**
     * Preferred combat distance — boss will try to retreat to this range
     * when the player gets too close, creating windows for ranged attacks.
     */
    val preferredRange: Float = 200f
) {
    // Ordered weakest → strongest. The spawn order follows enum ordinal.

    /**
     * Hive Queen — An insectoid queen that spawns minions.
     * Attacks: Spawn Minions, Acid Spray (cone projectiles), Burrow (teleport + AOE)
     * First boss encounter — lowest HP & damage, but tricky mechanics.
     */
    HIVE_QUEEN(
        displayName = "Hive Queen",
        assetName = "boss_hive_queen",
        baseHealth = 600,
        damage = 25f,
        moveSpeed = 40f,
        xpReward = 80,
        colliderRadius = 36f,
        behavior = AIBehavior.BOSS_HIVE_QUEEN,
        frameWidth = 48,
        frameHeight = 48,
        frameCount = 8,
        animationFps = 10f,
        scale = 2.0f,              // Small sprite scaled up to boss size
        accentColor = 0xFF44CC44,  // Toxic green
        multiHitResistance = 0.55f, // 55% reduced damage from multi-hit weapons
        preferredRange = 250f       // Prefers mid-range for acid spray
    ),

    /**
     * The Tank — A massive armored zombie.
     * Attacks: Ground Slam (AOE), Rock Throw (projectile), Charge (dash)
     * Mid-tier boss — high HP, heavy melee damage.
     */
    TANK(
        displayName = "The Tank",
        assetName = "boss_idle",
        baseHealth = 800,
        damage = 40f,
        moveSpeed = 35f,
        xpReward = 120,
        colliderRadius = 40f,
        behavior = AIBehavior.BOSS_TANK,
        frameWidth = 192,
        frameHeight = 144,
        frameCount = 5,
        animationFps = 6f,
        scale = 1.2f,
        accentColor = 0xFFDD8833,  // Amber / rust
        multiHitResistance = 0.60f, // 60% reduced — heavily armored
        preferredRange = 300f       // Prefers distance for rock throws
    ),

    /**
     * Abomination — A multi-armed horror that regenerates.
     * Attacks: Multi-Arm Swipe (wide melee arc), Regen (passive heal), Enrage (below 30% HP)
     * Final boss — highest HP & damage, regenerates, enrages at low HP.
     */
    ABOMINATION(
        displayName = "Abomination",
        assetName = "boss_abomination",
        baseHealth = 1200,
        damage = 50f,
        moveSpeed = 30f,
        xpReward = 180,
        colliderRadius = 44f,
        behavior = AIBehavior.BOSS_ABOMINATION,
        frameWidth = 80,
        frameHeight = 160,
        frameCount = 6,
        animationFps = 6f,
        scale = 1.2f,              // Multi-tentacled horror
        accentColor = 0xFFCC22FF,  // Necrotic purple
        multiHitResistance = 0.50f, // 50% reduced damage from multi-hit weapons
        preferredRange = 220f       // Mix of melee and ranged
    );

    /**
     * Calculate scaled HP for the Nth boss encounter.
     * Formula: baseHealth × (1 + 0.5 × bossNumber)
     */
    fun scaledHealth(bossNumber: Int): Int {
        return (baseHealth * (1f + 0.5f * bossNumber)).toInt()
    }
}

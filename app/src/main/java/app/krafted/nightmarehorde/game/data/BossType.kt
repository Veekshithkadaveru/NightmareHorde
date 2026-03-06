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
     * The Widowmaker — A giant venomous mutant spider.
     * Attacks: Spawn Spiderlings, Web Throw (slows player), Drop (teleport from ceiling + AOE)
     * First boss encounter — lowest HP, but floods the map with minions and webs.
     */
    WIDOWMAKER(
        displayName = "The Widowmaker",
        assetName = "boss_widowmaker",
        baseHealth = 600,
        damage = 25f,
        moveSpeed = 45f,
        xpReward = 80,
        colliderRadius = 36f,
        behavior = AIBehavior.BOSS_WIDOWMAKER,
        frameWidth = 80,
        frameHeight = 80,
        frameCount = 7,
        animationFps = 10f,
        scale = 2.5f,              // Boss size scalar
        accentColor = 0xFF8844FF,  // Venomous purple
        multiHitResistance = 0.55f, // 55% reduced damage from multi-hit weapons
        preferredRange = 250f       // Prefers mid-range for web throws
    ),

    /**
     * The Executioner — A colossal undead gladiator wielding a bloody battleaxe.
     * Attacks: Axe Sweep (heavy melee), Throwing Axes (projectile), Leaping Strike (dash + slam)
     * Mid-tier boss — massive HP, heavy slow melee damage.
     */
    EXECUTIONER(
        displayName = "The Executioner",
        assetName = "boss_executioner",
        baseHealth = 800,
        damage = 40f,
        moveSpeed = 35f,
        xpReward = 120,
        colliderRadius = 40f,
        behavior = AIBehavior.BOSS_EXECUTIONER,
        frameWidth = 72,
        frameHeight = 100,
        frameCount = 6,
        animationFps = 6f,
        scale = 2.5f,              // Boss size scalar
        accentColor = 0xFFAA2222,  // Blood red
        multiHitResistance = 0.60f, // 60% reduced — heavily armored
        preferredRange = 150f       // Closer than tank, throwing axes are shorter range
    ),

    /**
     * The Behemoth — A gargantuan mutated brute, replacing the Amalgam.
     * Attacks: Fleshy Sweep (wide melee arc), Bone Splinters (ground spikes), Mutate (passive regen + enrage)
     * Final boss — highest HP & damage, regenerates, enrages at low HP.
     */
    FLESH_AMALGAM(
        displayName = "The Behemoth",
        assetName = "boss_amalgam", // Keep the same asset key to overwrite without huge refactors
        baseHealth = 1200,
        damage = 50f,
        moveSpeed = 30f,
        xpReward = 180,
        colliderRadius = 40f,
        behavior = AIBehavior.BOSS_AMALGAM,
        frameWidth = 102, // 612 / 6 = 102
        frameHeight = 110,
        frameCount = 6,
        animationFps = 6f,
        scale = 2.5f,              // Massive hulking brute
        accentColor = 0xFF44AA33,  // Mutated green
        multiHitResistance = 0.50f,
        preferredRange = 220f
    );

    /**
     * Calculate scaled HP for the Nth boss encounter.
     * Formula: baseHealth × (1 + 0.5 × bossNumber)
     */
    fun scaledHealth(bossNumber: Int): Int {
        return (baseHealth * (1f + 0.5f * bossNumber)).toInt()
    }
}

package app.krafted.nightmarehorde.game.data

import app.krafted.nightmarehorde.engine.core.components.AIBehavior

/**
 * Defines the base stats and behavior for different zombie types.
 */
enum class ZombieType(
    val assetName: String,
    val maxHealth: Int,
    val damage: Float,
    val moveSpeed: Float,
    val xpReward: Int,
    val colliderRadius: Float,
    val behavior: AIBehavior,
    val behaviorRange: Float = 0f,
    val frameWidth: Int,
    val frameHeight: Int,
    val frameCount: Int,
    val animationFps: Float = 10f,
    val scale: Float = 1f
) {
    // Player reference: 32x32 @ scale 3.0 = 96x96 effective pixels
    // Regular zombies: 60-90% of player size. Brute: ~130%.
    WALKER(
        assetName = "zombie_walker_run",
        maxHealth = 15,
        damage = 5f,
        moveSpeed = 40f,
        xpReward = 1,
        colliderRadius = 12f,
        behavior = AIBehavior.CHASE,
        frameWidth = 80,
        frameHeight = 96,
        frameCount = 6,
        animationFps = 10f,
        scale = 0.9f  // 72x86 — standard zombie, slightly smaller than player
    ),
    RUNNER(
        assetName = "zombie_runner_run",
        maxHealth = 10,
        damage = 8f,
        moveSpeed = 90f,
        xpReward = 1,
        colliderRadius = 10f,
        behavior = AIBehavior.CHASE,
        frameWidth = 57,
        frameHeight = 42,
        frameCount = 6,
        animationFps = 12f,
        scale = 1.5f  // 86x63 — small and nimble
    ),
    BLOATER(
        assetName = "zombie_bloater_run",
        maxHealth = 50,
        damage = 15f,
        moveSpeed = 30f,
        xpReward = 3,
        colliderRadius = 16f,
        behavior = AIBehavior.EXPLODE,
        behaviorRange = 40f,
        frameWidth = 80,
        frameHeight = 64,
        frameCount = 4,
        animationFps = 8f,
        scale = 1.2f  // 96x77 — round toad, about player width
    ),
    SPITTER(
        assetName = "zombie_spitter_run",
        maxHealth = 20,
        damage = 10f,
        moveSpeed = 35f,
        xpReward = 2,
        colliderRadius = 12f,
        behavior = AIBehavior.RANGED,
        behaviorRange = 250f,
        frameWidth = 42,
        frameHeight = 38,
        frameCount = 10,
        animationFps = 10f,
        scale = 1.8f  // 76x68 — smaller ranged enemy
    ),
    BRUTE(
        assetName = "zombie_brute_run",
        maxHealth = 100,
        damage = 25f,
        moveSpeed = 50f,
        xpReward = 5,
        colliderRadius = 22f,
        behavior = AIBehavior.CHARGE,
        behaviorRange = 200f,
        frameWidth = 144,
        frameHeight = 80,
        frameCount = 6,
        animationFps = 8f,
        scale = 0.9f  // 130x72 — wide and imposing, the big tank
    ),
    CRAWLER(
        assetName = "zombie_crawler_run",
        maxHealth = 8,
        damage = 12f,
        moveSpeed = 80f,
        xpReward = 1,
        colliderRadius = 8f,
        behavior = AIBehavior.CHASE,
        frameWidth = 160,
        frameHeight = 144,
        frameCount = 6,
        animationFps = 12f,
        scale = 0.4f  // 64x58 — smallest, low-profile enemy
    ),
    SCREAMER(
        assetName = "zombie_screamer_run",
        maxHealth = 30,
        damage = 5f,
        moveSpeed = 45f,
        xpReward = 4,
        colliderRadius = 12f,
        behavior = AIBehavior.BUFF,
        behaviorRange = 150f,
        frameWidth = 64,
        frameHeight = 80,
        frameCount = 4,
        animationFps = 6f,
        scale = 0.9f  // 58x72 — medium, support enemy
    )
}

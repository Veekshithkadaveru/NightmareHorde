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
    val scale: Float = 1f,
    val defaultFlipX: Boolean = false
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
        frameWidth = 63, // 382 / 6 = 63.66 -> using 63
        frameHeight = 96,
        frameCount = 6,
        animationFps = 10f,
        scale = 1.0f  // Since height is 96, no scale needed to match ~86 as before (was 0.9*96=86). We can keep it 1.0f.
    ),
    RUNNER(
        assetName = "zombie_runner_run",
        maxHealth = 10,
        damage = 8f,
        moveSpeed = 90f,
        xpReward = 1,
        colliderRadius = 10f,
        behavior = AIBehavior.CHASE,
        frameWidth = 107, // 642 / 6 = 107
        frameHeight = 64,
        frameCount = 6,
        animationFps = 12f,
        scale = 1.0f  // Since height is 64, matches roughly the intended ~63.
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
        frameWidth = 46, // 276 / 6 = 46
        frameHeight = 64,
        frameCount = 6, 
        animationFps = 8f,
        scale = 2.2f  // Increased to 1.5x player size
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
        frameWidth = 95, // 574 / 6 = ~95
        frameHeight = 64,
        frameCount = 6, // Updated to 6 frames
        animationFps = 10f,
        scale = 1.0f,  // Standard 1.0 scale is fine since base height is ~64
        defaultFlipX = false // New sprites face right by default
    ),
    ARMORED(
        assetName = "zombie_armored_run",
        maxHealth = 150, // Heavily armored
        damage = 18f,
        moveSpeed = 30f, // Slow stopming movement
        xpReward = 6,
        colliderRadius = 14f,
        behavior = AIBehavior.CHASE,
        behaviorRange = 0f,
        frameWidth = 41, // 246 / 6
        frameHeight = 64,
        frameCount = 6,
        animationFps = 8f,
        scale = 2.2f  // Increased to 1.5x player size
    ),
    HELLHOUND(
        assetName = "zombie_hellhound_run",
        maxHealth = 35, // Low health, high speed
        damage = 10f,
        moveSpeed = 100f, // Very fast
        xpReward = 4,
        colliderRadius = 12f,
        behavior = AIBehavior.CHASE,
        behaviorRange = 0f,
        frameWidth = 66, // 396 / 6
        frameHeight = 40,
        frameCount = 6,
        animationFps = 16f,
        scale = 1.5f  // Height is 40, 1.5x puts it around 60px visual height
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
        frameWidth = 68, // 408 / 6 = 68
        frameHeight = 74,
        frameCount = 6,
        animationFps = 8f,
        scale = 2.0f  // Increased to 1.5x player size
    ),
    CRAWLER(
        assetName = "zombie_crawler_run",
        maxHealth = 40,
        damage = 12f,
        moveSpeed = 80f,
        xpReward = 3,
        colliderRadius = 14f,
        behavior = AIBehavior.CHASE,
        behaviorRange = 0f,
        frameWidth = 40, // 202 / 5 = ~40
        frameHeight = 32,
        frameCount = 5,
        animationFps = 15f,
        scale = 2.0f  // Since height is 32px, 2.0x scales it to match the ~64px height of others
    ),
    SCREAMER(
        assetName = "zombie_screamer_run",
        maxHealth = 30, // Low health
        damage = 5f, // Minimal melee damage, primary threat is calling others
        moveSpeed = 45f,
        xpReward = 4,
        colliderRadius = 10f,
        behavior = AIBehavior.FLEE, // Needs logic: run away while screaming
        behaviorRange = 300f,
        frameWidth = 51, // 306 / 6 = 51
        frameHeight = 64,
        frameCount = 6,
        animationFps = 12f,
        scale = 1.0f  // Standard zombie height scale since it's 64px
    )
}

package app.krafted.nightmarehorde.game.data

/**
 * Defines the different obstacle types that can be spawned in the world.
 * Each type has its own texture, visual size, and collider dimensions.
 *
 * All sizes are in world units. Sprites are individual PNGs loaded by textureKey.
 */
enum class ObstacleType(
    /** Drawable resource name (without extension) */
    val textureKey: String,
    /** Display width in world units */
    val spriteWidth: Float,
    /** Display height in world units */
    val spriteHeight: Float,
    /** Collider half-width for AABB (already accounts for visual scale) */
    val colliderHalfWidth: Float,
    /** Collider half-height for AABB (already accounts for visual scale) */
    val colliderHalfHeight: Float,
    /** Render scale multiplier */
    val scale: Float = 1f,
    /** Relative spawn weight (higher = more common) */
    val spawnWeight: Int = 10
) {
    ROCK(
        textureKey = "obstacle_rock_small",
        spriteWidth = 32f,
        spriteHeight = 32f,
        colliderHalfWidth = 10f,
        colliderHalfHeight = 10f,
        scale = 0.7f,
        spawnWeight = 15
    ),
    TREE(
        textureKey = "obstacle_tree_pine",
        spriteWidth = 48f,
        spriteHeight = 48f,
        colliderHalfWidth = 20f,
        colliderHalfHeight = 20f,
        scale = 1.5f,
        spawnWeight = 12
    ),
    BUSH(
        textureKey = "obstacle_bush",
        spriteWidth = 16f,
        spriteHeight = 32f,
        colliderHalfWidth = 14f,
        colliderHalfHeight = 14f,
        scale = 1.2f,
        spawnWeight = 15
    ),
    BARREL(
        textureKey = "obstacle_barrel",
        spriteWidth = 16f,
        spriteHeight = 16f,
        colliderHalfWidth = 10f,
        colliderHalfHeight = 10f,
        scale = 1.0f,
        spawnWeight = 10
    );

    companion object {
        /** Total weight of all types, for weighted random selection */
        val totalWeight: Int by lazy { entries.sumOf { it.spawnWeight } }

        /**
         * Pick a random obstacle type using weighted selection.
         * @param roll A random value in [0, totalWeight)
         */
        fun weightedRandom(roll: Int): ObstacleType {
            var remaining = roll
            for (type in entries) {
                remaining -= type.spawnWeight
                if (remaining < 0) return type
            }
            return entries.last()
        }
    }
}

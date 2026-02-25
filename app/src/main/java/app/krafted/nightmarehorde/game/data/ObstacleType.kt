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
    val validMaps: Set<MapType> = emptySet(),
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
        validMaps = setOf(MapType.ASHEN_WASTES, MapType.MILITARY_BASE, MapType.LAB),
        scale = 0.7f,
        spawnWeight = 15
    ),
    TREE(
        textureKey = "obstacle_tree_pine",
        spriteWidth = 48f,
        spriteHeight = 48f,
        colliderHalfWidth = 20f,
        colliderHalfHeight = 20f,
        validMaps = emptySet(),
        scale = 1.5f,
        spawnWeight = 12
    ),
    BUSH(
        textureKey = "obstacle_bush",
        spriteWidth = 16f,
        spriteHeight = 32f,
        colliderHalfWidth = 14f,
        colliderHalfHeight = 14f,
        validMaps = setOf(MapType.SUBURBS, MapType.MALL),
        scale = 1.2f,
        spawnWeight = 15
    ),
    BARREL(
        textureKey = "obstacle_barrel",
        spriteWidth = 16f,
        spriteHeight = 16f,
        colliderHalfWidth = 10f,
        colliderHalfHeight = 10f,
        validMaps = setOf(MapType.SUBURBS, MapType.MALL, MapType.MILITARY_BASE, MapType.LAB),
        scale = 1.0f,
        spawnWeight = 10
    ),

    // Suburbs
    CAR_WRECK(
        textureKey = "obstacle_car",
        spriteWidth = 74f,
        spriteHeight = 43f,
        colliderHalfWidth = 28f,
        colliderHalfHeight = 16f,
        validMaps = setOf(MapType.SUBURBS),
        scale = 1.2f,
        spawnWeight = 15
    ),
    STREET_LAMP(
        textureKey = "obstacle_lamp",
        spriteWidth = 35f,
        spriteHeight = 108f,
        colliderHalfWidth = 8f,
        colliderHalfHeight = 8f,
        validMaps = setOf(MapType.SUBURBS),
        scale = 1f,
        spawnWeight = 10
    ),

    // Mall
    STORE_DISPLAY(
        textureKey = "obstacle_display",
        spriteWidth = 73f,
        spriteHeight = 68f,
        colliderHalfWidth = 30f,
        colliderHalfHeight = 28f,
        validMaps = setOf(MapType.MALL),
        scale = 1.2f,
        spawnWeight = 15
    ),
    POT_PLANT(
        textureKey = "obstacle_plant",
        spriteWidth = 23f,
        spriteHeight = 14f,
        colliderHalfWidth = 8f,
        colliderHalfHeight = 6f,
        validMaps = setOf(MapType.MALL),
        scale = 1.5f,
        spawnWeight = 12
    ),
    BENCH(
        textureKey = "obstacle_bench",
        spriteWidth = 37f,
        spriteHeight = 20f,
        colliderHalfWidth = 16f,
        colliderHalfHeight = 8f,
        validMaps = setOf(MapType.MALL),
        scale = 1.3f,
        spawnWeight = 15
    ),

    // Ashen Wastes
    DEAD_TREE(
        textureKey = "obstacle_dead_tree",
        spriteWidth = 235f,
        spriteHeight = 172f,
        colliderHalfWidth = 30f,
        colliderHalfHeight = 15f,
        validMaps = setOf(MapType.ASHEN_WASTES),
        scale = 0.8f,
        spawnWeight = 10
    ),
    CRATER(
        textureKey = "obstacle_crater",
        spriteWidth = 32f,
        spriteHeight = 32f,
        colliderHalfWidth = 12f,
        colliderHalfHeight = 12f,
        validMaps = setOf(MapType.ASHEN_WASTES),
        scale = 1.5f,
        spawnWeight = 15
    ),

    // Military Base
    SANDBAG(
        textureKey = "obstacle_sandbag",
        spriteWidth = 39f,
        spriteHeight = 35f,
        colliderHalfWidth = 16f,
        colliderHalfHeight = 14f,
        validMaps = setOf(MapType.MILITARY_BASE),
        scale = 1f,
        spawnWeight = 20
    ),
    TANK_TRAP(
        textureKey = "obstacle_tank_trap",
        spriteWidth = 25f,
        spriteHeight = 25f,
        colliderHalfWidth = 10f,
        colliderHalfHeight = 10f,
        validMaps = setOf(MapType.MILITARY_BASE),
        scale = 1.2f,
        spawnWeight = 15
    ),
    WEAPONS_CRATE(
        textureKey = "obstacle_crate",
        spriteWidth = 39f,
        spriteHeight = 35f,
        colliderHalfWidth = 16f,
        colliderHalfHeight = 14f,
        validMaps = setOf(MapType.MILITARY_BASE),
        scale = 1f,
        spawnWeight = 10
    ),

    // Lab
    SERVER_RACK(
        textureKey = "obstacle_server",
        spriteWidth = 65f,
        spriteHeight = 55f,
        colliderHalfWidth = 28f,
        colliderHalfHeight = 24f,
        validMaps = setOf(MapType.LAB),
        scale = 1f,
        spawnWeight = 15
    ),
    CRYO_POD(
        textureKey = "obstacle_cryo",
        spriteWidth = 59f,
        spriteHeight = 87f,
        colliderHalfWidth = 24f,
        colliderHalfHeight = 38f,
        validMaps = setOf(MapType.LAB),
        scale = 1f,
        spawnWeight = 10
    ),
    LAB_DESK(
        textureKey = "obstacle_desk",
        spriteWidth = 73f,
        spriteHeight = 68f,
        colliderHalfWidth = 32f,
        colliderHalfHeight = 28f,
        validMaps = setOf(MapType.LAB),
        scale = 1f,
        spawnWeight = 12
    );

    companion object {
        /** Total weight of all types, for weighted random selection */
        val totalWeight: Int by lazy { entries.sumOf { it.spawnWeight } }

        /**
         * Pick a random obstacle type using weighted selection overall (legacy fallback).
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

        fun getTypesForMap(mapType: MapType): List<ObstacleType> {
            val valid = entries.filter { mapType in it.validMaps }
            return if (valid.isNotEmpty()) valid else listOf(ROCK, BARREL)
        }

        fun weightedRandomForMap(mapType: MapType, rng: kotlin.random.Random): ObstacleType {
            val validTypes = getTypesForMap(mapType)
            val totalMapWeight = validTypes.sumOf { it.spawnWeight }
            if (totalMapWeight <= 0) return validTypes.firstOrNull() ?: ROCK

            var roll = rng.nextInt(totalMapWeight)
            for (type in validTypes) {
                roll -= type.spawnWeight
                if (roll < 0) return type
            }
            return validTypes.last()
        }
    }
}

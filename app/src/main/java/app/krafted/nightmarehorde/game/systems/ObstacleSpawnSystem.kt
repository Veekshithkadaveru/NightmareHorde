package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.ObstacleTagComponent
import app.krafted.nightmarehorde.engine.core.components.PlayerTagComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.game.data.ObstacleType
import app.krafted.nightmarehorde.game.entities.ObstacleEntity
import kotlin.random.Random

/**
 * Procedurally spawns environmental obstacles in chunks around the player.
 *
 * Spawns a mix of individual obstacles and L-shaped wall formations.
 * Formations are groups of obstacles placed in straight lines that form
 * L, I, or corner shapes — creating interesting collision barriers.
 *
 * Uses seeded random per chunk for deterministic placement.
 */
class ObstacleSpawnSystem : GameSystem(priority = 5) {

    companion object {
        /** Size of each chunk in world units */
        const val CHUNK_SIZE = 600f

        /** How many chunks around the player to keep populated */
        const val ACTIVE_RADIUS = 3

        /** How many chunks away before despawning */
        const val DESPAWN_RADIUS = 5

        /** Number of formations per chunk (1-2) */
        const val MIN_FORMATIONS = 1
        const val MAX_FORMATIONS = 2

        /** Spacing between wall segments within a formation */
        const val WALL_SEGMENT_SPACING = 28f

        /** Min/max segments per arm of the L */
        const val MIN_ARM_LENGTH = 3
        const val MAX_ARM_LENGTH = 6

        /** Safe zone radius around world origin */
        const val SAFE_ZONE_RADIUS = 200f

        /** Min distance between formation origins */
        const val MIN_FORMATION_SPACING = 200f

        /** World seed for deterministic placement */
        const val WORLD_SEED = 42L
    }

    private val spawnedChunks = mutableSetOf<Long>()
    private val chunkEntities = mutableMapOf<Long, MutableList<Long>>()

    var onSpawnEntity: ((Entity) -> Unit)? = null
    var onDespawnEntity: ((Long) -> Unit)? = null

    /**
     * Optional chunk-coordinate bounds to restrict procedural spawning within a finite map.
     * Set by MapSystem when a bounded map is active. Defaults to unbounded (infinite world).
     */
    var chunkBoundsMinX: Int = Int.MIN_VALUE
    var chunkBoundsMinY: Int = Int.MIN_VALUE
    var chunkBoundsMaxX: Int = Int.MAX_VALUE
    var chunkBoundsMaxY: Int = Int.MAX_VALUE

    private var frameCounter = 0

    override fun update(deltaTime: Float, entities: List<Entity>) {
        frameCounter++
        if (frameCounter % 15 != 0) return

        val player = entities.firstOrNull {
            it.getComponent(PlayerTagComponent::class) != null
        } ?: return
        val playerTransform = player.getComponent(TransformComponent::class) ?: return

        val playerChunkX = worldToChunk(playerTransform.x)
        val playerChunkY = worldToChunk(playerTransform.y)

        // Spawn nearby chunks (clamped to optional map bounds)
        val spawnMinX = maxOf(playerChunkX - ACTIVE_RADIUS, chunkBoundsMinX)
        val spawnMaxX = minOf(playerChunkX + ACTIVE_RADIUS, chunkBoundsMaxX)
        val spawnMinY = maxOf(playerChunkY - ACTIVE_RADIUS, chunkBoundsMinY)
        val spawnMaxY = minOf(playerChunkY + ACTIVE_RADIUS, chunkBoundsMaxY)
        for (cx in spawnMinX..spawnMaxX) {
            for (cy in spawnMinY..spawnMaxY) {
                val chunkKey = packChunkKey(cx, cy)
                if (chunkKey !in spawnedChunks) {
                    spawnChunk(cx, cy, chunkKey)
                }
            }
        }

        // Despawn distant chunks
        val chunksToRemove = mutableListOf<Long>()
        chunkEntities.keys.forEach { key ->
            val (cx, cy) = unpackChunkKey(key)
            if (kotlin.math.abs(cx - playerChunkX) > DESPAWN_RADIUS ||
                kotlin.math.abs(cy - playerChunkY) > DESPAWN_RADIUS) {
                chunksToRemove.add(key)
            }
        }
        chunksToRemove.forEach { key ->
            chunkEntities[key]?.forEach { entityId ->
                onDespawnEntity?.invoke(entityId)
            }
            chunkEntities.remove(key)
            spawnedChunks.remove(key)
        }
    }

    private fun spawnChunk(chunkX: Int, chunkY: Int, chunkKey: Long) {
        spawnedChunks.add(chunkKey)

        val seed = WORLD_SEED xor (chunkX.toLong() * 73856093L) xor (chunkY.toLong() * 19349663L)
        val rng = Random(seed)

        val chunkWorldX = chunkX * CHUNK_SIZE
        val chunkWorldY = chunkY * CHUNK_SIZE

        val formationCount = rng.nextInt(MIN_FORMATIONS, MAX_FORMATIONS + 1)
        val entityIds = mutableListOf<Long>()
        val formationOrigins = mutableListOf<Pair<Float, Float>>()

        repeat(formationCount) {
            // Pick a random origin within the chunk (with margin for the arms)
            val margin = MAX_ARM_LENGTH * WALL_SEGMENT_SPACING
            val originX = chunkWorldX + margin + rng.nextFloat() * (CHUNK_SIZE - 2 * margin)
            val originY = chunkWorldY + margin + rng.nextFloat() * (CHUNK_SIZE - 2 * margin)

            // Skip if too close to world origin (safe zone)
            val distFromOrigin = kotlin.math.sqrt(originX * originX + originY * originY)
            if (distFromOrigin < SAFE_ZONE_RADIUS) return@repeat

            // Skip if too close to another formation in this chunk
            val tooClose = formationOrigins.any { (px, py) ->
                val dx = originX - px
                val dy = originY - py
                kotlin.math.sqrt(dx * dx + dy * dy) < MIN_FORMATION_SPACING
            }
            if (tooClose) return@repeat

            formationOrigins.add(originX to originY)

            // Pick which type to build this formation from (uniform within a formation)
            val type = ObstacleType.weightedRandom(rng.nextInt(ObstacleType.totalWeight))

            // Decide formation shape
            val shape = rng.nextInt(3) // 0 = L-shape, 1 = reverse-L, 2 = straight wall
            val arm1Length = rng.nextInt(MIN_ARM_LENGTH, MAX_ARM_LENGTH + 1)
            val arm2Length = rng.nextInt(MIN_ARM_LENGTH, MAX_ARM_LENGTH + 1)

            // Random rotation: 0=right+down, 1=right+up, 2=left+down, 3=left+up
            val rotation = rng.nextInt(4)
            val dir1X: Float
            val dir1Y: Float
            val dir2X: Float
            val dir2Y: Float

            when (rotation) {
                0 -> { dir1X = 1f; dir1Y = 0f; dir2X = 0f; dir2Y = 1f }
                1 -> { dir1X = 1f; dir1Y = 0f; dir2X = 0f; dir2Y = -1f }
                2 -> { dir1X = -1f; dir1Y = 0f; dir2X = 0f; dir2Y = 1f }
                else -> { dir1X = -1f; dir1Y = 0f; dir2X = 0f; dir2Y = -1f }
            }

            // Arm 1: horizontal line
            for (i in 0 until arm1Length) {
                val x = originX + dir1X * i * WALL_SEGMENT_SPACING
                val y = originY + dir1Y * i * WALL_SEGMENT_SPACING
                val entity = ObstacleEntity.create(type, x, y)
                onSpawnEntity?.invoke(entity)
                entityIds.add(entity.id)
            }

            // Arm 2: perpendicular line (L-shape) or skip (straight wall)
            if (shape < 2) {
                // Start arm 2 from the corner (end of arm 1 for shape 0, origin for shape 1)
                val cornerX: Float
                val cornerY: Float
                if (shape == 0) {
                    // L: arm2 starts at end of arm1
                    cornerX = originX + dir1X * (arm1Length - 1) * WALL_SEGMENT_SPACING
                    cornerY = originY + dir1Y * (arm1Length - 1) * WALL_SEGMENT_SPACING
                } else {
                    // Reverse L: arm2 starts at origin
                    cornerX = originX
                    cornerY = originY
                }

                for (i in 1 until arm2Length) { // start at 1 to skip the corner piece (already placed)
                    val x = cornerX + dir2X * i * WALL_SEGMENT_SPACING
                    val y = cornerY + dir2Y * i * WALL_SEGMENT_SPACING
                    val entity = ObstacleEntity.create(type, x, y)
                    onSpawnEntity?.invoke(entity)
                    entityIds.add(entity.id)
                }
            }
        }

        if (entityIds.isNotEmpty()) {
            chunkEntities[chunkKey] = entityIds.toMutableList()
        }
    }

    fun reset() {
        chunkEntities.values.flatten().forEach { entityId ->
            onDespawnEntity?.invoke(entityId)
        }
        chunkEntities.clear()
        spawnedChunks.clear()
        frameCounter = 0
        chunkBoundsMinX = Int.MIN_VALUE
        chunkBoundsMinY = Int.MIN_VALUE
        chunkBoundsMaxX = Int.MAX_VALUE
        chunkBoundsMaxY = Int.MAX_VALUE
    }

    private fun worldToChunk(worldCoord: Float): Int {
        return kotlin.math.floor(worldCoord / CHUNK_SIZE).toInt()
    }

    private fun packChunkKey(cx: Int, cy: Int): Long {
        return (cx.toLong() shl 32) or (cy.toLong() and 0xFFFFFFFFL)
    }

    private fun unpackChunkKey(key: Long): Pair<Int, Int> {
        val cx = (key shr 32).toInt()
        val cy = key.toInt()
        return cx to cy
    }
}

package app.krafted.nightmarehorde.game.maps

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.game.data.MapType
import app.krafted.nightmarehorde.game.data.ObstacleType
import app.krafted.nightmarehorde.game.entities.ObstacleEntity
import kotlin.random.Random

/**
 * ECS system that decorates the spawn area with map-specific static obstacles (priority 3).
 *
 * Runs once on the first tick. The world remains INFINITE — no boundary walls, no camera
 * clamping, no chunk-bound restrictions. ObstacleSpawnSystem continues to procedurally
 * populate the rest of the world as the player explores.
 *
 * Each map places a themed cluster of static props near the origin so the player immediately
 * sees the environment flavour. The infinite procedural world beyond is still populated by
 * ObstacleSpawnSystem with the default obstacle types.
 */
class MapSystem(
    private val mapType: MapType
) : GameSystem(priority = 3) {

    var onSpawnEntity: ((Entity) -> Unit)? = null

    private var initialized = false

    override fun update(deltaTime: Float, entities: List<Entity>) {
        if (initialized) return
        initialized = true
        spawnMapObstacles()
    }

    private fun spawnMapObstacles() {
        when (mapType) {
            MapType.SUBURBS       -> spawnSuburbsObstacles()
            MapType.MALL          -> spawnMallObstacles()
            MapType.ASHEN_WASTES  -> spawnAshenWastesObstacles()
            MapType.MILITARY_BASE -> spawnMilitaryBaseObstacles()
            MapType.LAB           -> spawnLabObstacles()
        }
    }

    // ─── SUBURBS ─────────────────────────────────────────────────────────────
    // Wrecked-car clusters (3 barrels in a row) scattered in the spawn area.

    private fun spawnSuburbsObstacles() {
        val rng = Random(mapType.ordinal.toLong() * 1000L + 7L)
        val carSpacing = 56f
        val spawnRadius = 1500f

        repeat(20) {
            val angle = rng.nextFloat() * 2f * kotlin.math.PI.toFloat()
            val dist  = 300f + rng.nextFloat() * spawnRadius
            val cx = kotlin.math.cos(angle) * dist
            val cy = kotlin.math.sin(angle) * dist
            for (i in -1..1) {
                onSpawnEntity?.invoke(ObstacleEntity.create(ObstacleType.BARREL, cx + i * carSpacing, cy))
            }
        }
    }

    // ─── MALL ─────────────────────────────────────────────────────────────────
    // Storefront dividers (barrel rows) + rock pillars as columns.

    private fun spawnMallObstacles() {
        val segmentStep = 36f
        val offsets = listOf(-800f, -400f, 400f, 800f)
        for (ox in offsets) {
            for (i in -4..4) {
                onSpawnEntity?.invoke(ObstacleEntity.create(ObstacleType.BARREL, ox + i * segmentStep, ox * 0.5f))
            }
        }
        val pillars = listOf(
            -600f to -600f, 600f to -600f, -600f to 600f, 600f to 600f,
            -1200f to -1200f, 1200f to -1200f, -1200f to 1200f, 1200f to 1200f
        )
        for ((px, py) in pillars) {
            onSpawnEntity?.invoke(ObstacleEntity.create(ObstacleType.ROCK, px, py))
            onSpawnEntity?.invoke(ObstacleEntity.create(ObstacleType.ROCK, px + 40f, py))
        }
    }

    // ─── ASHEN WASTES ───────────────────────────────────────────────────────
    
    private fun spawnAshenWastesObstacles() {
        val rng = Random(mapType.ordinal.toLong() * 1000L + 21L)
        repeat(75) {
            val angle = rng.nextFloat() * 2f * kotlin.math.PI.toFloat()
            val dist  = 400f + rng.nextFloat() * 3200f
            val wx = kotlin.math.cos(angle) * dist
            val wy = kotlin.math.sin(angle) * dist
            val type = ObstacleType.ROCK // Only rocks in the wastes
            onSpawnEntity?.invoke(ObstacleEntity.create(type, wx.toFloat(), wy.toFloat()))
        }
    }

    // ─── MILITARY BASE ────────────────────────────────────────────────────────
    // Perimeter rock walls + inner bunker squares.

    private fun spawnMilitaryBaseObstacles() {
        val wallSegSpacing = 24f
        val perimeterDist  = 1400f
        for (i in -16..16) {
            onSpawnEntity?.invoke(ObstacleEntity.create(ObstacleType.ROCK,  i * wallSegSpacing * 2f, -perimeterDist))
            onSpawnEntity?.invoke(ObstacleEntity.create(ObstacleType.ROCK,  i * wallSegSpacing * 2f,  perimeterDist))
            onSpawnEntity?.invoke(ObstacleEntity.create(ObstacleType.ROCK, -perimeterDist, i * wallSegSpacing * 2f))
            onSpawnEntity?.invoke(ObstacleEntity.create(ObstacleType.ROCK,  perimeterDist, i * wallSegSpacing * 2f))
        }
        val bunkers = listOf(-700f to -700f, 700f to -700f, -700f to 700f, 700f to 700f)
        for ((bx, by) in bunkers) {
            for ((cx, cy) in listOf(-20f to -20f, 20f to -20f, -20f to 20f, 20f to 20f)) {
                onSpawnEntity?.invoke(ObstacleEntity.create(ObstacleType.BARREL, bx + cx, by + cy))
            }
        }
    }

    // ─── LAB ─────────────────────────────────────────────────────────────────
    // Rock pillar grid + barrel terminals.

    private fun spawnLabObstacles() {
        val gridSpacing = 500f
        for (gx in -2..2) {
            for (gy in -2..2) {
                if (gx == 0 && gy == 0) continue
                val wx = gx * gridSpacing
                val wy = gy * gridSpacing
                if (wx * wx + wy * wy < 250f * 250f) continue
                onSpawnEntity?.invoke(ObstacleEntity.create(ObstacleType.ROCK, wx, wy))
                onSpawnEntity?.invoke(ObstacleEntity.create(ObstacleType.ROCK, wx + 40f, wy))
            }
        }
        val terminals = listOf(
            -750f to 0f, 750f to 0f, 0f to -750f, 0f to 750f,
            -1250f to 0f, 1250f to 0f, 0f to -1250f, 0f to 1250f
        )
        for ((tx, ty) in terminals) {
            onSpawnEntity?.invoke(ObstacleEntity.create(ObstacleType.BARREL, tx, ty))
        }
    }

    fun reset() {
        initialized = false
    }
}

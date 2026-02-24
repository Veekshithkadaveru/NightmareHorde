package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.ZombieTypeComponent
import app.krafted.nightmarehorde.game.data.ZombieType
import app.krafted.nightmarehorde.game.entities.ZombieEntity
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * VS-style progressive wave spawner.
 *
 * Encapsulates all spawning logic: spawn rate scaling, max enemy calculation,
 * batch sizes, zombie type unlock schedule, weighted random selection,
 * off-screen spawning, and distant-zombie despawning.
 *
 * Uses an authoritative [System.nanoTime]-based clock to avoid coroutine
 * delay drift over long play sessions.
 */
class WaveSpawner(
    private val gameLoop: GameLoop
) {
    private val rng = Random(System.currentTimeMillis())

    /** Day/Night cycle reference — provides a spawn rate multiplier during night. */
    var dayNightCycle: DayNightCycle? = null

    // ─── Configuration ────────────────────────────────────────────────────
    companion object {
        const val SPAWN_DISTANCE_MIN = 450f
        const val SPAWN_DISTANCE_MAX = 600f
        const val DESPAWN_DISTANCE = 1200f

        const val BASE_SPAWN_INTERVAL_MS = 800L
        const val MIN_SPAWN_INTERVAL_MS = 150L

        const val BASE_MAX_ENEMIES = 15
        const val ABSOLUTE_MAX_ENEMIES = 100

        // Zombie type unlock times (seconds)
        const val RUNNER_UNLOCK_TIME = 60f
        const val BLOATER_UNLOCK_TIME = 180f
        const val SPITTER_UNLOCK_TIME = 180f
        const val BRUTE_UNLOCK_TIME = 300f
        const val CRAWLER_UNLOCK_TIME = 300f
        const val SCREAMER_UNLOCK_TIME = 600f
    }

    // ─── Authoritative Timer ──────────────────────────────────────────────

    private var gameStartNanos: Long = 0L
    private var totalPausedNanos: Long = 0L
    private var pauseStartNanos: Long = 0L

    /** True elapsed game time in seconds, excluding paused time. */
    var elapsedGameTime: Float = 0f
        private set

    /** Cached active zombie count, updated once per spawn tick. */
    var cachedZombieCount: Int = 0
        private set

    fun resetTimer() {
        gameStartNanos = System.nanoTime()
        totalPausedNanos = 0L
        pauseStartNanos = 0L
        elapsedGameTime = 0f
        cachedZombieCount = 0
    }

    fun onPause() {
        if (pauseStartNanos == 0L) {
            pauseStartNanos = System.nanoTime()
        }
    }

    fun onResume() {
        if (pauseStartNanos != 0L) {
            totalPausedNanos += System.nanoTime() - pauseStartNanos
            pauseStartNanos = 0L
        }
    }

    /** Call once per spawn tick to update the authoritative timer. */
    fun tick() {
        val now = System.nanoTime()
        val activePause = if (pauseStartNanos != 0L) now - pauseStartNanos else 0L
        elapsedGameTime = (now - gameStartNanos - totalPausedNanos - activePause) / 1_000_000_000f
    }

    // ─── Scaling Functions ────────────────────────────────────────────────

    fun calculateSpawnInterval(): Long {
        val progress = (elapsedGameTime / 900f).coerceIn(0f, 1f)
        val baseInterval = BASE_SPAWN_INTERVAL_MS -
                ((BASE_SPAWN_INTERVAL_MS - MIN_SPAWN_INTERVAL_MS) * progress).toLong()

        // Night: higher spawnRateMultiplier → shorter interval → more spawns
        val nightMultiplier = dayNightCycle?.spawnRateMultiplier ?: 1f
        val interval = (baseInterval / nightMultiplier).toLong()
        return interval.coerceAtLeast(MIN_SPAWN_INTERVAL_MS)
    }

    fun calculateMaxEnemies(): Int {
        val maxByTime = when {
            elapsedGameTime < 60f -> 15
            elapsedGameTime < 180f -> 25
            elapsedGameTime < 300f -> 35
            elapsedGameTime < 600f -> 50
            elapsedGameTime < 900f -> 75
            else -> 100
        }
        return maxByTime.coerceAtMost(ABSOLUTE_MAX_ENEMIES)
    }

    fun calculateBatchSize(): Int {
        return when {
            elapsedGameTime < 60f -> 1
            elapsedGameTime < 180f -> 2
            elapsedGameTime < 300f -> 3
            elapsedGameTime < 600f -> 4
            else -> 5
        }
    }

    // ─── Zombie Type Selection ────────────────────────────────────────────

    fun getAvailableZombieTypes(): List<ZombieType> {
        val types = mutableListOf(ZombieType.WALKER)

        if (elapsedGameTime >= RUNNER_UNLOCK_TIME) types.add(ZombieType.RUNNER)
        if (elapsedGameTime >= BLOATER_UNLOCK_TIME) types.add(ZombieType.BLOATER)
        if (elapsedGameTime >= SPITTER_UNLOCK_TIME) types.add(ZombieType.SPITTER)
        if (elapsedGameTime >= BRUTE_UNLOCK_TIME) types.add(ZombieType.BRUTE)
        if (elapsedGameTime >= CRAWLER_UNLOCK_TIME) types.add(ZombieType.CRAWLER)
        if (elapsedGameTime >= SCREAMER_UNLOCK_TIME) types.add(ZombieType.SCREAMER)

        return types
    }

    fun pickWeightedZombieType(available: List<ZombieType>): ZombieType {
        val weights = available.map { type ->
            when (type) {
                ZombieType.WALKER -> 40
                ZombieType.RUNNER -> 25
                ZombieType.CRAWLER -> 15
                ZombieType.BLOATER -> 8
                ZombieType.SPITTER -> 6
                ZombieType.BRUTE -> 4
                ZombieType.SCREAMER -> 2
            }
        }

        val totalWeight = weights.sum()
        var roll = rng.nextInt(totalWeight)

        for (i in available.indices) {
            roll -= weights[i]
            if (roll < 0) return available[i]
        }

        return available.last()
    }

    // ─── Spawn / Despawn ──────────────────────────────────────────────────

    /**
     * Spawn a zombie off-screen at a random angle from the player.
     * @return the spawned entity, or null if no player transform.
     */
    fun spawnZombieOffScreen(playerTransform: TransformComponent): Entity {
        val angle = rng.nextFloat() * 2f * Math.PI.toFloat()
        val distance = SPAWN_DISTANCE_MIN +
                rng.nextFloat() * (SPAWN_DISTANCE_MAX - SPAWN_DISTANCE_MIN)

        val spawnX = playerTransform.x + cos(angle) * distance
        val spawnY = playerTransform.y + sin(angle) * distance

        val availableTypes = getAvailableZombieTypes()
        val type = pickWeightedZombieType(availableTypes)

        val zombie = ZombieEntity(x = spawnX, y = spawnY, type = type)
        gameLoop.addEntity(zombie)
        return zombie
    }

    /**
     * Refresh the cached zombie count from the entity snapshot.
     */
    fun refreshZombieCount() {
        cachedZombieCount = gameLoop.getEntitiesSnapshot().count { entity ->
            entity.isActive && entity.hasComponent(ZombieTypeComponent::class)
        }
    }

    /**
     * Despawn zombies that are too far from the player.
     */
    fun despawnDistantZombies(playerTransform: TransformComponent) {
        val despawnDistSq = DESPAWN_DISTANCE * DESPAWN_DISTANCE

        gameLoop.getEntitiesSnapshot().forEach { entity ->
            if (!entity.isActive) return@forEach
            if (!entity.hasComponent(ZombieTypeComponent::class)) return@forEach

            val transform = entity.getComponent(TransformComponent::class) ?: return@forEach
            val dx = transform.x - playerTransform.x
            val dy = transform.y - playerTransform.y
            val distSq = dx * dx + dy * dy

            if (distSq > despawnDistSq) {
                entity.isActive = false
            }
        }
    }
}

package app.krafted.nightmarehorde.game.maps

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.LaserTrapComponent
import app.krafted.nightmarehorde.engine.core.components.PlayerTagComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.physics.Collider
import app.krafted.nightmarehorde.game.data.MapType
import app.krafted.nightmarehorde.game.entities.AmmoPickup
import app.krafted.nightmarehorde.game.entities.HealthPickup
import app.krafted.nightmarehorde.game.weapons.WeaponType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random

/**
 * ECS system that manages interactive map features per map type (priority 8).
 *
 * The world is INFINITE, so pickups are spawned near the player's current position
 * rather than at fixed world coordinates.
 *
 * Features by map:
 *  - MALL:          Ammo pickups drop near the player every [AMMO_SPAWN_INTERVAL] seconds.

 *  - MILITARY_BASE: Ammo pickups drop near the player every [AMMO_SPAWN_INTERVAL] seconds.
 *  - LAB:           Laser trap zones placed at fixed positions near origin; damage applied
 *                   when the player overlaps them.
 */
class MapFeatureSystem(
    private val mapType: MapType
) : GameSystem(priority = 8) {

    companion object {
        const val AMMO_SPAWN_INTERVAL   = 30f
        const val HEALTH_SPAWN_INTERVAL = 25f
        const val LASER_TRAP_SIZE       = 160f

        /** Pickup drop distance from player (just off-screen but within walking reach) */
        const val PICKUP_DROP_MIN_DIST = 400f
        const val PICKUP_DROP_MAX_DIST = 700f
    }

    var onSpawnEntity: ((Entity) -> Unit)? = null
    var onDespawnEntity: ((Long) -> Unit)? = null
    var onEnemyDeath: ((Entity) -> Unit)? = null

    private var ammoTimer   = 0f
    private var healthTimer = 0f
    private val rng = Random(mapType.ordinal.toLong() * 999L + 31L)

    // Chunking logic for static hazards (like Laser Traps)
    private val spawnedChunks = mutableSetOf<Long>()
    private val chunkEntities = mutableMapOf<Long, MutableList<Long>>()
    
    private val CHUNK_SIZE = 800f
    private val ACTIVE_RADIUS = 2
    private val DESPAWN_RADIUS = 4

    override fun update(deltaTime: Float, entities: List<Entity>) {
        // Handle chunk-based map features (Laser traps in LAB)
        if (mapType == MapType.LAB) {
            tickChunkFeatures(entities)
        }

        when (mapType) {
            MapType.MALL,
            MapType.MILITARY_BASE -> tickAmmoSpawn(deltaTime, entities)
            
            MapType.ASHEN_WASTES -> {
                tickAmmoSpawn(deltaTime, entities)
                tickHealthSpawn(deltaTime, entities)
            }

            MapType.LAB           -> tickLaserTraps(deltaTime, entities)
            else -> {}
        }
    }

    private fun tickChunkFeatures(entities: List<Entity>) {
        val (px, py) = playerPosition(entities) ?: return
        
        val playerChunkX = kotlin.math.floor(px / CHUNK_SIZE).toInt()
        val playerChunkY = kotlin.math.floor(py / CHUNK_SIZE).toInt()

        // Spawn nearby chunks
        for (cx in (playerChunkX - ACTIVE_RADIUS)..(playerChunkX + ACTIVE_RADIUS)) {
            for (cy in (playerChunkY - ACTIVE_RADIUS)..(playerChunkY + ACTIVE_RADIUS)) {
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

        val seed = mapType.ordinal.toLong() xor (chunkX.toLong() * 73856093L) xor (chunkY.toLong() * 19349663L)
        val chunkRng = Random(seed)

        val chunkWorldX = chunkX * CHUNK_SIZE
        val chunkWorldY = chunkY * CHUNK_SIZE

        val entityIds = mutableListOf<Long>()

        // For Lab, maybe spawn 0 to 2 laser traps per chunk
        if (mapType == MapType.LAB) {
            val trapCount = chunkRng.nextInt(0, 3) 
            repeat(trapCount) {
                // Random position within the chunk
                val offsetX = chunkRng.nextFloat() * CHUNK_SIZE
                val offsetY = chunkRng.nextFloat() * CHUNK_SIZE
                
                val trap = createLaserTrap(chunkWorldX + offsetX, chunkWorldY + offsetY)
                onSpawnEntity?.invoke(trap)
                entityIds.add(trap.id)
            }
        }

        if (entityIds.isNotEmpty()) {
            chunkEntities[chunkKey] = entityIds
        }
    }

    private fun packChunkKey(cx: Int, cy: Int): Long {
        return (cx.toLong() shl 32) or (cy.toLong() and 0xFFFFFFFFL)
    }

    private fun unpackChunkKey(key: Long): Pair<Int, Int> {
        val cx = (key shr 32).toInt()
        val cy = key.toInt()
        return cx to cy
    }

    // ─── Ammo spawn near player ───────────────────────────────────────────────

    private fun tickAmmoSpawn(deltaTime: Float, entities: List<Entity>) {
        ammoTimer += deltaTime
        if (ammoTimer < AMMO_SPAWN_INTERVAL) return
        ammoTimer = 0f

        val (px, py) = playerPosition(entities) ?: return
        val (spawnX, spawnY) = randomNearby(px, py)

        val ammoTypes = listOf(
            WeaponType.ASSAULT_RIFLE to 15,
            WeaponType.SHOTGUN       to 6,
            WeaponType.SMG           to 20,
            WeaponType.FLAMETHROWER  to 30
        )
        val (wType, amount) = ammoTypes[rng.nextInt(ammoTypes.size)]
        onSpawnEntity?.invoke(AmmoPickup.create(x = spawnX, y = spawnY, amount = amount, weaponType = wType))
    }

    // ─── Health spawn near player ─────────────────────────────────────────────

    private fun tickHealthSpawn(deltaTime: Float, entities: List<Entity>) {
        healthTimer += deltaTime
        if (healthTimer < HEALTH_SPAWN_INTERVAL) return
        healthTimer = 0f

        val (px, py) = playerPosition(entities) ?: return
        val (spawnX, spawnY) = randomNearby(px, py)
        onSpawnEntity?.invoke(HealthPickup.create(x = spawnX, y = spawnY, healAmount = 10))
    }

    // ─── Laser traps (Lab) ────────────────────────────────────────────────────

    private fun createLaserTrap(x: Float, y: Float): Entity {
        return Entity().apply {
            addComponent(TransformComponent(x = x, y = y, scale = 1f))
            addComponent(SpriteComponent(
                textureKey = "laser_grid_vfx",
                layer = 1,
                width = LASER_TRAP_SIZE,
                height = LASER_TRAP_SIZE
            ))
            addComponent(ColliderComponent(
                collider = Collider.AABB(LASER_TRAP_SIZE, LASER_TRAP_SIZE),
                layer = CollisionLayer.OBSTACLE,
                collidesWithLayers = setOf(CollisionLayer.PLAYER, CollisionLayer.ENEMY),
                isTrigger = true
            ))
            addComponent(LaserTrapComponent(damagePerInterval = 5f, damageInterval = 1.0f))
        }
    }

    private fun tickLaserTraps(deltaTime: Float, entities: List<Entity>) {
        val targets = entities.filter { 
            it.getComponent(HealthComponent::class) != null && 
            it.getComponent(TransformComponent::class) != null && 
            it.getComponent(ColliderComponent::class) != null 
        }

        entities.forEach { entity ->
            val trap          = entity.getComponent(LaserTrapComponent::class) ?: return@forEach
            val trapTransform = entity.getComponent(TransformComponent::class) ?: return@forEach
            val trapCollider  = entity.getComponent(ColliderComponent::class)  ?: return@forEach
            val trapAABB      = trapCollider.collider as? Collider.AABB        ?: return@forEach

            trap.timeSinceDamage += deltaTime

            if (trap.timeSinceDamage >= trap.damageInterval) {
                targets.forEach { target ->
                    val targetTransform = target.getComponent(TransformComponent::class) ?: return@forEach
                    val targetCollider  = target.getComponent(ColliderComponent::class) ?: return@forEach
                    val targetHealth    = target.getComponent(HealthComponent::class) ?: return@forEach
                    val targetStats     = target.getComponent(StatsComponent::class)

                    val targetRadius = when (val shape = targetCollider.collider) {
                        is Collider.Circle -> shape.radius
                        is Collider.AABB   -> minOf(shape.halfWidth, shape.halfHeight)
                    }

                    val dx = kotlin.math.abs(targetTransform.x - trapTransform.x)
                    val dy = kotlin.math.abs(targetTransform.y - trapTransform.y)
                    
                    if (dx < (trapAABB.halfWidth + targetRadius) &&
                        dy < (trapAABB.halfHeight + targetRadius)
                    ) {
                        val damageDealt = targetHealth.takeDamage(trap.damagePerInterval.toInt(), targetStats?.armor ?: 0)
                        
                        val popup = app.krafted.nightmarehorde.game.entities.DamagePopupEntity(
                            x = targetTransform.x,
                            y = targetTransform.y - 30f,
                            damage = damageDealt
                        )
                        onSpawnEntity?.invoke(popup)

                        app.krafted.nightmarehorde.game.entities.HitEffectEntity.burst(
                            x = targetTransform.x,
                            y = targetTransform.y
                        ).forEach { onSpawnEntity?.invoke(it) }

                        if (!targetHealth.isAlive && target.getComponent(PlayerTagComponent::class) == null) {
                            onEnemyDeath?.invoke(target)
                            target.isActive = false
                        }
                    }
                }
                
                // Reset trap timer after applying damage wave to everyone standing inside
                trap.timeSinceDamage = 0f
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun playerPosition(entities: List<Entity>): Pair<Float, Float>? {
        val player = entities.firstOrNull { it.getComponent(PlayerTagComponent::class) != null }
            ?: return null
        val t = player.getComponent(TransformComponent::class) ?: return null
        return t.x to t.y
    }

    /** Returns a random point at [PICKUP_DROP_MIN_DIST, PICKUP_DROP_MAX_DIST] from (px, py). */
    private fun randomNearby(px: Float, py: Float): Pair<Float, Float> {
        val angle = rng.nextFloat() * 2f * PI.toFloat()
        val dist  = PICKUP_DROP_MIN_DIST + rng.nextFloat() * (PICKUP_DROP_MAX_DIST - PICKUP_DROP_MIN_DIST)
        return (px + cos(angle) * dist) to (py + sin(angle) * dist)
    }

    fun reset() {
        chunkEntities.values.flatten().forEach { entityId ->
            onDespawnEntity?.invoke(entityId)
        }
        chunkEntities.clear()
        spawnedChunks.clear()
        
        ammoTimer   = 0f
        healthTimer = 0f
    }
}

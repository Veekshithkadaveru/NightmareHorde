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

    private var initialized = false
    private var ammoTimer   = 0f
    private var healthTimer = 0f
    private val rng = Random(mapType.ordinal.toLong() * 999L + 31L)

    // Laser trap positions are fixed near origin — player will encounter them naturally.
    private val laserTrapOffsets: List<Pair<Float, Float>> = when (mapType) {
        MapType.LAB -> listOf(
            -250f to -250f,  250f to -250f,
            -250f to  250f,  250f to  250f,
            -750f to    0f,  750f to    0f,
              0f to -750f,     0f to  750f
        )
        else -> emptyList()
    }

    override fun update(deltaTime: Float, entities: List<Entity>) {
        if (!initialized) {
            initialized = true
            if (mapType == MapType.LAB) {
                for ((tx, ty) in laserTrapOffsets) spawnLaserTrap(tx, ty)
            }
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

    private fun spawnLaserTrap(x: Float, y: Float) {
        val trap = Entity().apply {
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
        onSpawnEntity?.invoke(trap)
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
                    val targetTransform = target.getComponent(TransformComponent::class)!!
                    val targetCollider  = target.getComponent(ColliderComponent::class)!!
                    val targetHealth    = target.getComponent(HealthComponent::class)!!
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
                        targetHealth.takeDamage(trap.damagePerInterval.toInt(), targetStats?.armor ?: 0)
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
        initialized = false
        ammoTimer   = 0f
        healthTimer = 0f
    }
}

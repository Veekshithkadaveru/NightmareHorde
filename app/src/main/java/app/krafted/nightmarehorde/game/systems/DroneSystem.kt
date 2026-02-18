package app.krafted.nightmarehorde.game.systems

import androidx.compose.ui.graphics.Color
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.Vector2
import app.krafted.nightmarehorde.engine.core.components.DroneComponent
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.ObstacleTagComponent
import app.krafted.nightmarehorde.engine.core.components.PlayerTagComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.game.data.DroneType
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.ParticleComponent
import app.krafted.nightmarehorde.engine.physics.Collider
import app.krafted.nightmarehorde.game.entities.HitEffectEntity
import app.krafted.nightmarehorde.game.entities.ProjectileEntity
import kotlin.math.cos
import kotlin.math.sin

/**
 * Manages orbital drone behavior: orbit positioning, fuel drain, targeting, and firing.
 * Priority 22: runs after AISystem (18) and AutoAimSystem (20), before WeaponSystem (30).
 */
class DroneSystem(
    private val gameLoop: GameLoop
) : GameSystem(priority = 22) {

    companion object {
        const val BURST_FIRE_RATE = 0.08f
    }

    // Reusable buffers to avoid per-frame allocations
    private val droneBuffer = ArrayList<Entity>(3)
    private val targetBuffer = ArrayList<Entity>(128)
    private val obstacleBuffer = ArrayList<Entity>(64)

    // Cached per frame for drone multipliers
    private var cachedPlayerStats: StatsComponent? = null

    /** Called when Inferno/Arc drone kills an enemy directly (not via projectile). */
    var onDroneKill: ((deadEntity: Entity, droneEntityId: Long) -> Unit)? = null

    override fun update(deltaTime: Float, entities: List<Entity>) {
        // Single-pass classification
        var playerTransform: TransformComponent? = null
        droneBuffer.clear()
        targetBuffer.clear()

        cachedPlayerStats = null
        obstacleBuffer.clear()
        for (entity in entities) {
            if (!entity.isActive) continue
            if (entity.hasComponent(PlayerTagComponent::class)) {
                playerTransform = entity.getComponent(TransformComponent::class)
                cachedPlayerStats = entity.getComponent(StatsComponent::class)
            }
            if (entity.hasComponent(DroneComponent::class)) {
                droneBuffer.add(entity)
            }
            if (entity.hasComponent(HealthComponent::class) &&
                !entity.hasComponent(PlayerTagComponent::class) &&
                !entity.hasComponent(DroneComponent::class)
            ) {
                targetBuffer.add(entity)
            }
            if (entity.hasComponent(ObstacleTagComponent::class)) {
                obstacleBuffer.add(entity)
            }
        }

        if (playerTransform == null) return

        // Count non-lost drones for formation
        var activeDroneCount = 0
        for (droneEntity in droneBuffer) {
            val dc = droneEntity.getComponent(DroneComponent::class) ?: continue
            if (!dc.isLost) activeDroneCount++
        }

        // Update each drone
        for (droneEntity in droneBuffer) {
            val drone = droneEntity.getComponent(DroneComponent::class) ?: continue
            val droneTransform = droneEntity.getComponent(TransformComponent::class) ?: continue

            if (drone.isLost) {
                droneEntity.isActive = false
                continue
            }

            // Fuel drain
            updateFuel(drone, deltaTime)

            // Power-down grace
            if (drone.isPoweredDown) {
                updateGraceWindow(drone, deltaTime)
                updateOrbitPosition(droneTransform, playerTransform, drone, deltaTime, activeDroneCount, isPoweredDown = true)
                updateVisuals(drone, droneEntity, deltaTime, isPoweredDown = true)
                continue // No firing while powered down
            }

            // Orbit position
            updateOrbitPosition(droneTransform, playerTransform, drone, deltaTime, activeDroneCount, isPoweredDown = false)

            // Targeting and firing
            updateTargetingAndFiring(droneEntity, drone, droneTransform, deltaTime)

            // Visual state
            updateVisuals(drone, droneEntity, deltaTime, isPoweredDown = false)
        }
    }

    // ── Fuel ──────────────────────────────────────────────────────────────

    private fun updateFuel(drone: DroneComponent, deltaTime: Float) {
        if (drone.isPoweredDown) return
        val baseDrainRate = drone.droneType.fuelDrainRate(drone.level)
        val fuelEfficiency = cachedPlayerStats?.droneFuelEfficiency ?: 1f
        val drainRate = baseDrainRate / fuelEfficiency.coerceAtLeast(0.1f)
        drone.fuel -= drainRate * deltaTime
        if (drone.fuel <= 0f) {
            drone.fuel = 0f
            drone.isPoweredDown = true
            drone.graceTimer = DroneComponent.GRACE_WINDOW_SECONDS
        }
    }

    private fun updateGraceWindow(drone: DroneComponent, deltaTime: Float) {
        drone.graceTimer -= deltaTime
        if (drone.graceTimer <= 0f) {
            drone.isLost = true
        }
    }

    // ── Orbit Position ───────────────────────────────────────────────────

    private fun updateOrbitPosition(
        droneTransform: TransformComponent,
        playerTransform: TransformComponent,
        drone: DroneComponent,
        deltaTime: Float,
        activeDroneCount: Int,
        isPoweredDown: Boolean
    ) {
        val twoPi = 2f * Math.PI.toFloat()

        // Advance base angle continuously
        val orbitSpeed = if (isPoweredDown) {
            DroneComponent.POWERED_DOWN_ORBIT_SPEED
        } else {
            DroneComponent.ORBIT_SPEED
        }
        drone.orbitBaseAngle += orbitSpeed * deltaTime

        // Keep in [0, 2*PI) to prevent float drift over long sessions
        if (drone.orbitBaseAngle >= twoPi) drone.orbitBaseAngle -= twoPi

        // Formation offset: evenly space drones around the circle
        val formationOffset = if (activeDroneCount > 1) {
            (twoPi / activeDroneCount) * drone.slotIndex
        } else {
            0f
        }
        val finalAngle = drone.orbitBaseAngle + formationOffset

        // Position on orbit circle
        droneTransform.x = playerTransform.x + cos(finalAngle) * DroneComponent.ORBIT_RADIUS
        droneTransform.y = playerTransform.y + sin(finalAngle) * DroneComponent.ORBIT_RADIUS
    }

    // ── Targeting and Firing ─────────────────────────────────────────────

    private fun updateTargetingAndFiring(
        droneEntity: Entity,
        drone: DroneComponent,
        droneTransform: TransformComponent,
        deltaTime: Float
    ) {
        val droneType = drone.droneType
        val effectiveFireRate = droneType.fireRateAtLevel(drone.level)
        val baseDamage = droneType.damageAtLevel(drone.level)
        val effectiveDamage = baseDamage * (cachedPlayerStats?.droneDamageMultiplier ?: 1f)

        // Tick cooldown
        drone.fireCooldown -= deltaTime

        // Handle burst continuation for Gunner Lv3
        if (droneType == DroneType.GUNNER && drone.level >= 3 && drone.burstShotsRemaining > 0) {
            drone.burstCooldown -= deltaTime
            if (drone.burstCooldown <= 0f) {
                fireBurstShot(droneEntity, drone, droneTransform, effectiveDamage)
            }
        }

        if (drone.fireCooldown > 0f) return

        // Find nearest target within effective range
        val effectiveRange = droneType.effectiveRange(drone.level)
        val target = findNearestTarget(droneTransform, effectiveRange) ?: return
        val targetTransform = target.getComponent(TransformComponent::class) ?: return
        drone.currentTargetId = target.id

        val direction = Vector2(
            targetTransform.x - droneTransform.x,
            targetTransform.y - droneTransform.y
        ).normalized()

        when (droneType) {
            DroneType.GUNNER -> fireGunner(droneEntity, drone, droneTransform, direction, effectiveDamage)
            DroneType.SCATTER -> fireScatter(droneEntity, drone, droneTransform, direction, effectiveDamage)
            DroneType.INFERNO -> fireInferno(drone, droneTransform, effectiveDamage, droneEntity.id)
            DroneType.ARC -> fireArc(drone, droneTransform, target, effectiveDamage, droneEntity.id)
        }

        drone.fireCooldown = 1f / effectiveFireRate
    }

    private fun findNearestTarget(fromTransform: TransformComponent, range: Float): Entity? {
        val rangeSquared = range * range
        var nearest: Entity? = null
        var minDistSq = Float.MAX_VALUE

        for (entity in targetBuffer) {
            if (!entity.isActive) continue
            val transform = entity.getComponent(TransformComponent::class) ?: continue
            val health = entity.getComponent(HealthComponent::class) ?: continue
            if (!health.isAlive) continue
            val distSq = fromTransform.distanceSquaredTo(transform)
            if (distSq <= rangeSquared && distSq < minDistSq) {
                minDistSq = distSq
                nearest = entity
            }
        }
        return nearest
    }

    // ── Fire Modes ───────────────────────────────────────────────────────

    private fun fireGunner(
        droneEntity: Entity, drone: DroneComponent,
        transform: TransformComponent, direction: Vector2, damage: Float
    ) {
        if (drone.level >= 3) {
            // Lv3: 3-round burst with piercing
            drone.burstShotsRemaining = 3
            drone.burstCooldown = 0f
            fireBurstShot(droneEntity, drone, transform, damage)
        } else {
            val projectile = ProjectileEntity(
                x = transform.x,
                y = transform.y,
                rotation = direction.angle(),
                speed = drone.droneType.projectileSpeed,
                damage = damage,
                ownerId = droneEntity.id,
                lifeTime = drone.droneType.range / drone.droneType.projectileSpeed,
                colliderRadius = 4f
            )
            gameLoop.addEntity(projectile)
        }
    }

    private fun fireBurstShot(
        droneEntity: Entity, drone: DroneComponent,
        transform: TransformComponent, damage: Float
    ) {
        if (drone.burstShotsRemaining <= 0) return

        // Re-acquire target direction
        val target = targetBuffer.firstOrNull { it.isActive && it.id == drone.currentTargetId }
        val targetTransform = target?.getComponent(TransformComponent::class)
        val direction = if (targetTransform != null) {
            Vector2(targetTransform.x - transform.x, targetTransform.y - transform.y).normalized()
        } else {
            Vector2(1f, 0f)
        }

        val projectile = ProjectileEntity(
            x = transform.x,
            y = transform.y,
            rotation = direction.angle(),
            speed = drone.droneType.projectileSpeed,
            damage = damage,
            ownerId = droneEntity.id,
            lifeTime = drone.droneType.range / drone.droneType.projectileSpeed,
            penetrating = true,
            colliderRadius = 4f
        )
        gameLoop.addEntity(projectile)

        drone.burstShotsRemaining--
        drone.burstCooldown = BURST_FIRE_RATE
    }

    private fun fireScatter(
        droneEntity: Entity, drone: DroneComponent,
        transform: TransformComponent, direction: Vector2, damage: Float
    ) {
        val count = drone.droneType.projectileCount
        val totalSpread = drone.droneType.spreadAngle
        val angleStep = totalSpread / (count - 1)
        val startAngle = -totalSpread / 2f
        val isPiercing = drone.level >= 3

        for (i in 0 until count) {
            val spread = startAngle + (angleStep * i)
            val dir = direction.rotate(spread)
            val projectile = ProjectileEntity(
                x = transform.x,
                y = transform.y,
                rotation = dir.angle(),
                speed = drone.droneType.projectileSpeed,
                damage = damage,
                ownerId = droneEntity.id,
                lifeTime = drone.droneType.range / drone.droneType.projectileSpeed,
                colliderRadius = 5f,
                penetrating = isPiercing
            )
            gameLoop.addEntity(projectile)
        }
    }

    private fun fireInferno(
        drone: DroneComponent,
        transform: TransformComponent,
        damage: Float,
        droneEntityId: Long
    ) {
        val effectiveRange = drone.droneType.effectiveRange(drone.level)
        val effectiveRangeSquared = effectiveRange * effectiveRange
        val maxTargets = drone.droneType.maxAoeTargets

        var hitCount = 0
        for (entity in targetBuffer) {
            if (hitCount >= maxTargets) break
            if (!entity.isActive) continue
            val targetTransform = entity.getComponent(TransformComponent::class) ?: continue
            val health = entity.getComponent(HealthComponent::class) ?: continue
            if (!health.isAlive) continue

            val distSq = transform.distanceSquaredTo(targetTransform)
            if (distSq <= effectiveRangeSquared) {
                // Skip targets hidden behind an obstacle
                if (isBlockedByObstacle(transform.x, transform.y, targetTransform.x, targetTransform.y)) continue

                health.takeDamage(damage.toInt())
                hitCount++

                // Burn VFX
                spawnBurnEffect(targetTransform.x, targetTransform.y)

                if (!health.isAlive) {
                    onDroneKill?.invoke(entity, droneEntityId)
                    entity.isActive = false
                }
            }
        }
    }

    private fun fireArc(
        drone: DroneComponent,
        droneTransform: TransformComponent,
        firstTarget: Entity,
        damage: Float,
        droneEntityId: Long
    ) {
        // Skip first target if it's behind an obstacle from the drone
        val firstTargetTransform = firstTarget.getComponent(TransformComponent::class) ?: return
        if (isBlockedByObstacle(droneTransform.x, droneTransform.y, firstTargetTransform.x, firstTargetTransform.y)) return

        val maxChain = drone.droneType.chainTargetsAtLevel(drone.level)
        val hitIds = mutableSetOf<Long>()
        var currentTarget = firstTarget
        var prevTransform = droneTransform
        var chainCount = 0

        while (chainCount < maxChain) {
            if (!currentTarget.isActive || hitIds.contains(currentTarget.id)) break
            val targetTransform = currentTarget.getComponent(TransformComponent::class) ?: break
            val health = currentTarget.getComponent(HealthComponent::class) ?: break
            if (!health.isAlive) break

            // Skip target if it's behind an obstacle from the previous chain link
            if (isBlockedByObstacle(prevTransform.x, prevTransform.y, targetTransform.x, targetTransform.y)) break

            health.takeDamage(damage.toInt())
            hitIds.add(currentTarget.id)
            chainCount++

            // Arc VFX between chain links
            spawnArcEffect(prevTransform.x, prevTransform.y, targetTransform.x, targetTransform.y)

            if (!health.isAlive) {
                onDroneKill?.invoke(currentTarget, droneEntityId)
                currentTarget.isActive = false
            }

            prevTransform = targetTransform

            // Find next chain target (also respects obstacle LOS in next iteration)
            val nextTarget = findNearestTargetExcluding(targetTransform, drone.droneType.range, hitIds)
            if (nextTarget != null) {
                currentTarget = nextTarget
            } else {
                break
            }
        }
    }

    private fun findNearestTargetExcluding(
        fromTransform: TransformComponent,
        range: Float,
        excludeIds: Set<Long>
    ): Entity? {
        val rangeSquared = range * range
        var nearest: Entity? = null
        var minDistSq = Float.MAX_VALUE

        for (entity in targetBuffer) {
            if (!entity.isActive || excludeIds.contains(entity.id)) continue
            val transform = entity.getComponent(TransformComponent::class) ?: continue
            val health = entity.getComponent(HealthComponent::class) ?: continue
            if (!health.isAlive) continue
            val distSq = fromTransform.distanceSquaredTo(transform)
            if (distSq <= rangeSquared && distSq < minDistSq) {
                minDistSq = distSq
                nearest = entity
            }
        }
        return nearest
    }

    // ── Obstacle Line-of-Sight ────────────────────────────────────────────

    /**
     * Returns true if a segment from (x1,y1) to (x2,y2) is blocked by any obstacle AABB.
     * Uses a segment-vs-AABB slab test (Liang–Barsky style).
     */
    private fun isBlockedByObstacle(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        for (obs in obstacleBuffer) {
            val t = obs.getComponent(TransformComponent::class) ?: continue
            val c = obs.getComponent(ColliderComponent::class) ?: continue
            val aabb = c.collider as? Collider.AABB ?: continue
            if (segmentIntersectsAabb(x1, y1, x2, y2, t.x, t.y, aabb.halfWidth, aabb.halfHeight)) {
                return true
            }
        }
        return false
    }

    /**
     * Slab-method segment vs AABB intersection test.
     * Returns true if the segment from (sx,sy)→(ex,ey) overlaps the axis-aligned box.
     */
    private fun segmentIntersectsAabb(
        sx: Float, sy: Float, ex: Float, ey: Float,
        bx: Float, by: Float, hw: Float, hh: Float
    ): Boolean {
        val dx = ex - sx
        val dy = ey - sy
        var tMin = 0f
        var tMax = 1f

        // X slab
        if (kotlin.math.abs(dx) < 1e-6f) {
            if (sx < bx - hw || sx > bx + hw) return false
        } else {
            val invDx = 1f / dx
            var t1 = ((bx - hw) - sx) * invDx
            var t2 = ((bx + hw) - sx) * invDx
            if (t1 > t2) { val tmp = t1; t1 = t2; t2 = tmp }
            tMin = maxOf(tMin, t1)
            tMax = minOf(tMax, t2)
            if (tMin > tMax) return false
        }

        // Y slab
        if (kotlin.math.abs(dy) < 1e-6f) {
            if (sy < by - hh || sy > by + hh) return false
        } else {
            val invDy = 1f / dy
            var t1 = ((by - hh) - sy) * invDy
            var t2 = ((by + hh) - sy) * invDy
            if (t1 > t2) { val tmp = t1; t1 = t2; t2 = tmp }
            tMin = maxOf(tMin, t1)
            tMax = minOf(tMax, t2)
            if (tMin > tMax) return false
        }

        return true
    }

    // ── VFX ──────────────────────────────────────────────────────────────

    private fun spawnBurnEffect(x: Float, y: Float) {
        HitEffectEntity.burst(
            x = x, y = y,
            count = 3,
            baseColor = Color(0xFFFF4400),
            speed = 60f,
            lifeTime = 0.25f,
            size = 8f
        ).forEach { gameLoop.addEntity(it) }
    }

    private fun spawnArcEffect(x1: Float, y1: Float, x2: Float, y2: Float) {
        val midX = (x1 + x2) / 2f
        val midY = (y1 + y2) / 2f
        val dx = x2 - x1
        val dy = y2 - y1
        val direction = Vector2(dx, dy)

        val particle = Entity().apply {
            addComponent(TransformComponent(
                x = midX,
                y = midY,
                rotation = direction.angle()
            ))
            addComponent(ParticleComponent(
                color = Color(0xFF8844FF),
                size = 6f,
                lifeTime = 0.2f,
                width = direction.length() * 0.5f,
                height = 3f
            ))
        }
        gameLoop.addEntity(particle)
    }

    // ── Visuals ──────────────────────────────────────────────────────────

    private fun updateVisuals(
        drone: DroneComponent,
        droneEntity: Entity,
        deltaTime: Float,
        isPoweredDown: Boolean
    ) {
        val sprite = droneEntity.getComponent(SpriteComponent::class) ?: return

        if (isPoweredDown) {
            drone.dimAlpha = (drone.dimAlpha - deltaTime * 2f).coerceAtLeast(0.3f)
            sprite.alpha = drone.dimAlpha
            return
        }

        drone.dimAlpha = 1f

        // Low fuel glow pulse
        if (drone.isLowFuel) {
            drone.glowPulseTimer += deltaTime * DroneComponent.LOW_FUEL_PULSE_SPEED
            val pulse = sin(drone.glowPulseTimer * Math.PI.toFloat() * 2f) * 0.3f + 0.7f
            sprite.alpha = pulse
        } else {
            sprite.alpha = 1f
            drone.glowPulseTimer = 0f
        }
    }
}

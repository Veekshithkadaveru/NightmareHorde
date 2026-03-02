package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.AIBehavior
import app.krafted.nightmarehorde.engine.core.components.AIComponent
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.ProjectileComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.physics.Collider
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class AISystem @Inject constructor() : GameSystem(priority = 18) {

    companion object {
        // Spitter
        const val SPIT_COOLDOWN = 2.0f
        const val SPIT_PROJECTILE_SPEED = 180f
        const val SPIT_PROJECTILE_LIFETIME = 3.0f

        // Brute charge
        const val CHARGE_COOLDOWN = 4.0f
        const val CHARGE_DURATION = 0.6f
        const val CHARGE_SPEED_MULTIPLIER = 3.5f

        // Screamer buff
        const val BUFF_INTERVAL = 3.0f
        const val BUFF_DURATION = 5.0f
        const val BUFF_SPEED_MULTIPLIER = 1.3f
        const val BUFF_DAMAGE_MULTIPLIER = 1.25f

        // Bloater explosion
        const val EXPLOSION_RADIUS = 80f
    }

    private var playerEntity: Entity? = null

    /** Callback to spawn entities (projectiles, explosion effects) into the game loop */
    var onSpawnEntity: ((Entity) -> Unit)? = null

    /** Day/Night cycle reference — provides a global speed multiplier for zombies. */
    var dayNightCycle: DayNightCycle? = null

    fun setPlayer(player: Entity) {
        playerEntity = player
    }

    fun clearPlayer() {
        playerEntity = null
    }

    override fun update(deltaTime: Float, entities: List<Entity>) {
        val player = playerEntity ?: return
        val playerTransform = player.getComponent(TransformComponent::class) ?: return

        // Cache the night speed multiplier once per frame
        nightSpeedMultiplier = dayNightCycle?.speedMultiplier ?: 1f

        // Decay buff timers on all entities
        entities.forEach { entity ->
            val ai = entity.getComponent(AIComponent::class) ?: return@forEach
            if (ai.isBuffed) {
                ai.buffTimeRemaining -= deltaTime
                if (ai.buffTimeRemaining <= 0f) {
                    removeBuff(entity, ai)
                }
            }
        }

        entities.forEach { entity ->
            val ai = entity.getComponent(AIComponent::class) ?: return@forEach
            val transform = entity.getComponent(TransformComponent::class) ?: return@forEach
            val velocity = entity.getComponent(VelocityComponent::class) ?: return@forEach
            val stats = entity.getComponent(StatsComponent::class) ?: return@forEach

            ai.target = player

            // Tick cooldowns
            if (ai.attackCooldown > 0f) ai.attackCooldown -= deltaTime
            if (ai.chargeCooldown > 0f) ai.chargeCooldown -= deltaTime

            when (ai.behavior) {
                AIBehavior.CHASE -> updateChaseBehavior(transform, velocity, stats, playerTransform)
                AIBehavior.EXPLODE -> updateExplodeBehavior(entity, transform, velocity, stats, playerTransform, ai, player, entities)
                AIBehavior.RANGED -> updateRangedBehavior(entity, transform, velocity, stats, playerTransform, ai, deltaTime)
                AIBehavior.BUFF -> updateBuffBehavior(transform, velocity, stats, playerTransform, ai, entities, deltaTime)
                AIBehavior.CHARGE -> updateChargeBehavior(transform, velocity, stats, playerTransform, ai, deltaTime)
                else -> { /* IDLE or FLEE */ }
            }
        }
    }

    /** The effective night speed multiplier, cached once per frame. */
    private var nightSpeedMultiplier: Float = 1f

    private fun updateChaseBehavior(
        transform: TransformComponent,
        velocity: VelocityComponent,
        stats: StatsComponent,
        targetTransform: TransformComponent
    ) {
        val dx = targetTransform.x - transform.x
        val dy = targetTransform.y - transform.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance > 0.01f) {
            val dirX = dx / distance
            val dirY = dy / distance

            val speed = stats.moveSpeed * nightSpeedMultiplier
            velocity.vx = dirX * speed
            velocity.vy = dirY * speed
            // Note: No rotation set — side-view sprites use flipX for direction
            // (handled by ZombieAnimationSystem)
        }
    }

    private fun updateExplodeBehavior(
        entity: Entity,
        transform: TransformComponent,
        velocity: VelocityComponent,
        stats: StatsComponent,
        targetTransform: TransformComponent,
        ai: AIComponent,
        player: Entity,
        allEntities: List<Entity>
    ) {
        // Chase the player
        updateChaseBehavior(transform, velocity, stats, targetTransform)

        val dx = targetTransform.x - transform.x
        val dy = targetTransform.y - transform.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance <= ai.range) {
            val health = entity.getComponent(HealthComponent::class)
            if (health != null && health.isAlive) {
                // Kill self to trigger explosion
                health.setHealth(0)
                entity.isActive = false

                // Apply AOE damage to player if in explosion radius
                applyExplosionDamage(transform, stats, player, EXPLOSION_RADIUS)

                // Spawn explosion particle effect
                spawnExplosionEffect(transform.x, transform.y)
            }
        }
    }

    private fun applyExplosionDamage(
        explosionTransform: TransformComponent,
        stats: StatsComponent,
        player: Entity,
        radius: Float
    ) {
        val playerTransform = player.getComponent(TransformComponent::class) ?: return
        val playerHealth = player.getComponent(HealthComponent::class) ?: return
        val playerStats = player.getComponent(StatsComponent::class)

        val dx = playerTransform.x - explosionTransform.x
        val dy = playerTransform.y - explosionTransform.y
        val distSq = dx * dx + dy * dy

        if (distSq <= radius * radius) {
            val armor = playerStats?.armor ?: 0
            playerHealth.takeDamage(stats.baseDamage.toInt(), armor)
        }
    }

    private fun spawnExplosionEffect(x: Float, y: Float) {
        val spawn = onSpawnEntity ?: return
        // Green-tinted explosion particles for bloater
        app.krafted.nightmarehorde.game.entities.HitEffectEntity.burst(
            x = x,
            y = y,
            count = 12,
            baseColor = androidx.compose.ui.graphics.Color(0xFF44FF44),
            speed = 200f,
            lifeTime = 0.5f,
            size = 10f
        ).forEach { spawn(it) }
    }

    private fun updateRangedBehavior(
        entity: Entity,
        transform: TransformComponent,
        velocity: VelocityComponent,
        stats: StatsComponent,
        targetTransform: TransformComponent,
        ai: AIComponent,
        deltaTime: Float
    ) {
        val dx = targetTransform.x - transform.x
        val dy = targetTransform.y - transform.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance > ai.range) {
            // Move closer if out of range
            val dirX = dx / distance
            val dirY = dy / distance
            val speed = stats.moveSpeed * nightSpeedMultiplier
            velocity.vx = dirX * speed
            velocity.vy = dirY * speed
        } else {
            // In range: stop and fire
            velocity.vx = 0f
            velocity.vy = 0f

            if (ai.attackCooldown <= 0f) {
                fireAcidProjectile(entity, transform, targetTransform, stats)
                ai.attackCooldown = SPIT_COOLDOWN
            }
        }
    }

    private fun fireAcidProjectile(
        owner: Entity,
        ownerTransform: TransformComponent,
        targetTransform: TransformComponent,
        stats: StatsComponent
    ) {
        val spawn = onSpawnEntity ?: return

        val dx = targetTransform.x - ownerTransform.x
        val dy = targetTransform.y - ownerTransform.y
        val distance = sqrt(dx * dx + dy * dy)
        if (distance <= 0f) return

        val dirX = dx / distance
        val dirY = dy / distance

        val projectile = Entity().apply {
            addComponent(TransformComponent(
                x = ownerTransform.x + dirX * 20f,
                y = ownerTransform.y + dirY * 20f
            ))
            addComponent(VelocityComponent(
                vx = dirX * SPIT_PROJECTILE_SPEED,
                vy = dirY * SPIT_PROJECTILE_SPEED
            ))
            addComponent(ColliderComponent(
                collider = Collider.Circle(8f),
                layer = CollisionLayer.ENEMY,
                isTrigger = true
            ))
            addComponent(ProjectileComponent(
                damage = stats.baseDamage,
                ownerId = owner.id,
                maxLifetime = SPIT_PROJECTILE_LIFETIME
            ))
        }
        spawn(projectile)
    }

    private fun updateBuffBehavior(
        transform: TransformComponent,
        velocity: VelocityComponent,
        stats: StatsComponent,
        targetTransform: TransformComponent,
        ai: AIComponent,
        allEntities: List<Entity>,
        deltaTime: Float
    ) {
        // Screamer moves towards player but stays at a distance
        val dx = targetTransform.x - transform.x
        val dy = targetTransform.y - transform.y
        val distance = sqrt(dx * dx + dy * dy)

        // Stay at buff range - don't get too close
        val preferredDistance = ai.range * 0.8f
        val speed = stats.moveSpeed * nightSpeedMultiplier
        if (distance > ai.range) {
            val dirX = dx / distance
            val dirY = dy / distance
            velocity.vx = dirX * speed
            velocity.vy = dirY * speed
        } else if (distance < preferredDistance * 0.5f && distance > 0.01f) {
            // Too close, back off (guard distance > 0 to prevent NaN from div-by-zero)
            val dirX = dx / distance
            val dirY = dy / distance
            velocity.vx = -dirX * speed * 0.5f
            velocity.vy = -dirY * speed * 0.5f
        } else {
            velocity.vx = 0f
            velocity.vy = 0f
        }

        // Periodically buff nearby allies
        ai.buffTimer += deltaTime
        if (ai.buffTimer >= BUFF_INTERVAL) {
            ai.buffTimer = 0f
            applyBuffToNearbyZombies(transform, ai.range, allEntities)
        }
    }

    private fun applyBuffToNearbyZombies(
        screamerTransform: TransformComponent,
        range: Float,
        allEntities: List<Entity>
    ) {
        allEntities.forEach { entity ->
            val otherAi = entity.getComponent(AIComponent::class) ?: return@forEach
            // Don't buff self or already-buffed entities
            if (otherAi.behavior == AIBehavior.BUFF) return@forEach
            if (otherAi.isBuffed) return@forEach

            val otherTransform = entity.getComponent(TransformComponent::class) ?: return@forEach
            val otherStats = entity.getComponent(StatsComponent::class) ?: return@forEach

            val dx = otherTransform.x - screamerTransform.x
            val dy = otherTransform.y - screamerTransform.y
            val distSq = dx * dx + dy * dy

            if (distSq <= range * range) {
                // Apply buff
                otherAi.isBuffed = true
                otherAi.buffTimeRemaining = BUFF_DURATION
                otherStats.moveSpeed *= BUFF_SPEED_MULTIPLIER
                otherStats.damageMultiplier *= BUFF_DAMAGE_MULTIPLIER
            }
        }
    }

    private fun removeBuff(entity: Entity, ai: AIComponent) {
        ai.isBuffed = false
        ai.buffTimeRemaining = 0f
        val stats = entity.getComponent(StatsComponent::class) ?: return
        stats.moveSpeed /= BUFF_SPEED_MULTIPLIER
        stats.damageMultiplier /= BUFF_DAMAGE_MULTIPLIER
    }

    private fun updateChargeBehavior(
        transform: TransformComponent,
        velocity: VelocityComponent,
        stats: StatsComponent,
        targetTransform: TransformComponent,
        ai: AIComponent,
        deltaTime: Float
    ) {
        val dx = targetTransform.x - transform.x
        val dy = targetTransform.y - transform.y
        val distance = sqrt(dx * dx + dy * dy)

        if (ai.isCharging) {
            // Currently charging: move fast towards locked target position
            ai.chargeTimer -= deltaTime
            val cdx = ai.chargeTargetX - transform.x
            val cdy = ai.chargeTargetY - transform.y
            val cDist = sqrt(cdx * cdx + cdy * cdy)

            if (cDist > 5f && ai.chargeTimer > 0f) {
                val dirX = cdx / cDist
                val dirY = cdy / cDist
                val chargeSpeed = stats.moveSpeed * CHARGE_SPEED_MULTIPLIER * nightSpeedMultiplier
                velocity.vx = dirX * chargeSpeed
                velocity.vy = dirY * chargeSpeed
            } else {
                // Charge ended
                ai.isCharging = false
                ai.chargeCooldown = CHARGE_COOLDOWN
                velocity.vx = 0f
                velocity.vy = 0f
            }
        } else {
            // Normal movement: chase player
            if (distance > 0f) {
                val dirX = dx / distance
                val dirY = dy / distance
                val speed = stats.moveSpeed * nightSpeedMultiplier
                velocity.vx = dirX * speed
                velocity.vy = dirY * speed
            }

            // Start charge when in range and cooldown is ready
            if (distance <= ai.range && ai.chargeCooldown <= 0f) {
                ai.isCharging = true
                ai.chargeTimer = CHARGE_DURATION
                // Lock target position at start of charge
                ai.chargeTargetX = targetTransform.x
                ai.chargeTargetY = targetTransform.y
            }
        }
    }
}

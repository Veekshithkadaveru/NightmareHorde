package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.AIBehavior
import app.krafted.nightmarehorde.engine.core.components.AIComponent
import app.krafted.nightmarehorde.engine.core.components.BossComponent
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.ProjectileComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.physics.Collider
import app.krafted.nightmarehorde.game.entities.HitEffectEntity
import app.krafted.nightmarehorde.game.systems.BossSystem.Companion.BOSS_MAX_MELEE_HITS_PER_WINDOW
import app.krafted.nightmarehorde.game.systems.BossSystem.Companion.BOSS_MELEE_HIT_WINDOW

class CombatSystem(private val gameLoop: app.krafted.nightmarehorde.engine.core.GameLoop) : GameSystem(priority = 100) {

    /** Callback for on-death effects (e.g. Bloater explosion) */
    var onEnemyDeath: ((Entity) -> Unit)? = null

    /** Callback fired when a melee projectile hits a boss entity — used for retaliation */
    var onBossMeleeHit: ((Entity) -> Unit)? = null

    // Reusable buffers — avoids allocating two new filtered lists every single
    // frame.  Over a 7-minute session at 60 FPS that eliminates ~50,000
    // unnecessary list allocations and the associated GC pressure.
    private val projectileBuffer = ArrayList<Entity>(64)
    private val targetBuffer = ArrayList<Entity>(128)

    override fun update(deltaTime: Float, entities: List<Entity>) {
        // Single-pass classification into reusable buffers (zero allocation)
        projectileBuffer.clear()
        targetBuffer.clear()
        for (entity in entities) {
            if (entity.hasComponent(ProjectileComponent::class)) projectileBuffer.add(entity)
            if (entity.hasComponent(HealthComponent::class)) targetBuffer.add(entity)
        }

        for (projectileEntity in projectileBuffer) {
            val projectile = projectileEntity.getComponent(ProjectileComponent::class) ?: continue
            if (!projectileEntity.isActive) continue

            val projTransform = projectileEntity.getComponent(TransformComponent::class) ?: continue
            val projCollider = projectileEntity.getComponent(ColliderComponent::class) ?: continue

            for (targetEntity in targetBuffer) {
                if (targetEntity.id == projectile.ownerId || !targetEntity.isActive) continue
                if (targetEntity.hasComponent(ProjectileComponent::class)) continue

                val targetTransform = targetEntity.getComponent(TransformComponent::class) ?: continue
                val targetCollider = targetEntity.getComponent(ColliderComponent::class) ?: continue

                // Enemy projectiles only hit player, player projectiles only hit enemies
                if (projCollider.layer == CollisionLayer.ENEMY && targetCollider.layer == CollisionLayer.ENEMY) continue
                if (projCollider.layer == CollisionLayer.PROJECTILE && targetCollider.layer == CollisionLayer.PLAYER) continue

                if (checkCollision(projTransform, projCollider, targetTransform, targetCollider)) {
                    handleHit(projectileEntity, projectile, targetEntity)
                    if (!projectile.penetrating && !projectileEntity.isActive) break
                }
            }
        }
    }

    private fun checkCollision(
        t1: TransformComponent, c1: ColliderComponent,
        t2: TransformComponent, c2: ColliderComponent
    ): Boolean {
        val r1 = (c1.collider as? Collider.Circle)?.radius ?: return false
        val r2 = (c2.collider as? Collider.Circle)?.radius ?: return false
        
        val dx = t1.x - t2.x
        val dy = t1.y - t2.y
        val distanceSquared = dx * dx + dy * dy
        val radiusSum = r1 + r2
        return distanceSquared <= radiusSum * radiusSum
    }

    private fun handleHit(projectileEntity: Entity, projectile: ProjectileComponent, targetEntity: Entity) {
        val health = targetEntity.getComponent(HealthComponent::class)
        if (health != null && health.isAlive) {
            // ── Boss penetrating-hit cap + multi-hit resistance ──
            // Prevents penetrating weapons (whip blade = 12 segments) from melting bosses.
            // Two layers:
            //   1) Hard cap: max 3 penetrating hits per 0.4s window (rest are ignored)
            //   2) Resistance: each allowed hit still deals reduced damage (50-60%)
            var finalDamage = projectile.damage
            val bossComp = targetEntity.getComponent(BossComponent::class)
            if (bossComp != null && projectile.penetrating) {
                // Hard cap: reject hits beyond the max per window
                if (bossComp.meleeHitCount >= BOSS_MAX_MELEE_HITS_PER_WINDOW) {
                    return  // Absorb — no damage, no effects
                }
                // Track this hit and start/extend the window timer
                bossComp.meleeHitCount = bossComp.meleeHitCount + 1
                bossComp.meleeHitWindowTimer = BOSS_MELEE_HIT_WINDOW
                // Resistance: reduce damage further
                val resist = bossComp.bossType.multiHitResistance
                finalDamage = (projectile.damage * (1.0f - resist)).coerceAtLeast(1.0f)
            }
            val damageDealt = health.takeDamage(finalDamage.toInt())

            // Spawn Damage Popup + Hit Particles
            val targetTransform = targetEntity.getComponent(TransformComponent::class)
            if (targetTransform != null) {
                val popup = app.krafted.nightmarehorde.game.entities.DamagePopupEntity(
                    x = targetTransform.x,
                    y = targetTransform.y - 30f, // Spawn slightly above
                    damage = damageDealt
                )
                gameLoop.addEntity(popup)

                // Hit effect particle burst
                HitEffectEntity.burst(
                    x = targetTransform.x,
                    y = targetTransform.y
                ).forEach { gameLoop.addEntity(it) }
            }

            // Boss melee retaliation: if this is a melee projectile hitting a boss, notify BossSystem
            if (projectile.isMelee && health.isAlive && targetEntity.hasComponent(BossComponent::class)) {
                onBossMeleeHit?.invoke(targetEntity)
            }

            if (!health.isAlive) {
                onEnemyDeath?.invoke(targetEntity)
                targetEntity.isActive = false
            }
        }

        if (!projectile.penetrating) {
            projectileEntity.isActive = false
        }
    }
}

package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.ProjectileComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.physics.Collider
import app.krafted.nightmarehorde.game.entities.HitEffectEntity

class CombatSystem(private val gameLoop: app.krafted.nightmarehorde.engine.core.GameLoop) : GameSystem(priority = 100) {

    override fun update(deltaTime: Float, entities: List<Entity>) {
        val projectiles = entities.filter { it.hasComponent(ProjectileComponent::class) }
        val targets = entities.filter { it.hasComponent(HealthComponent::class) }

        for (projectileEntity in projectiles) {
            val projectile = projectileEntity.getComponent(ProjectileComponent::class) ?: continue
            if (!projectileEntity.isActive) continue

            val projTransform = projectileEntity.getComponent(TransformComponent::class) ?: continue
            val projCollider = projectileEntity.getComponent(ColliderComponent::class) ?: continue

            for (targetEntity in targets) {
                if (targetEntity.id == projectile.ownerId || !targetEntity.isActive) continue
                if (targetEntity.hasComponent(ProjectileComponent::class)) continue

                val targetTransform = targetEntity.getComponent(TransformComponent::class) ?: continue
                val targetCollider = targetEntity.getComponent(ColliderComponent::class) ?: continue

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
            val damageDealt = health.takeDamage(projectile.damage.toInt())
            
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
            
            if (!health.isAlive) {
                targetEntity.isActive = false
            }
        }

        if (!projectile.penetrating) {
            projectileEntity.isActive = false
        }
    }
}

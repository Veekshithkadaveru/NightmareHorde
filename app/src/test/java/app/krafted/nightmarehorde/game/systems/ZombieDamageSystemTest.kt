package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.AIComponent
import app.krafted.nightmarehorde.engine.core.components.AIBehavior
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.PlayerTagComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.physics.Collider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ZombieDamageSystemTest {

    private lateinit var system: ZombieDamageSystem
    private lateinit var player: Entity
    private lateinit var zombie: Entity

    @Before
    fun setup() {
        system = ZombieDamageSystem()

        player = Entity().apply {
            addComponent(PlayerTagComponent())
            addComponent(TransformComponent(x = 0f, y = 0f))
            addComponent(HealthComponent(maxHealth = 100))
            addComponent(StatsComponent(armor = 0))
            addComponent(ColliderComponent(
                collider = Collider.Circle(15f),
                layer = CollisionLayer.PLAYER
            ))
        }

        zombie = Entity().apply {
            addComponent(TransformComponent(x = 20f, y = 0f))
            addComponent(StatsComponent(moveSpeed = 40f, baseDamage = 10f))
            addComponent(AIComponent(behavior = AIBehavior.CHASE))
            addComponent(ColliderComponent(
                collider = Collider.Circle(15f),
                layer = CollisionLayer.ENEMY
            ))
        }
    }

    @Test
    fun `Zombie deals contact damage when overlapping player`() {
        val health = player.getComponent(HealthComponent::class)!!

        system.update(0.016f, listOf(player, zombie))

        assertTrue(health.currentHealth < 100)
    }

    @Test
    fun `Zombie does not deal damage when far from player`() {
        zombie.getComponent(TransformComponent::class)?.x = 500f
        val health = player.getComponent(HealthComponent::class)!!

        system.update(0.016f, listOf(player, zombie))

        assertEquals(100, health.currentHealth)
    }

    @Test
    fun `Contact damage respects invincibility frames`() {
        val health = player.getComponent(HealthComponent::class)!!

        // First hit
        system.update(0.016f, listOf(player, zombie))
        val hpAfterFirst = health.currentHealth

        // Immediately after, player should be invincible
        system.update(0.016f, listOf(player, zombie))

        assertEquals(hpAfterFirst, health.currentHealth)
    }

    @Test
    fun `Contact damage uses zombie damage multiplier`() {
        zombie.getComponent(StatsComponent::class)!!.damageMultiplier = 2f
        val health = player.getComponent(HealthComponent::class)!!

        system.update(0.016f, listOf(player, zombie))

        // 10 baseDamage * 2 multiplier = 20 damage
        assertEquals(80, health.currentHealth)
    }

    @Test
    fun `No damage when player is dead`() {
        val health = player.getComponent(HealthComponent::class)!!
        health.setHealth(0)

        system.update(0.016f, listOf(player, zombie))

        assertEquals(0, health.currentHealth)
    }
}

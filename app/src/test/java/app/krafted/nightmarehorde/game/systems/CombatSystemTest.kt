package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.PlayerTagComponent
import app.krafted.nightmarehorde.engine.core.components.ProjectileComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.physics.Collider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CombatSystemTest {

    private lateinit var system: CombatSystem
    private var deadEnemy: Entity? = null

    @Before
    fun setup() {
        system = CombatSystem(GameLoop())
        deadEnemy = null
        system.onEnemyDeath = { deadEnemy = it }
    }

    /** Player overlapping the projectile at the origin, with the given armor. */
    private fun makePlayer(maxHealth: Int = 100, armor: Int = 0) = Entity().apply {
        addComponent(PlayerTagComponent())
        addComponent(TransformComponent(x = 0f, y = 0f))
        addComponent(HealthComponent(maxHealth = maxHealth))
        addComponent(StatsComponent(armor = armor))
        addComponent(ColliderComponent(Collider.Circle(15f), layer = CollisionLayer.PLAYER))
    }

    /** Enemy projectile (Spitter spit / boss attack) sitting on top of its target. */
    private fun makeEnemyProjectile(damage: Float) = Entity().apply {
        addComponent(ProjectileComponent(damage = damage, ownerId = -1L))
        addComponent(TransformComponent(x = 0f, y = 0f))
        addComponent(ColliderComponent(Collider.Circle(5f), layer = CollisionLayer.ENEMY))
    }

    @Test
    fun `Enemy projectile killing player does not fire onEnemyDeath and leaves player active`() {
        val player = makePlayer(maxHealth = 100)
        val projectile = makeEnemyProjectile(damage = 100f)
        val health = player.getComponent(HealthComponent::class)!!

        system.update(0.016f, listOf(projectile, player))

        // Player dropped to 0 HP but must remain in the loop for PlayerSystem to handle.
        assertFalse(health.isAlive)
        assertTrue(player.isActive)
        assertNull(deadEnemy)
    }

    @Test
    fun `Player armor reduces projectile damage`() {
        val player = makePlayer(maxHealth = 100, armor = 10)
        val projectile = makeEnemyProjectile(damage = 30f)
        val health = player.getComponent(HealthComponent::class)!!

        system.update(0.016f, listOf(projectile, player))

        // 30 incoming - 10 armor = 20 dealt → 80 remaining.
        assertEquals(80, health.currentHealth)
    }

    @Test
    fun `Enemy target killed by projectile still fires onEnemyDeath and is deactivated`() {
        val enemy = Entity().apply {
            addComponent(TransformComponent(x = 0f, y = 0f))
            addComponent(HealthComponent(maxHealth = 50))
            addComponent(ColliderComponent(Collider.Circle(15f), layer = CollisionLayer.ENEMY))
        }
        // Player-fired projectile (PROJECTILE layer) hits the enemy.
        val projectile = Entity().apply {
            addComponent(ProjectileComponent(damage = 100f, ownerId = -1L))
            addComponent(TransformComponent(x = 0f, y = 0f))
            addComponent(ColliderComponent(Collider.Circle(5f), layer = CollisionLayer.PROJECTILE))
        }

        system.update(0.016f, listOf(projectile, enemy))

        assertSame(enemy, deadEnemy)
        assertFalse(enemy.isActive)
    }
}

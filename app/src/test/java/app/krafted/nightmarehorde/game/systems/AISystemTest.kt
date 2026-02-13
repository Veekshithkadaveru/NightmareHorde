package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.AIBehavior
import app.krafted.nightmarehorde.engine.core.components.AIComponent
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AISystemTest {

    private lateinit var aiSystem: AISystem
    private lateinit var player: Entity
    private lateinit var zombie: Entity

    @Before
    fun setup() {
        aiSystem = AISystem()

        player = Entity().apply {
            addComponent(TransformComponent(x = 0f, y = 0f))
            addComponent(HealthComponent(maxHealth = 100))
            addComponent(StatsComponent(armor = 0))
        }

        zombie = Entity().apply {
            addComponent(TransformComponent(x = 100f, y = 0f))
            addComponent(VelocityComponent(0f, 0f))
            addComponent(StatsComponent(moveSpeed = 10f, baseDamage = 5f))
            addComponent(AIComponent())
            addComponent(HealthComponent(maxHealth = 50))
        }

        aiSystem.setPlayer(player)
    }

    // --- CHASE behavior ---

    @Test
    fun `Chase behavior moves towards player`() {
        zombie.getComponent(AIComponent::class)?.behavior = AIBehavior.CHASE

        aiSystem.update(0.016f, listOf(zombie))

        val velocity = zombie.getComponent(VelocityComponent::class)!!
        assertEquals(-10f, velocity.vx, 0.1f)
        assertEquals(0f, velocity.vy, 0.1f)
    }

    @Test
    fun `Chase behavior moves diagonally when offset`() {
        zombie.getComponent(TransformComponent::class)?.apply {
            x = 100f
            y = 100f
        }
        zombie.getComponent(AIComponent::class)?.behavior = AIBehavior.CHASE

        aiSystem.update(0.016f, listOf(zombie))

        val velocity = zombie.getComponent(VelocityComponent::class)!!
        // Should move at ~7.07 in each axis (10 / sqrt(2))
        assertTrue(velocity.vx < 0f)
        assertTrue(velocity.vy < 0f)
    }

    // --- RANGED behavior ---

    @Test
    fun `Ranged behavior stops at range`() {
        zombie.getComponent(TransformComponent::class)?.x = 50f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.RANGED
        ai.range = 60f

        aiSystem.update(0.016f, listOf(zombie))

        val velocity = zombie.getComponent(VelocityComponent::class)!!
        assertEquals(0f, velocity.vx, 0.1f)
        assertEquals(0f, velocity.vy, 0.1f)
    }

    @Test
    fun `Ranged behavior chases when out of range`() {
        zombie.getComponent(TransformComponent::class)?.x = 100f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.RANGED
        ai.range = 50f

        aiSystem.update(0.016f, listOf(zombie))

        val velocity = zombie.getComponent(VelocityComponent::class)!!
        assertEquals(-10f, velocity.vx, 0.1f)
        assertEquals(0f, velocity.vy, 0.1f)
    }

    @Test
    fun `Ranged behavior fires projectile when in range and cooldown ready`() {
        zombie.getComponent(TransformComponent::class)?.x = 50f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.RANGED
        ai.range = 60f
        ai.attackCooldown = 0f

        var spawnedEntities = 0
        aiSystem.onSpawnEntity = { spawnedEntities++ }

        aiSystem.update(0.016f, listOf(zombie))

        assertEquals(1, spawnedEntities)
        assertTrue(ai.attackCooldown > 0f)
    }

    @Test
    fun `Ranged behavior does not fire when on cooldown`() {
        zombie.getComponent(TransformComponent::class)?.x = 50f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.RANGED
        ai.range = 60f
        ai.attackCooldown = 1.0f

        var spawnedEntities = 0
        aiSystem.onSpawnEntity = { spawnedEntities++ }

        aiSystem.update(0.016f, listOf(zombie))

        assertEquals(0, spawnedEntities)
    }

    // --- EXPLODE behavior ---

    @Test
    fun `Explode behavior chases player when far away`() {
        zombie.getComponent(TransformComponent::class)?.x = 200f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.EXPLODE
        ai.range = 40f

        aiSystem.update(0.016f, listOf(zombie))

        val velocity = zombie.getComponent(VelocityComponent::class)!!
        assertTrue(velocity.vx < 0f) // Moving towards player
    }

    @Test
    fun `Explode behavior detonates when in range`() {
        zombie.getComponent(TransformComponent::class)?.x = 30f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.EXPLODE
        ai.range = 40f

        val zombieHealth = zombie.getComponent(HealthComponent::class)!!

        var particleCount = 0
        aiSystem.onSpawnEntity = { particleCount++ }

        aiSystem.update(0.016f, listOf(zombie))

        // Zombie should be dead
        assertFalse(zombieHealth.isAlive)
        assertFalse(zombie.isActive)
    }

    @Test
    fun `Explode behavior damages player within explosion radius`() {
        zombie.getComponent(TransformComponent::class)?.x = 30f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.EXPLODE
        ai.range = 40f
        zombie.getComponent(StatsComponent::class)!!.baseDamage = 15f

        aiSystem.onSpawnEntity = {} // Need callback for particles

        val playerHealth = player.getComponent(HealthComponent::class)!!
        val initialHp = playerHealth.currentHealth

        aiSystem.update(0.016f, listOf(zombie))

        assertTrue(playerHealth.currentHealth < initialHp)
    }

    // --- CHARGE behavior ---

    @Test
    fun `Charge behavior chases normally when far from player`() {
        zombie.getComponent(TransformComponent::class)?.x = 500f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.CHARGE
        ai.range = 200f

        aiSystem.update(0.016f, listOf(zombie))

        val velocity = zombie.getComponent(VelocityComponent::class)!!
        assertEquals(-10f, velocity.vx, 0.1f) // Normal speed chase
        assertFalse(ai.isCharging)
    }

    @Test
    fun `Charge behavior starts charge when in range and cooldown ready`() {
        zombie.getComponent(TransformComponent::class)?.x = 150f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.CHARGE
        ai.range = 200f
        ai.chargeCooldown = 0f

        aiSystem.update(0.016f, listOf(zombie))

        assertTrue(ai.isCharging)
        assertTrue(ai.chargeTimer > 0f)
    }

    @Test
    fun `Charge behavior does not start when on cooldown`() {
        zombie.getComponent(TransformComponent::class)?.x = 150f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.CHARGE
        ai.range = 200f
        ai.chargeCooldown = 3.0f

        aiSystem.update(0.016f, listOf(zombie))

        assertFalse(ai.isCharging)
    }

    @Test
    fun `Charge behavior moves at boosted speed`() {
        zombie.getComponent(TransformComponent::class)?.x = 150f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.CHARGE
        ai.range = 200f
        ai.chargeCooldown = 0f

        // First update starts the charge
        aiSystem.update(0.016f, listOf(zombie))
        assertTrue(ai.isCharging)

        // Second update should move at charge speed
        aiSystem.update(0.016f, listOf(zombie))

        val velocity = zombie.getComponent(VelocityComponent::class)!!
        val expectedChargeSpeed = 10f * AISystem.CHARGE_SPEED_MULTIPLIER
        // Velocity magnitude should be near charge speed
        val speed = kotlin.math.sqrt(velocity.vx * velocity.vx + velocity.vy * velocity.vy)
        assertEquals(expectedChargeSpeed, speed, 1f)
    }

    @Test
    fun `Charge behavior ends after timer expires`() {
        zombie.getComponent(TransformComponent::class)?.x = 150f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.CHARGE
        ai.range = 200f
        ai.isCharging = true
        ai.chargeTimer = 0.1f
        ai.chargeTargetX = 0f
        ai.chargeTargetY = 0f

        // Simulate enough time to exhaust the charge timer
        aiSystem.update(0.15f, listOf(zombie))

        assertFalse(ai.isCharging)
        assertTrue(ai.chargeCooldown > 0f)
    }

    // --- BUFF behavior ---

    @Test
    fun `Buff behavior moves towards player when far`() {
        zombie.getComponent(TransformComponent::class)?.x = 300f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.BUFF
        ai.range = 150f

        aiSystem.update(0.016f, listOf(zombie))

        val velocity = zombie.getComponent(VelocityComponent::class)!!
        assertTrue(velocity.vx < 0f) // Moving towards player
    }

    @Test
    fun `Buff behavior stops at preferred distance`() {
        zombie.getComponent(TransformComponent::class)?.x = 100f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.BUFF
        ai.range = 150f // 100 is within range

        aiSystem.update(0.016f, listOf(zombie))

        val velocity = zombie.getComponent(VelocityComponent::class)!!
        assertEquals(0f, velocity.vx, 0.1f)
    }

    @Test
    fun `Buff behavior buffs nearby allied zombie`() {
        zombie.getComponent(TransformComponent::class)?.x = 100f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.BUFF
        ai.range = 150f
        ai.buffTimer = AISystem.BUFF_INTERVAL // Ready to buff

        // Create an ally zombie nearby
        val ally = Entity().apply {
            addComponent(TransformComponent(x = 110f, y = 0f))
            addComponent(VelocityComponent())
            addComponent(StatsComponent(moveSpeed = 40f, baseDamage = 5f))
            addComponent(AIComponent(behavior = AIBehavior.CHASE))
        }
        val allyOriginalSpeed = ally.getComponent(StatsComponent::class)!!.moveSpeed

        aiSystem.update(0.016f, listOf(zombie, ally))

        val allyAi = ally.getComponent(AIComponent::class)!!
        val allyStats = ally.getComponent(StatsComponent::class)!!

        assertTrue(allyAi.isBuffed)
        assertTrue(allyStats.moveSpeed > allyOriginalSpeed)
    }

    @Test
    fun `Buff behavior does not buff other Screamers`() {
        zombie.getComponent(TransformComponent::class)?.x = 100f
        val ai = zombie.getComponent(AIComponent::class)!!
        ai.behavior = AIBehavior.BUFF
        ai.range = 150f
        ai.buffTimer = AISystem.BUFF_INTERVAL

        val otherScreamer = Entity().apply {
            addComponent(TransformComponent(x = 110f, y = 0f))
            addComponent(VelocityComponent())
            addComponent(StatsComponent(moveSpeed = 45f))
            addComponent(AIComponent(behavior = AIBehavior.BUFF))
        }

        aiSystem.update(0.016f, listOf(zombie, otherScreamer))

        assertFalse(otherScreamer.getComponent(AIComponent::class)!!.isBuffed)
    }

    @Test
    fun `Buff expires and stats are restored`() {
        val ally = Entity().apply {
            addComponent(TransformComponent(x = 50f, y = 0f))
            addComponent(VelocityComponent())
            addComponent(StatsComponent(moveSpeed = 40f, baseDamage = 5f))
            addComponent(AIComponent(
                behavior = AIBehavior.CHASE,
                isBuffed = true,
                buffTimeRemaining = 0.01f // Almost expired
            ))
        }
        // Simulate buffed stats
        ally.getComponent(StatsComponent::class)!!.apply {
            moveSpeed *= AISystem.BUFF_SPEED_MULTIPLIER
            damageMultiplier *= AISystem.BUFF_DAMAGE_MULTIPLIER
        }

        aiSystem.update(0.02f, listOf(ally)) // Enough to expire buff

        val allyAi = ally.getComponent(AIComponent::class)!!
        val allyStats = ally.getComponent(StatsComponent::class)!!

        assertFalse(allyAi.isBuffed)
        assertEquals(40f, allyStats.moveSpeed, 0.5f) // Restored
        assertEquals(1f, allyStats.damageMultiplier, 0.05f) // Restored
    }

    // --- No player ---

    @Test
    fun `System does nothing without player`() {
        val system = AISystem()
        // Don't set player
        zombie.getComponent(AIComponent::class)?.behavior = AIBehavior.CHASE

        system.update(0.016f, listOf(zombie))

        val velocity = zombie.getComponent(VelocityComponent::class)!!
        assertEquals(0f, velocity.vx, 0.001f)
    }
}

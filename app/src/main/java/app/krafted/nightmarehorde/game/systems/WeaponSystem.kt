package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.Vector2
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.WeaponComponent
import app.krafted.nightmarehorde.game.entities.ProjectileEntity
import app.krafted.nightmarehorde.game.weapons.Weapon

class WeaponSystem(
    private val gameLoop: GameLoop
) : GameSystem(priority = 30) { // Run before movement

    override fun update(deltaTime: Float, entities: List<Entity>) {
        entities.forEach { entity ->
            val weaponComp = entity.getComponent(WeaponComponent::class) ?: return@forEach
            val transform = entity.getComponent(TransformComponent::class) ?: return@forEach

            val weapon = weaponComp.equippedWeapon ?: return@forEach

            // Always tick cooldown every frame
            weapon.tickCooldown(deltaTime)

            // Only fire when cooldown has expired
            if (!weapon.isReady()) return@forEach

            // Use facing direction set by PlayerSystem (persists last aim when standing still)
            val direction = weaponComp.facingDirection
            // Guard against zero-length direction (should not happen, but prevents invisible projectiles)
            if (direction.lengthSquared() < 0.001f) return@forEach

            // Fire!
            fireWeapon(entity, weapon, transform, direction, weaponComp)
            weapon.resetCooldown()
        }
    }
    
    private val random = java.util.Random()

    companion object {
        const val DEBUG_INFINITE_AMMO = true
    }

    private fun fireWeapon(
        owner: Entity,
        weapon: Weapon,
        transform: TransformComponent,
        direction: Vector2,
        weaponComp: WeaponComponent
    ) {
        if (!DEBUG_INFINITE_AMMO && !weapon.infiniteAmmo) {
            if (weaponComp.currentAmmo <= 0) return
            weaponComp.currentAmmo--
        }

        when {
            weapon.isFlame -> fireFlame(owner, weapon, transform, direction)
            weapon.isMelee -> fireSword(owner, weapon, transform, direction)
            else -> fireStandard(owner, weapon, transform, direction)
        }
    }

    private fun fireStandard(
        owner: Entity,
        weapon: Weapon,
        transform: TransformComponent,
        direction: Vector2
    ) {
        val count = weapon.projectileCount
        var startAngle = 0f
        var angleStep = 0f

        if (count > 1) {
            val totalSpread = weapon.spreadAngle
            angleStep = totalSpread / (count - 1)
            startAngle = -totalSpread / 2f
        }

        for (i in 0 until count) {
            val spread = startAngle + (angleStep * i)
            val finalDirection = if (count > 1) direction.rotate(spread) else direction

            // Direction angle in radians for transform rotation
            val projectile = ProjectileEntity(
                x = transform.x,
                y = transform.y,
                rotation = finalDirection.angle(),
                speed = weapon.projectileSpeed,
                damage = weapon.damage,
                ownerId = owner.id,
                lifeTime = weapon.range / weapon.projectileSpeed // Convert range to lifetime
            )
            gameLoop.addEntity(projectile)
        }
    }

    private fun fireFlame(
        owner: Entity,
        weapon: Weapon,
        transform: TransformComponent,
        direction: Vector2
    ) {
        // Random spread within the cone for each flame particle
        val randomSpread = (random.nextFloat() - 0.5f) * weapon.spreadAngle
        val finalDirection = direction.rotate(randomSpread)

        // Slight speed variation for organic flame look
        val speedVariation = weapon.projectileSpeed * (0.8f + random.nextFloat() * 0.4f)

        val projectile = ProjectileEntity(
            x = transform.x + finalDirection.x * 15f, // Offset slightly forward
            y = transform.y + finalDirection.y * 15f,
            rotation = finalDirection.angle(),
            speed = speedVariation,
            damage = weapon.damage,
            ownerId = owner.id,
            lifeTime = 0.6f // Short-lived flame particle
        )
        gameLoop.addEntity(projectile)
    }

    private fun fireSword(
        owner: Entity,
        weapon: Weapon,
        transform: TransformComponent,
        direction: Vector2
    ) {
        // VS-style sword slash: ONE large horizontal slash on the facing side of the player.
        val offsetDist = weapon.range
        val projectile = ProjectileEntity(
            x = transform.x + direction.x * offsetDist,
            y = transform.y + direction.y * offsetDist,
            rotation = direction.angle(),
            speed = 0f, // Stationary slash
            damage = weapon.damage,
            ownerId = owner.id,
            lifeTime = 0.25f // Brief flash
        )
        gameLoop.addEntity(projectile)
    }
}

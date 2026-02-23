package app.krafted.nightmarehorde.game.systems

import androidx.compose.ui.graphics.Color
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.Vector2
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.WeaponComponent
import app.krafted.nightmarehorde.engine.core.components.WeaponInventoryComponent
import app.krafted.nightmarehorde.game.entities.ProjectileEntity
import app.krafted.nightmarehorde.game.weapons.Weapon
import app.krafted.nightmarehorde.game.weapons.WeaponType

class WeaponSystem(
    private val gameLoop: GameLoop
) : GameSystem(priority = 30) { // Run before movement

    override fun update(deltaTime: Float, entities: List<Entity>) {
        entities.forEach { entity ->
            val weaponComp = entity.getComponent(WeaponComponent::class) ?: return@forEach
            val transform = entity.getComponent(TransformComponent::class) ?: return@forEach

            // Use inventory if available, otherwise fall back to WeaponComponent
            val inventory = entity.getComponent(WeaponInventoryComponent::class)
            val weapon = if (inventory != null) {
                val activeWeapon = inventory.getActiveWeapon() ?: return@forEach
                weaponComp.equippedWeapon = activeWeapon
                activeWeapon
            } else {
                weaponComp.equippedWeapon ?: return@forEach
            }

            weapon.tickCooldown(deltaTime)
            if (!weapon.isReady()) return@forEach

            val direction = weaponComp.facingDirection
            if (direction.lengthSquared() < 0.001f) return@forEach

            // Check ammo via inventory
            if (inventory != null && !weapon.infiniteAmmo) {
                val slot = inventory.getActiveSlot()
                if (slot != null && slot.currentAmmo <= 0) {
                    inventory.fallbackToDefault()
                    weaponComp.equippedWeapon = inventory.getActiveWeapon()
                    onAmmoEmpty?.invoke(weapon.type)
                    return@forEach
                }
            }

            fireWeapon(entity, weapon, transform, direction, inventory)
            weapon.resetCooldown()

            // Apply cooldown reduction from stats
            val stats = entity.getComponent(StatsComponent::class)
            if (stats != null && stats.cooldownReduction > 0f) {
                weapon.applyCooldownReduction(stats.cooldownReduction)
            }
        }
    }
    
    private val random = java.util.Random()

    var onAmmoEmpty: ((WeaponType) -> Unit)? = null

    companion object {
        const val DEBUG_INFINITE_AMMO = false

        // Flame particle colors — random pick for organic fire look
        private val FLAME_COLORS = listOf(
            Color(0xFFFF4500),  // OrangeRed
            Color(0xFFFF6600),  // Deep orange
            Color(0xFFFF8800),  // Orange
            Color(0xFFFFAA00),  // Amber
            Color(0xFFFFCC00),  // Yellow-orange
            Color(0xFFFFDD44),  // Bright yellow
        )

        // Whip blade sweep config
        const val WHIP_ARC_DEGREES = 180f     // Full semicircle sweep
        const val WHIP_SEGMENT_COUNT = 12     // More segments = smoother blade arc
        const val WHIP_HITBOX_RADIUS = 14f    // Each segment's collision radius
        const val WHIP_BLADE_LENGTH = 28f     // Base length of each blade segment (oval width)
        const val WHIP_BLADE_THICKNESS = 6f   // Thickness at widest point (oval height)

        // Broad sword thrust config — forward cleave, not an arc
        const val SWORD_SEGMENT_COUNT = 4      // Segments stacked along the thrust line
        const val SWORD_HITBOX_RADIUS = 20f    // Large hitbox per segment — wide blade
        const val SWORD_BLADE_WIDTH = 40f      // Width of each blade segment (perpendicular to thrust)
        const val SWORD_BLADE_DEPTH = 12f      // Depth of each segment (along thrust direction)
    }

    private fun fireWeapon(
        owner: Entity,
        weapon: Weapon,
        transform: TransformComponent,
        direction: Vector2,
        inventory: WeaponInventoryComponent?
    ) {
        if (!weapon.infiniteAmmo) {
            if (inventory != null) {
                if (!inventory.consumeAmmo(weapon.type)) return
            } else if (!DEBUG_INFINITE_AMMO) {
                val weaponComp = owner.getComponent(WeaponComponent::class) ?: return
                if (weaponComp.currentAmmo <= 0) return
                weaponComp.currentAmmo--
            }
        }

        when {
            weapon.isFlame -> fireFlame(owner, weapon, transform, direction)
            weapon.isSword -> fireSwordSlash(owner, weapon, transform, direction)
            weapon.isMelee -> fireWhipSweep(owner, weapon, transform, direction)
            else -> fireStandard(owner, weapon, transform, direction)
        }
    }

    private fun fireStandard(
        owner: Entity,
        weapon: Weapon,
        transform: TransformComponent,
        direction: Vector2
    ) {
        val stats = owner.getComponent(StatsComponent::class)
        val count = weapon.projectileCount + (stats?.projectileCountBonus ?: 0)
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

            val projectile = ProjectileEntity(
                x = transform.x,
                y = transform.y,
                rotation = finalDirection.angle(),
                speed = weapon.projectileSpeed,
                damage = weapon.damage,
                ownerId = owner.id,
                lifeTime = weapon.range / weapon.projectileSpeed
            )
            gameLoop.addEntity(projectile)
        }
    }

    /**
     * Flamethrower: fires orange/red/yellow particle projectiles in a cone.
     * Each particle is a fading colored circle that deals damage and pierces enemies.
     */
    private fun fireFlame(
        owner: Entity,
        weapon: Weapon,
        transform: TransformComponent,
        direction: Vector2
    ) {
        // Random spread within the cone for each flame particle
        val randomSpread = (random.nextFloat() - 0.5f) * weapon.spreadAngle
        val finalDirection = direction.rotate(randomSpread)

        // Slight speed variation for cohesive flame stream
        val speedVariation = weapon.projectileSpeed * (0.8f + random.nextFloat() * 0.4f)

        // Random fire color for each particle
        val color = FLAME_COLORS[random.nextInt(FLAME_COLORS.size)]
        // Bigger flame particles for devastating visual
        val size = 8f + random.nextFloat() * 10f

        val projectile = ProjectileEntity(
            x = transform.x + finalDirection.x * 20f,
            y = transform.y + finalDirection.y * 20f,
            rotation = finalDirection.angle(),
            speed = speedVariation,
            damage = weapon.damage,
            ownerId = owner.id,
            lifeTime = 0.85f,
            penetrating = true,
            colliderRadius = 14f,
            particleColor = color,    // Renders as fire circle, not bullet sprite
            particleSize = size
        )
        gameLoop.addEntity(projectile)
    }

    /**
     * VS-style whip blade sweep: spawns a wide arc of elongated blade segments
     * around the facing direction. Each segment is a thin, rotated oval oriented
     * tangent to the arc — together they form a curved slash like Vampire Survivors'
     * whip. Segments taper: thicker/longer at the middle of the arc, thinner at edges.
     */
    private fun fireWhipSweep(
        owner: Entity,
        weapon: Weapon,
        transform: TransformComponent,
        direction: Vector2
    ) {
        val stats = owner.getComponent(StatsComponent::class)
        val areaMult = stats?.areaMultiplier ?: 1f
        val halfArc = WHIP_ARC_DEGREES / 2f
        val angleStep = WHIP_ARC_DEGREES / (WHIP_SEGMENT_COUNT - 1).coerceAtLeast(1)
        val reachDistance = weapon.range * areaMult

        for (i in 0 until WHIP_SEGMENT_COUNT) {
            val sweepAngle = -halfArc + angleStep * i
            val segDirection = direction.rotate(sweepAngle)

            // Normalized position along the arc [0..1], with 0.5 = center
            val t = i.toFloat() / (WHIP_SEGMENT_COUNT - 1).coerceAtLeast(1)

            // Distance from player increases along the arc — curved reach
            val segDistance = reachDistance * (0.65f + 0.35f * t)

            // Taper: segments at the center of the arc are larger, edges are thinner
            // Uses a bell curve peaking at t=0.5
            val taper = 1f - 2f * (t - 0.5f) * (t - 0.5f)  // peak at center
            val bladeLen = WHIP_BLADE_LENGTH * (0.5f + 0.7f * taper)
            val bladeThick = WHIP_BLADE_THICKNESS * (0.4f + 0.8f * taper)

            // Orient each segment tangent to the arc (perpendicular to segDirection)
            // This makes the ovals lie along the curve, forming a continuous slash line
            val tangentAngle = segDirection.angle() + (Math.PI / 2.0).toFloat()

            // Color gradient: white-core at center, icy blue at edges
            val centerBlend = taper  // 1.0 at center, 0.0 at edges
            val color = Color(
                red = 0.75f + 0.25f * centerBlend,
                green = 0.88f + 0.12f * centerBlend,
                blue = 1.0f,
                alpha = 1.0f
            )

            val projectile = ProjectileEntity(
                x = transform.x + segDirection.x * segDistance,
                y = transform.y + segDirection.y * segDistance,
                rotation = tangentAngle,
                speed = 0f,                 // Stationary — stays where spawned
                damage = weapon.damage,
                ownerId = owner.id,
                lifeTime = 0.25f,           // Brief flash
                penetrating = true,         // Hits all enemies in the arc
                colliderRadius = WHIP_HITBOX_RADIUS,
                particleColor = color,
                particleSize = 10f,         // Fallback (unused when width/height set)
                particleWidth = bladeLen,   // Elongated oval — blade segment
                particleHeight = bladeThick,
                isMelee = true              // Marks as melee for boss retaliation
            )
            gameLoop.addEntity(projectile)
        }
    }

    /**
     * Broad sword thrust: a straight-line forward cleave in the facing direction.
     * Segments are stacked along the thrust axis (close → far), creating a
     * rectangular blade that extends outward from the knight — like a heavy
     * overhead chop or forward stab. Completely distinct from the whip's arc.
     *
     * Visual: golden blade segments aligned perpendicular to the thrust direction,
     * tapering from wide (near player) to pointed (at tip).
     */
    private fun fireSwordSlash(
        owner: Entity,
        weapon: Weapon,
        transform: TransformComponent,
        direction: Vector2
    ) {
        val stats = owner.getComponent(StatsComponent::class)
        val areaMult = stats?.areaMultiplier ?: 1f
        val reachDistance = weapon.range * areaMult

        // Spacing between segments along the thrust line
        val segmentSpacing = reachDistance / SWORD_SEGMENT_COUNT

        for (i in 0 until SWORD_SEGMENT_COUNT) {
            // Normalized position along the blade [0..1], 0 = near player, 1 = tip
            val t = (i + 1).toFloat() / SWORD_SEGMENT_COUNT

            // Distance from player — segments evenly spaced along thrust direction
            val segDistance = segmentSpacing * (i + 1)

            // Taper: widest at base (near player), narrowing to a point at the tip
            val taper = 1f - (t * 0.5f)  // 1.0 at base → 0.5 at tip
            val bladeWidth = SWORD_BLADE_WIDTH * taper
            val bladeDepth = SWORD_BLADE_DEPTH * (0.8f + 0.2f * taper)

            // Orient each segment perpendicular to the thrust direction
            // so the wide side faces sideways (like a flat blade edge)
            val bladeAngle = direction.angle()

            // Color gradient: bright golden core near player, darker orange at tip
            val color = Color(
                red = 1.0f,
                green = 0.85f - 0.25f * t,   // Fades from bright gold to deep orange
                blue = 0.3f - 0.2f * t,       // Fades from warm to dark
                alpha = 1.0f
            )

            val projectile = ProjectileEntity(
                x = transform.x + direction.x * segDistance,
                y = transform.y + direction.y * segDistance,
                rotation = bladeAngle,
                speed = 0f,
                damage = weapon.damage,
                ownerId = owner.id,
                lifeTime = 0.3f,
                penetrating = true,
                colliderRadius = SWORD_HITBOX_RADIUS,
                particleColor = color,
                particleSize = 12f,
                particleWidth = bladeWidth,
                particleHeight = bladeDepth,
                isMelee = true
            )
            gameLoop.addEntity(projectile)
        }
    }
}

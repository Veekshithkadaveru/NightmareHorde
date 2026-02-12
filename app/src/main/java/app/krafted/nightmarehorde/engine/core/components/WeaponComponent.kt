package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component
import app.krafted.nightmarehorde.engine.core.Vector2
import app.krafted.nightmarehorde.game.weapons.Weapon

data class WeaponComponent(
    var equippedWeapon: Weapon? = null,
    var currentAmmo: Int = 0,
    var totalAmmo: Int = 0,
    /** Firing direction set by PlayerSystem each frame. WeaponSystem reads this directly. */
    var facingDirection: Vector2 = Vector2(1f, 0f)
) : Component

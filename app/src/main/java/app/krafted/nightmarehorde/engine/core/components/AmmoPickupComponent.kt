package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component
import app.krafted.nightmarehorde.game.weapons.WeaponType

data class AmmoPickupComponent(
    val amount: Int,
    val weaponType: WeaponType? = null
) : Component

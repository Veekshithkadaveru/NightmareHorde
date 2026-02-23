package app.krafted.nightmarehorde.game.weapons

/**
 * Commando's starting ranged weapon.
 * Fires 2 bullets per shot with slight spread. Lower per-bullet damage
 * than the regular Pistol, but the Commando's +50% fire rate passive
 * makes this a bullet hose.
 */
class DualPistolWeapon : Weapon(
    name = "Dual Pistols",
    damage = 8f,
    fireRate = 4f,
    range = 400f,
    projectileSpeed = 500f,
    maxAmmo = 0,
    infiniteAmmo = true,
    projectileCount = 2,
    spreadAngle = 8f,
    type = WeaponType.PISTOL
)

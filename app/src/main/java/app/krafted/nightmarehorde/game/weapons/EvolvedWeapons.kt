package app.krafted.nightmarehorde.game.weapons

/**
 * Evolved weapon variants created through the weapon evolution system.
 * Each evolved weapon is a significant upgrade over its base form.
 */

/** Pistol + Hollow Points (maxed) = Plasma Pistol */
class PlasmaPistolWeapon : Weapon(
    name = "Plasma Pistol",
    damage = 15f,
    fireRate = 3f,
    range = 500f,
    projectileSpeed = 600f,
    maxAmmo = 0,
    infiniteAmmo = true,
    penetrating = true,
    type = WeaponType.PISTOL
)

/** Shotgun + Multishot (maxed) = Hellfire Shotgun */
class HellfireShotgunWeapon : Weapon(
    name = "Hellfire Shotgun",
    damage = 20f,
    fireRate = 1.2f,
    range = 200f,
    projectileSpeed = 500f,
    maxAmmo = 12,
    projectileCount = 12,
    spreadAngle = 50f,
    type = WeaponType.SHOTGUN
)

/** Melee + Blast Radius (maxed) = Death Scythe */
class DeathScytheWeapon : Weapon(
    name = "Death Scythe",
    damage = 45f,
    fireRate = 2f,
    range = 180f,
    maxAmmo = 0,
    infiniteAmmo = true,
    penetrating = true,
    type = WeaponType.MELEE,
    isMelee = true
)

/** Flamethrower + Overclock (maxed) = Inferno Engine */
class InfernoEngineWeapon : Weapon(
    name = "Inferno Engine",
    damage = 22f,
    fireRate = 20f,
    range = 400f,
    projectileSpeed = 450f,
    maxAmmo = 400,
    spreadAngle = 30f,
    penetrating = true,
    type = WeaponType.FLAMETHROWER,
    isFlame = true
)

package app.krafted.nightmarehorde.game.weapons

class SMGWeapon : Weapon(
    name = "SMG",
    damage = 12f,
    fireRate = 12f, // Very fast
    range = 350f,
    projectileSpeed = 750f,
    maxAmmo = 180,
    spreadAngle = 10f, // Slight inaccuracy
    type = WeaponType.SMG
)

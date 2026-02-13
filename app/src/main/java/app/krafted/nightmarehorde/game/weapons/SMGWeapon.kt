package app.krafted.nightmarehorde.game.weapons

class SMGWeapon : Weapon(
    name = "SMG",
    damage = 8f,          // Low per-bullet, compensated by fire rate
    fireRate = 8f,         // 8 shots/sec — bullet hose
    range = 350f,          // Medium range
    projectileSpeed = 650f,
    maxAmmo = 40,          // Large clip
    spreadAngle = 12f,     // Slight inaccuracy
    type = WeaponType.SMG
)

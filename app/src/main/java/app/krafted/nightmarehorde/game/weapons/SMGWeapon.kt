package app.krafted.nightmarehorde.game.weapons

class SMGWeapon : Weapon(
    type = WeaponType.SMG,
    name = "SMG",
    damage = 8f,
    fireRate = 8f, // 8 shots per second
    range = 5000f, // Long range — travels until hitting obstacles/enemies
    projectileSpeed = 650f,
    maxAmmo = 40,
    infiniteAmmo = false
)

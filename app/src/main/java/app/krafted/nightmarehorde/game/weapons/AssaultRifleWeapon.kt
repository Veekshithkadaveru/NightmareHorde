package app.krafted.nightmarehorde.game.weapons

class AssaultRifleWeapon : Weapon(
    type = WeaponType.ASSAULT_RIFLE,
    name = "Assault Rifle",
    damage = 12f,
    fireRate = 5f, // 5 shots per second
    range = 5000f, // Long range — travels until hitting obstacles/enemies
    projectileSpeed = 700f,
    maxAmmo = 30,
    infiniteAmmo = false
)

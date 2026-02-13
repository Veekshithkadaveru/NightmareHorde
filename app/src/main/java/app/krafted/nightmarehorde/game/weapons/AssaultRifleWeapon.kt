package app.krafted.nightmarehorde.game.weapons

class AssaultRifleWeapon : Weapon(
    name = "Assault Rifle",
    damage = 12f,        // Moderate DMG, high DPS from fire rate
    fireRate = 5f,        // 5 shots/sec — sustained fire
    range = 500f,         // Long range — the go-to ranged weapon
    projectileSpeed = 700f,
    maxAmmo = 30,         // Clip-based
    type = WeaponType.ASSAULT_RIFLE
)

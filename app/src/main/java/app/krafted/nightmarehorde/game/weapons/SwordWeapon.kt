package app.krafted.nightmarehorde.game.weapons

class SwordWeapon : Weapon(
    name = "Whip Blade",
    damage = 25f,         // 2x+ close-range bonus — sweeps entire arc
    fireRate = 1.5f,       // 1.5 sweeps/sec — impactful, not spammable
    range = 120f,          // Wide sweep reach (VS whip style)
    projectileSpeed = 0f,  // Instant stationary hitbox arc
    maxAmmo = 0,
    infiniteAmmo = true,
    penetrating = true,    // Cleaves through all enemies in the arc
    type = WeaponType.MELEE,
    isMelee = true
)

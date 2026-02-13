package app.krafted.nightmarehorde.game.weapons

class ShotgunWeapon : Weapon(
    name = "Shotgun",
    damage = 16f,         // 16 DMG × 6 pellets = 96 potential — 2x close-range bonus
    fireRate = 1f,         // 1 shot/sec — slow but devastating up close
    range = 150f,          // Short range — forces close combat
    projectileSpeed = 450f,
    maxAmmo = 8,           // Small clip
    projectileCount = 6,   // 6 pellet spread
    spreadAngle = 45f,
    type = WeaponType.SHOTGUN
)

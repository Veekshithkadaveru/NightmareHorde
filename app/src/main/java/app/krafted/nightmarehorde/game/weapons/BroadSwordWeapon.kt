package app.krafted.nightmarehorde.game.weapons

/**
 * Terrible Knight's starting melee weapon.
 * A heavy, focused sword slash — tighter arc than the Whip Blade but
 * deals significantly more damage per hit. Slower swing speed makes
 * each strike deliberate and powerful.
 *
 * Compared to Whip Blade:
 *   - 60% more damage (40 vs 25)
 *   - Tighter arc (90° vs 150°) — more focused but less coverage
 *   - Fewer segments (6 vs 12) — less forgiving but hits harder
 *   - Slower fire rate (1.0 vs 1.5) — heavy, impactful swings
 *   - Shorter range (90 vs 120) — forces close combat
 */
class BroadSwordWeapon : Weapon(
    name = "Broad Sword",
    damage = 40f,          // Heavy damage per segment hit
    fireRate = 1.0f,        // 1 swing/sec — deliberate, heavy strikes
    range = 90f,            // Short reach — true melee range
    projectileSpeed = 0f,   // Instant stationary hitbox arc
    maxAmmo = 0,
    infiniteAmmo = true,
    penetrating = true,     // Cleaves through enemies in the arc
    type = WeaponType.SWORD,
    isSword = true
)

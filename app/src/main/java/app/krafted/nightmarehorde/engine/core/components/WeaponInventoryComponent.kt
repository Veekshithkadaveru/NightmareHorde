package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component
import app.krafted.nightmarehorde.game.weapons.Weapon
import app.krafted.nightmarehorde.game.weapons.WeaponType

/**
 * Manages the player's weapon inventory with per-weapon ammo tracking.
 *
 * Thread-safety: all mutating operations are synchronized to allow safe access
 * from both the game loop thread and coroutine-based systems (e.g. spawn loop,
 * HUD observer).
 */
class WeaponInventoryComponent : Component {

    /** LinkedHashMap preserves insertion order for deterministic weapon cycling. */
    private val weapons = linkedMapOf<WeaponType, WeaponSlot>()

    var activeWeaponType: WeaponType = WeaponType.PISTOL

    companion object {
        /** Kill thresholds at which each weapon is unlocked. */
        val UNLOCK_THRESHOLDS: Map<WeaponType, Int> = mapOf(
            WeaponType.PISTOL to 0,
            WeaponType.MELEE to 0,
            WeaponType.SHOTGUN to 50,
            WeaponType.ASSAULT_RIFLE to 100,
            WeaponType.SMG to 200,
            WeaponType.FLAMETHROWER to 350
        )

        /** Max total ammo = weapon.maxAmmo * this multiplier. */
        const val AMMO_CAP_MULTIPLIER = 5
    }

    data class WeaponSlot(
        val weapon: Weapon,
        var currentAmmo: Int
    )

    @Synchronized
    fun addWeapon(weapon: Weapon, initialAmmo: Int = 0) {
        weapons[weapon.type] = WeaponSlot(weapon, if (weapon.infiniteAmmo) 0 else initialAmmo)
    }

    @Synchronized
    fun getActiveWeapon(): Weapon? = weapons[activeWeaponType]?.weapon

    @Synchronized
    fun getActiveSlot(): WeaponSlot? = weapons[activeWeaponType]

    @Synchronized
    fun getSlot(type: WeaponType): WeaponSlot? = weapons[type]

    @Synchronized
    fun getUnlockedTypes(): List<WeaponType> = weapons.keys.toList()

    @Synchronized
    fun hasWeapon(type: WeaponType): Boolean = weapons.containsKey(type)

    /**
     * Add ammo for the given weapon type, capped at [weapon.maxAmmo * AMMO_CAP_MULTIPLIER * capacityMultiplier].
     * @param capacityMultiplier from StatsComponent.ammoCapacityMultiplier (1.0 = default cap)
     * @return true if ammo was added, false if weapon not found, infinite, or invalid amount.
     */
    @Synchronized
    fun addAmmo(type: WeaponType, amount: Int, capacityMultiplier: Float = 1f): Boolean {
        if (amount <= 0) return false
        val slot = weapons[type] ?: return false
        if (slot.weapon.infiniteAmmo) return false
        val cap = (slot.weapon.maxAmmo * AMMO_CAP_MULTIPLIER * capacityMultiplier).toInt()
        slot.currentAmmo = (slot.currentAmmo + amount).coerceAtMost(cap)
        return true
    }

    @Synchronized
    fun consumeAmmo(type: WeaponType): Boolean {
        val slot = weapons[type] ?: return false
        if (slot.weapon.infiniteAmmo) return true
        if (slot.currentAmmo <= 0) return false
        slot.currentAmmo--
        return true
    }

    @Synchronized
    fun switchToNext(): WeaponType {
        val types = weapons.keys.toList()
        if (types.size <= 1) return activeWeaponType
        val currentIndex = types.indexOf(activeWeaponType)
        val nextIndex = (currentIndex + 1) % types.size
        activeWeaponType = types[nextIndex]
        return activeWeaponType
    }

    @Synchronized
    fun switchToPrevious(): WeaponType {
        val types = weapons.keys.toList()
        if (types.size <= 1) return activeWeaponType
        val currentIndex = types.indexOf(activeWeaponType)
        val prevIndex = if (currentIndex <= 0) types.size - 1 else currentIndex - 1
        activeWeaponType = types[prevIndex]
        return activeWeaponType
    }

    @Synchronized
    fun switchTo(type: WeaponType): Boolean {
        if (!weapons.containsKey(type)) return false
        activeWeaponType = type
        return true
    }

    @Synchronized
    fun fallbackToDefault(): WeaponType {
        activeWeaponType = WeaponType.PISTOL
        return activeWeaponType
    }

    /**
     * Replace a weapon in the inventory with an evolved version.
     * Preserves current ammo and slot position.
     */
    @Synchronized
    fun replaceWeapon(type: WeaponType, newWeapon: Weapon) {
        val slot = weapons[type] ?: return
        weapons[type] = WeaponSlot(newWeapon, slot.currentAmmo)
    }
}

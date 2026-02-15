package app.krafted.nightmarehorde.game.systems

import android.util.Log
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.WeaponComponent
import app.krafted.nightmarehorde.engine.core.components.WeaponInventoryComponent
import app.krafted.nightmarehorde.game.weapons.AssaultRifleWeapon
import app.krafted.nightmarehorde.game.weapons.FlamethrowerWeapon
import app.krafted.nightmarehorde.game.weapons.PistolWeapon
import app.krafted.nightmarehorde.game.weapons.SMGWeapon
import app.krafted.nightmarehorde.game.weapons.ShotgunWeapon
import app.krafted.nightmarehorde.game.weapons.SwordWeapon
import app.krafted.nightmarehorde.game.weapons.Weapon
import app.krafted.nightmarehorde.game.weapons.WeaponType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages weapon unlocking, switching, and HUD state for the player's arsenal.
 *
 * Extracted from GameViewModel to keep the ViewModel focused on game lifecycle
 * orchestration rather than weapon bookkeeping.
 */
class WeaponManager {

    /** Currently active weapon type */
    private val _activeWeaponType = MutableStateFlow(WeaponType.PISTOL)
    val activeWeaponType: StateFlow<WeaponType> = _activeWeaponType.asStateFlow()

    /** Current ammo for active weapon (-1 = infinite) */
    private val _currentAmmo = MutableStateFlow(-1)
    val currentAmmo: StateFlow<Int> = _currentAmmo.asStateFlow()

    /** Current weapon display name */
    private val _currentWeaponName = MutableStateFlow("Pistol")
    val currentWeaponName: StateFlow<String> = _currentWeaponName.asStateFlow()

    /** List of unlocked weapon types for HUD weapon bar */
    private val _unlockedWeapons = MutableStateFlow(listOf(WeaponType.PISTOL, WeaponType.MELEE))
    val unlockedWeapons: StateFlow<List<WeaponType>> = _unlockedWeapons.asStateFlow()

    /** Notification when a weapon is unlocked (null = no notification) */
    private val _weaponUnlockNotification = MutableStateFlow<WeaponType?>(null)
    val weaponUnlockNotification: StateFlow<WeaponType?> = _weaponUnlockNotification.asStateFlow()

    fun reset() {
        _activeWeaponType.value = WeaponType.PISTOL
        _currentAmmo.value = -1
        _currentWeaponName.value = "Pistol"
        _unlockedWeapons.value = listOf(WeaponType.PISTOL, WeaponType.MELEE)
        _weaponUnlockNotification.value = null
    }

    // ─── Unlock Logic ─────────────────────────────────────────────────────

    /**
     * Check if any weapons should be unlocked at the given kill count.
     * Adds newly unlocked weapons to the player's inventory.
     */
    fun checkWeaponUnlocks(kills: Int, playerEntity: Entity) {
        val inventory = playerEntity.getComponent(WeaponInventoryComponent::class) ?: return

        WeaponInventoryComponent.UNLOCK_THRESHOLDS.forEach { (weaponType, threshold) ->
            if (kills >= threshold && !inventory.hasWeapon(weaponType)) {
                val weapon = createWeaponForType(weaponType)
                inventory.addWeapon(weapon, initialAmmo = weapon.maxAmmo)

                _weaponUnlockNotification.value = weaponType
                _unlockedWeapons.value = inventory.getUnlockedTypes()

                Log.d("WeaponManager", "Weapon unlocked: ${weapon.name} at $kills kills!")
            }
        }
    }

    // ─── Switching ────────────────────────────────────────────────────────

    fun cycleWeaponForward(playerEntity: Entity) {
        val inventory = playerEntity.getComponent(WeaponInventoryComponent::class) ?: return
        val weaponComp = playerEntity.getComponent(WeaponComponent::class) ?: return

        val newType = inventory.switchToNext()
        weaponComp.equippedWeapon = inventory.getActiveWeapon()
        _activeWeaponType.value = newType
        _currentWeaponName.value = inventory.getActiveWeapon()?.name ?: "Unknown"
        updateAmmoDisplay(inventory)
    }

    fun switchWeapon(targetType: WeaponType, playerEntity: Entity) {
        val inventory = playerEntity.getComponent(WeaponInventoryComponent::class) ?: return
        val weaponComp = playerEntity.getComponent(WeaponComponent::class) ?: return

        if (inventory.switchTo(targetType)) {
            weaponComp.equippedWeapon = inventory.getActiveWeapon()
            _activeWeaponType.value = targetType
            _currentWeaponName.value = inventory.getActiveWeapon()?.name ?: "Unknown"
            updateAmmoDisplay(inventory)
        }
    }

    fun dismissWeaponNotification() {
        _weaponUnlockNotification.value = null
    }

    /**
     * Called when a weapon runs out of ammo — falls back to Pistol.
     */
    fun onAmmoEmpty(emptyWeaponType: WeaponType) {
        Log.d("WeaponManager", "Ammo empty for $emptyWeaponType, switching to Pistol")
        _activeWeaponType.value = WeaponType.PISTOL
        _currentWeaponName.value = "Pistol"
        _currentAmmo.value = -1
    }

    // ─── HUD Refresh ──────────────────────────────────────────────────────

    /**
     * Refresh HUD state from the player's current inventory.
     * Should be called periodically (e.g. every frame from HUD observer).
     */
    fun refreshHudState(playerEntity: Entity) {
        val inventory = playerEntity.getComponent(WeaponInventoryComponent::class) ?: return
        val slot = inventory.getActiveSlot()
        _currentAmmo.value = if (slot?.weapon?.infiniteAmmo == true) -1 else slot?.currentAmmo ?: 0
        _activeWeaponType.value = inventory.activeWeaponType
        _currentWeaponName.value = inventory.getActiveWeapon()?.name ?: "Unknown"
    }

    fun updateAmmoDisplay(inventory: WeaponInventoryComponent) {
        val slot = inventory.getActiveSlot()
        _currentAmmo.value = if (slot?.weapon?.infiniteAmmo == true) -1 else slot?.currentAmmo ?: 0
    }

    // ─── Factory ──────────────────────────────────────────────────────────

    companion object {
        fun createWeaponForType(type: WeaponType): Weapon {
            return when (type) {
                WeaponType.PISTOL -> PistolWeapon()
                WeaponType.MELEE -> SwordWeapon()
                WeaponType.ASSAULT_RIFLE -> AssaultRifleWeapon()
                WeaponType.SHOTGUN -> ShotgunWeapon()
                WeaponType.SMG -> SMGWeapon()
                WeaponType.FLAMETHROWER -> FlamethrowerWeapon()
            }
        }
    }
}

package app.krafted.nightmarehorde.game.systems

import android.util.Log
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.components.DroneComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.game.data.DroneType
import app.krafted.nightmarehorde.game.entities.DroneEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages drone acquisition, max-3 cap, fuel refueling, formation recalculation,
 * and HUD state. Non-system class used by GameViewModel (same pattern as WeaponManager).
 */
class DroneManager(
    private val gameLoop: GameLoop
) {
    companion object {
        const val MAX_DRONES = 3
        const val REFUEL_XP_ORB = 3f
        const val REFUEL_ANY_KILL = 1.5f
        const val REFUEL_DRONE_KILL = 3f
        const val REFUEL_BOSS_KILL = 30f
    }

    private val activeDroneIds = mutableListOf<Long>()

    // ─── HUD State ──────────────────────────────────────────────────────

    data class DroneHudInfo(
        val droneType: DroneType,
        val level: Int,
        val fuelPercent: Float,
        val isPoweredDown: Boolean
    )

    private val _droneHudState = MutableStateFlow<List<DroneHudInfo>>(emptyList())
    val droneHudState: StateFlow<List<DroneHudInfo>> = _droneHudState.asStateFlow()

    private val _droneUnlockNotification = MutableStateFlow<DroneType?>(null)
    val droneUnlockNotification: StateFlow<DroneType?> = _droneUnlockNotification.asStateFlow()

    fun reset() {
        activeDroneIds.clear()
        _droneHudState.value = emptyList()
        _droneUnlockNotification.value = null
    }

    // ─── Drone Acquisition ──────────────────────────────────────────────

    /**
     * Grant a new drone to the player. Returns false if at max capacity
     * or if the player entity has no transform.
     */
    fun grantDrone(
        droneType: DroneType,
        playerEntity: Entity,
        level: Int = 1
    ): Boolean {
        if (activeDroneIds.size >= MAX_DRONES) return false

        val playerTransform = playerEntity.getComponent(TransformComponent::class) ?: return false
        val slotIndex = activeDroneIds.size
        val initialAngle = if (slotIndex > 0) {
            (2f * Math.PI.toFloat() / (slotIndex + 1)) * slotIndex
        } else {
            0f
        }

        val drone = DroneEntity(
            x = playerTransform.x + DroneComponent.ORBIT_RADIUS * kotlin.math.cos(initialAngle),
            y = playerTransform.y + DroneComponent.ORBIT_RADIUS * kotlin.math.sin(initialAngle),
            droneType = droneType,
            ownerEntityId = playerEntity.id,
            slotIndex = slotIndex,
            initialOrbitAngle = initialAngle
        )

        gameLoop.addEntity(drone)
        activeDroneIds.add(drone.id)

        recalculateFormation()

        _droneUnlockNotification.value = droneType
        Log.d("DroneManager", "Granted ${droneType.displayName} (slot $slotIndex)")
        return true
    }

    /**
     * Upgrade an existing drone of the given type. Returns false if not found or already max level.
     */
    fun upgradeDrone(droneType: DroneType): Boolean {
        val snapshot = gameLoop.getEntitiesSnapshot()
        for (entityId in activeDroneIds) {
            val entity = snapshot.find { it.id == entityId && it.isActive } ?: continue
            val drone = entity.getComponent(DroneComponent::class) ?: continue
            if (drone.droneType == droneType && drone.level < 3) {
                drone.level++
                drone.maxFuel = droneType.maxFuel(drone.level)
                drone.fuel = drone.maxFuel // Full refuel on upgrade
                Log.d("DroneManager", "Upgraded ${droneType.displayName} to Lv${drone.level}")
                return true
            }
        }
        return false
    }

    // ─── Fuel Refueling ─────────────────────────────────────────────────

    /**
     * Refuel all active drones by the given amount (seconds of fuel).
     * Reactivates powered-down drones if they receive fuel.
     */
    fun refuelAllDrones(amount: Float) {
        val snapshot = gameLoop.getEntitiesSnapshot()
        for (entityId in activeDroneIds) {
            val entity = snapshot.find { it.id == entityId && it.isActive } ?: continue
            val drone = entity.getComponent(DroneComponent::class) ?: continue
            refuelDrone(drone, amount)
        }
    }

    /**
     * Refuel a specific drone by entity ID (for drone-kill bonus attribution).
     */
    fun refuelDroneById(droneEntityId: Long, amount: Float) {
        val snapshot = gameLoop.getEntitiesSnapshot()
        val entity = snapshot.find { it.id == droneEntityId && it.isActive } ?: return
        val drone = entity.getComponent(DroneComponent::class) ?: return
        refuelDrone(drone, amount)
    }

    private fun refuelDrone(drone: DroneComponent, amount: Float) {
        drone.fuel = (drone.fuel + amount).coerceAtMost(drone.maxFuel)
        if (drone.isPoweredDown && drone.fuel > 0f) {
            drone.isPoweredDown = false
            drone.graceTimer = 0f
            Log.d("DroneManager", "${drone.droneType.displayName} reactivated!")
        }
    }

    // ─── Cleanup & Formation ────────────────────────────────────────────

    /**
     * Remove IDs of drones that have been lost (grace expired, entity deactivated).
     * Recalculates formation if any were removed.
     */
    fun cleanupLostDrones() {
        val snapshot = gameLoop.getEntitiesSnapshot()
        var changed = false
        val iter = activeDroneIds.iterator()
        while (iter.hasNext()) {
            val id = iter.next()
            val entity = snapshot.find { it.id == id }
            if (entity == null || !entity.isActive) {
                iter.remove()
                changed = true
            }
        }
        if (changed) {
            recalculateFormation()
        }
    }

    private fun recalculateFormation() {
        val snapshot = gameLoop.getEntitiesSnapshot()
        for ((index, entityId) in activeDroneIds.withIndex()) {
            val entity = snapshot.find { it.id == entityId && it.isActive } ?: continue
            val drone = entity.getComponent(DroneComponent::class) ?: continue
            drone.slotIndex = index
        }
    }

    // ─── Queries ────────────────────────────────────────────────────────

    fun hasDroneType(droneType: DroneType): Boolean {
        val snapshot = gameLoop.getEntitiesSnapshot()
        return activeDroneIds.any { id ->
            val entity = snapshot.find { it.id == id && it.isActive }
            entity?.getComponent(DroneComponent::class)?.droneType == droneType
        }
    }

    fun getDroneLevel(droneType: DroneType): Int {
        val snapshot = gameLoop.getEntitiesSnapshot()
        for (id in activeDroneIds) {
            val entity = snapshot.find { it.id == id && it.isActive } ?: continue
            val drone = entity.getComponent(DroneComponent::class) ?: continue
            if (drone.droneType == droneType) return drone.level
        }
        return 0
    }

    fun getActiveDroneCount(): Int = activeDroneIds.size

    fun dismissDroneNotification() {
        _droneUnlockNotification.value = null
    }

    // ─── HUD Refresh ────────────────────────────────────────────────────

    /**
     * Refresh drone HUD state. Called periodically by GameViewModel's HUD observer.
     */
    fun refreshHudState() {
        val snapshot = gameLoop.getEntitiesSnapshot()
        val hudList = activeDroneIds.mapNotNull { id ->
            val entity = snapshot.find { it.id == id && it.isActive } ?: return@mapNotNull null
            val drone = entity.getComponent(DroneComponent::class) ?: return@mapNotNull null
            DroneHudInfo(
                droneType = drone.droneType,
                level = drone.level,
                fuelPercent = drone.fuelPercent,
                isPoweredDown = drone.isPoweredDown
            )
        }
        _droneHudState.value = hudList
    }
}

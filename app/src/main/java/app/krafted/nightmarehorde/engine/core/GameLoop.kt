package app.krafted.nightmarehorde.engine.core

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core game loop running on [Dispatchers.Default] at a target of 60 FPS.
 *
 * Entity storage design
 * ---------------------
 * **Problem:** The previous [CopyOnWriteArrayList] copied its entire backing
 * array on every [addEntity] and [removeAll] call.  With thousands of
 * short-lived entities (particles, damage popups) being created and destroyed
 * each session, this produced enormous GC pressure that led to OOM crashes on
 * real devices after ~7 minutes of gameplay.
 *
 * **Solution:** Entities are now stored in a plain [ArrayList] that is only
 * touched from the game-loop coroutine.  Cross-thread additions go through a
 * lock-free [ConcurrentLinkedQueue] and are drained at the start of every
 * frame.  Dead-entity cleanup uses [ArrayList.removeIf] which compacts
 * in-place with zero temporary allocations.  A double-buffered snapshot is
 * published once per frame (via @Volatile) for other threads.
 */
@Singleton
class GameLoop @Inject constructor() {
    private var job: Job? = null
    private val isRunning = AtomicBoolean(false)
    private val _isPaused = AtomicBoolean(false)

    // Systems rarely mutate — CopyOnWriteArrayList is fine for them
    private val systems = CopyOnWriteArrayList<GameSystem>()

    // ─── Entity Storage ───────────────────────────────────────────────────
    //
    // Main list:  Only touched inside update(), which runs on a single
    //             coroutine on Dispatchers.Default.  Using ArrayList avoids
    //             the per-write full-array copy of CopyOnWriteArrayList.
    //
    // Pending queue:  addEntity() can be called from any thread (Main for
    //                 spawning, Default for CombatSystem particles).  Entities
    //                 are drained into `entities` at the very start of each
    //                 frame.
    //
    // Snapshot:  A fresh copy is published once per frame via @Volatile,
    //            guaranteeing readers never see a half-modified list.
    // ──────────────────────────────────────────────────────────────────────

    private val entities = ArrayList<Entity>(256)
    private val pendingAdditions = ConcurrentLinkedQueue<Entity>()


    @Volatile
    private var snapshot: List<Entity> = emptyList()

    // Flag to request clear from any thread — processed at start of next frame
    private val pendingClear = AtomicBoolean(false)

    // Target 60 FPS
    private val targetFrameTime = 1_000_000_000L / 60L // in nanoseconds

    fun start(scope: CoroutineScope) {
        if (isRunning.getAndSet(true)) return
        Log.d("GameLoop", "Game Loop Started")

        job = scope.launch(Dispatchers.Default) {
            var lastTime = System.nanoTime()

            while (isActive && isRunning.get()) {
                val currentTime = System.nanoTime()
                val elapsed = currentTime - lastTime
                lastTime = currentTime

                // Delta time in seconds, clamped to max 50ms to prevent spirals
                val deltaTime = (elapsed / 1_000_000_000f).coerceAtMost(0.05f)

                update(deltaTime)

                // Enforce frame rate cap (busy-wait for precision or sleep)
                val processTime = System.nanoTime() - currentTime
                val sleepTime = targetFrameTime - processTime

                if (sleepTime > 0) {
                    val sleepMs = sleepTime / 1_000_000
                    val sleepNs = (sleepTime % 1_000_000).toInt()
                    // Basic sleep is usually fine for >1ms gaps
                    if (sleepMs > 0) {
                        try {
                            Thread.sleep(sleepMs, sleepNs)
                        } catch (_: InterruptedException) {
                            // Ignore
                        }
                    }
                }
            }
        }
    }

    fun stop() {
        if (isRunning.getAndSet(false)) {
            _isPaused.set(false)
            job?.cancel()
            Log.d("GameLoop", "Game Loop Stopped")
        }
    }

    /**
     * Pauses the game loop — systems stop updating but entities and state
     * are preserved. The coroutine stays alive so rendering continues.
     */
    fun pause() {
        _isPaused.set(true)
        Log.d("GameLoop", "Game Loop Paused")
    }

    /**
     * Resumes a paused game loop.
     */
    fun resume() {
        _isPaused.set(false)
        Log.d("GameLoop", "Game Loop Resumed")
    }

    val isPaused: Boolean get() = _isPaused.get()

    fun addSystem(system: GameSystem) {
        systems.add(system)
        // Sort by priority - lower values run first
        // Note: CopyOnWriteArrayList copies on write, so sort is safe but creates a new array
        systems.sortBy { it.priority }
    }

    /**
     * Thread-safe: may be called from any thread.
     * The entity will appear in the main list on the next frame.
     */
    fun addEntity(entity: Entity) {
        pendingAdditions.add(entity)
    }

    /**
     * Thread-safe: marks the entity inactive so it is swept at the
     * end of the current (or next) frame.
     */
    fun removeEntity(entity: Entity) {
        entity.isActive = false
    }

    /**
     * Returns a read-only snapshot of the entity list that is safe to read
     * from any thread.  Updated once per frame at the end of [update].
     */
    fun getEntitiesSnapshot(): List<Entity> = snapshot

    /**
     * Requests clearing all entities and systems.
     * The actual clear happens at the start of the next frame to avoid
     * blocking the main thread on the game loop's synchronized block.
     */
    fun clear() {
        pendingClear.set(true)
        // Also drain any pending additions so they don't leak into the next session
        pendingAdditions.clear()
    }

    // ─── Frame Update ─────────────────────────────────────────────────────

    private fun update(deltaTime: Float) {
        // Handle pending clear request (lock-free, no contention with main thread)
        if (pendingClear.getAndSet(false)) {
            entities.clear()
            pendingAdditions.clear()
            snapshot = emptyList()
            systems.clear()
            return
        }

        // 1. Drain pending additions (lock-free queue → ArrayList).
        //    Entities added by systems during this frame will appear next frame.
        while (true) {
            val e = pendingAdditions.poll() ?: break
            entities.add(e)
        }

        // 1b. If paused, still publish snapshot (for rendering) but skip systems.
        if (_isPaused.get()) {
            publishSnapshot()
            return
        }

        // 2. Run all systems.  `entities` is not structurally modified during
        //    system execution because addEntity() goes to the pending queue
        //    and removeEntity() only flips a boolean.
        systems.forEach { system ->
            system.update(deltaTime, entities)
        }

        // 3. Remove dead entities IN-PLACE.
        //    ArrayList.removeIf uses a single O(n) pass with an internal BitSet
        //    — zero temporary lists, zero per-element array copies.
        entities.removeIf { !it.isActive }

        // 4. Publish a read-only snapshot for cross-thread readers.
        publishSnapshot()
    }

    /**
     * Publishes a fresh, immutable snapshot each frame.
     *
     * Previous double-buffered approach reused two ArrayLists, but readers
     * on other threads (DroneManager, UpgradePool) could still be iterating
     * a buffer when the game loop recycled it 2 frames later, causing
     * ConcurrentModificationException.  A new ArrayList per frame is cheap
     * (~one small allocation at 60 FPS) and guarantees thread safety.
     */
    private fun publishSnapshot() {
        snapshot = ArrayList(entities)
    }
}

package app.krafted.nightmarehorde.engine.core

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameLoop @Inject constructor() {
    private var job: Job? = null
    private val isRunning = AtomicBoolean(false)
    
    // Thread-safe lists for modification during iteration
    private val systems = CopyOnWriteArrayList<GameSystem>()
    private val entities = CopyOnWriteArrayList<Entity>()
    
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
                        } catch (e: InterruptedException) {
                            // Ignore
                        }
                    }
                }
            }
        }
    }

    fun stop() {
        if (isRunning.getAndSet(false)) {
            job?.cancel()
            Log.d("GameLoop", "Game Loop Stopped")
        }
    }

    fun addSystem(system: GameSystem) {
        systems.add(system)
        // Sort by priority - lower values run first
        // Note: CopyOnWriteArrayList copies on write, so sort is safe but creates a new array
        systems.sortBy { it.priority }
    }

    fun addEntity(entity: Entity) {
        entities.add(entity)
    }
    
    fun removeEntity(entity: Entity) {
        entities.remove(entity)
    }
    
    fun getEntitiesSnapshot(): List<Entity> {
        return entities.toList()
    }
    
    fun clear() {
        entities.clear()
        // Systems typically stay for the session
    }

    private fun update(deltaTime: Float) {
        // CopyOnWriteArrayList allows safe iteration without extra copying
        // Note inside this loop, modifying entities is safe but won't be seen until next frame via COWAL
        systems.forEach { system ->
            system.update(deltaTime, entities)
        }
        
        // Cleanup dead entities
        entities.removeAll { !it.isActive }
    }
}

package app.krafted.nightmarehorde.engine.core

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Generic object pool to reduce GC pressure.
 */
class ObjectPool<T>(
    private val factory: () -> T,
    private val reset: (T) -> Unit
) {
    private val pool = ConcurrentLinkedQueue<T>()

    fun acquire(): T {
        return pool.poll() ?: factory()
    }

    fun release(item: T) {
        reset(item)
        pool.offer(item)
    }
}

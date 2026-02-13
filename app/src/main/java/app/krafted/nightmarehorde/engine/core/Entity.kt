package app.krafted.nightmarehorde.engine.core

import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass

/**
 * Represents a game object with a unique ID and a collection of components.
 * Optimized for performance with Long ID and HashMap.
 */
open class Entity(val id: Long = nextId()) {
    @Volatile var isActive: Boolean = true
    // Not thread-safe, assuming access from single-threaded GameLoop or synchronized external scope
    private val components = HashMap<KClass<out Component>, Component>()

    fun <T : Component> addComponent(component: T) {
        components[component::class] = component
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> getComponent(type: KClass<T>): T? {
        return components[type] as? T
    }

    fun <T : Component> removeComponent(type: KClass<T>) {
        components.remove(type)
    }

    fun hasComponent(type: KClass<out Component>): Boolean {
        return components.containsKey(type)
    }

    inline fun <reified T : Component> get(): T? = getComponent(T::class)
    inline fun <reified T : Component> add(component: T) = addComponent(component)
    inline fun <reified T : Component> remove() = removeComponent(T::class)
    inline fun <reified T : Component> has(): Boolean = hasComponent(T::class)

    companion object {
        private val counter = AtomicLong(0)
        fun nextId() = counter.incrementAndGet()
        fun resetIdCounter() = counter.set(0)
    }
}

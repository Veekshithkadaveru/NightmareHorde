package app.krafted.nightmarehorde.engine.physics

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.TransformComponent

/**
 * Spatial hash grid for efficient broad-phase collision detection.
 * Divides the world into cells and only checks entities in the same or neighboring cells.
 * Reduces collision checks from O(n²) to approximately O(n).
 * 
 * Performance: Uses pre-allocated result lists to reduce per-frame GC pressure.
 */
class SpatialHashGrid(
    private val cellSize: Float = 100f
) {
    private val cells = mutableMapOf<Long, MutableList<Entity>>()
    
    // Track all inserted entities for iteration
    private val allEntities = mutableListOf<Entity>()
    
    // Pre-allocated result lists to reduce GC pressure in hot paths
    private val queryResultBuffer = mutableListOf<Entity>()
    private val nearbyResultBuffer = mutableListOf<Entity>()
    
    /**
     * Insert an entity into the grid at the given position.
     */
    fun insert(entity: Entity, x: Float, y: Float) {
        val key = hashKey(x, y)
        val cell = cells.getOrPut(key) { mutableListOf() }
        cell.add(entity)
        allEntities.add(entity)
    }
    
    /**
     * Insert an entity using its TransformComponent position.
     */
    fun insert(entity: Entity) {
        val transform = entity.getComponent(TransformComponent::class) ?: return
        insert(entity, transform.x, transform.y)
    }
    
    /**
     * Query all entities within a circular area.
     * Returns entities from the center cell and all neighboring cells within range.
     * 
     * Note: Uses internal buffer and returns a copy to avoid concurrent modification.
     * For hot loops, consider using queryInto() with your own list.
     */
    fun query(centerX: Float, centerY: Float, radius: Float): List<Entity> {
        queryResultBuffer.clear()
        queryInto(centerX, centerY, radius, queryResultBuffer)
        return queryResultBuffer.toList()
    }
    
    /**
     * Query entities into a provided list (zero-allocation in hot path).
     * The list is NOT cleared - caller should clear if needed.
     */
    fun queryInto(centerX: Float, centerY: Float, radius: Float, result: MutableList<Entity>) {
        // Calculate the range of cells to check
        val minCellX = ((centerX - radius) / cellSize).toInt()
        val maxCellX = ((centerX + radius) / cellSize).toInt()
        val minCellY = ((centerY - radius) / cellSize).toInt()
        val maxCellY = ((centerY + radius) / cellSize).toInt()
        
        for (cellX in minCellX..maxCellX) {
            for (cellY in minCellY..maxCellY) {
                val key = hashKeyFromCell(cellX, cellY)
                cells[key]?.let { result.addAll(it) }
            }
        }
    }
    
    /**
     * Query all entities within an AABB rectangle.
     */
    fun queryRect(left: Float, top: Float, right: Float, bottom: Float): List<Entity> {
        queryResultBuffer.clear()
        queryRectInto(left, top, right, bottom, queryResultBuffer)
        return queryResultBuffer.toList()
    }
    
    /**
     * Query entities in rectangle into a provided list (zero-allocation in hot path).
     */
    fun queryRectInto(left: Float, top: Float, right: Float, bottom: Float, result: MutableList<Entity>) {
        val minCellX = (left / cellSize).toInt()
        val maxCellX = (right / cellSize).toInt()
        val minCellY = (top / cellSize).toInt()
        val maxCellY = (bottom / cellSize).toInt()
        
        for (cellX in minCellX..maxCellX) {
            for (cellY in minCellY..maxCellY) {
                val key = hashKeyFromCell(cellX, cellY)
                cells[key]?.let { result.addAll(it) }
            }
        }
    }
    
    /**
     * Get entities in the same cell as the given position (and neighboring cells).
     */
    fun getNearbyEntities(x: Float, y: Float): List<Entity> {
        nearbyResultBuffer.clear()
        val cellX = (x / cellSize).toInt()
        val cellY = (y / cellSize).toInt()
        
        // Check 3x3 grid of cells around the position
        for (dx in -1..1) {
            for (dy in -1..1) {
                val key = hashKeyFromCell(cellX + dx, cellY + dy)
                cells[key]?.let { nearbyResultBuffer.addAll(it) }
            }
        }
        
        return nearbyResultBuffer.toList()
    }
    
    /**
     * Clear all entities from the grid.
     * Should be called at the start of each frame before re-inserting entities.
     * 
     * Note: Clears cells but retains cell map to reuse allocated lists.
     */
    fun clear() {
        // Clear cell contents but keep allocated lists for reuse
        cells.values.forEach { it.clear() }
        allEntities.clear()
        queryResultBuffer.clear()
        nearbyResultBuffer.clear()
    }
    
    /**
     * Full reset - clears cells AND deallocates cell lists.
     * Use when changing levels or when memory pressure is high.
     */
    fun reset() {
        cells.clear()
        allEntities.clear()
        queryResultBuffer.clear()
        nearbyResultBuffer.clear()
    }
    
    /**
     * Get all entities currently in the grid.
     */
    fun getAllEntities(): List<Entity> = allEntities
    
    /**
     * Get the number of non-empty cells (for debugging/stats).
     */
    fun getCellCount(): Int = cells.count { it.value.isNotEmpty() }
    
    /**
     * Get total entity count.
     */
    fun getEntityCount(): Int = allEntities.size
    
    /**
     * Hash a world position to a cell key.
     * Uses a long to pack both X and Y cell coordinates.
     */
    private fun hashKey(x: Float, y: Float): Long {
        val cellX = (x / cellSize).toInt()
        val cellY = (y / cellSize).toInt()
        return hashKeyFromCell(cellX, cellY)
    }
    
    /**
     * Create a hash key from cell coordinates.
     */
    private fun hashKeyFromCell(cellX: Int, cellY: Int): Long {
        return (cellX.toLong() shl 32) or (cellY.toLong() and 0xFFFFFFFFL)
    }
}

package app.krafted.nightmarehorde.engine.rendering

/**
 * Controls frame-based sprite animations.
 * Supports looping, one-shot, and ping-pong animation modes.
 */
class AnimationController {
    
    /** Current frame index */
    var currentFrame: Int = 0
        private set
    
    /** Whether the animation is currently playing */
    var isPlaying: Boolean = false
        private set
    
    /** Whether the animation has finished (for one-shot mode) */
    var isFinished: Boolean = false
        private set
    
    private var frameTime: Float = 0f
    private var elapsedTime: Float = 0f
    private var frameStart: Int = 0
    private var frameEnd: Int = 0
    private var mode: AnimationMode = AnimationMode.LOOP
    private var pingPongDirection: Int = 1
    
    /**
     * Configure and start an animation.
     * @param startFrame First frame index (inclusive)
     * @param endFrame Last frame index (inclusive)
     * @param fps Frames per second
     * @param mode Animation playback mode
     */
    fun play(
        startFrame: Int,
        endFrame: Int,
        fps: Float,
        mode: AnimationMode = AnimationMode.LOOP
    ) {
        this.frameStart = startFrame
        this.frameEnd = endFrame
        this.frameTime = 1f / fps
        this.mode = mode
        this.currentFrame = startFrame
        this.elapsedTime = 0f
        this.isPlaying = true
        this.isFinished = false
        this.pingPongDirection = 1
    }
    
    /**
     * Stop the animation and reset to first frame.
     */
    fun stop() {
        isPlaying = false
        currentFrame = frameStart
        elapsedTime = 0f
    }
    
    /**
     * Pause the animation at current frame.
     */
    fun pause() {
        isPlaying = false
    }
    
    /**
     * Resume a paused animation.
     */
    fun resume() {
        if (!isFinished) {
            isPlaying = true
        }
    }
    
    /**
     * Update the animation state.
     * Should be called every frame with delta time.
     */
    fun update(deltaTime: Float) {
        if (!isPlaying || isFinished) return
        
        elapsedTime += deltaTime
        
        while (elapsedTime >= frameTime) {
            elapsedTime -= frameTime
            advanceFrame()
        }
    }
    
    private fun advanceFrame() {
        when (mode) {
            AnimationMode.LOOP -> {
                currentFrame++
                if (currentFrame > frameEnd) {
                    currentFrame = frameStart
                }
            }
            AnimationMode.ONCE -> {
                currentFrame++
                if (currentFrame > frameEnd) {
                    currentFrame = frameEnd
                    isPlaying = false
                    isFinished = true
                }
            }
            AnimationMode.PING_PONG -> {
                currentFrame += pingPongDirection
                if (currentFrame >= frameEnd) {
                    currentFrame = frameEnd
                    pingPongDirection = -1
                } else if (currentFrame <= frameStart) {
                    currentFrame = frameStart
                    pingPongDirection = 1
                }
            }
        }
    }
    
    enum class AnimationMode {
        LOOP,       // Repeat from start when finished
        ONCE,       // Play once and stop at last frame
        PING_PONG   // Alternate between start and end
    }
}

package com.haisnap.spatialguitar.ui.home

internal data class EasyStrumDecision<T : Any>(
    val target: T,
    val gain: Float,
    val inputUptimeMillis: Long,
    val speedMetersPerSecond: Float,
    val direction: GuitarStrikeDirection,
)

/**
 * Forgiving strum-paddle state machine for sing-and-play mode.
 *
 * A new press or a deliberate re-entry always sounds. While the pointer stays
 * on the broad pad, a slow stroke, direction reversal, or sustained sweep can
 * retrigger without requiring the user to find individual virtual strings.
 */
internal class EasyStrumDetector<T : Any> {
    private data class PointerState<T : Any>(
        val motionTracker: GuitarStrikeMotionTracker =
            GuitarStrikeMotionTracker(
                enterSpeedMetersPerSecond = EASY_ENTER_SPEED_MPS,
                exitSpeedMetersPerSecond = EASY_EXIT_SPEED_MPS,
            ),
        var wasOnTarget: Boolean = false,
        var lastTriggerAtMillis: Long = Long.MIN_VALUE / 2,
        var lastDirection: GuitarStrikeDirection = GuitarStrikeDirection.NONE,
    )

    private val pointers = mutableMapOf<String, PointerState<T>>()

    fun update(
        pointerKey: String,
        pressed: Boolean,
        isUpEvent: Boolean,
        target: T?,
        motion: GuitarMotionSample?,
        isPoke: Boolean,
        uptimeMillis: Long,
    ): EasyStrumDecision<T>? {
        if (isUpEvent || !pressed) {
            pointers.remove(pointerKey)
            return null
        }

        val existingState = pointers[pointerKey]
        val isNewPress = existingState == null
        val state = existingState ?: PointerState<T>().also { pointers[pointerKey] = it }
        val strikeMotion =
            motion?.let { state.motionTracker.update(it, isPoke) }
                ?: state.motionTracker.reset(isPoke)

        if (target == null) {
            state.wasOnTarget = false
            return null
        }

        val enteredTarget = !state.wasOnTarget
        state.wasOnTarget = true
        val elapsedSinceTrigger = uptimeMillis - state.lastTriggerAtMillis
        val directionChanged =
            strikeMotion.active &&
                strikeMotion.direction != GuitarStrikeDirection.NONE &&
                strikeMotion.direction != state.lastDirection
        val motionRetrigger =
            strikeMotion.active &&
                elapsedSinceTrigger >= MOTION_RETRIGGER_INTERVAL_MS &&
                (strikeMotion.becameActive ||
                    directionChanged ||
                    elapsedSinceTrigger >= SUSTAINED_SWEEP_INTERVAL_MS)
        val shouldTrigger =
            isNewPress ||
                (enteredTarget && elapsedSinceTrigger >= REENTRY_INTERVAL_MS) ||
                motionRetrigger
        if (!shouldTrigger) return null

        val direction =
            if (strikeMotion.active) strikeMotion.direction else GuitarStrikeDirection.TAP
        state.lastTriggerAtMillis = uptimeMillis
        state.lastDirection = direction
        return EasyStrumDecision(
            target = target,
            gain =
                if (strikeMotion.active) {
                    strikeMotion.gain.coerceAtLeast(EASY_MIN_GAIN)
                } else {
                    GuitarGestureVelocity.initial(isPoke).coerceAtLeast(EASY_MIN_GAIN)
                },
            inputUptimeMillis = uptimeMillis,
            speedMetersPerSecond = strikeMotion.speedMetersPerSecond,
            direction = direction,
        )
    }

    private companion object {
        const val EASY_ENTER_SPEED_MPS = 0.055f
        const val EASY_EXIT_SPEED_MPS = 0.022f
        const val EASY_MIN_GAIN = 0.70f
        const val REENTRY_INTERVAL_MS = 90L
        const val MOTION_RETRIGGER_INTERVAL_MS = 110L
        const val SUSTAINED_SWEEP_INTERVAL_MS = 320L
    }
}

package com.haisnap.spatialguitar.ui.home

internal data class GuitarStrikeDecision<T : Any>(
    val target: T,
    val gain: Float,
    val inputUptimeMillis: Long,
    val speedMetersPerSecond: Float,
    val direction: GuitarStrikeDirection,
)

/** Pure pointer state machine kept separate from SpatialPointerInfo for tests. */
internal class GuitarPointerStrikeDetector<T : Any> {
    private data class PointerState<T : Any>(
        val motionTracker: GuitarStrikeMotionTracker = GuitarStrikeMotionTracker(),
        var lastStruckTarget: T? = null,
        var missingTargetSinceMillis: Long? = null,
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
    ): GuitarStrikeDecision<T>? {
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

        if (strikeMotion.becameInactive) {
            state.lastStruckTarget = null
        }

        if (target == null) {
            if (state.missingTargetSinceMillis == null) {
                state.missingTargetSinceMillis = uptimeMillis
            } else if (
                uptimeMillis - requireNotNull(state.missingTargetSinceMillis) >= TARGET_EXIT_GRACE_MS
            ) {
                state.lastStruckTarget = null
            }
            return null
        }

        state.missingTargetSinceMillis?.let { missingSince ->
            if (uptimeMillis - missingSince >= TARGET_EXIT_GRACE_MS) {
                state.lastStruckTarget = null
            }
        }
        state.missingTargetSinceMillis = null

        val targetChanged = state.lastStruckTarget !== target
        if (!targetChanged || (!isNewPress && !strikeMotion.active)) return null

        state.lastStruckTarget = target
        return GuitarStrikeDecision(
            target = target,
            gain =
                if (strikeMotion.active) {
                    strikeMotion.gain
                } else {
                    GuitarGestureVelocity.initial(isPoke)
                },
            inputUptimeMillis = uptimeMillis,
            speedMetersPerSecond = strikeMotion.speedMetersPerSecond,
            direction =
                if (strikeMotion.active) strikeMotion.direction else GuitarStrikeDirection.TAP,
        )
    }

    private companion object {
        const val TARGET_EXIT_GRACE_MS = 32L
    }
}

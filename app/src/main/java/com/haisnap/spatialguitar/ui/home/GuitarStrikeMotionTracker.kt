package com.haisnap.spatialguitar.ui.home

import java.util.ArrayDeque

internal data class GuitarStrikeMotion(
    val speedMetersPerSecond: Float,
    val gain: Float,
    val direction: GuitarStrikeDirection,
    val active: Boolean,
    val becameActive: Boolean,
    val becameInactive: Boolean,
)

/**
 * Converts meter-based device poses into a stable string-crossing motion.
 *
 * The short time-weighted window rejects pose jitter and direction reversals.
 * A Schmitt trigger then uses separate enter/exit thresholds so motion near a
 * single threshold cannot repeatedly arm and disarm the same string.
 */
internal class GuitarStrikeMotionTracker(
    smoothingWindowMillis: Long = DEFAULT_SMOOTHING_WINDOW_MS,
    enterSpeedMetersPerSecond: Float = DEFAULT_ENTER_SPEED_MPS,
    exitSpeedMetersPerSecond: Float = DEFAULT_EXIT_SPEED_MPS,
) {
    private val smoother = DirectionalVelocitySmoother(smoothingWindowMillis)
    private val gate = GuitarStrikeHysteresis(enterSpeedMetersPerSecond, exitSpeedMetersPerSecond)
    private var previous: GuitarMotionSample? = null

    fun update(current: GuitarMotionSample, isPoke: Boolean): GuitarStrikeMotion {
        val prior = previous
        previous = current
        if (prior == null) return idle(isPoke)

        val projected = GuitarGestureVelocity.projectedVelocityOrNull(prior, current)
        if (projected == null || projected.elapsedMillis > MAX_SAMPLE_GAP_MS) {
            return reset(isPoke, keepCurrent = current)
        }

        val smoothed = smoother.add(projected, current.uptimeMillis)
        val transition = gate.update(smoothed.speedMetersPerSecond)
        return GuitarStrikeMotion(
            speedMetersPerSecond = smoothed.speedMetersPerSecond,
            gain = GuitarGestureVelocity.fromSpeed(smoothed.speedMetersPerSecond, isPoke),
            direction = smoothed.direction,
            active = transition.active,
            becameActive = transition.becameActive,
            becameInactive = transition.becameInactive,
        )
    }

    fun reset(isPoke: Boolean): GuitarStrikeMotion = reset(isPoke, keepCurrent = null)

    private fun reset(isPoke: Boolean, keepCurrent: GuitarMotionSample?): GuitarStrikeMotion {
        previous = keepCurrent
        smoother.clear()
        val transition = gate.reset()
        return GuitarStrikeMotion(
            speedMetersPerSecond = 0f,
            gain = GuitarGestureVelocity.initial(isPoke),
            direction = GuitarStrikeDirection.NONE,
            active = false,
            becameActive = false,
            becameInactive = transition.becameInactive,
        )
    }

    private fun idle(isPoke: Boolean): GuitarStrikeMotion =
        GuitarStrikeMotion(
            speedMetersPerSecond = 0f,
            gain = GuitarGestureVelocity.initial(isPoke),
            direction = GuitarStrikeDirection.NONE,
            active = false,
            becameActive = false,
            becameInactive = false,
        )

    private companion object {
        const val DEFAULT_SMOOTHING_WINDOW_MS = 48L
        const val MAX_SAMPLE_GAP_MS = 120L
        const val DEFAULT_ENTER_SPEED_MPS = 0.10f
        const val DEFAULT_EXIT_SPEED_MPS = 0.045f
    }
}

internal data class GuitarStrikeGateTransition(
    val active: Boolean,
    val becameActive: Boolean,
    val becameInactive: Boolean,
)

internal class GuitarStrikeHysteresis(
    private val enterSpeedMetersPerSecond: Float,
    private val exitSpeedMetersPerSecond: Float,
) {
    private var active = false

    init {
        require(enterSpeedMetersPerSecond > exitSpeedMetersPerSecond)
        require(exitSpeedMetersPerSecond >= 0f)
    }

    fun update(speedMetersPerSecond: Float): GuitarStrikeGateTransition {
        val wasActive = active
        active =
            if (wasActive) {
                speedMetersPerSecond > exitSpeedMetersPerSecond
            } else {
                speedMetersPerSecond >= enterSpeedMetersPerSecond
            }
        return GuitarStrikeGateTransition(
            active = active,
            becameActive = !wasActive && active,
            becameInactive = wasActive && !active,
        )
    }

    fun reset(): GuitarStrikeGateTransition {
        val wasActive = active
        active = false
        return GuitarStrikeGateTransition(
            active = false,
            becameActive = false,
            becameInactive = wasActive,
        )
    }
}

private class DirectionalVelocitySmoother(private val windowMillis: Long) {
    private data class TimedVelocity(
        val velocity: GuitarProjectedVelocity,
        val uptimeMillis: Long,
    )

    private val samples = ArrayDeque<TimedVelocity>()

    fun add(velocity: GuitarProjectedVelocity, uptimeMillis: Long): GuitarProjectedVelocity {
        samples.addLast(TimedVelocity(velocity, uptimeMillis))
        val oldestAllowed = uptimeMillis - windowMillis
        while (samples.size > 1 && samples.first.uptimeMillis < oldestAllowed) {
            samples.removeFirst()
        }

        var weightedY = 0f
        var weightedZ = 0f
        var totalMillis = 0L
        samples.forEach { sample ->
            val duration = sample.velocity.elapsedMillis
            weightedY += sample.velocity.yMetersPerSecond * duration
            weightedZ += sample.velocity.zMetersPerSecond * duration
            totalMillis += duration
        }
        if (totalMillis <= 0L) return velocity
        return GuitarProjectedVelocity(
            yMetersPerSecond = weightedY / totalMillis,
            zMetersPerSecond = weightedZ / totalMillis,
            elapsedMillis = totalMillis,
        )
    }

    fun clear() {
        samples.clear()
    }
}

package com.haisnap.spatialguitar.ui.home

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

internal data class GuitarMotionSample(
    val x: Float,
    val y: Float,
    val z: Float,
    val uptimeMillis: Long,
)

internal data class GuitarProjectedVelocity(
    val yMetersPerSecond: Float,
    val zMetersPerSecond: Float,
    val elapsedMillis: Long,
) {
    val speedMetersPerSecond: Float
        get() =
            sqrt(
                yMetersPerSecond * yMetersPerSecond +
                    zMetersPerSecond * zMetersPerSecond
            )

    val direction: GuitarStrikeDirection
        get() =
            when {
                speedMetersPerSecond <= 0f -> GuitarStrikeDirection.NONE
                abs(yMetersPerSecond) >= abs(zMetersPerSecond) && yMetersPerSecond < 0f ->
                    GuitarStrikeDirection.DOWNSTROKE
                abs(yMetersPerSecond) >= abs(zMetersPerSecond) -> GuitarStrikeDirection.UPSTROKE
                zMetersPerSecond < 0f -> GuitarStrikeDirection.POKE_IN
                else -> GuitarStrikeDirection.POKE_OUT
            }
}

internal enum class GuitarStrikeDirection {
    NONE,
    TAP,
    DOWNSTROKE,
    UPSTROKE,
    POKE_IN,
    POKE_OUT,
}

internal object GuitarGestureVelocity {
    // The legacy Web version randomized gain in 0.5..1.0. Spatial input keeps
    // that proven range, but maps it deterministically to controller/Poke speed.
    fun initial(isPoke: Boolean): Float = if (isPoke) 0.72f else 0.60f

    fun fromMotion(previous: GuitarMotionSample?, current: GuitarMotionSample, isPoke: Boolean): Float {
        if (previous == null) return initial(isPoke)
        val projectedVelocity = projectedVelocityOrNull(previous, current) ?: return initial(isPoke)
        return fromSpeed(projectedVelocity.speedMetersPerSecond, isPoke)
    }

    /**
     * Strings run along local/world X in the current unrotated guitar layout.
     * Only Y/Z motion crosses the string; X motion slides along it and must not
     * create artificial strike strength.
     */
    fun projectedVelocityOrNull(
        previous: GuitarMotionSample,
        current: GuitarMotionSample,
    ): GuitarProjectedVelocity? {
        val elapsedMillis = current.uptimeMillis - previous.uptimeMillis
        if (elapsedMillis <= 0L) return null
        val secondsScale = 1_000f / elapsedMillis
        return GuitarProjectedVelocity(
            yMetersPerSecond = (current.y - previous.y) * secondsScale,
            zMetersPerSecond = (current.z - previous.z) * secondsScale,
            elapsedMillis = elapsedMillis,
        )
    }

    fun worldSampleOrNull(
        xMeters: Float,
        yMeters: Float,
        zMeters: Float,
        uptimeMillis: Long,
    ): GuitarMotionSample? {
        if (!xMeters.isFinite() || !yMeters.isFinite() || !zMeters.isFinite()) return null
        val magnitudeSquared = xMeters * xMeters + yMeters * yMeters + zMeters * zMeters
        if (magnitudeSquared <= MISSING_POSE_EPSILON_SQUARED) return null
        return GuitarMotionSample(xMeters, yMeters, zMeters, uptimeMillis)
    }

    internal fun fromSpeed(speedMetersPerSecond: Float, isPoke: Boolean): Float {
        val normalizedSpeed = (speedMetersPerSecond / FULL_VELOCITY_SPEED_MPS).coerceIn(0f, 1f)
        val shapedSpeed = normalizedSpeed.pow(VELOCITY_EXPONENT)
        val pokeBoost = if (isPoke) POKE_GAIN_BOOST else 0f
        return (MIN_GAIN + GAIN_RANGE * shapedSpeed + pokeBoost).coerceIn(MIN_GAIN, 1f)
    }

    private const val MIN_GAIN = 0.50f
    private const val GAIN_RANGE = 0.50f
    private const val FULL_VELOCITY_SPEED_MPS = 1.20f
    private const val VELOCITY_EXPONENT = 0.72f
    private const val POKE_GAIN_BOOST = 0.04f
    private const val MISSING_POSE_EPSILON_SQUARED = 0.00000001f
}

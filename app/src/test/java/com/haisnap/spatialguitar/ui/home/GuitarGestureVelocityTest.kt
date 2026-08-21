package com.haisnap.spatialguitar.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuitarGestureVelocityTest {
    @Test
    fun fasterMotionProducesStrongerStrike() {
        val start = GuitarMotionSample(0f, 0f, 0f, 1_000L)
        val slow = GuitarGestureVelocity.fromMotion(start, GuitarMotionSample(0f, 0.01f, 0f, 1_100L), false)
        val fast = GuitarGestureVelocity.fromMotion(start, GuitarMotionSample(0f, 0.08f, 0f, 1_100L), false)

        assertTrue(fast > slow)
    }

    @Test
    fun pokeStartsStrongerAndVelocityIsClamped() {
        assertTrue(GuitarGestureVelocity.initial(true) > GuitarGestureVelocity.initial(false))
        val start = GuitarMotionSample(0f, 0f, 0f, 1_000L)
        val result = GuitarGestureVelocity.fromMotion(start, GuitarMotionSample(0f, 0f, -10f, 1_001L), true)

        assertEquals(1f, result)
    }

    @Test
    fun motionAlongTheStringDoesNotCreateStrikeStrength() {
        val start = GuitarMotionSample(0f, 0.2f, -0.4f, 1_000L)
        val alongString = GuitarMotionSample(0.2f, 0.2f, -0.4f, 1_020L)

        assertEquals(0.5f, GuitarGestureVelocity.fromMotion(start, alongString, false))
    }

    @Test
    fun curvePreservesLegacyHalfToFullGainRange() {
        assertEquals(0.5f, GuitarGestureVelocity.fromSpeed(0f, false))
        assertEquals(1f, GuitarGestureVelocity.fromSpeed(1.2f, false))
        assertTrue(
            GuitarGestureVelocity.fromSpeed(0.6f, false) >
                GuitarGestureVelocity.fromSpeed(0.2f, false)
        )
    }

    @Test
    fun missingOrInvalidWorldPoseDoesNotPretendPixelsAreMeters() {
        assertEquals(null, GuitarGestureVelocity.worldSampleOrNull(0f, 0f, 0f, 1_000L))
        assertEquals(null, GuitarGestureVelocity.worldSampleOrNull(Float.NaN, 0f, 0f, 1_000L))
        assertTrue(GuitarGestureVelocity.worldSampleOrNull(0.1f, 1.2f, -0.4f, 1_000L) != null)
    }
}

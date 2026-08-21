package com.haisnap.spatialguitar.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuitarStrikeMotionTrackerTest {
    @Test
    fun projectsMotionOntoStringNormalPlaneAndReportsDirection() {
        val tracker = GuitarStrikeMotionTracker()
        tracker.update(sample(x = 0.2f, y = 0.1f, z = -0.4f, time = 1_000L), false)

        val alongString = tracker.update(sample(x = 0.22f, y = 0.1f, z = -0.4f, time = 1_020L), false)
        val downstroke = tracker.update(sample(x = 0.22f, y = 0.09f, z = -0.4f, time = 1_040L), false)

        assertEquals(0f, alongString.speedMetersPerSecond, 0.0001f)
        assertFalse(alongString.active)
        assertTrue(downstroke.active)
        assertEquals(GuitarStrikeDirection.DOWNSTROKE, downstroke.direction)
    }

    @Test
    fun shortWindowCancelsAlternatingPoseJitter() {
        val tracker = GuitarStrikeMotionTracker()
        tracker.update(sample(y = 0.1f, time = 1_000L), false)
        tracker.update(sample(y = 0.1014f, time = 1_010L), false)
        val cancelled = tracker.update(sample(y = 0.1f, time = 1_020L), false)

        assertEquals(0f, cancelled.speedMetersPerSecond, 0.0001f)
        assertFalse(cancelled.active)
        assertTrue(cancelled.becameInactive)
    }

    @Test
    fun hysteresisStaysActiveBetweenExitAndEnterThresholds() {
        val gate = GuitarStrikeHysteresis(enterSpeedMetersPerSecond = 0.10f, exitSpeedMetersPerSecond = 0.045f)

        assertFalse(gate.update(0.09f).active)
        assertTrue(gate.update(0.11f).becameActive)
        assertTrue(gate.update(0.07f).active)
        assertTrue(gate.update(0.04f).becameInactive)
        assertFalse(gate.update(0.07f).active)
    }

    @Test
    fun stalePoseGapResetsMotionInsteadOfCreatingASpike() {
        val tracker = GuitarStrikeMotionTracker()
        tracker.update(sample(y = 0.1f, time = 1_000L), false)
        val stale = tracker.update(sample(y = 0.5f, time = 1_500L), false)

        assertFalse(stale.active)
        assertEquals(0f, stale.speedMetersPerSecond, 0.0001f)
    }

    @Test
    fun zMotionIsRecognizedAsPokeDirection() {
        val tracker = GuitarStrikeMotionTracker()
        tracker.update(sample(z = -0.3f, time = 1_000L), true)
        val poke = tracker.update(sample(z = -0.31f, time = 1_020L), true)

        assertTrue(poke.active)
        assertEquals(GuitarStrikeDirection.POKE_IN, poke.direction)
    }

    private fun sample(
        x: Float = 0.2f,
        y: Float = 0.1f,
        z: Float = -0.4f,
        time: Long,
    ) = GuitarMotionSample(x, y, z, time)
}

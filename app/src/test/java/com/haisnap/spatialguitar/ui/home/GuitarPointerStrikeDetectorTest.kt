package com.haisnap.spatialguitar.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuitarPointerStrikeDetectorTest {
    @Test
    fun staticPressStillProducesOneAccessibleTap() {
        val detector = GuitarPointerStrikeDetector<String>()

        val first = detector.update("p", true, false, "s0", sample(time = 1_000L), false, 1_000L)
        val held = detector.update("p", true, false, "s0", sample(time = 1_020L), false, 1_020L)

        assertEquals("s0", first?.target)
        assertEquals(GuitarStrikeDirection.TAP, first?.direction)
        assertNull(held)
    }

    @Test
    fun activeDownstrokeTraversesStringsButAlongStringMotionDoesNot() {
        val detector = GuitarPointerStrikeDetector<String>()
        detector.update("p", true, false, null, sample(time = 1_000L), false, 1_000L)

        val alongString =
            detector.update("p", true, false, "s0", sample(x = 0.22f, time = 1_020L), false, 1_020L)
        val firstString =
            detector.update("p", true, false, "s0", sample(x = 0.22f, y = 0.08f, time = 1_040L), false, 1_040L)
        val secondString =
            detector.update("p", true, false, "s1", sample(x = 0.22f, y = 0.06f, time = 1_060L), false, 1_060L)

        assertNull(alongString)
        assertEquals("s0", firstString?.target)
        assertEquals(GuitarStrikeDirection.DOWNSTROKE, firstString?.direction)
        assertEquals("s1", secondString?.target)
        assertTrue(requireNotNull(secondString).gain >= requireNotNull(firstString).gain)
    }

    @Test
    fun briefTargetMissDoesNotRetriggerTheSameString() {
        val detector = GuitarPointerStrikeDetector<String>()
        detector.update("p", true, false, null, sample(time = 1_000L), false, 1_000L)
        val first = detector.update("p", true, false, "s0", sample(y = 0.08f, time = 1_020L), false, 1_020L)
        detector.update("p", true, false, null, sample(y = 0.07f, time = 1_030L), false, 1_030L)
        val bouncedBack =
            detector.update("p", true, false, "s0", sample(y = 0.06f, time = 1_045L), false, 1_045L)

        assertEquals("s0", first?.target)
        assertNull(bouncedBack)
    }

    @Test
    fun sustainedExitAllowsARealReentryStrike() {
        val detector = GuitarPointerStrikeDetector<String>()
        detector.update("p", true, false, null, sample(time = 1_000L), false, 1_000L)
        detector.update("p", true, false, "s0", sample(y = 0.08f, time = 1_020L), false, 1_020L)
        detector.update("p", true, false, null, sample(y = 0.06f, time = 1_040L), false, 1_040L)
        val reentry =
            detector.update("p", true, false, "s0", sample(y = 0.02f, time = 1_080L), false, 1_080L)

        assertEquals("s0", reentry?.target)
    }

    @Test
    fun pointerStateIsIndependentForTwoHandsOrControllers() {
        val detector = GuitarPointerStrikeDetector<String>()

        val left = detector.update("left", true, false, "s0", sample(time = 1_000L), true, 1_000L)
        val right = detector.update("right", true, false, "s5", sample(time = 1_000L), false, 1_000L)
        val leftHeld = detector.update("left", true, false, "s0", sample(time = 1_020L), true, 1_020L)

        assertEquals("s0", left?.target)
        assertEquals("s5", right?.target)
        assertNull(leftHeld)
    }

    private fun sample(
        x: Float = 0.2f,
        y: Float = 0.1f,
        z: Float = -0.4f,
        time: Long,
    ) = GuitarMotionSample(x, y, z, time)
}

package com.haisnap.spatialguitar.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EasyStrumDetectorTest {
    @Test
    fun firstTouchAlwaysProducesAnAudibleChord() {
        val detector = EasyStrumDetector<String>()

        val first = detector.update("p", true, false, "pad", sample(time = 1_000L), false, 1_000L)
        val held = detector.update("p", true, false, "pad", sample(time = 1_020L), false, 1_020L)

        assertEquals("pad", first?.target)
        assertEquals(GuitarStrikeDirection.TAP, first?.direction)
        assertTrue(requireNotNull(first).gain >= 0.70f)
        assertNull(held)
    }

    @Test
    fun leavingAndReenteringTheWidePadRetriggersWithoutSpeedRequirement() {
        val detector = EasyStrumDetector<String>()
        detector.update("p", true, false, "pad", sample(time = 1_000L), true, 1_000L)
        detector.update("p", true, false, null, sample(time = 1_040L), true, 1_040L)

        val reentered = detector.update("p", true, false, "pad", sample(time = 1_100L), true, 1_100L)

        assertEquals("pad", reentered?.target)
        assertEquals(GuitarStrikeDirection.TAP, reentered?.direction)
    }

    @Test
    fun slowDownAndUpStrokesBothRetriggerInsideOnePad() {
        val detector = EasyStrumDetector<String>()
        detector.update("p", true, false, "pad", sample(y = 0.10f, time = 1_000L), false, 1_000L)

        val down = detector.update("p", true, false, "pad", sample(y = 0.09f, time = 1_120L), false, 1_120L)
        val up = detector.update("p", true, false, "pad", sample(y = 0.10f, time = 1_240L), false, 1_240L)

        assertEquals(GuitarStrikeDirection.DOWNSTROKE, down?.direction)
        assertEquals(GuitarStrikeDirection.UPSTROKE, up?.direction)
    }

    @Test
    fun releasingResetsPointerStateForTheNextCertainTouch() {
        val detector = EasyStrumDetector<String>()
        detector.update("p", true, false, "pad", sample(time = 1_000L), false, 1_000L)
        detector.update("p", false, true, null, null, false, 1_020L)

        val next = detector.update("p", true, false, "pad", sample(time = 1_040L), false, 1_040L)

        assertEquals("pad", next?.target)
    }

    private fun sample(
        x: Float = 0.2f,
        y: Float = 0.1f,
        z: Float = -0.4f,
        time: Long,
    ) = GuitarMotionSample(x, y, z, time)
}

package com.haisnap.spatialguitar.scene

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuitarFretLayoutTest {
    @Test
    fun twelfthFretEndsAtHalfScaleLength() {
        val bounds = GuitarFretLayout.segmentBounds(-0.385f, -0.291f, 0.250f, 16)
        val twelfthFretEnd = bounds[12].second
        val expected = -0.291f + (0.250f - -0.291f) * 0.5f

        assertEquals(16, bounds.size)
        assertEquals(expected, twelfthFretEnd, 0.00001f)
    }

    @Test
    fun frettedSegmentsGetProgressivelyNarrower() {
        val bounds = GuitarFretLayout.segmentBounds(-0.385f, -0.291f, 0.250f, 16)
        val widths = bounds.drop(1).map { it.second - it.first }

        assertTrue(widths.zipWithNext().all { (left, right) -> right < left })
    }
}

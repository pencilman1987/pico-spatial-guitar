package com.haisnap.spatialguitar.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class GuitarPlacementTest {
    @Test
    fun dragDeltasAccumulateInMeters() {
        val placement =
            GuitarPlacement.Centered
                .movedBy(0.04f, -0.03f, 0.02f)
                .movedBy(0.01f, 0.02f, -0.01f)

        assertEquals(0.05f, placement.xMeters, 0.0001f)
        assertEquals(-0.01f, placement.yMeters, 0.0001f)
        assertEquals(0.01f, placement.zMeters, 0.0001f)
    }

    @Test
    fun placementIsClampedSoTheGuitarCanAlwaysBeRecovered() {
        val placement = GuitarPlacement.Centered.movedBy(4f, -4f, 4f)

        assertEquals(GuitarPlacement.MAX_X_METERS, placement.xMeters, 0f)
        assertEquals(GuitarPlacement.MIN_Y_METERS, placement.yMeters, 0f)
        assertEquals(GuitarPlacement.MAX_Z_METERS, placement.zMeters, 0f)
    }
}

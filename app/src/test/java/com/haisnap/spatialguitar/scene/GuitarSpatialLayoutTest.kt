package com.haisnap.spatialguitar.scene

import org.junit.Assert.assertTrue
import org.junit.Test

class GuitarSpatialLayoutTest {
    @Test
    fun highEIsBelowLowEWhenTheGuitarFacesThePlayer() {
        assertTrue(GuitarSpatialLayout.stringY(0) < GuitarSpatialLayout.stringY(5))
    }

    @Test
    fun adjacentStringHitBoxesDoNotOverlap() {
        assertTrue(GuitarSpatialLayout.STRING_HIT_HEIGHT < GuitarSpatialLayout.STRING_SPACING)
    }

    @Test
    fun stringsStayCloseToTheArtworkPlane() {
        val scaledSeparation =
            (GuitarSpatialLayout.STRING_Z - GuitarSpatialLayout.ARTWORK_Z) *
                GuitarSpatialLayout.ROOT_SCALE
        assertTrue(scaledSeparation < 0.006f)
    }
}

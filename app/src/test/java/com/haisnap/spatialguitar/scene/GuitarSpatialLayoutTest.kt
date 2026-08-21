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

    @Test
    fun accompanimentSafetyZoneCoversMostOfTheGuitarBody() {
        val worldWidth =
            GuitarSpatialLayout.SAFE_STRUM_WIDTH * GuitarSpatialLayout.ROOT_SCALE
        val worldHeight =
            GuitarSpatialLayout.SAFE_STRUM_HEIGHT * GuitarSpatialLayout.ROOT_SCALE

        assertTrue(worldWidth >= 0.55f)
        assertTrue(worldHeight >= 0.44f)
    }

    @Test
    fun accompanimentSafetyZoneReachesWellInFrontOfTheGuitar() {
        val frontReach =
            (GuitarSpatialLayout.SAFE_STRUM_CENTER_Z +
                GuitarSpatialLayout.SAFE_STRUM_DEPTH / 2f) *
                GuitarSpatialLayout.ROOT_SCALE

        assertTrue(frontReach >= 0.14f)
    }

    @Test
    fun accompanimentSafetyZoneVerticallyContainsEveryStringWithWideMargin() {
        val highestString = GuitarSpatialLayout.stringY(5)
        val lowestString = GuitarSpatialLayout.stringY(0)
        val halfHeight = GuitarSpatialLayout.SAFE_STRUM_HEIGHT / 2f

        assertTrue(GuitarSpatialLayout.SAFE_STRUM_CENTER_Y + halfHeight > highestString + 0.10f)
        assertTrue(GuitarSpatialLayout.SAFE_STRUM_CENTER_Y - halfHeight < lowestString - 0.10f)
    }
}

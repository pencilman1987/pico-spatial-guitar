package com.haisnap.spatialguitar.ui.home

/** Meter-based offset for the complete guitar assembly inside its SpatialView. */
internal data class GuitarPlacement(
    val xMeters: Float = 0f,
    val yMeters: Float = 0f,
    val zMeters: Float = 0f,
) {
    fun movedBy(
        deltaXMeters: Float,
        deltaYMeters: Float,
        deltaZMeters: Float,
    ) = GuitarPlacement(
        xMeters = (xMeters + deltaXMeters).coerceIn(MIN_X_METERS, MAX_X_METERS),
        yMeters = (yMeters + deltaYMeters).coerceIn(MIN_Y_METERS, MAX_Y_METERS),
        zMeters = (zMeters + deltaZMeters).coerceIn(MIN_Z_METERS, MAX_Z_METERS),
    )

    companion object {
        val Centered = GuitarPlacement()

        internal const val MIN_X_METERS = -0.28f
        internal const val MAX_X_METERS = 0.28f
        internal const val MIN_Y_METERS = -0.18f
        internal const val MAX_Y_METERS = 0.18f
        internal const val MIN_Z_METERS = -0.12f
        internal const val MAX_Z_METERS = 0.18f
    }
}

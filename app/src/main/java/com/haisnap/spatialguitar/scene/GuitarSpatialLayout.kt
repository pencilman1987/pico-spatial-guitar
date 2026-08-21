package com.haisnap.spatialguitar.scene

/** Meter-based layout shared by the Compose attachment and ECS overlay. */
internal object GuitarSpatialLayout {
    const val ROOT_Y = -0.065f
    const val ROOT_SCALE = 1.31f
    const val ARTWORK_Z = 0f
    const val STRING_Z = 0.0035f
    const val SOUND_HOLE_Z = 0.002f
    const val STATUS_X = 0.21f
    const val STATUS_Y = 0.215f
    const val STATUS_Z = 0.055f

    const val STRING_SPACING = 0.0075f
    const val STRING_HIT_HEIGHT = 0.0065f
    private const val HIGH_E_Y = -0.01875f

    /** String 1/high E is visually lowest; string 6/low E is highest. */
    fun stringY(stringIndex: Int): Float {
        require(stringIndex in 0..5)
        return HIGH_E_Y + stringIndex * STRING_SPACING
    }
}

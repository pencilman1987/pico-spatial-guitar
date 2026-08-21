package com.haisnap.spatialguitar.domain.model

data class FretTarget(
    val stringIndex: Int,
    val fret: Int,
) {
    init {
        require(stringIndex in 0..5) { "stringIndex must be between 0 and 5" }
        require(fret in 0..15) { "fret must be between 0 and 15" }
    }
}

data class GuitarStringSpec(
    val index: Int,
    val label: String,
    val openMidi: Int,
    val finish: StringFinish,
)

enum class StringFinish {
    SILVER,
    BRONZE,
}

data class GuitarNote(
    val target: FretTarget,
    val midi: Int,
    val name: String,
)

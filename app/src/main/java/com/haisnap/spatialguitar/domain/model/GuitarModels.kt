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

enum class GuitarPlayMode {
    ACCOMPANIMENT,
    SOLO,
}

/**
 * Beginner-friendly open-position voicings, ordered from string 1/high E to
 * string 6/low E. A null entry means that string is muted for the chord.
 */
enum class GuitarChord(
    val displayName: String,
    val midiByString: List<Int?>,
) {
    C_MAJOR("C", listOf(64, 60, 55, 52, 48, null)),
    G_MAJOR("G", listOf(67, 59, 55, 50, 47, 43)),
    A_MINOR("Am", listOf(64, 60, 57, 52, 45, null)),
    F_MAJOR("F", listOf(65, 60, 57, 53, 48, 41)),
    E_MINOR("Em", listOf(64, 59, 55, 52, 47, 40)),
    D_MINOR("Dm", listOf(65, 62, 57, 50, null, null)),
    E_MAJOR("E", listOf(64, 59, 56, 52, 47, 40)),
    ;

    init {
        require(midiByString.size == 6)
    }
}

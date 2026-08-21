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
    private val rootPitchClass: Int,
    private val isMinor: Boolean,
    val midiByString: List<Int?>,
) {
    C_MAJOR("C", 0, false, listOf(64, 60, 55, 52, 48, null)),
    G_MAJOR("G", 7, false, listOf(67, 59, 55, 50, 47, 43)),
    A_MINOR("Am", 9, true, listOf(64, 60, 57, 52, 45, null)),
    F_MAJOR("F", 5, false, listOf(65, 60, 57, 53, 48, 41)),
    E_MINOR("Em", 4, true, listOf(64, 59, 55, 52, 47, 40)),
    D_MINOR("Dm", 2, true, listOf(65, 62, 57, 50, null, null)),
    E_MAJOR("E", 4, false, listOf(64, 59, 56, 52, 47, 40)),
    ;

    init {
        require(midiByString.size == 6)
    }

    fun displayNameAt(transposeSemitones: Int): String =
        pitchName(rootPitchClass + transposeSemitones) + if (isMinor) "m" else ""

    fun midiByStringAt(transposeSemitones: Int): List<Int?> =
        midiByString.map { midi -> midi?.plus(transposeSemitones) }
}

internal fun pitchName(pitchClass: Int): String =
    PITCH_NAMES[Math.floorMod(pitchClass, PITCH_NAMES.size)]

private val PITCH_NAMES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

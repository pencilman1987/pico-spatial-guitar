package com.haisnap.spatialguitar.audio

import com.haisnap.spatialguitar.domain.model.GuitarTimbre

internal data class GuitarSampleRegion(
    val lowMidi: Int,
    val highMidi: Int,
    val rootMidi: Int,
    val fileName: String,
) {
    fun contains(midi: Int): Boolean = midi in lowMidi..highMidi
}

internal data class GuitarSampleSet(
    val timbre: GuitarTimbre,
    val assetDirectory: String,
    val outputGain: Float,
    val regions: List<GuitarSampleRegion>,
)

/** Licensed sample maps cropped to the application's playable E2-G5 range. */
internal object GuitarSampleMap {
    private val nylon =
        GuitarSampleSet(
            timbre = GuitarTimbre.NYLON,
            assetDirectory = "audio/freepats_spanish_classical_guitar/samples",
            // Peak-normalized steel attacks measured 1.44 dB below the nylon
            // median. Attenuating A preserves headroom and makes A/B fair.
            outputGain = 0.85f,
            regions =
                listOf(
                    region(40, "E2.wav"),
                    region(41, "F2.wav"),
                    region(42, 43, 43, "G2.wav"),
                    region(44, 45, 45, "A2.wav"),
                    region(46, 47, 47, "B2.wav"),
                    region(48, "C3.wav"),
                    region(49, 50, 50, "D3.wav"),
                    region(51, 52, 52, "E3.wav"),
                    region(53, "F3.wav"),
                    region(54, "F#3.wav"),
                    region(55, "G3.wav"),
                    region(56, "G#3.wav"),
                    region(57, "A3.wav"),
                    region(58, "A#3.wav"),
                    region(59, "B3.wav"),
                    region(60, "C4.wav"),
                    region(61, "C#4.wav"),
                    region(62, "D4.wav"),
                    region(63, "D#4.wav"),
                    region(64, "E4.wav"),
                    region(65, "F4.wav"),
                    region(66, "F#4.wav"),
                    region(67, "G4.wav"),
                    region(68, 69, 69, "A4.wav"),
                    region(70, "A#4.wav"),
                    region(71, "B4.wav"),
                    region(72, "C5.wav"),
                    region(73, "C#5.wav"),
                    region(74, "D5.wav"),
                    region(75, "D#5.wav"),
                    region(76, "E5.wav"),
                    region(77, "F5.wav"),
                    region(78, "F#5.wav"),
                    region(79, "G5.wav"),
                ),
        )

    private val steel =
        GuitarSampleSet(
            timbre = GuitarTimbre.STEEL,
            assetDirectory = "audio/discord_martin_hd28_steel/samples",
            outputGain = 1f,
            regions =
                listOf(
                    region(40, 41, 40, "MartinGM2_040__E2_1.wav"),
                    region(42, 44, 43, "MartinGM2_043__G2_1.wav"),
                    region(45, 47, 46, "MartinGM2_046_Bb2_1.wav"),
                    region(48, 50, 49, "MartinGM2_049_Db3_1.wav"),
                    region(51, 53, 52, "MartinGM2_052__E3_1.wav"),
                    region(54, 56, 55, "MartinGM2_055__G3_1.wav"),
                    region(57, 59, 58, "MartinGM2_058_Bb3_1.wav"),
                    region(60, 62, 61, "MartinGM2_061_Db4_1.wav"),
                    region(63, 66, 64, "MartinGM2_064__E4_1.wav"),
                    region(67, 69, 68, "MartinGM2_068_Ab4_1.wav"),
                    region(70, 72, 71, "MartinGM2_071__B4_1.wav"),
                    region(73, 75, 74, "MartinGM2_074__D5_1.wav"),
                    region(76, 78, 77, "MartinGM2_077__F5_1.wav"),
                    region(79, 79, 80, "MartinGM2_080_Ab5_1.wav"),
                ),
        )

    private val sets = GuitarTimbre.entries.associateWith { timbre ->
        when (timbre) {
            GuitarTimbre.NYLON -> nylon
            GuitarTimbre.STEEL -> steel
        }
    }

    fun sampleSet(timbre: GuitarTimbre): GuitarSampleSet = requireNotNull(sets[timbre])

    fun forMidi(timbre: GuitarTimbre, midi: Int): GuitarSampleRegion? =
        sampleSet(timbre).regions.firstOrNull { it.contains(midi) }

    fun allRegions(timbre: GuitarTimbre): List<GuitarSampleRegion> = sampleSet(timbre).regions

    fun assetPath(timbre: GuitarTimbre, region: GuitarSampleRegion): String =
        "${sampleSet(timbre).assetDirectory}/${region.fileName}"

    private fun region(midi: Int, fileName: String) =
        GuitarSampleRegion(midi, midi, midi, fileName)

    private fun region(lowMidi: Int, highMidi: Int, rootMidi: Int, fileName: String) =
        GuitarSampleRegion(lowMidi, highMidi, rootMidi, fileName)
}

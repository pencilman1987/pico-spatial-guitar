package com.haisnap.spatialguitar.ui.home

import com.haisnap.spatialguitar.domain.model.FretTarget
import com.haisnap.spatialguitar.domain.model.GuitarChord
import com.haisnap.spatialguitar.domain.model.GuitarNote
import com.haisnap.spatialguitar.domain.model.GuitarPlayMode
import com.haisnap.spatialguitar.domain.model.GuitarSong
import com.haisnap.spatialguitar.domain.model.GuitarSongStep
import com.haisnap.spatialguitar.domain.model.GuitarTimbre

data class GuitarHomeUiState(
    val activeNote: GuitarNote? = null,
    val velocity: Float = 0f,
    val playSequence: Long = 0L,
    val timbre: GuitarTimbre = GuitarTimbre.NYLON,
    val playMode: GuitarPlayMode = GuitarPlayMode.ACCOMPANIMENT,
    val selectedChord: GuitarChord = GuitarChord.C_MAJOR,
    val selectedSong: GuitarSong? = GuitarSong.SING_TOGETHER,
    val songStepIndex: Int = 0,
    val songStrumsInStep: Int = 0,
    val transposeSemitones: Int = 0,
    val isMoveMode: Boolean = false,
    val status: String = "跟唱 · 一起唱 · C 调",
) {
    val currentSongStep: GuitarSongStep?
        get() = selectedSong?.let { it.steps[songStepIndex.coerceIn(it.steps.indices)] }

    val nextSongStep: GuitarSongStep?
        get() = selectedSong?.let { it.steps[(songStepIndex + 1) % it.steps.size] }

    val remainingStrums: Int
        get() = (currentSongStep?.strums ?: 0).minus(songStrumsInStep).coerceAtLeast(0)

    val transposeDisplay: String
        get() =
            selectedSong?.let { "${it.keyNameAt(transposeSemitones)} 调" }
                ?: when {
                    transposeSemitones > 0 -> "+$transposeSemitones"
                    else -> transposeSemitones.toString()
                }
}

sealed interface GuitarHomeEvent {
    data class Played(
        val note: GuitarNote,
        val velocity: Float,
    ) : GuitarHomeEvent

    data class TimbreSelected(val timbre: GuitarTimbre) : GuitarHomeEvent

    data class PlayModeChanged(val mode: GuitarPlayMode) : GuitarHomeEvent

    data class ChordSelected(val chord: GuitarChord) : GuitarHomeEvent

    data class SongSelected(val song: GuitarSong?) : GuitarHomeEvent

    data class TransposeChanged(val deltaSemitones: Int) : GuitarHomeEvent

    data class ChordStrummed(
        val chord: GuitarChord,
        val velocity: Float,
    ) : GuitarHomeEvent

    data class MoveModeChanged(val enabled: Boolean) : GuitarHomeEvent

    data object Reset : GuitarHomeEvent

    data object AudioFailed : GuitarHomeEvent
}

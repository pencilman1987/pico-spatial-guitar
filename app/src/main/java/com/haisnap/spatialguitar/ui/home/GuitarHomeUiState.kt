package com.haisnap.spatialguitar.ui.home

import com.haisnap.spatialguitar.domain.model.FretTarget
import com.haisnap.spatialguitar.domain.model.GuitarChord
import com.haisnap.spatialguitar.domain.model.GuitarNote
import com.haisnap.spatialguitar.domain.model.GuitarPlayMode
import com.haisnap.spatialguitar.domain.model.GuitarTimbre

data class GuitarHomeUiState(
    val activeNote: GuitarNote? = null,
    val velocity: Float = 0f,
    val playSequence: Long = 0L,
    val timbre: GuitarTimbre = GuitarTimbre.NYLON,
    val playMode: GuitarPlayMode = GuitarPlayMode.ACCOMPANIMENT,
    val selectedChord: GuitarChord = GuitarChord.C_MAJOR,
    val isMoveMode: Boolean = false,
    val status: String = "伴奏模式 · 选择和弦，扫过音孔",
)

sealed interface GuitarHomeEvent {
    data class Played(
        val note: GuitarNote,
        val velocity: Float,
    ) : GuitarHomeEvent

    data class TimbreSelected(val timbre: GuitarTimbre) : GuitarHomeEvent

    data class PlayModeChanged(val mode: GuitarPlayMode) : GuitarHomeEvent

    data class ChordSelected(val chord: GuitarChord) : GuitarHomeEvent

    data class ChordStrummed(
        val chord: GuitarChord,
        val velocity: Float,
    ) : GuitarHomeEvent

    data class MoveModeChanged(val enabled: Boolean) : GuitarHomeEvent

    data object Reset : GuitarHomeEvent

    data object AudioFailed : GuitarHomeEvent
}

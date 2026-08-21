package com.haisnap.spatialguitar.ui.home

import com.haisnap.spatialguitar.domain.model.FretTarget
import com.haisnap.spatialguitar.domain.model.GuitarNote
import com.haisnap.spatialguitar.domain.model.GuitarTimbre

data class GuitarHomeUiState(
    val activeNote: GuitarNote? = null,
    val velocity: Float = 0f,
    val playSequence: Long = 0L,
    val timbre: GuitarTimbre = GuitarTimbre.NYLON,
    val isMoveMode: Boolean = false,
    val status: String = "准备就绪",
)

sealed interface GuitarHomeEvent {
    data class Played(
        val note: GuitarNote,
        val velocity: Float,
    ) : GuitarHomeEvent

    data class TimbreSelected(val timbre: GuitarTimbre) : GuitarHomeEvent

    data class MoveModeChanged(val enabled: Boolean) : GuitarHomeEvent

    data object Reset : GuitarHomeEvent

    data object AudioFailed : GuitarHomeEvent
}

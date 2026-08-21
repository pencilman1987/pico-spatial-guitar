package com.haisnap.spatialguitar.ui.home

import androidx.lifecycle.ViewModel
import com.haisnap.spatialguitar.data.repository.DefaultGuitarRepository
import com.haisnap.spatialguitar.domain.model.FretTarget
import com.haisnap.spatialguitar.domain.usecase.CalculateGuitarNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GuitarHomeViewModel(
    private val calculateNote: CalculateGuitarNoteUseCase =
        CalculateGuitarNoteUseCase(DefaultGuitarRepository()),
) : ViewModel() {
    private val _state = MutableStateFlow(GuitarHomeUiState())
    val state: StateFlow<GuitarHomeUiState> = _state.asStateFlow()

    fun noteFor(target: FretTarget) = calculateNote(target)

    fun onEvent(event: GuitarHomeEvent) {
        when (event) {
            is GuitarHomeEvent.Played -> {
                val note = event.note
                _state.update {
                    it.copy(
                        activeNote = note,
                        velocity = event.velocity.coerceIn(0.18f, 1f),
                        playSequence = it.playSequence + 1L,
                        status = "${note.name}  ·  第 ${note.target.fret} 品",
                    )
                }
            }

            is GuitarHomeEvent.TimbreSelected -> {
                _state.update {
                    it.copy(
                        timbre = event.timbre,
                        status = "音色 ${event.timbre.abLabel} · ${event.timbre.displayName}",
                    )
                }
            }

            GuitarHomeEvent.Reset -> _state.value = GuitarHomeUiState()
            GuitarHomeEvent.AudioFailed -> _state.update { it.copy(status = "音频暂不可用") }
        }
    }
}

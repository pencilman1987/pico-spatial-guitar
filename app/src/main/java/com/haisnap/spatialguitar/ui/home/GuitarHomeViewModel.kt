package com.haisnap.spatialguitar.ui.home

import androidx.lifecycle.ViewModel
import com.haisnap.spatialguitar.data.repository.DefaultGuitarRepository
import com.haisnap.spatialguitar.domain.model.FretTarget
import com.haisnap.spatialguitar.domain.model.GuitarPlayMode
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

            is GuitarHomeEvent.PlayModeChanged -> {
                _state.update {
                    if (event.mode == it.playMode) return@update it
                    it.copy(
                        playMode = event.mode,
                        isMoveMode = false,
                        selectedSong = null,
                        songStepIndex = 0,
                        songStrumsInStep = 0,
                        status =
                            if (event.mode == GuitarPlayMode.ACCOMPANIMENT) {
                                "伴奏模式 · 选择和弦，扫过音孔"
                            } else {
                                "单音模式 · 横跨琴弦演奏"
                            },
                    )
                }
            }

            is GuitarHomeEvent.ChordSelected -> {
                _state.update {
                    it.copy(
                        selectedChord = event.chord,
                        selectedSong = null,
                        songStepIndex = 0,
                        songStrumsInStep = 0,
                        activeNote = null,
                        status =
                            "已选 ${event.chord.displayNameAt(it.transposeSemitones)} · 扫过音孔",
                    )
                }
            }

            is GuitarHomeEvent.SongSelected -> {
                _state.update {
                    val firstStep = event.song?.steps?.firstOrNull()
                    it.copy(
                        playMode = GuitarPlayMode.ACCOMPANIMENT,
                        selectedSong = event.song,
                        songStepIndex = 0,
                        songStrumsInStep = 0,
                        selectedChord = firstStep?.chord ?: it.selectedChord,
                        activeNote = null,
                        isMoveMode = false,
                        status =
                            event.song?.let { song ->
                                "跟唱 · ${song.title} · ${song.keyNameAt(it.transposeSemitones)} 调"
                            } ?: "自由伴奏 · 选择和弦",
                    )
                }
            }

            is GuitarHomeEvent.TransposeChanged -> {
                _state.update {
                    val transposed =
                        (it.transposeSemitones + event.deltaSemitones).coerceIn(
                            MIN_TRANSPOSE_SEMITONES,
                            MAX_TRANSPOSE_SEMITONES,
                        )
                    it.copy(
                        transposeSemitones = transposed,
                        status =
                            it.selectedSong?.let { song ->
                                "${song.title} · ${song.keyNameAt(transposed)} 调"
                            } ?: "移调 ${transposeLabel(transposed)}",
                    )
                }
            }

            is GuitarHomeEvent.ChordStrummed -> {
                _state.update {
                    val song = it.selectedSong
                    if (song == null) {
                        it.copy(
                            selectedChord = event.chord,
                            activeNote = null,
                            velocity = event.velocity.coerceIn(0.18f, 1f),
                            playSequence = it.playSequence + 1L,
                            status =
                                "${event.chord.displayNameAt(it.transposeSemitones)} · 自由伴奏",
                        )
                    } else {
                        val step = song.steps[it.songStepIndex]
                        val strumCount = it.songStrumsInStep + 1
                        if (strumCount >= step.strums) {
                            val nextIndex = (it.songStepIndex + 1) % song.steps.size
                            val nextStep = song.steps[nextIndex]
                            it.copy(
                                selectedChord = nextStep.chord,
                                songStepIndex = nextIndex,
                                songStrumsInStep = 0,
                                activeNote = null,
                                velocity = event.velocity.coerceIn(0.18f, 1f),
                                playSequence = it.playSequence + 1L,
                                status =
                                    "换 ${nextStep.chord.displayNameAt(it.transposeSemitones)} · 继续扫",
                            )
                        } else {
                            it.copy(
                                selectedChord = step.chord,
                                songStrumsInStep = strumCount,
                                activeNote = null,
                                velocity = event.velocity.coerceIn(0.18f, 1f),
                                playSequence = it.playSequence + 1L,
                                status =
                                    "${step.chord.displayNameAt(it.transposeSemitones)} · 再扫 ${step.strums - strumCount} 下",
                            )
                        }
                    }
                }
            }

            is GuitarHomeEvent.MoveModeChanged -> {
                _state.update {
                    it.copy(
                        isMoveMode = event.enabled,
                        status = if (event.enabled) "移动模式 · 拖动琴身" else "演奏模式",
                    )
                }
            }

            GuitarHomeEvent.Reset -> _state.value = GuitarHomeUiState()
            GuitarHomeEvent.AudioFailed -> _state.update { it.copy(status = "音频暂不可用") }
        }
    }

    private fun transposeLabel(semitones: Int): String =
        when {
            semitones > 0 -> "+$semitones"
            else -> semitones.toString()
        }

    private companion object {
        const val MIN_TRANSPOSE_SEMITONES = -5
        const val MAX_TRANSPOSE_SEMITONES = 6
    }
}

package com.haisnap.spatialguitar.ui.home

import com.haisnap.spatialguitar.data.repository.DefaultGuitarRepository
import com.haisnap.spatialguitar.domain.model.FretTarget
import com.haisnap.spatialguitar.domain.model.GuitarChord
import com.haisnap.spatialguitar.domain.model.GuitarPlayMode
import com.haisnap.spatialguitar.domain.model.GuitarSong
import com.haisnap.spatialguitar.domain.model.GuitarTimbre
import com.haisnap.spatialguitar.domain.usecase.CalculateGuitarNoteUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GuitarHomeViewModelTest {
    private fun viewModel() =
        GuitarHomeViewModel(CalculateGuitarNoteUseCase(DefaultGuitarRepository()))

    @Test
    fun initialStateIsReady() {
        val state = viewModel().state.value

        assertEquals("跟唱 · 一起唱 · C 调", state.status)
        assertNull(state.activeNote)
        assertEquals(GuitarTimbre.NYLON, state.timbre)
        assertEquals(GuitarPlayMode.ACCOMPANIMENT, state.playMode)
        assertEquals(GuitarChord.C_MAJOR, state.selectedChord)
        assertEquals(GuitarSong.SING_TOGETHER, state.selectedSong)
        assertEquals("窗边的风轻轻吹进来", state.currentSongStep?.lyric)
        assertEquals(4, state.remainingStrums)
        assertEquals(0, state.transposeSemitones)
        assertEquals(false, state.isMoveMode)
    }

    @Test
    fun playUpdatesActiveNoteAndSequence() {
        val viewModel = viewModel()

        val note = viewModel.noteFor(FretTarget(0, 3))
        viewModel.onEvent(GuitarHomeEvent.Played(note, 0.7f))

        assertEquals("G4", viewModel.state.value.activeNote?.name)
        assertEquals(1L, viewModel.state.value.playSequence)
    }

    @Test
    fun playClampsVelocityAtUpperBoundary() {
        val viewModel = viewModel()

        val note = viewModel.noteFor(FretTarget(2, 7))
        viewModel.onEvent(GuitarHomeEvent.Played(note, 2f))

        assertEquals(1f, viewModel.state.value.velocity)
    }

    @Test
    fun resetRestoresReadyState() {
        val viewModel = viewModel()
        val note = viewModel.noteFor(FretTarget(5, 12))
        viewModel.onEvent(GuitarHomeEvent.Played(note, 0.5f))

        viewModel.onEvent(GuitarHomeEvent.Reset)

        assertEquals(GuitarHomeUiState(), viewModel.state.value)
    }

    @Test
    fun audioFailureProducesVisibleStatus() {
        val viewModel = viewModel()

        viewModel.onEvent(GuitarHomeEvent.AudioFailed)

        assertEquals("音频暂不可用", viewModel.state.value.status)
    }

    @Test
    fun timbreSwitchKeepsNylonAsDefaultAndSelectsSteelWithoutResettingPlayState() {
        val viewModel = viewModel()
        val note = viewModel.noteFor(FretTarget(0, 3))
        viewModel.onEvent(GuitarHomeEvent.Played(note, 0.7f))

        viewModel.onEvent(GuitarHomeEvent.TimbreSelected(GuitarTimbre.STEEL))

        assertEquals(GuitarTimbre.STEEL, viewModel.state.value.timbre)
        assertEquals("G4", viewModel.state.value.activeNote?.name)
        assertEquals("音色 B · 钢弦", viewModel.state.value.status)
    }

    @Test
    fun moveModeCanBeEnabledWithoutResettingTheActiveNote() {
        val viewModel = viewModel()
        val note = viewModel.noteFor(FretTarget(0, 3))
        viewModel.onEvent(GuitarHomeEvent.Played(note, 0.7f))

        viewModel.onEvent(GuitarHomeEvent.MoveModeChanged(true))

        assertEquals(true, viewModel.state.value.isMoveMode)
        assertEquals("G4", viewModel.state.value.activeNote?.name)
        assertEquals("移动模式 · 拖动琴身", viewModel.state.value.status)
    }

    @Test
    fun chordSelectionAndStrumProvideOneTouchAccompanimentState() {
        val viewModel = viewModel()

        viewModel.onEvent(GuitarHomeEvent.ChordSelected(GuitarChord.A_MINOR))
        viewModel.onEvent(GuitarHomeEvent.ChordStrummed(GuitarChord.A_MINOR, 0.74f))

        assertEquals(GuitarChord.A_MINOR, viewModel.state.value.selectedChord)
        assertEquals("Am · 自由伴奏", viewModel.state.value.status)
        assertEquals(0.74f, viewModel.state.value.velocity)
        assertEquals(1L, viewModel.state.value.playSequence)
    }

    @Test
    fun soloModeRemainsAvailableAndLeavesMoveMode() {
        val viewModel = viewModel()
        viewModel.onEvent(GuitarHomeEvent.MoveModeChanged(true))

        viewModel.onEvent(GuitarHomeEvent.PlayModeChanged(GuitarPlayMode.SOLO))

        assertEquals(GuitarPlayMode.SOLO, viewModel.state.value.playMode)
        assertEquals(false, viewModel.state.value.isMoveMode)
        assertEquals("单音模式 · 横跨琴弦演奏", viewModel.state.value.status)
    }

    @Test
    fun selectingSongStartsAtItsFirstLyricAndChord() {
        val viewModel = viewModel()

        viewModel.onEvent(GuitarHomeEvent.SongSelected(GuitarSong.EVENING_BREEZE))

        val state = viewModel.state.value
        assertEquals(GuitarSong.EVENING_BREEZE, state.selectedSong)
        assertEquals(GuitarChord.A_MINOR, state.selectedChord)
        assertEquals("晚风从小路那头走来", state.currentSongStep?.lyric)
        assertEquals(4, state.remainingStrums)
    }

    @Test
    fun fourthGuidedStrumAdvancesLyricAndChordAutomatically() {
        val viewModel = viewModel()
        viewModel.onEvent(GuitarHomeEvent.SongSelected(GuitarSong.SING_TOGETHER))

        repeat(4) {
            viewModel.onEvent(GuitarHomeEvent.ChordStrummed(GuitarChord.C_MAJOR, 0.7f))
        }

        val state = viewModel.state.value
        assertEquals(1, state.songStepIndex)
        assertEquals(0, state.songStrumsInStep)
        assertEquals(GuitarChord.G_MAJOR, state.selectedChord)
        assertEquals("把今天的心情唱出来", state.currentSongStep?.lyric)
    }

    @Test
    fun guidedSongLoopsAfterItsLastMeasure() {
        val viewModel = viewModel()
        val song = GuitarSong.SING_TOGETHER
        viewModel.onEvent(GuitarHomeEvent.SongSelected(song))

        repeat(song.steps.sumOf { it.strums }) {
            viewModel.onEvent(GuitarHomeEvent.ChordStrummed(viewModel.state.value.selectedChord, 0.7f))
        }

        assertEquals(0, viewModel.state.value.songStepIndex)
        assertEquals(song.steps.first().chord, viewModel.state.value.selectedChord)
    }

    @Test
    fun transposeIsBoundedAndUpdatesSongKeyAndChordLabels() {
        val viewModel = viewModel()
        viewModel.onEvent(GuitarHomeEvent.SongSelected(GuitarSong.SING_TOGETHER))

        repeat(12) { viewModel.onEvent(GuitarHomeEvent.TransposeChanged(1)) }

        assertEquals(6, viewModel.state.value.transposeSemitones)
        assertEquals("F# 调", viewModel.state.value.transposeDisplay)
        assertEquals("F#", viewModel.state.value.selectedChord.displayNameAt(6))
    }

    @Test
    fun manualChordSelectionReturnsToFreeAccompaniment() {
        val viewModel = viewModel()
        viewModel.onEvent(GuitarHomeEvent.SongSelected(GuitarSong.SING_TOGETHER))

        viewModel.onEvent(GuitarHomeEvent.ChordSelected(GuitarChord.E_MINOR))

        assertNull(viewModel.state.value.selectedSong)
        assertEquals(GuitarChord.E_MINOR, viewModel.state.value.selectedChord)
    }
}

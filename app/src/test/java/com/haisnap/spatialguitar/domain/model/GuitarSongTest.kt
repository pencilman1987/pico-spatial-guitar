package com.haisnap.spatialguitar.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuitarSongTest {
    @Test
    fun bundledPracticeSongsHaveCompleteOriginalCueSheets() {
        GuitarSong.entries.forEach { song ->
            assertEquals(8, song.steps.size)
            assertTrue(song.steps.all { it.lyric.isNotBlank() && it.strums == 4 })
        }
    }

    @Test
    fun songKeyLabelsTransposeWithMajorAndMinorQuality() {
        assertEquals("D", GuitarSong.SING_TOGETHER.keyNameAt(2))
        assertEquals("Bm", GuitarSong.EVENING_BREEZE.keyNameAt(2))
        assertEquals("G#m", GuitarSong.EVENING_BREEZE.keyNameAt(-1))
    }
}

package com.haisnap.spatialguitar.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuitarChordTest {
    @Test
    fun everyEasyChordMapsAllSixPhysicalStrings() {
        GuitarChord.entries.forEach { chord ->
            assertEquals(6, chord.midiByString.size)
            assertTrue(chord.midiByString.count { it != null } >= 4)
        }
    }

    @Test
    fun defaultSingerSongwriterSetContainsSevenCommonChords() {
        assertEquals(listOf("C", "G", "Am", "F", "Em", "Dm", "E"), GuitarChord.entries.map { it.displayName })
    }
}

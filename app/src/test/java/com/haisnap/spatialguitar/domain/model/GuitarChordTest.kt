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

    @Test
    fun chordLabelsFollowTransposeWithoutChangingChordQuality() {
        assertEquals("D", GuitarChord.C_MAJOR.displayNameAt(2))
        assertEquals("F#m", GuitarChord.E_MINOR.displayNameAt(2))
        assertEquals("B", GuitarChord.F_MAJOR.displayNameAt(6))
        assertEquals("Gm", GuitarChord.A_MINOR.displayNameAt(-2))
    }

    @Test
    fun everySoundingStringMovesByTheRequestedSemitoneOffset() {
        assertEquals(
            listOf(66, 62, 57, 54, 50, null),
            GuitarChord.C_MAJOR.midiByStringAt(2),
        )
        assertEquals(
            listOf(62, 57, 53, 50, 45, 38),
            GuitarChord.E_MINOR.midiByStringAt(-2),
        )
    }
}

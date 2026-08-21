package com.haisnap.spatialguitar.audio

import com.haisnap.spatialguitar.domain.model.GuitarTimbre
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuitarSampleMapTest {
    @Test
    fun everyPlayableMidiNoteHasALicensedSampleRegion() {
        GuitarTimbre.entries.forEach { timbre ->
            assertTrue((40..79).all { GuitarSampleMap.forMidi(timbre, it) != null })
        }
    }

    @Test
    fun sharedRegionsPreserveTheOriginalSfzRootNote() {
        val region = GuitarSampleMap.forMidi(GuitarTimbre.NYLON, 42)

        assertNotNull(region)
        assertEquals(43, region?.rootMidi)
        assertEquals("G2.wav", region?.fileName)
    }

    @Test
    fun notesOutsideTheBundledInstrumentRangeDoNotMap() {
        GuitarTimbre.entries.forEach { timbre ->
            assertNull(GuitarSampleMap.forMidi(timbre, 39))
            assertNull(GuitarSampleMap.forMidi(timbre, 80))
        }
    }

    @Test
    fun steelMapPreservesUpstreamRootNotesAtPlayableEdges() {
        assertEquals(40, GuitarSampleMap.forMidi(GuitarTimbre.STEEL, 40)?.rootMidi)
        assertEquals(80, GuitarSampleMap.forMidi(GuitarTimbre.STEEL, 79)?.rootMidi)
    }

    @Test
    fun comparisonGainMatchesMeasuredMedianAttackLevels() {
        assertEquals(0.85f, GuitarSampleMap.sampleSet(GuitarTimbre.NYLON).outputGain)
        assertEquals(1f, GuitarSampleMap.sampleSet(GuitarTimbre.STEEL).outputGain)
    }
}

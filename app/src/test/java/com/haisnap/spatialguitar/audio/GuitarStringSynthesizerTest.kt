package com.haisnap.spatialguitar.audio

import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuitarStringSynthesizerTest {
    private val synthesizer = GuitarStringSynthesizer()

    @Test
    fun bassStringSustainsLongerThanTrebleString() {
        val highE = synthesizer.synthesize(stringIndex = 0, midi = 64)
        val lowE = synthesizer.synthesize(stringIndex = 5, midi = 40)

        assertTrue(lowE.size > highE.size)
        assertEquals(0, highE.last().toInt())
        assertEquals(0, lowE.last().toInt())
    }

    @Test
    fun samePitchOnDifferentStringsKeepsDifferentTimbre() {
        val highEOpen = synthesizer.synthesize(stringIndex = 0, midi = 64)
        val bStringFifthFret = synthesizer.synthesize(stringIndex = 1, midi = 64)

        assertFalse(highEOpen.contentEquals(bStringFifthFret))
    }

    @Test
    fun tailDecaysBelowInitialBody() {
        val pcm = synthesizer.synthesize(stringIndex = 3, midi = 50)
        val window = 4_410
        val bodyRms = rms(pcm, window, window * 2)
        val tailRms = rms(pcm, pcm.size - window, pcm.size)

        assertTrue(tailRms < bodyRms * 0.35)
    }

    private fun rms(pcm: ShortArray, start: Int, end: Int): Double {
        var energy = 0.0
        for (index in start until end) {
            val value = pcm[index].toDouble() / Short.MAX_VALUE
            energy += value * value
        }
        return sqrt(energy / (end - start))
    }
}

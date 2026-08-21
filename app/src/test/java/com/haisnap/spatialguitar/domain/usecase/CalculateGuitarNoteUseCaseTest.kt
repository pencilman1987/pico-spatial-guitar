package com.haisnap.spatialguitar.domain.usecase

import com.haisnap.spatialguitar.data.repository.DefaultGuitarRepository
import com.haisnap.spatialguitar.domain.model.FretTarget
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateGuitarNoteUseCaseTest {
    private val useCase = CalculateGuitarNoteUseCase(DefaultGuitarRepository())

    @Test
    fun highEOpenStringMapsToE4() {
        val note = useCase(FretTarget(stringIndex = 0, fret = 0))

        assertEquals(64, note.midi)
        assertEquals("E4", note.name)
    }

    @Test
    fun lowEAtTwelfthFretMapsToE3() {
        val note = useCase(FretTarget(stringIndex = 5, fret = 12))

        assertEquals(52, note.midi)
        assertEquals("E3", note.name)
    }
}

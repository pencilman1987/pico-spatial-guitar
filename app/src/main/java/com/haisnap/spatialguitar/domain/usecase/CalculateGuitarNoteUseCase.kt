package com.haisnap.spatialguitar.domain.usecase

import com.haisnap.spatialguitar.data.repository.GuitarRepository
import com.haisnap.spatialguitar.domain.model.FretTarget
import com.haisnap.spatialguitar.domain.model.GuitarNote

class CalculateGuitarNoteUseCase(
    private val repository: GuitarRepository,
) {
    operator fun invoke(target: FretTarget): GuitarNote {
        val string = repository.strings().first { it.index == target.stringIndex }
        val midi = string.openMidi + target.fret
        return GuitarNote(target = target, midi = midi, name = midiName(midi))
    }

    private fun midiName(midi: Int): String {
        val names = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        return names[midi % 12] + (midi / 12 - 1)
    }
}

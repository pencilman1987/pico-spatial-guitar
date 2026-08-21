package com.haisnap.spatialguitar.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/** Produces a short, offline PCM model of one physical guitar string. */
internal class GuitarStringSynthesizer(
    private val sampleRate: Int = 44_100,
) {
    fun synthesize(stringIndex: Int, midi: Int): ShortArray {
        require(stringIndex in OPEN_MIDI.indices) { "Unsupported string: $stringIndex" }
        val fret = midi - OPEN_MIDI[stringIndex]
        require(fret in 0..MAX_FRET) { "MIDI $midi is outside string $stringIndex" }

        val frequency = 440.0 * 2.0.pow((midi - 69) / 12.0)
        val durationSeconds = (1.90 + stringIndex * 0.18 - fret * 0.018).coerceAtLeast(1.58)
        val sampleCount = (sampleRate * durationSeconds).roundToInt()
        val delayLength = (sampleRate / frequency).roundToInt().coerceAtLeast(2)
        val delay = FloatArray(delayLength)
        val random = Random(stringIndex * 10_007 + midi * 97 + 11)

        // Plain treble strings retain more pick brightness; wound bass strings
        // receive a slightly rounder excitation while keeping a firm attack.
        val brightness = if (stringIndex < 3) 0.72f else 0.54f
        var shapedNoise = 0f
        delay.indices.forEach { index ->
            val noise = random.nextFloat() * 2f - 1f
            shapedNoise += (noise - shapedNoise) * brightness
            delay[index] = shapedNoise
        }

        // Target roughly -60 dB by the end of this string-dependent decay.
        // Computing the gain per round trip avoids the unnaturally short high
        // notes produced by a single feedback value for every pitch.
        val loopGain = exp(ln(0.001) / (frequency * durationSeconds)).toFloat()
        val raw = FloatArray(sampleCount)
        val attackSamples = (sampleRate * 0.0015f).roundToInt().coerceAtLeast(1)
        val fadeSamples = (sampleRate * 0.045f).roundToInt().coerceAtLeast(1)
        val pickSamples = (sampleRate * 0.12f).roundToInt()
        var pickDecay = 1f
        val pickDecayPerSample = exp(-82.0 / sampleRate).toFloat()
        var body110Decay = 1f
        var body220Decay = 1f
        val body110DecayPerSample = exp(-4.2 / sampleRate).toFloat()
        val body220DecayPerSample = exp(-5.4 / sampleRate).toFloat()
        val omega110 = 2.0 * PI * 110.0 / sampleRate
        val omega220 = 2.0 * PI * 220.0 / sampleRate
        val oscillator110 = RecursiveSine(2.0 * cos(omega110), sin(omega110))
        val oscillator220 = RecursiveSine(2.0 * cos(omega220), sin(omega220))
        var cursor = 0
        var peak = 0f

        raw.indices.forEach { sample ->
            val next = (cursor + 1) % delayLength
            val plucked = delay[cursor]
            delay[cursor] = (delay[cursor] + delay[next]) * 0.5f * loopGain
            cursor = next

            val attack = (sample.toFloat() / attackSamples).coerceIn(0f, 1f)
            val remaining = (sampleCount - 1 - sample).toFloat()
            val tailFade = (remaining / fadeSamples).coerceIn(0f, 1f)
            val pickTransient =
                if (sample < pickSamples) {
                    ((random.nextFloat() * 2f - 1f) * pickDecay * 0.10f).also {
                        pickDecay *= pickDecayPerSample
                    }
                } else {
                    0f
                }
            val soundboard =
                (
                    oscillator110.next() * body110Decay * 0.026 +
                        oscillator220.next() * body220Decay * 0.014
                ).toFloat()
            body110Decay *= body110DecayPerSample
            body220Decay *= body220DecayPerSample
            val value = (plucked * 0.90f + pickTransient + soundboard) * attack * tailFade
            raw[sample] = value
            peak = maxOf(peak, abs(value))
        }

        val gain = if (peak > 0f) 0.86f / peak else 0f
        return ShortArray(sampleCount) { sample ->
            (raw[sample] * gain * Short.MAX_VALUE)
                .coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat())
                .roundToInt()
                .toShort()
        }
    }

    private companion object {
        val OPEN_MIDI = intArrayOf(64, 59, 55, 50, 45, 40)
        const val MAX_FRET = 15
    }

    private class RecursiveSine(
        private val coefficient: Double,
        initialCurrent: Double,
    ) {
        private var previous = 0.0
        private var current = initialCurrent

        fun next(): Double {
            val value = previous
            val following = coefficient * current - previous
            previous = current
            current = following
            return value
        }
    }
}

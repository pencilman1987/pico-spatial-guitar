package com.haisnap.spatialguitar.audio

import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.SystemClock
import android.util.Log
import com.haisnap.spatialguitar.domain.model.GuitarChord
import com.haisnap.spatialguitar.domain.model.GuitarTimbre
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

class GuitarAudioEngine(assetManager: AssetManager) : Closeable {
    private val sampleRate = 44_100
    private val samplePool = GuitarSamplePool(assetManager)
    private val synthesizer = GuitarStringSynthesizer(sampleRate)
    private val voices = LinkedHashMap<VoiceKey, Voice>(MAX_CACHED_VOICES, 0.75f, true)
    private val samples = ConcurrentHashMap<VoiceKey, ShortArray>()
    private val activeByString = mutableMapOf<Int, Voice>()
    private val activeSampleStreamByString = mutableMapOf<Int, Int>()

    /** Starts SoundPool decoding and warms emergency fallback PCM off the UI thread. */
    fun prepare() {
        samplePool.prepare()
        OPEN_MIDI.forEachIndexed { stringIndex, midi ->
            fallbackSampleFor(VoiceKey(stringIndex, midi))
        }
        GuitarChord.entries
            .flatMap { chord ->
                chord.midiByString.mapIndexedNotNull { stringIndex, midi ->
                    midi?.let { VoiceKey(stringIndex, it) }
                }
            }
            .distinct()
            .forEach(::fallbackSampleFor)
    }

    @Synchronized
    fun play(
        timbre: GuitarTimbre,
        stringIndex: Int,
        midi: Int,
        velocity: Float,
        inputUptimeMillis: Long = SystemClock.uptimeMillis(),
    ): Boolean {
        val triggerNanos = System.nanoTime()
        val inputGain = velocity.coerceIn(MIN_GAIN, 1f)
        val samplePlayback = samplePool.play(timbre, midi, inputGain)
        if (samplePlayback != null) {
            activeSampleStreamByString.put(stringIndex, samplePlayback.streamId)?.let(samplePool::stop)
            activeByString.remove(stringIndex)?.stop()
            logTriggerLatency(
                inputUptimeMillis = inputUptimeMillis,
                engineStartNanos = triggerNanos,
                timbre = timbre,
                midi = midi,
                inputGain = inputGain,
                outputGain = samplePlayback.appliedGain,
                source = samplePlayback.assetPath,
            )
            return true
        }

        val key = VoiceKey(stringIndex, midi)
        val pcm = fallbackSampleFor(key)
        val voice = voices.getOrPut(key) { Voice(createTrack(pcm)) }

        // One physical string can only sustain one fret at a time. Different
        // strings remain independent even when they produce the same MIDI note.
        activeSampleStreamByString.remove(stringIndex)?.let(samplePool::stop)
        activeByString.put(stringIndex, voice)?.takeIf { it !== voice }?.stop()
        trimVoiceCache()
        val played = voice.play(inputGain)
        logTriggerLatency(
            inputUptimeMillis,
            triggerNanos,
            timbre,
            midi,
            inputGain,
            inputGain,
            "procedural_fallback",
        )
        return played
    }

    /** Plays a complete beginner chord immediately, low string to high string. */
    @Synchronized
    fun playChord(
        timbre: GuitarTimbre,
        chord: GuitarChord,
        velocity: Float,
        inputUptimeMillis: Long = SystemClock.uptimeMillis(),
    ): Boolean {
        var anyPlayed = false
        chord.midiByString.indices.reversed().forEach { stringIndex ->
            val midi = chord.midiByString[stringIndex]
            if (midi == null) {
                stopString(stringIndex)
            } else {
                val notePlayed =
                    play(
                        timbre = timbre,
                        stringIndex = stringIndex,
                        midi = midi,
                        velocity = velocity.coerceAtLeast(EASY_CHORD_MIN_GAIN),
                        inputUptimeMillis = inputUptimeMillis,
                    )
                anyPlayed = notePlayed || anyPlayed
            }
        }
        return anyPlayed
    }

    @Synchronized
    override fun close() {
        samplePool.close()
        voices.values.toSet().forEach(Voice::close)
        voices.clear()
        samples.clear()
        activeByString.clear()
        activeSampleStreamByString.clear()
    }

    private fun createTrack(pcm: ShortArray): AudioTrack {
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcm.size * Short.SIZE_BYTES)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .build()
            .also { it.write(pcm, 0, pcm.size, AudioTrack.WRITE_BLOCKING) }
    }

    private fun fallbackSampleFor(key: VoiceKey): ShortArray =
        samples.computeIfAbsent(key) { synthesizer.synthesize(it.stringIndex, it.midi) }

    private fun logTriggerLatency(
        inputUptimeMillis: Long,
        engineStartNanos: Long,
        timbre: GuitarTimbre,
        midi: Int,
        inputGain: Float,
        outputGain: Float,
        source: String,
    ) {
        val pointerToCommandMillis = (SystemClock.uptimeMillis() - inputUptimeMillis).coerceAtLeast(0L)
        val engineCommandMicros = (System.nanoTime() - engineStartNanos) / 1_000L
        Log.d(
            TAG,
            "pointer_to_audio_command_ms=$pointerToCommandMillis " +
                "engine_command_us=$engineCommandMicros timbre=${timbre.name} midi=$midi " +
                "input_gain=$inputGain output_gain=$outputGain source=$source",
        )
    }

    private fun trimVoiceCache() {
        if (voices.size <= MAX_CACHED_VOICES) return
        val active = activeByString.values.toSet()
        val iterator = voices.entries.iterator()
        while (voices.size > MAX_CACHED_VOICES && iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value !in active) {
                entry.value.close()
                iterator.remove()
            }
        }
    }

    private fun stopString(stringIndex: Int) {
        activeSampleStreamByString.remove(stringIndex)?.let(samplePool::stop)
        activeByString.remove(stringIndex)?.stop()
    }

    private class Voice(private val track: AudioTrack) : Closeable {
        fun play(velocity: Float): Boolean =
            try {
                track.pause()
                track.setPlaybackHeadPosition(0)
                track.setVolume(velocity)
                track.play()
                true
            } catch (_: IllegalStateException) {
                false
            }

        fun stop() {
            runCatching {
                track.pause()
                track.setPlaybackHeadPosition(0)
            }
        }

        override fun close() {
            track.release()
        }
    }

    private data class VoiceKey(val stringIndex: Int, val midi: Int)

    private companion object {
        const val TAG = "SpatialGuitarAudio"
        const val MIN_GAIN = 0.50f
        const val EASY_CHORD_MIN_GAIN = 0.68f
        const val MAX_CACHED_VOICES = 24
        val OPEN_MIDI = intArrayOf(64, 59, 55, 50, 45, 40)
    }
}

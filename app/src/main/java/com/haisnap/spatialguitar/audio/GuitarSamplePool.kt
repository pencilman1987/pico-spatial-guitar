package com.haisnap.spatialguitar.audio

import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import android.util.Log
import com.haisnap.spatialguitar.domain.model.GuitarTimbre
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

internal class GuitarSamplePool(private val assetManager: AssetManager) : Closeable {
    data class Playback(
        val streamId: Int,
        val appliedGain: Float,
        val assetPath: String,
    )

    private data class LoadedSample(
        val timbre: GuitarTimbre,
        val assetPath: String,
    )

    private val soundPool =
        SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .build()
    private val soundIdByPath = ConcurrentHashMap<String, Int>()
    private val sampleBySoundId = ConcurrentHashMap<Int, LoadedSample>()
    private val loadedSoundIds = ConcurrentHashMap.newKeySet<Int>()
    private val loadedByTimbre =
        GuitarTimbre.entries.associateWith { ConcurrentHashMap.newKeySet<Int>() }
    private val expectedByTimbre =
        GuitarTimbre.entries.associateWith { timbre ->
            GuitarSampleMap.allRegions(timbre)
                .distinctBy { GuitarSampleMap.assetPath(timbre, it) }
                .size
        }
    private val prepareStartedAt = ConcurrentHashMap<GuitarTimbre, Long>()

    init {
        soundPool.setOnLoadCompleteListener { _, soundId, status ->
            if (status == 0) {
                loadedSoundIds += soundId
                val sample = sampleBySoundId[soundId] ?: return@setOnLoadCompleteListener
                val loadedForTimbre = requireNotNull(loadedByTimbre[sample.timbre])
                loadedForTimbre += soundId
                val expected = requireNotNull(expectedByTimbre[sample.timbre])
                if (loadedForTimbre.size == expected) {
                    val elapsed =
                        SystemClock.elapsedRealtime() -
                            (prepareStartedAt[sample.timbre] ?: SystemClock.elapsedRealtime())
                    Log.i(
                        TAG,
                        "timbre_ready=${sample.timbre.name} samples=$expected decode_ms=$elapsed",
                    )
                }
                if (loadedSoundIds.size == expectedByTimbre.values.sum()) {
                    Log.i(TAG, "licensed_samples_ready=${loadedSoundIds.size} timbres=2")
                }
            } else {
                Log.e(TAG, "sample_load_failed status=$status sample=${sampleBySoundId[soundId]}")
            }
        }
    }

    /** Starts asynchronous decoding of both licensed A/B timbres. */
    @Synchronized
    fun prepare() {
        GuitarTimbre.entries.forEach { timbre ->
            prepareStartedAt.putIfAbsent(timbre, SystemClock.elapsedRealtime())
            GuitarSampleMap.allRegions(timbre)
                .distinctBy { GuitarSampleMap.assetPath(timbre, it) }
                .forEach { region ->
                    val path = GuitarSampleMap.assetPath(timbre, region)
                    if (soundIdByPath.containsKey(path)) return@forEach
                    runCatching {
                        assetManager.openFd(path).use { descriptor ->
                            soundPool.load(
                                descriptor.fileDescriptor,
                                descriptor.startOffset,
                                descriptor.length,
                                LOAD_PRIORITY,
                            )
                        }
                    }.onSuccess { soundId ->
                        soundIdByPath[path] = soundId
                        sampleBySoundId[soundId] = LoadedSample(timbre, path)
                    }.onFailure { error ->
                        Log.e(TAG, "sample_open_failed timbre=${timbre.name} path=$path", error)
                    }
                }
        }
    }

    fun play(timbre: GuitarTimbre, midi: Int, velocity: Float): Playback? {
        val sampleSet = GuitarSampleMap.sampleSet(timbre)
        val region = GuitarSampleMap.forMidi(timbre, midi) ?: return null
        val path = GuitarSampleMap.assetPath(timbre, region)
        val soundId = soundIdByPath[path] ?: return null
        if (soundId !in loadedSoundIds) return null
        val rate = 2.0.pow((midi - region.rootMidi) / 12.0).toFloat()
        val appliedGain = (velocity * sampleSet.outputGain).coerceIn(0f, 1f)
        val streamId = soundPool.play(soundId, appliedGain, appliedGain, PLAY_PRIORITY, NO_LOOP, rate)
        if (streamId == PLAY_FAILED) return null
        return Playback(streamId, appliedGain, path)
    }

    fun stop(streamId: Int) {
        if (streamId != PLAY_FAILED) soundPool.stop(streamId)
    }

    override fun close() {
        soundPool.release()
        soundIdByPath.clear()
        sampleBySoundId.clear()
        loadedSoundIds.clear()
        loadedByTimbre.values.forEach(MutableSet<Int>::clear)
        prepareStartedAt.clear()
    }

    private companion object {
        const val TAG = "SpatialGuitarSamples"
        const val MAX_STREAMS = 12
        const val LOAD_PRIORITY = 1
        const val PLAY_PRIORITY = 1
        const val NO_LOOP = 0
        const val PLAY_FAILED = 0
    }
}

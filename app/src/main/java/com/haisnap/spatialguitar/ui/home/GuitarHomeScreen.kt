package com.haisnap.spatialguitar.ui.home

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haisnap.spatialguitar.audio.GuitarAudioEngine
import com.haisnap.spatialguitar.domain.model.FretTarget
import com.haisnap.spatialguitar.domain.model.GuitarTimbre
import com.haisnap.spatialguitar.scene.GuitarRuntime
import com.haisnap.spatialguitar.scene.GuitarSpatialLayout
import com.haisnap.spatialguitar.ui.home.components.GuitarArtwork
import com.haisnap.spatialguitar.ui.home.components.GuitarStatusPanel
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.ui.foundation.content.SpatialView
import com.pico.spatial.ui.foundation.gesture.SpatialPointerInfo
import com.pico.spatial.ui.foundation.gesture.TargetEntity
import com.pico.spatial.ui.foundation.gesture.detectSpatialPointerEvent
import com.pico.spatial.ui.foundation.gesture.data.InteractionKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun GuitarHomeScreen(viewModel: GuitarHomeViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val applicationContext = LocalContext.current.applicationContext
    val audioEngine = remember(applicationContext) { GuitarAudioEngine(applicationContext.assets) }

    DisposableEffect(audioEngine) {
        onDispose(audioEngine::close)
    }

    LaunchedEffect(audioEngine) {
        withContext(Dispatchers.Default) { audioEngine.prepare() }
    }

    val onPlay =
        remember(audioEngine, viewModel, state.timbre) {
            { target: FretTarget, velocity: Float, inputUptimeMillis: Long ->
                val note = viewModel.noteFor(target)
                val played =
                    runCatching {
                        audioEngine.play(
                            timbre = state.timbre,
                            stringIndex = target.stringIndex,
                            midi = note.midi,
                            velocity = velocity,
                            inputUptimeMillis = inputUptimeMillis,
                        )
                    }.getOrDefault(false)
                viewModel.onEvent(
                    if (played) GuitarHomeEvent.Played(note, velocity) else GuitarHomeEvent.AudioFailed
                )
            }
        }

    val onTimbreSelected =
        remember(viewModel) {
            { timbre: GuitarTimbre -> viewModel.onEvent(GuitarHomeEvent.TimbreSelected(timbre)) }
        }

    GuitarHomeContent(state = state, onPlay = onPlay, onTimbreSelected = onTimbreSelected)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun GuitarHomeContent(
    state: GuitarHomeUiState,
    onPlay: (FretTarget, Float, Long) -> Unit,
    onTimbreSelected: (GuitarTimbre) -> Unit,
) {
    val context = LocalContext.current
    var runtime by remember { mutableStateOf<GuitarRuntime?>(null) }
    val currentOnPlay by rememberUpdatedState(onPlay)

    DisposableEffect(Unit) {
        onDispose {
            runtime?.close()
            runtime = null
        }
    }

    SpatialView(
        modifier =
            Modifier.fillMaxSize().pointerInput(Unit) {
                detectSpatialPointerEvent(
                    context = context,
                    targetedToEntity = TargetEntity.any { it.getName().startsWith("guitar_s") },
                    onEvent =
                        guitarPointerHandler { entity, velocity, inputUptimeMillis ->
                            runtime?.strike(entity, velocity, inputUptimeMillis)
                        },
                )
            },
        initial = { content, attachments ->
            runtime =
                GuitarRuntime(
                    rootPosition = Vector3(0f, GuitarSpatialLayout.ROOT_Y, 0f),
                    onPlayed = { target, velocity, inputUptimeMillis ->
                        currentOnPlay(target, velocity, inputUptimeMillis)
                    },
                ).also { guitar ->
                    attachments.entity("guitar_art")?.let { artwork ->
                        artwork.components[TransformComponent::class.java]?.setPosition(
                            Vector3(0f, GuitarSpatialLayout.ROOT_Y, GuitarSpatialLayout.ARTWORK_Z)
                        )
                        content.addEntity(artwork)
                    }
                    content.addEntity(guitar.root)
                    attachments.entity("guitar_status")?.let { statusPanel ->
                        statusPanel.components[TransformComponent::class.java]?.setPosition(
                            Vector3(
                                GuitarSpatialLayout.STATUS_X,
                                GuitarSpatialLayout.STATUS_Y,
                                GuitarSpatialLayout.STATUS_Z,
                            )
                        )
                        content.addEntity(statusPanel)
                    }
                }
        },
        attachments = {
            AttachmentPanel(id = "guitar_art") {
                GuitarArtwork()
            }
            AttachmentPanel(id = "guitar_status") {
                GuitarStatusPanel(state = state, onTimbreSelected = onTimbreSelected)
            }
        },
    )
}

private fun guitarPointerHandler(onTarget: (Entity, Float, Long) -> Unit): (List<SpatialPointerInfo>) -> Boolean {
    val strikeDetector = GuitarPointerStrikeDetector<Entity>()
    return { events ->
        events.forEach { event ->
            val pointerKey = event.pointerId.toString()
            val motion =
                if (event.isUpEvent() || !event.pressed) {
                    null
                } else {
                    // inputDevicePose is in Spatial space (meters). event.x/y are
                    // Compose pixels and must never be used as meter distances.
                    val devicePosition = event.inputDevicePose.rawPosition
                    GuitarGestureVelocity.worldSampleOrNull(
                        xMeters = devicePosition.x,
                        yMeters = devicePosition.y,
                        zMeters = devicePosition.z,
                        uptimeMillis = event.uptimeMillis,
                    )
                }

            strikeDetector
                .update(
                    pointerKey = pointerKey,
                    pressed = event.pressed,
                    isUpEvent = event.isUpEvent(),
                    target = event.targetedEntity,
                    motion = motion,
                    isPoke = event.kind == InteractionKind.Poke,
                    uptimeMillis = event.uptimeMillis,
                )
                ?.let { strike ->
                    onTarget(strike.target, strike.gain, strike.inputUptimeMillis)
                    // Audio has already been commanded by onTarget; telemetry
                    // stays outside the latency-critical portion of the hit.
                    Log.d(
                        INPUT_LOG_TAG,
                        "direction=${strike.direction} " +
                            "string_normal_speed_mps=${strike.speedMetersPerSecond} " +
                            "gain=${strike.gain} target=${strike.target.getName()}",
                    )
                }
        }
        true
    }
}

private const val INPUT_LOG_TAG = "SpatialGuitarInput"

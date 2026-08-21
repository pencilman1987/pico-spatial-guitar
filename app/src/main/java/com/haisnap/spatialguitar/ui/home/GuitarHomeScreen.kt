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
import androidx.compose.ui.platform.LocalDensity
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
import com.pico.spatial.ui.foundation.gesture.detectSpatialDragGesture
import com.pico.spatial.ui.foundation.gesture.data.InteractionKind
import com.pico.spatial.ui.platform.LengthUnit
import com.pico.spatial.ui.platform.LocalPhysicalLengthConverter
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
    val onMoveModeChanged =
        remember(viewModel) {
            { enabled: Boolean -> viewModel.onEvent(GuitarHomeEvent.MoveModeChanged(enabled)) }
        }

    GuitarHomeContent(
        state = state,
        onPlay = onPlay,
        onTimbreSelected = onTimbreSelected,
        onMoveModeChanged = onMoveModeChanged,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun GuitarHomeContent(
    state: GuitarHomeUiState,
    onPlay: (FretTarget, Float, Long) -> Unit,
    onTimbreSelected: (GuitarTimbre) -> Unit,
    onMoveModeChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val physicalLengthConverter = LocalPhysicalLengthConverter.current
    var sceneHandles by remember { mutableStateOf<GuitarSceneHandles?>(null) }
    var placement by remember { mutableStateOf(GuitarPlacement.Centered) }
    val currentOnPlay by rememberUpdatedState(onPlay)
    val pixelsToMeters =
        remember(density, physicalLengthConverter) {
            { pixels: Float ->
                val dp = with(density) { pixels.toDp() }
                physicalLengthConverter.dpToLength(dp, LengthUnit.Meters)
            }
        }

    DisposableEffect(Unit) {
        onDispose {
            sceneHandles?.runtime?.close()
            sceneHandles = null
        }
    }

    SpatialView(
        modifier =
            Modifier.fillMaxSize().pointerInput(state.isMoveMode, pixelsToMeters) {
                if (state.isMoveMode) {
                    detectSpatialDragGesture(
                        context = context,
                        targetedToEntity = TargetEntity.any(::isGuitarMoveTarget),
                    ) { drag ->
                        // Drag is in view pixels (+Y down); ECS is meters (+Y up).
                        placement =
                            placement.movedBy(
                                deltaXMeters = pixelsToMeters(drag.dragAmount.x),
                                deltaYMeters = -pixelsToMeters(drag.dragAmount.y),
                                deltaZMeters = pixelsToMeters(drag.dragAmount.z),
                            )
                        sceneHandles?.setPlacement(placement)
                        Log.d(
                            PLACEMENT_LOG_TAG,
                            "x_m=${placement.xMeters} y_m=${placement.yMeters} z_m=${placement.zMeters}",
                        )
                    }
                } else {
                    detectSpatialPointerEvent(
                        context = context,
                        targetedToEntity = TargetEntity.any { it.getName().startsWith("guitar_s") },
                        onEvent =
                            guitarPointerHandler { entity, velocity, inputUptimeMillis ->
                                sceneHandles?.runtime?.strike(entity, velocity, inputUptimeMillis)
                            },
                    )
                }
            },
        initial = { content, attachments ->
            val guitar =
                GuitarRuntime(
                    rootPosition = Vector3(0f, GuitarSpatialLayout.ROOT_Y, 0f),
                    onPlayed = { target, velocity, inputUptimeMillis ->
                        currentOnPlay(target, velocity, inputUptimeMillis)
                    },
                )
            val artwork = attachments.entity("guitar_art")?.also {
                it.setName(ARTWORK_ENTITY_NAME)
                content.addEntity(it)
            }
            content.addEntity(guitar.root)
            val statusPanel = attachments.entity("guitar_status")?.also(content::addEntity)
            sceneHandles = GuitarSceneHandles(guitar, artwork, statusPanel).also {
                it.setPlacement(placement)
            }
        },
        attachments = {
            AttachmentPanel(id = "guitar_art") {
                GuitarArtwork()
            }
            AttachmentPanel(id = "guitar_status") {
                GuitarStatusPanel(
                    state = state,
                    onTimbreSelected = onTimbreSelected,
                    onMoveModeChanged = onMoveModeChanged,
                    onCenterRequested = {
                        placement = GuitarPlacement.Centered
                        sceneHandles?.setPlacement(placement)
                        Log.d(PLACEMENT_LOG_TAG, "centered")
                    },
                )
            }
        },
    )
}

private class GuitarSceneHandles(
    val runtime: GuitarRuntime,
    private val artwork: Entity?,
    private val statusPanel: Entity?,
) {
    fun setPlacement(placement: GuitarPlacement) {
        runtime.setPosition(
            Vector3(
                placement.xMeters,
                GuitarSpatialLayout.ROOT_Y + placement.yMeters,
                placement.zMeters,
            )
        )
        artwork?.components?.get(TransformComponent::class.java)?.setPosition(
            Vector3(
                placement.xMeters,
                GuitarSpatialLayout.ROOT_Y + placement.yMeters,
                GuitarSpatialLayout.ARTWORK_Z + placement.zMeters,
            )
        )
        statusPanel?.components?.get(TransformComponent::class.java)?.setPosition(
            Vector3(
                GuitarSpatialLayout.STATUS_X + placement.xMeters,
                GuitarSpatialLayout.STATUS_Y + placement.yMeters,
                GuitarSpatialLayout.STATUS_Z + placement.zMeters,
            )
        )
    }
}

private fun isGuitarMoveTarget(entity: Entity): Boolean {
    val name = entity.getName()
    return name == GuitarRuntime.MOVE_SURFACE_NAME ||
        name == ARTWORK_ENTITY_NAME ||
        name.startsWith("guitar_s")
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
private const val PLACEMENT_LOG_TAG = "SpatialGuitarPlacement"
private const val ARTWORK_ENTITY_NAME = "guitar_art"

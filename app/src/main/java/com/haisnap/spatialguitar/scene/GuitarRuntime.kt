package com.haisnap.spatialguitar.scene

import android.os.Handler
import android.os.Looper
import com.haisnap.spatialguitar.domain.model.FretTarget
import com.pico.spatial.core.ecs.CollisionComponent
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.HoverEffectComponent
import com.pico.spatial.core.ecs.InteractableComponent
import com.pico.spatial.core.ecs.ModelComponent
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.ecs.resource.BlendingMode
import com.pico.spatial.core.ecs.resource.MeshResource
import com.pico.spatial.core.ecs.resource.PhysicsMaterialResource
import com.pico.spatial.core.ecs.resource.ShapeResource
import com.pico.spatial.core.ecs.resource.UnlitMaterial
import com.pico.spatial.core.math.Color4
import com.pico.spatial.core.math.Vector3
import java.io.Closeable
import java.util.IdentityHashMap

/**
 * Interactive overlay for the generated guitar artwork.
 *
 * The bitmap owns the instrument silhouette, wood, fretboard and hardware.
 * ECS owns only the six playable strings, their simple colliders and the
 * short-lived sound-hole pulse. This keeps the visual proportions realistic
 * without changing the original 6 x 16 play grid.
 */
class GuitarRuntime(
    rootPosition: Vector3,
    private val onPlayed: (FretTarget, Float, Long) -> Unit,
) : Closeable {
    val root = Entity()

    private val handler = Handler(Looper.getMainLooper())
    private val resources = mutableListOf<AutoCloseable>()
    private val targets = IdentityHashMap<Entity, FretTarget>()
    private val stringVisuals = IdentityHashMap<Entity, StringVisual>()
    private val lastHitAt = mutableMapOf<FretTarget, Long>()
    private var resonanceVisual: ResonanceVisual? = null

    init {
        root.setName("guitar_interaction_root")
        root.components[TransformComponent::class.java]?.apply {
            setPosition(rootPosition)
            setScaleVector(
                Vector3(
                    GuitarSpatialLayout.ROOT_SCALE,
                    GuitarSpatialLayout.ROOT_SCALE,
                    GuitarSpatialLayout.ROOT_SCALE,
                )
            )
        }
        addMoveSurface()
        addSoundHolePulse()
        addStringsAndTargets()
    }

    fun setPosition(position: Vector3) {
        root.components[TransformComponent::class.java]?.setPosition(position)
    }

    fun strike(entity: Entity, velocity: Float, inputUptimeMillis: Long): Boolean {
        val target = targets[entity] ?: return false
        if (inputUptimeMillis - (lastHitAt[target] ?: Long.MIN_VALUE / 2) < HIT_COOLDOWN_MS) return false
        lastHitAt[target] = inputUptimeMillis

        val strength = velocity.coerceIn(0.18f, 1f)
        onPlayed(target, strength, inputUptimeMillis)
        stringVisuals[entity]?.let(::animateString)
        resonanceVisual?.let(::animateResonance)
        return true
    }

    private fun addSoundHolePulse() {
        val mesh = MeshResource.createSphere(0.037f)
        val material = UnlitMaterial.create(BlendingMode.OPAQUE).apply { setBaseColor(SOUND_HOLE) }
        resources += mesh
        resources += material

        val entity =
            Entity().apply {
                setName("sound_hole_pulse")
                components.set(ModelComponent(mesh, material))
                components[TransformComponent::class.java]?.apply {
                    setPosition(Vector3(SOUND_HOLE_X, 0f, GuitarSpatialLayout.SOUND_HOLE_Z))
                    setScaleVector(Vector3(1f, 1f, 0.08f))
                }
            }
        root.addChild(entity)
        resonanceVisual =
            ResonanceVisual(
                transform = requireNotNull(entity.components[TransformComponent::class.java]),
                material = material,
            )
    }

    /**
     * Invisible collider behind the strings. It is only targeted while move mode
     * is active, giving the body and neck one continuous grab surface.
     */
    private fun addMoveSurface() {
        val shape = ShapeResource.createBox(Vector3(0.82f, 0.32f, 0.003f))
        val physicsMaterial = PhysicsMaterialResource()
        resources += shape
        resources += physicsMaterial

        root.addChild(
            Entity().apply {
                setName(MOVE_SURFACE_NAME)
                components.set(InteractableComponent())
                components.set(
                    CollisionComponent(
                        collisionShape = listOf(shape),
                        physicsMaterial = physicsMaterial,
                    )
                )
                components[TransformComponent::class.java]?.setPosition(
                    Vector3(-0.02f, 0f, MOVE_SURFACE_Z)
                )
            }
        )
    }

    private fun addStringsAndTargets() {
        val segments = segmentBounds()
        val targetShape = ShapeResource.createBox(Vector3(1f, GuitarSpatialLayout.STRING_HIT_HEIGHT, 0.014f))
        val physicsMaterial = PhysicsMaterialResource()
        resources += targetShape
        resources += physicsMaterial

        for (stringIndex in 0 until STRING_COUNT) {
            val y = GuitarSpatialLayout.stringY(stringIndex)
            val thickness = 0.0012f + stringIndex * 0.00018f
            val restingColor = if (stringIndex < 3) SILVER_STRING else BRONZE_STRING

            val stringVisual =
                addVisibleString(
                    name = "string_visual_$stringIndex",
                    startX = NECK_START,
                    endX = BRIDGE_X,
                    y = y,
                    thickness = thickness,
                    color = restingColor,
                )
            segments.forEachIndexed { fret, bounds ->
                val width = bounds.second - bounds.first
                val target = FretTarget(stringIndex, fret)
                val entity =
                    Entity().apply {
                        setName("guitar_s${stringIndex}_f$fret")
                        components.set(InteractableComponent())
                        components.set(HoverEffectComponent())
                        components.set(
                            CollisionComponent(
                                collisionShape = listOf(targetShape),
                                physicsMaterial = physicsMaterial,
                            )
                        )
                        components[TransformComponent::class.java]?.apply {
                            setPosition(
                                Vector3(
                                    (bounds.first + bounds.second) * 0.5f,
                                    y,
                                    GuitarSpatialLayout.STRING_Z,
                                )
                            )
                            setScaleVector(Vector3(width, 1f, 1f))
                        }
                    }
                root.addChild(entity)
                targets[entity] = target
                stringVisuals[entity] = stringVisual
            }
        }
    }

    private fun addVisibleString(
        name: String,
        startX: Float,
        endX: Float,
        y: Float,
        thickness: Float,
        color: Color4,
    ): StringVisual {
        val width = endX - startX
        val mesh = MeshResource.createBox(Vector3(width, thickness, 0.004f), thickness)
        val material = UnlitMaterial.create(BlendingMode.OPAQUE).apply { setBaseColor(color) }
        resources += mesh
        resources += material
        val entity =
            Entity().apply {
                setName(name)
                components.set(ModelComponent(mesh, material))
                components[TransformComponent::class.java]?.setPosition(
                    Vector3((startX + endX) * 0.5f, y, GuitarSpatialLayout.STRING_Z)
                )
            }
        root.addChild(entity)
        return StringVisual(
            transform = requireNotNull(entity.components[TransformComponent::class.java]),
            material = material,
            restingColor = color,
        )
    }

    private fun animateString(visual: StringVisual) {
        val animationId = ++visual.animationId
        visual.transform.setScaleVector(Vector3(1f, 2.4f, 1.8f))
        visual.material.setBaseColor(ACTIVE_BLUE)
        handler.postDelayed(
            {
                if (visual.animationId == animationId) {
                    visual.transform.setScaleVector(Vector3(1f, 1f, 1f))
                    visual.material.setBaseColor(visual.restingColor)
                }
            },
            150L,
        )
    }

    private fun animateResonance(visual: ResonanceVisual) {
        val animationId = ++visual.animationId
        visual.transform.setScaleVector(Vector3(1.16f, 1.16f, 0.08f))
        visual.material.setBaseColor(ACTIVE_BLUE)
        handler.postDelayed(
            {
                if (visual.animationId == animationId) {
                    visual.transform.setScaleVector(Vector3(1f, 1f, 0.08f))
                    visual.material.setBaseColor(SOUND_HOLE)
                }
            },
            180L,
        )
    }

    private fun segmentBounds(): List<Pair<Float, Float>> {
        return GuitarFretLayout.segmentBounds(
            neckStart = NECK_START,
            nutX = NUT_X,
            bridgeX = BRIDGE_X,
            fretCount = FRET_COUNT,
        )
    }

    override fun close() {
        handler.removeCallbacksAndMessages(null)
        root.destroy(recursively = true)
        resources.asReversed().forEach { runCatching { it.close() } }
        resources.clear()
    }

    private data class StringVisual(
        val transform: TransformComponent,
        val material: UnlitMaterial,
        val restingColor: Color4,
        var animationId: Long = 0L,
    )

    private data class ResonanceVisual(
        val transform: TransformComponent,
        val material: UnlitMaterial,
        var animationId: Long = 0L,
    )

    companion object {
        const val MOVE_SURFACE_NAME = "guitar_move_surface"

        const val STRING_COUNT = 6
        const val FRET_COUNT = 16
        const val NECK_START = -0.385f
        const val NUT_X = -0.291f
        const val BRIDGE_X = 0.250f
        const val SOUND_HOLE_X = 0.140f
        const val HIT_COOLDOWN_MS = 55L
        const val MOVE_SURFACE_Z = 0.0008f

        val SOUND_HOLE = Color4(0.008f, 0.006f, 0.004f, 1f)
        val SILVER_STRING = Color4(0.90f, 0.91f, 0.94f, 1f)
        val BRONZE_STRING = Color4(0.72f, 0.45f, 0.16f, 1f)
        val ACTIVE_BLUE = Color4(0f, 0.44f, 0.89f, 1f)
    }
}

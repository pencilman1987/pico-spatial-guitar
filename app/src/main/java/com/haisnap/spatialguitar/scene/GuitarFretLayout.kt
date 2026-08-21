package com.haisnap.spatialguitar.scene

import kotlin.math.pow

/** Maps the source app's open-string target plus frets 1..15 onto a real scale. */
internal object GuitarFretLayout {
    fun segmentBounds(
        neckStart: Float,
        nutX: Float,
        bridgeX: Float,
        fretCount: Int,
    ): List<Pair<Float, Float>> {
        require(neckStart < nutX && nutX < bridgeX)
        require(fretCount >= 2)

        val scaleLength = bridgeX - nutX
        val segments = ArrayList<Pair<Float, Float>>(fretCount)
        segments += neckStart to nutX
        for (fret in 1 until fretCount) {
            val start = fretPosition(nutX, scaleLength, fret - 1)
            val end = fretPosition(nutX, scaleLength, fret)
            segments += start to end
        }
        return segments
    }

    private fun fretPosition(nutX: Float, scaleLength: Float, fret: Int): Float =
        nutX + scaleLength * (1.0 - 2.0.pow(-fret / 12.0)).toFloat()
}

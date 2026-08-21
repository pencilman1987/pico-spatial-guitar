package com.haisnap.spatialguitar.data.repository

import com.haisnap.spatialguitar.domain.model.GuitarStringSpec
import com.haisnap.spatialguitar.domain.model.StringFinish

class DefaultGuitarRepository : GuitarRepository {
    private val standardTuning =
        listOf(
            GuitarStringSpec(0, "高音 E", 64, StringFinish.SILVER),
            GuitarStringSpec(1, "B", 59, StringFinish.SILVER),
            GuitarStringSpec(2, "G", 55, StringFinish.SILVER),
            GuitarStringSpec(3, "D", 50, StringFinish.BRONZE),
            GuitarStringSpec(4, "A", 45, StringFinish.BRONZE),
            GuitarStringSpec(5, "低音 E", 40, StringFinish.BRONZE),
        )

    override fun strings(): List<GuitarStringSpec> = standardTuning
}

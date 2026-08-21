package com.haisnap.spatialguitar.data.repository

import com.haisnap.spatialguitar.domain.model.GuitarStringSpec

interface GuitarRepository {
    fun strings(): List<GuitarStringSpec>
}

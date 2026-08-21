package com.haisnap.spatialguitar.domain.model

enum class GuitarTimbre(
    val abLabel: String,
    val displayName: String,
    val technicalName: String,
) {
    NYLON("A", "尼龙弦", "FreePats Nylon"),
    STEEL("B", "钢弦", "Martin HD28 Steel"),
}

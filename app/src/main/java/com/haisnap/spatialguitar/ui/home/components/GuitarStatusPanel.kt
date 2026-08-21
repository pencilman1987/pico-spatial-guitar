package com.haisnap.spatialguitar.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.haisnap.spatialguitar.ui.home.GuitarHomeUiState
import com.haisnap.spatialguitar.domain.model.GuitarTimbre
import com.pico.spatial.ui.design.ChipsDefaults
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.design.ToggleableChip
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material

@Composable
fun GuitarStatusPanel(
    state: GuitarHomeUiState,
    onTimbreSelected: (GuitarTimbre) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .size(620.dp, 205.dp)
                .clip(RoundedCornerShape(24.dp))
                .backgroundMaterial(enable = true, style = Material.Regular)
                .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "STANDARD E", style = PicoTheme.typography.labelSmall)
            GuitarTimbre.entries.forEach { timbre ->
                ToggleableChip(
                    label = { Text("${timbre.abLabel} ${timbre.displayName}") },
                    isToggleOn = state.timbre == timbre,
                    onClick = { onTimbreSelected(timbre) },
                    chipSize = ChipsDefaults.Small,
                )
            }
        }
        Text(text = state.activeNote?.name ?: "READY", style = PicoTheme.typography.titleLarge)
        Text(
            text =
                state.activeNote?.let {
                    "第 ${it.target.stringIndex + 1} 弦  ·  第 ${it.target.fret} 品  ·  ${(state.velocity * 100).toInt()}%"
                } ?: "${state.timbre.technicalName} · 横跨琴弦扫弦",
            style = PicoTheme.typography.bodyMedium,
        )
    }
}

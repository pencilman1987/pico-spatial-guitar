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
import com.haisnap.spatialguitar.domain.model.GuitarChord
import com.haisnap.spatialguitar.domain.model.GuitarPlayMode
import com.haisnap.spatialguitar.ui.home.GuitarHomeUiState
import com.haisnap.spatialguitar.domain.model.GuitarTimbre
import com.pico.spatial.ui.design.ChipsDefaults
import com.pico.spatial.ui.design.ButtonChip
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.design.ToggleableChip
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material

@Composable
fun GuitarStatusPanel(
    state: GuitarHomeUiState,
    onTimbreSelected: (GuitarTimbre) -> Unit,
    onPlayModeSelected: (GuitarPlayMode) -> Unit,
    onChordSelected: (GuitarChord) -> Unit,
    onMoveModeChanged: (Boolean) -> Unit,
    onCenterRequested: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .size(760.dp, 250.dp)
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
            Text(text = "PLAY & SING", style = PicoTheme.typography.labelSmall)
            GuitarPlayMode.entries.forEach { mode ->
                ToggleableChip(
                    label = {
                        Text(if (mode == GuitarPlayMode.ACCOMPANIMENT) "伴奏" else "单音")
                    },
                    isToggleOn = state.playMode == mode,
                    onClick = { onPlayModeSelected(mode) },
                    chipSize = ChipsDefaults.Small,
                )
            }
            GuitarTimbre.entries.forEach { timbre ->
                ToggleableChip(
                    label = { Text("${timbre.abLabel} ${timbre.displayName}") },
                    isToggleOn = state.timbre == timbre,
                    onClick = { onTimbreSelected(timbre) },
                    chipSize = ChipsDefaults.Small,
                )
            }
            ToggleableChip(
                label = { Text(if (state.isMoveMode) "移动中" else "移动") },
                isToggleOn = state.isMoveMode,
                onClick = { onMoveModeChanged(!state.isMoveMode) },
                chipSize = ChipsDefaults.Small,
            )
            ButtonChip(
                label = { Text("居中") },
                onClick = onCenterRequested,
                chipSize = ChipsDefaults.Small,
            )
        }
        if (state.playMode == GuitarPlayMode.ACCOMPANIMENT) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "和弦", style = PicoTheme.typography.labelSmall)
                GuitarChord.entries.forEach { chord ->
                    ToggleableChip(
                        label = { Text(chord.displayName) },
                        isToggleOn = state.selectedChord == chord,
                        onClick = { onChordSelected(chord) },
                        chipSize = ChipsDefaults.Small,
                    )
                }
            }
        }
        Text(
            text =
                when {
                    state.isMoveMode -> "MOVE"
                    state.playMode == GuitarPlayMode.ACCOMPANIMENT -> state.selectedChord.displayName
                    else -> state.activeNote?.name ?: "READY"
                },
            style = PicoTheme.typography.titleLarge,
        )
        Text(
            text =
                if (state.isMoveMode) {
                    "拖动琴身调整位置 · 完成后关闭移动"
                } else if (state.playMode == GuitarPlayMode.ACCOMPANIMENT) {
                    "大范围扫过音孔 · 轻触也会发声 · 上下往返即可伴奏"
                } else {
                    state.activeNote?.let {
                        "第 ${it.target.stringIndex + 1} 弦  ·  第 ${it.target.fret} 品  ·  ${(state.velocity * 100).toInt()}%"
                    } ?: "${state.timbre.technicalName} · 横跨琴弦扫弦"
                },
            style = PicoTheme.typography.bodyMedium,
        )
    }
}

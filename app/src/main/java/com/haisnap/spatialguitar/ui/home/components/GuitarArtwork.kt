package com.haisnap.spatialguitar.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.haisnap.spatialguitar.R

@Composable
fun GuitarArtwork(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.acoustic_guitar_front_v1),
        contentDescription = null,
        modifier = Modifier.size(width = 1700.dp, height = 708.dp).then(modifier),
        contentScale = ContentScale.Fit,
    )
}

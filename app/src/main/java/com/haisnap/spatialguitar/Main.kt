package com.haisnap.spatialguitar

import com.haisnap.spatialguitar.ui.home.GuitarHomeScreen
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.dsl.DefaultWindowContainer
import com.pico.spatial.ui.foundation.dsl.SpatialAppScope

fun mainApp(scope: SpatialAppScope) =
    with(scope) {
        DefaultWindowContainer {
            PicoTheme {
                GuitarHomeScreen()
            }
        }
    }

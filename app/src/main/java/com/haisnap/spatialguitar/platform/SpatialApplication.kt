package com.haisnap.spatialguitar.platform

import android.app.Application
import com.pico.spatial.ui.foundation.dsl.launch
import com.haisnap.spatialguitar.mainApp

class SpatialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        launch(::mainApp)
    }
}

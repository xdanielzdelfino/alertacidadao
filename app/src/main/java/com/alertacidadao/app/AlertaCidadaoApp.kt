package com.alertacidadao.app

import android.app.Application
import org.osmdroid.config.Configuration
import java.io.File

class AlertaCidadaoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val osmdroidConfig = Configuration.getInstance()
        osmdroidConfig.userAgentValue = packageName
        osmdroidConfig.osmdroidBasePath = File(filesDir, "osmdroid")
        osmdroidConfig.osmdroidTileCache = File(osmdroidConfig.osmdroidBasePath, "tiles")
        osmdroidConfig.load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
    }
}
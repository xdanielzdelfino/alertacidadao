package com.alertacidadao.app.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.alertacidadao.app.R

fun AppCompatActivity.setupSystemBars() {
    window.statusBarColor = ContextCompat.getColor(this, R.color.primary)
    window.navigationBarColor = ContextCompat.getColor(this, R.color.auth_background)
    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = false
        isAppearanceLightNavigationBars = true
    }
}
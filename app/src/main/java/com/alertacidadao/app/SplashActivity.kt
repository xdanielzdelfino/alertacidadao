package com.alertacidadao.app

import android.content.Intent
import android.os.Bundle
import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.alertacidadao.app.data.AuthRepository

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val nextActivity = if (AuthRepository.isLoggedIn(this)) {
                MainActivity::class.java
            } else {
                LoginActivity::class.java
            }

            startActivity(Intent(this, nextActivity))
            finish()
        }, 1200)
    }
}
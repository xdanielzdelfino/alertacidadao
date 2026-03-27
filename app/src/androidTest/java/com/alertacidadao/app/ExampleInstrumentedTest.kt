package com.alertacidadao.app

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Teste instrumentado, que será executado em um dispositivo Android.
 *
 * Veja a documentação de testes: http://d.android.com/tools/testing
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Contexto do app sob teste.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.alertacidadao.app", appContext.packageName)
    }
}
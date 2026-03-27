package com.alertacidadao.app.ui

import com.alertacidadao.app.R

import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.bindFigmaPasswordToggle(passwordField: EditText, iconResId: Int) {
    endIconMode = TextInputLayout.END_ICON_CUSTOM

    var showingPassword = false

    fun updateEndIcon() {
        val res = if (showingPassword) android.R.drawable.ic_menu_view else android.R.drawable.ic_menu_view
        // preferir ícones de visibilidade do projeto quando disponíveis
        val drawable = ContextCompat.getDrawable(context, if (showingPassword) R.drawable.ic_visibility else R.drawable.ic_visibility_off)
        endIconDrawable = drawable
    }

    showingPassword = false
    updateEndIcon()

    passwordField.transformationMethod = PasswordTransformationMethod.getInstance()
    passwordField.setSelection(passwordField.text?.length ?: 0)

    setEndIconOnClickListener {
        showingPassword = !showingPassword
        passwordField.transformationMethod = if (showingPassword) null else PasswordTransformationMethod.getInstance()
        passwordField.setSelection(passwordField.text?.length ?: 0)
        updateEndIcon()
    }
}
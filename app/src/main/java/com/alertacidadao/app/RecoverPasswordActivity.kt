package com.alertacidadao.app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alertacidadao.app.data.AuthRepository
import com.alertacidadao.app.databinding.ActivityRecoverPasswordBinding
import com.alertacidadao.app.ui.setupSystemBars
import com.alertacidadao.app.ui.bindFigmaPasswordToggle

class RecoverPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecoverPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSystemBars()

        binding = ActivityRecoverPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.layoutPassword.bindFigmaPasswordToggle(binding.inputPassword, R.drawable.figma_recover_eye)

        binding.buttonBack.setOnClickListener { finish() }

        binding.buttonResetPassword.setOnClickListener { resetPassword() }
        binding.textLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun resetPassword() {
        val email = binding.inputEmail.text?.toString().orEmpty().trim()
        val newPassword = binding.inputPassword.text?.toString().orEmpty()

        binding.layoutEmail.error = null
        binding.layoutPassword.error = null

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.error = "Informe um e-mail válido"
            return
        }

        if (newPassword.length < 6) {
            binding.layoutPassword.error = "A senha precisa ter 6 caracteres ou mais"
            return
        }

        val updated = AuthRepository.resetPassword(this, email, newPassword)
        if (!updated) {
            Toast.makeText(this, "E-mail não encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Senha atualizada com sucesso", Toast.LENGTH_SHORT).show()
        finish()
    }
}
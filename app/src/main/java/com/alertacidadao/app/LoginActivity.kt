package com.alertacidadao.app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alertacidadao.app.data.AuthRepository
import com.alertacidadao.app.databinding.ActivityLoginBinding
import com.alertacidadao.app.ui.setupSystemBars
import com.alertacidadao.app.ui.bindFigmaPasswordToggle

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSystemBars()

        if (AuthRepository.isLoggedIn(this)) {
            goToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.layoutPassword.bindFigmaPasswordToggle(binding.inputPassword, R.drawable.figma_login_eye)

        binding.buttonLogin.setOnClickListener { attemptLogin() }
        binding.textCreateAccount.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }
        binding.textForgotPassword.setOnClickListener {
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = binding.inputEmail.text?.toString().orEmpty().trim()
        val password = binding.inputPassword.text?.toString().orEmpty()

        binding.layoutEmail.error = null
        binding.layoutPassword.error = null

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.error = "Informe um e-mail válido"
            return
        }

        if (password.length < 6) {
            binding.layoutPassword.error = "Informe uma senha com 6 caracteres ou mais"
            return
        }

        if (!AuthRepository.authenticate(this, email, password)) {
            Toast.makeText(this, "E-mail ou senha inválidos", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Login realizado com sucesso", Toast.LENGTH_SHORT).show()
        goToMain()
    }

    private fun goToMain() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }
}
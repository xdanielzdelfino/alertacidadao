package com.alertacidadao.app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alertacidadao.app.data.AuthRepository
import com.alertacidadao.app.databinding.ActivityCreateAccountBinding
import com.alertacidadao.app.ui.setupSystemBars
import com.alertacidadao.app.ui.bindFigmaPasswordToggle

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSystemBars()

        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.layoutPassword.bindFigmaPasswordToggle(binding.inputPassword, R.drawable.figma_create_eye)
        binding.layoutConfirmPassword.bindFigmaPasswordToggle(binding.inputConfirmPassword, R.drawable.figma_create_eye)

        binding.buttonBack.setOnClickListener { finish() }
        binding.textLogin.setOnClickListener { finish() }

        binding.buttonCreateAccount.setOnClickListener { createAccount() }
    }

    private fun createAccount() {
        val name = binding.inputName.text?.toString().orEmpty().trim()
        val neighborhood = binding.inputNeighborhood.text?.toString().orEmpty().trim()
        val email = binding.inputEmail.text?.toString().orEmpty().trim()
        val password = binding.inputPassword.text?.toString().orEmpty()
        val confirmPassword = binding.inputConfirmPassword.text?.toString().orEmpty()

        binding.layoutEmail.error = null
        binding.layoutPassword.error = null
        binding.layoutConfirmPassword.error = null

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.error = "Informe um e-mail válido"
            return
        }

        if (password.length < 6) {
            binding.layoutPassword.error = "A senha precisa ter 6 caracteres ou mais"
            return
        }

        if (password != confirmPassword) {
            binding.layoutConfirmPassword.error = "As senhas não conferem"
            return
        }

        if (!binding.checkTerms.isChecked) {
            Toast.makeText(this, "Aceite os termos para continuar", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isBlank()) {
            binding.layoutName.error = "Informe seu nome completo"
            return
        }

        val created = AuthRepository.createAccount(this, email, password, name = name, neighborhood = neighborhood)
        if (!created) {
            Toast.makeText(this, "Não foi possível criar a conta", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Conta criada com sucesso", Toast.LENGTH_SHORT).show()
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }
}
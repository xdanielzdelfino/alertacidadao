package com.alertacidadao.app.data

import android.content.Context
import android.util.Patterns
import java.security.MessageDigest

object AuthRepository {

    private const val PREFS_NAME = "alerta_cidadao_auth"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD_HASH = "password_hash"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_NAME = "name"
    private const val KEY_NEIGHBORHOOD = "neighborhood"

    fun hasAccount(context: Context): Boolean =
        getEmail(context) != null && getPasswordHash(context) != null

    fun isLoggedIn(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_LOGGED_IN, false)

    fun getEmail(context: Context): String? =
        getPrefs(context).getString(KEY_EMAIL, null)

    fun createAccount(context: Context, email: String, password: String, name: String? = null, neighborhood: String? = null): Boolean {
        val normalizedEmail = email.trim().lowercase()
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) return false
        if (password.length < 6) return false

        val prefs = getPrefs(context)
        val existingEmail = prefs.getString(KEY_EMAIL, null)
        if (existingEmail != null && existingEmail != normalizedEmail) return false

        prefs.edit()
            .putString(KEY_EMAIL, normalizedEmail)
            .putString(KEY_PASSWORD_HASH, hash(password))
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()

        // campos de perfil opcionais
        name?.takeIf { it.isNotBlank() }?.let { prefs.edit().putString(KEY_NAME, it.trim()).apply() }
        neighborhood?.takeIf { it.isNotBlank() }?.let { prefs.edit().putString(KEY_NEIGHBORHOOD, it.trim()).apply() }
        return true
    }

    fun authenticate(context: Context, email: String, password: String): Boolean {
        val normalizedEmail = email.trim().lowercase()
        val storedEmail = getEmail(context) ?: return false
        val storedPasswordHash = getPasswordHash(context) ?: return false

        val isValid = storedEmail == normalizedEmail && storedPasswordHash == hash(password)
        if (isValid) {
            getPrefs(context).edit().putBoolean(KEY_LOGGED_IN, true).apply()
        }
        return isValid
    }

    fun resetPassword(context: Context, email: String, newPassword: String): Boolean {
        val normalizedEmail = email.trim().lowercase()
        val storedEmail = getEmail(context) ?: return false
        if (storedEmail != normalizedEmail || newPassword.length < 6) return false

        getPrefs(context).edit()
            .putString(KEY_PASSWORD_HASH, hash(newPassword))
            .apply()
        return true
    }

    fun logout(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    fun getName(context: Context): String? =
        getPrefs(context).getString(KEY_NAME, null)

    fun setName(context: Context, name: String?) {
        getPrefs(context).edit().putString(KEY_NAME, name).apply()
    }

    fun getNeighborhood(context: Context): String? =
        getPrefs(context).getString(KEY_NEIGHBORHOOD, null)

    fun setNeighborhood(context: Context, neighborhood: String?) {
        getPrefs(context).edit().putString(KEY_NEIGHBORHOOD, neighborhood).apply()
    }

    private fun getPasswordHash(context: Context): String? =
        getPrefs(context).getString(KEY_PASSWORD_HASH, null)

    private fun getPrefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
package com.digitalpause.app.data

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Secure on-device parent/guardian password management.
 * Plaintext passwords are never stored, logged, or displayed.
 * Passwords are saved with a 16-byte random cryptographic salt and SHA-256 hash.
 */
class PasswordManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "digitalpause_secure_auth"
        private const val KEY_SALT = "auth_salt"
        private const val KEY_HASH = "auth_hash"
        private const val SALT_LENGTH_BYTES = 16
    }

    /**
     * Checks if a parent password has been configured.
     */
    fun hasPassword(): Boolean {
        val hash = prefs.getString(KEY_HASH, null)
        val salt = prefs.getString(KEY_SALT, null)
        return !hash.isNullOrBlank() && !salt.isNullOrBlank()
    }

    /**
     * Hashes the parent PIN with a freshly generated random salt and persists it.
     */
    fun saveParentPassword(pin: String): Boolean {
        if (pin.isBlank() || pin.length < 4) return false

        val random = SecureRandom()
        val saltBytes = ByteArray(SALT_LENGTH_BYTES)
        random.nextBytes(saltBytes)

        val hashBytes = hashWithSalt(pin, saltBytes)

        val saltBase64 = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        val hashBase64 = Base64.encodeToString(hashBytes, Base64.NO_WRAP)

        return prefs.edit()
            .putString(KEY_SALT, saltBase64)
            .putString(KEY_HASH, hashBase64)
            .commit()
    }

    /**
     * Validates the entered PIN against the stored hash in constant time.
     * Never reveals hints or whether the PIN was close.
     */
    fun verifyPassword(pin: String): Boolean {
        val storedHashBase64 = prefs.getString(KEY_HASH, null) ?: return false
        val storedSaltBase64 = prefs.getString(KEY_SALT, null) ?: return false

        return try {
            val saltBytes = Base64.decode(storedSaltBase64, Base64.NO_WRAP)
            val storedHashBytes = Base64.decode(storedHashBase64, Base64.NO_WRAP)

            val computedHashBytes = hashWithSalt(pin, saltBytes)

            // Constant-time comparison to prevent timing attacks
            MessageDigest.isEqual(storedHashBytes, computedHashBytes)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Clears the stored password (used only during full app data reset).
     */
    fun clearPassword() {
        prefs.edit().clear().apply()
    }

    private fun hashWithSalt(pin: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        return digest.digest(pin.toByteArray(Charsets.UTF_8))
    }
}

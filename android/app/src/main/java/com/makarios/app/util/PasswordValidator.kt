package com.makarios.app.util

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

/**
 * Validates user credentials for Makarios Auth:
 * - Email format validation
 * - Password length check (>= 8 characters)
 * - Client-side rejection of the 10,000 most common passwords
 */
object PasswordValidator {

    private val commonPasswords = HashSet<String>(10050)
    @Volatile
    private var isLoaded = false

    // Built-in fallback common passwords for fast checking and unit tests
    private val fallbackCommonPasswords = setOf(
        "password", "12345678", "123456789", "qwertyui", "qwertyuiop",
        "football", "baseball", "iloveyou", "trustno1", "letmein1",
        "sunshine", "princess", "welcome1", "admin123", "password123",
        "master123", "shadow12", "superman", "starwars", "secret12"
    )

    /**
     * Initializes the common password blacklist from assets or resources.
     */
    fun init(context: Context? = null) {
        if (isLoaded) return
        synchronized(this) {
            if (isLoaded) return
            try {
                val inputStream = if (context != null) {
                    try {
                        context.assets.open("common_passwords.txt")
                    } catch (_: Exception) {
                        javaClass.classLoader?.getResourceAsStream("common_passwords.txt")
                    }
                } else {
                    javaClass.classLoader?.getResourceAsStream("common_passwords.txt")
                }

                if (inputStream != null) {
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).useLines { lines ->
                        for (line in lines) {
                            val trimmed = line.trim().lowercase(Locale.ROOT)
                            if (trimmed.isNotEmpty()) {
                                commonPasswords.add(trimmed)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // If loading fails, fallback set is used
            }
            isLoaded = true
        }
    }

    /**
     * Loads passwords from a raw string or list directly (useful for tests or custom initialization).
     */
    fun loadFromLines(lines: Sequence<String>) {
        synchronized(this) {
            lines.forEach { line ->
                val trimmed = line.trim().lowercase(Locale.ROOT)
                if (trimmed.isNotEmpty()) {
                    commonPasswords.add(trimmed)
                }
            }
            isLoaded = true
        }
    }

    /**
     * Validates an email address.
     */
    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.isBlank()) return false
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(trimmed)
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    /**
     * Validates a password according to Makarios requirements:
     * - Minimum 8 characters
     * - Not in the 10,000 most common passwords list
     */
    fun validatePassword(password: String, context: Context? = null): ValidationResult {
        if (!isLoaded && context != null) {
            init(context)
        }

        if (password.length < 8) {
            return ValidationResult.Invalid("Use 8 or more characters. A phrase you can remember works well.")
        }

        val normalized = password.trim().lowercase(Locale.ROOT)

        if (commonPasswords.contains(normalized) || fallbackCommonPasswords.contains(normalized)) {
            return ValidationResult.Invalid("This password is too common. Please choose a more unique phrase.")
        }

        return ValidationResult.Valid
    }
}

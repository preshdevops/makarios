package com.makarios.app.util

/**
 * UsernameValidator
 *
 * Enforces username syntax and reserves system, brand, and sacred titles
 * to protect platform integrity, prevent impersonation, and uphold a dignified standard.
 */
object UsernameValidator {

    val MIN_LENGTH = 3
    val MAX_LENGTH = 20

    // Reserved usernames: system, platform, divine names, and official roles
    val RESERVED_USERNAMES = setOf(
        // System and platform reserved
        "admin",
        "administrator",
        "makarios",
        "makariosapp",
        "official",
        "support",
        "help",
        "root",
        "system",
        "team",
        "staff",
        "moderator",
        "mod",
        "security",
        "dev",
        "developer",
        "api",
        "service",
        "bot",
        "verified",
        "billing",
        "contact",
        "info",
        "news",
        "press",
        "privacy",
        "terms",
        "null",
        "undefined",
        "guest",
        "anonymous",

        // Sacred and divine names (to prevent irreverence or impersonation)
        "god",
        "jesus",
        "jesuschrist",
        "christ",
        "holyspirit",
        "yahweh",
        "jehovah",
        "lord",
        "almighty",
        "creator",
        "savior",
        "messiah",
        "elohim",
        "adonal",
        "adonai",

        // Official platform leadership titles
        "pastor_official",
        "bishop_official",
        "apostle_official"
    )

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
    }

    /**
     * Cleans and normalizes raw input (trims, removes leading '@', converts to lowercase).
     */
    fun normalize(input: String): String {
        return input.trim().removePrefix("@").lowercase()
    }

    /**
     * Validates a username against length, regex, and reserved words.
     */
    fun validate(input: String): ValidationResult {
        val username = normalize(input)

        if (username.length < MIN_LENGTH) {
            return ValidationResult.Invalid("Username must be at least $MIN_LENGTH characters")
        }

        if (username.length > MAX_LENGTH) {
            return ValidationResult.Invalid("Username cannot exceed $MAX_LENGTH characters")
        }

        // Only alphanumeric characters and single underscores allowed
        val pattern = Regex("^[a-z0-9]([a-z0-9_]*[a-z0-9])?$")
        if (!pattern.matches(username)) {
            return ValidationResult.Invalid("Use only letters, numbers, and underscores (cannot start or end with underscore)")
        }

        if (username.contains("__")) {
            return ValidationResult.Invalid("Username cannot contain consecutive underscores")
        }

        if (RESERVED_USERNAMES.contains(username)) {
            return ValidationResult.Invalid("This username is reserved")
        }

        return ValidationResult.Valid
    }
}

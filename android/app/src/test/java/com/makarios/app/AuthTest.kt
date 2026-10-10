package com.makarios.app

import com.makarios.app.util.PasswordValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AuthTest {

    @Test
    fun emailValidation_validEmails_areAccepted() {
        assertTrue(PasswordValidator.isValidEmail("user@example.com"))
        assertTrue(PasswordValidator.isValidEmail("precious@makarios.app"))
        assertTrue(PasswordValidator.isValidEmail("grace.peace@domain.org"))
        assertTrue(PasswordValidator.isValidEmail("test.user+tag@domain.co.uk"))
    }

    @Test
    fun emailValidation_invalidEmails_areRejected() {
        assertFalse(PasswordValidator.isValidEmail(""))
        assertFalse(PasswordValidator.isValidEmail("notanemail"))
        assertFalse(PasswordValidator.isValidEmail("@missingusername.com"))
        assertFalse(PasswordValidator.isValidEmail("user@.com"))
        assertFalse(PasswordValidator.isValidEmail("user@domain"))
    }

    @Test
    fun passwordValidation_tooShort_isRejected() {
        val result = PasswordValidator.validatePassword("pass1")
        assertTrue(result is PasswordValidator.ValidationResult.Invalid)
        assertEquals(
            "Use 8 or more characters. A phrase you can remember works well.",
            (result as PasswordValidator.ValidationResult.Invalid).reason
        )
    }

    @Test
    fun passwordValidation_commonPasswords_areRejected() {
        // Test fallback and common password list entries
        val commonSamples = listOf(
            "password",
            "12345678",
            "qwertyui",
            "football",
            "baseball",
            "iloveyou",
            "sunshine",
            "welcome1"
        )

        for (pass in commonSamples) {
            val result = PasswordValidator.validatePassword(pass)
            assertTrue("Expected '$pass' to be rejected as common password", result is PasswordValidator.ValidationResult.Invalid)
            assertEquals(
                "This password is too common. Please choose a more unique phrase.",
                (result as PasswordValidator.ValidationResult.Invalid).reason
            )
        }
    }

    @Test
    fun passwordValidation_strongPassword_isAccepted() {
        val result = PasswordValidator.validatePassword("walkInHisGrace2026")
        assertTrue("Expected strong phrase to be valid", result is PasswordValidator.ValidationResult.Valid)
    }

    @Test
    fun authCopy_strictlyContainsNoEmDashes() {
        val authScreensFile = File("src/main/java/com/makarios/app/ui/screens/AuthScreens.kt")
        if (authScreensFile.exists()) {
            val content = authScreensFile.readText()
            assertFalse("AuthScreens.kt must not contain em dashes", content.contains("—"))
        }

        val authManagerFile = File("src/main/java/com/makarios/app/data/AuthManager.kt")
        if (authManagerFile.exists()) {
            val content = authManagerFile.readText()
            assertFalse("AuthManager.kt must not contain em dashes", content.contains("—"))
        }

        val profileScreenFile = File("src/main/java/com/makarios/app/ui/screens/ProfileScreen.kt")
        if (profileScreenFile.exists()) {
            val content = profileScreenFile.readText()
            assertFalse("ProfileScreen.kt must not contain em dashes", content.contains("—"))
        }
    }
}

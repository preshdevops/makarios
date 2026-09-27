package com.makarios.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.makarios.app.data.AuthManager
import com.makarios.app.ui.theme.*

@Composable
fun AuthDialog(
    initialIsSignUp: Boolean = true,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var isSignUp by remember { mutableStateOf(initialIsSignUp) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resetSent by remember { mutableStateOf(false) }

    fun submit() {
        if (isLoading) return
        errorMessage = null
        focusManager.clearFocus()

        if (email.isBlank()) {
            errorMessage = "Please enter your email address"
            return
        }
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }

        isLoading = true
        if (isSignUp) {
            AuthManager.signUpWithEmail(
                email = email,
                password = password,
                onSuccess = {
                    isLoading = false
                    Toast.makeText(context, "Account created successfully ✓", Toast.LENGTH_SHORT).show()
                    onSuccess()
                },
                onError = { error ->
                    isLoading = false
                    errorMessage = error
                }
            )
        } else {
            AuthManager.signInWithEmail(
                email = email,
                password = password,
                onSuccess = {
                    isLoading = false
                    Toast.makeText(context, "Welcome back ✓", Toast.LENGTH_SHORT).show()
                    onSuccess()
                },
                onError = { error ->
                    isLoading = false
                    errorMessage = error
                }
            )
        }
    }

    fun continueAsGuest() {
        if (isLoading) return
        isLoading = true
        errorMessage = null
        AuthManager.signInAnonymously(
            onSuccess = {
                isLoading = false
                Toast.makeText(context, "Continuing as Guest ✓", Toast.LENGTH_SHORT).show()
                onSuccess()
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    fun handleForgotPassword() {
        if (email.isBlank()) {
            errorMessage = "Please enter your email first to reset your password"
            return
        }
        isLoading = true
        errorMessage = null
        AuthManager.sendPasswordReset(
            email = email,
            onSuccess = {
                isLoading = false
                resetSent = true
                Toast.makeText(context, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp))
                .background(Porcelain)
                .border(1.dp, Border, RoundedCornerShape(26.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSignUp) "Create Account" else "Welcome Back",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.3).sp,
                        color = Espresso
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Surface)
                            .border(1.dp, Border, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Espresso,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isSignUp)
                        "Back up your authored declarations & sync across all your devices."
                    else
                        "Sign in to restore your sacred collection & settings.",
                    fontFamily = BodyFontFamily,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = Stone,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Error message banner
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFDE8E4))
                            .border(1.dp, Color(0xFFF5B7B1), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            fontFamily = BodyFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF922B21)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Email Address", fontFamily = BodyFontFamily, fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedBorderColor = Terracotta,
                        unfocusedBorderColor = Border,
                        focusedLabelColor = Terracotta,
                        cursorColor = Terracotta
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password (6+ chars)", fontFamily = BodyFontFamily, fontSize = 13.sp) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = StoneMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedBorderColor = Terracotta,
                        unfocusedBorderColor = Border,
                        focusedLabelColor = Terracotta,
                        cursorColor = Terracotta
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isSignUp) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot password?",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Terracotta,
                            modifier = Modifier
                                .clickable { handleForgotPassword() }
                                .padding(vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary submit button
                Button(
                    onClick = { submit() },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Terracotta,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isSignUp) "Create Sacred Account" else "Sign In",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle Sign In vs Create Account
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSignUp) "Already have an account? " else "Need an account? ",
                        fontFamily = BodyFontFamily,
                        fontSize = 12.5.sp,
                        color = Stone
                    )
                    Text(
                        text = if (isSignUp) "Sign In" else "Create Account",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = Terracotta,
                        modifier = Modifier
                            .clickable {
                                isSignUp = !isSignUp
                                errorMessage = null
                            }
                            .padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = Border, thickness = 0.5.dp)

                Spacer(modifier = Modifier.height(14.dp))

                // Join / Continue as Guest button
                TextButton(
                    onClick = { continueAsGuest() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Continue as Guest",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = StoneMuted
                    )
                }
            }
        }
    }
}

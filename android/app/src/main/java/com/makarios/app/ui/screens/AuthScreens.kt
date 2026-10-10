package com.makarios.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.R
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.AuthManager
import com.makarios.app.data.DataMigrationManager
import com.makarios.app.ui.theme.*
import com.makarios.app.util.PasswordValidator
import kotlinx.coroutines.launch

/**
 * Makarios Auth Flow
 *
 * Dedicated 3-screen authentication system:
 * - Choose: First-class guest option, official Google Sign-In, Sign up / Sign in choices.
 * - Sign Up: Guest data migration notice, input fields with validation, common-password blacklist.
 * - Sign In: Inline error handling (#7A2012, no toasts), Forgot Password with inline feedback.
 *
 * Strict Rule: No em dashes in copy.
 */
enum class AuthFlowStep {
    CHOOSE,
    SIGN_UP,
    SIGN_IN
}

@Composable
fun AuthFlowHost(
    onContinueToApp: () -> Unit,
    modifier: Modifier = Modifier,
    initialStep: AuthFlowStep = AuthFlowStep.CHOOSE,
    onBack: (() -> Unit)? = null
) {
    var currentStep by remember { mutableStateOf(initialStep) }
    SystemBarsController(Light.Dawn)

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally { width -> width } + fadeIn(tween(300))) togetherWith
                            (slideOutHorizontally { width -> -width } + fadeOut(tween(300)))
                } else {
                    (slideInHorizontally { width -> -width } + fadeIn(tween(300))) togetherWith
                            (slideOutHorizontally { width -> width } + fadeOut(tween(300)))
                }
            },
            label = "auth_screen_transition"
        ) { step ->
            when (step) {
                AuthFlowStep.CHOOSE -> {
                    AuthChooseScreen(
                        onCreateAccount = { currentStep = AuthFlowStep.SIGN_UP },
                        onSignIn = { currentStep = AuthFlowStep.SIGN_IN },
                        onContinueAsGuest = {
                            AuthManager.signOut()
                            onContinueToApp()
                        },
                        onAuthSuccess = onContinueToApp
                    )
                }
                AuthFlowStep.SIGN_UP -> {
                    AuthSignUpScreen(
                        onBack = { currentStep = AuthFlowStep.CHOOSE },
                        onSignInClick = { currentStep = AuthFlowStep.SIGN_IN },
                        onSuccess = onContinueToApp
                    )
                }
                AuthFlowStep.SIGN_IN -> {
                    AuthSignInScreen(
                        onBack = { currentStep = AuthFlowStep.CHOOSE },
                        onCreateAccountClick = { currentStep = AuthFlowStep.SIGN_UP },
                        onContinueAsGuest = {
                            AuthManager.signOut()
                            onContinueToApp()
                        },
                        onSuccess = onContinueToApp
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SCREEN 1: CHOOSE
// -----------------------------------------------------------------------------
@Composable
fun AuthChooseScreen(
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isGoogleLoading by remember { mutableStateOf(false) }
    var inlineError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawLight(
                    light = Light.Dawn,
                    size = size,
                    horizon = HorizonSpec(.70f, .79f, .88f),
                    discPos = Offset(size.width * 0.80f, size.height * 0.40f),
                    discScale = 0.85f,
                    grain = true
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Wordmark: "makarios" (Newsreader italic 22)
            Text(
                text = "makarios",
                style = TextStyle(
                    fontFamily = NewsreaderFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp
                ),
                color = Ink,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Heading: (Newsreader 500 36/44, balanced)
            Text(
                text = "Keep your declarations safe.",
                style = TextStyle(
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 36.sp,
                    lineHeight = 44.sp,
                    fontFeatureSettings = "lnum, tnum"
                ),
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sub: (16/24, 78% ink)
            Text(
                text = "Sign in to sync across your devices. Or start as a guest and sign in any time.",
                style = TextStyle(
                    fontFamily = HankenGroteskFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                color = Ink.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (inlineError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                AuthInlineError(message = inlineError!!)
            }

            Spacer(modifier = Modifier.weight(1f).defaultMinSize(minHeight = 40.dp))

            // Buttons (56dp pills, 12dp gap)
            // 1. Primary Ink: "Create an account"
            Button(
                onClick = onCreateAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .sizeIn(minHeight = 48.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ink,
                    contentColor = Cream
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Create an account",
                    style = MakariosTypography.labelLarge.copy(
                        fontFamily = HankenGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = Cream
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Outline: "Continue with Google"
            OutlinedButton(
                onClick = {
                    if (isGoogleLoading) return@OutlinedButton
                    isGoogleLoading = true
                    inlineError = null
                    coroutineScope.launch {
                        AuthManager.signInWithGoogle(
                            context = context,
                            onSuccess = {
                                isGoogleLoading = false
                                val user = AuthManager.currentUser
                                if (user != null) {
                                    coroutineScope.launch {
                                        DataMigrationManager.migrateUserData(context, user.uid)
                                    }
                                }
                                onAuthSuccess()
                            },
                            onError = { err ->
                                isGoogleLoading = false
                                inlineError = err
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .sizeIn(minHeight = 48.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Ink.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.85f),
                    contentColor = Ink
                )
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Ink,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            style = MakariosTypography.labelLarge.copy(
                                fontFamily = HankenGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = Ink
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Text row: "Already have an account? Sign in" (48dp touch target)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(onClick = onSignIn),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MakariosTypography.bodyMedium.copy(fontSize = 14.sp),
                    color = Ink.copy(alpha = 0.78f)
                )
                Text(
                    text = "Sign in",
                    style = MakariosTypography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    ),
                    color = Ink
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Divider "or"
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Ink.copy(alpha = 0.20f),
                    thickness = 1.dp
                )
                Text(
                    text = "or",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MakariosTypography.labelSmall.copy(fontSize = 13.sp),
                    color = Ink.copy(alpha = 0.60f)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Ink.copy(alpha = 0.20f),
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Outline pill: "Continue as guest"
            OutlinedButton(
                onClick = onContinueAsGuest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .sizeIn(minHeight = 48.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.5.dp, Ink.copy(alpha = 0.65f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Ink
                )
            ) {
                Text(
                    text = "Continue as guest",
                    style = MakariosTypography.labelLarge.copy(
                        fontFamily = HankenGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = Ink
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Caption: "Guests are saved on this phone only."
            Text(
                text = "Guests are saved on this phone only.",
                style = MakariosTypography.labelSmall.copy(
                    fontFamily = HankenGroteskFontFamily,
                    fontSize = 13.sp
                ),
                color = Ink.copy(alpha = 0.72f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Footer 12sp: "By continuing you agree to the Terms and the Privacy Policy."
            TermsAndPrivacyFooter(context = context)

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// -----------------------------------------------------------------------------
// SCREEN 2: SIGN UP
// -----------------------------------------------------------------------------
@Composable
fun AuthSignUpScreen(
    onBack: () -> Unit,
    onSignInClick: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var inlineError by remember { mutableStateOf<String?>(null) }

    val keptCount = remember { AffirmationRepository.getSaved().size }

    BackHandler { onBack() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawLight(
                    light = Light.Dawn,
                    size = size,
                    horizon = HorizonSpec(.70f, .79f, .88f),
                    discPos = Offset(size.width * 0.80f, size.height * 0.40f),
                    discScale = 0.85f,
                    grain = true
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .systemBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Back button (48dp touch target)
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Heading: Newsreader 34
            Text(
                text = "Create your account",
                style = TextStyle(
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 34.sp,
                    lineHeight = 42.sp,
                    fontFeatureSettings = "lnum, tnum"
                ),
                color = Ink
            )

            // Guest migration banner if local data exists
            if (keptCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                GuestMigrationBanner(keptCount = keptCount)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Field 1: "What should we call you?" (first name, optional)
            AuthFieldLabel(label = "What should we call you?")
            Spacer(modifier = Modifier.height(6.dp))
            AuthTextField(
                value = name,
                onValueChange = { name = it; inlineError = null },
                placeholder = "First name (optional)",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Field 2: "Email"
            AuthFieldLabel(label = "Email")
            Spacer(modifier = Modifier.height(6.dp))
            AuthTextField(
                value = email,
                onValueChange = { email = it; inlineError = null },
                placeholder = "you@example.com",
                isError = inlineError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Field 3: "Password"
            AuthFieldLabel(label = "Password")
            Spacer(modifier = Modifier.height(6.dp))
            AuthTextField(
                value = password,
                onValueChange = { password = it; inlineError = null },
                placeholder = "At least 8 characters",
                isError = inlineError != null,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Ink.copy(alpha = 0.65f)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            // Helper text: "Use 8 or more characters. A phrase you can remember works well."
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Use 8 or more characters. A phrase you can remember works well.",
                style = MakariosTypography.labelSmall.copy(fontSize = 12.sp),
                color = Ink.copy(alpha = 0.68f)
            )

            if (inlineError != null) {
                Spacer(modifier = Modifier.height(14.dp))
                AuthInlineError(message = inlineError!!)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Ink: "Create account"
            Button(
                onClick = {
                    if (isLoading) return@Button
                    focusManager.clearFocus()
                    inlineError = null

                    if (!PasswordValidator.isValidEmail(email)) {
                        inlineError = "Please enter a valid email address."
                        return@Button
                    }

                    val passwordCheck = PasswordValidator.validatePassword(password, context)
                    if (passwordCheck is PasswordValidator.ValidationResult.Invalid) {
                        inlineError = passwordCheck.reason
                        return@Button
                    }

                    isLoading = true
                    AuthManager.signUpWithEmail(
                        name = name.takeIf { it.isNotBlank() },
                        email = email,
                        password = password,
                        onSuccess = {
                            val user = AuthManager.currentUser
                            if (user != null) {
                                coroutineScope.launch {
                                    DataMigrationManager.migrateUserData(context, user.uid)
                                }
                            }
                            isLoading = false
                            onSuccess()
                        },
                        onError = { err ->
                            isLoading = false
                            inlineError = err
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .sizeIn(minHeight = 48.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ink,
                    contentColor = Cream
                ),
                enabled = !isLoading && !isGoogleLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Cream,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Create account",
                        style = MakariosTypography.labelLarge.copy(
                            fontFamily = HankenGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = Cream
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider "or"
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Ink.copy(alpha = 0.20f), thickness = 1.dp)
                Text("or", modifier = Modifier.padding(horizontal = 12.dp), style = MakariosTypography.labelSmall.copy(fontSize = 13.sp), color = Ink.copy(alpha = 0.60f))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Ink.copy(alpha = 0.20f), thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Button
            OutlinedButton(
                onClick = {
                    if (isGoogleLoading) return@OutlinedButton
                    isGoogleLoading = true
                    inlineError = null
                    coroutineScope.launch {
                        AuthManager.signInWithGoogle(
                            context = context,
                            onSuccess = {
                                isGoogleLoading = false
                                val user = AuthManager.currentUser
                                if (user != null) {
                                    coroutineScope.launch {
                                        DataMigrationManager.migrateUserData(context, user.uid)
                                    }
                                }
                                onSuccess()
                            },
                            onError = { err ->
                                isGoogleLoading = false
                                inlineError = err
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .sizeIn(minHeight = 48.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Ink.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.85f),
                    contentColor = Ink
                )
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Ink,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            style = MakariosTypography.labelLarge.copy(
                                fontFamily = HankenGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = Ink
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Link: "Already have an account? Sign in"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(onClick = onSignInClick),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MakariosTypography.bodyMedium.copy(fontSize = 14.sp),
                    color = Ink.copy(alpha = 0.78f)
                )
                Text(
                    text = "Sign in",
                    style = MakariosTypography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    ),
                    color = Ink
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// -----------------------------------------------------------------------------
// SCREEN 3: SIGN IN
// -----------------------------------------------------------------------------
@Composable
fun AuthSignInScreen(
    onBack: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var inlineError by remember { mutableStateOf<String?>(null) }
    var resetEmailSentTo by remember { mutableStateOf<String?>(null) }
    var isResettingPassword by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawLight(
                    light = Light.Dawn,
                    size = size,
                    horizon = HorizonSpec(.70f, .79f, .88f),
                    discPos = Offset(size.width * 0.80f, size.height * 0.40f),
                    discScale = 0.85f,
                    grain = true
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .systemBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Back button (48dp touch target)
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Heading: "Welcome back"
            Text(
                text = "Welcome back",
                style = TextStyle(
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 34.sp,
                    lineHeight = 42.sp,
                    fontFeatureSettings = "lnum, tnum"
                ),
                color = Ink
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Sub: "Your declarations are waiting."
            Text(
                text = "Your declarations are waiting.",
                style = MakariosTypography.bodyLarge.copy(
                    fontFamily = HankenGroteskFontFamily,
                    fontSize = 16.sp
                ),
                color = Ink.copy(alpha = 0.78f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Field 1: "Email"
            AuthFieldLabel(label = "Email")
            Spacer(modifier = Modifier.height(6.dp))
            AuthTextField(
                value = email,
                onValueChange = {
                    email = it
                    inlineError = null
                    resetEmailSentTo = null
                },
                placeholder = "you@example.com",
                isError = inlineError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Field 2: "Password"
            AuthFieldLabel(label = "Password")
            Spacer(modifier = Modifier.height(6.dp))
            AuthTextField(
                value = password,
                onValueChange = {
                    password = it
                    inlineError = null
                },
                placeholder = "Your password",
                isError = inlineError != null,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Ink.copy(alpha = 0.65f)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            // Inline error under the field (red #7A2012, 2dp outline, error icon, never toast)
            if (inlineError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AuthInlineError(message = inlineError!!)
            }

            if (resetEmailSentTo != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Check your email. We sent a reset link to $resetEmailSentTo.",
                        style = MakariosTypography.bodySmall.copy(fontSize = 13.sp),
                        color = Color(0xFF1B5E20)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // "Forgot password?" (underlined, right-aligned, 48dp touch target)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .sizeIn(minHeight = 48.dp)
                        .clickable {
                            if (email.isBlank()) {
                                inlineError = "Enter your email address to reset your password."
                                return@clickable
                            }
                            if (isResettingPassword) return@clickable
                            isResettingPassword = true
                            inlineError = null
                            AuthManager.sendPasswordReset(
                                email = email,
                                onSuccess = {
                                    isResettingPassword = false
                                    resetEmailSentTo = email.trim()
                                },
                                onError = { err ->
                                    isResettingPassword = false
                                    inlineError = err
                                }
                            )
                        },
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = if (isResettingPassword) "Sending reset email..." else "Forgot password?",
                        style = MakariosTypography.labelSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        ),
                        color = Ink
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Ink: "Sign in"
            Button(
                onClick = {
                    if (isLoading) return@Button
                    focusManager.clearFocus()
                    inlineError = null

                    if (email.isBlank() || password.isBlank()) {
                        inlineError = "That email and password do not match."
                        return@Button
                    }

                    isLoading = true
                    AuthManager.signInWithEmail(
                        email = email,
                        password = password,
                        onSuccess = {
                            val user = AuthManager.currentUser
                            if (user != null) {
                                coroutineScope.launch {
                                    DataMigrationManager.migrateUserData(context, user.uid)
                                }
                            }
                            isLoading = false
                            onSuccess()
                        },
                        onError = { err ->
                            isLoading = false
                            inlineError = err
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .sizeIn(minHeight = 48.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ink,
                    contentColor = Cream
                ),
                enabled = !isLoading && !isGoogleLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Cream,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign in",
                        style = MakariosTypography.labelLarge.copy(
                            fontFamily = HankenGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = Cream
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider "or"
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Ink.copy(alpha = 0.20f), thickness = 1.dp)
                Text("or", modifier = Modifier.padding(horizontal = 12.dp), style = MakariosTypography.labelSmall.copy(fontSize = 13.sp), color = Ink.copy(alpha = 0.60f))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Ink.copy(alpha = 0.20f), thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Button
            OutlinedButton(
                onClick = {
                    if (isGoogleLoading) return@OutlinedButton
                    isGoogleLoading = true
                    inlineError = null
                    coroutineScope.launch {
                        AuthManager.signInWithGoogle(
                            context = context,
                            onSuccess = {
                                isGoogleLoading = false
                                val user = AuthManager.currentUser
                                if (user != null) {
                                    coroutineScope.launch {
                                        DataMigrationManager.migrateUserData(context, user.uid)
                                    }
                                }
                                onSuccess()
                            },
                            onError = { err ->
                                isGoogleLoading = false
                                inlineError = err
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .sizeIn(minHeight = 48.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Ink.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.85f),
                    contentColor = Ink
                )
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Ink,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            style = MakariosTypography.labelLarge.copy(
                                fontFamily = HankenGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = Ink
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Link: "New here? Create an account"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(onClick = onCreateAccountClick),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New here? ",
                    style = MakariosTypography.bodyMedium.copy(fontSize = 14.sp),
                    color = Ink.copy(alpha = 0.78f)
                )
                Text(
                    text = "Create an account",
                    style = MakariosTypography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    ),
                    color = Ink
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quiet: "Continue as guest instead"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(onClick = onContinueAsGuest),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Continue as guest instead",
                    style = MakariosTypography.labelSmall.copy(
                        fontSize = 14.sp,
                        color = Ink.copy(alpha = 0.70f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// -----------------------------------------------------------------------------
// REUSABLE AUTH COMPONENTS
// -----------------------------------------------------------------------------

@Composable
private fun AuthFieldLabel(label: String) {
    Text(
        text = label,
        style = MakariosTypography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        ),
        color = Ink
    )
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val errorBorderColor = Color(0xFF7A2012)
    val normalBorderColor = Ink.copy(alpha = 0.22f)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MakariosTypography.bodyMedium.copy(fontSize = 15.sp),
                color = Ink.copy(alpha = 0.45f)
            )
        },
        singleLine = true,
        isError = isError,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFFFF4E4).copy(alpha = 0.55f),
            unfocusedContainerColor = Color(0xFFFFF4E4).copy(alpha = 0.55f),
            errorContainerColor = Color(0xFFFFF4E4).copy(alpha = 0.55f),
            focusedBorderColor = if (isError) errorBorderColor else Ink,
            unfocusedBorderColor = if (isError) errorBorderColor else normalBorderColor,
            errorBorderColor = errorBorderColor,
            cursorColor = Ink,
            focusedTextColor = Ink,
            unfocusedTextColor = Ink
        )
    )
}

@Composable
fun AuthInlineError(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = "Error",
            tint = Color(0xFF7A2012),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = message,
            style = MakariosTypography.bodySmall.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF7A2012)
        )
    }
}

@Composable
private fun GuestMigrationBanner(keptCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFF4E4).copy(alpha = 0.85f))
            .border(1.dp, Ink.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Ink),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudUpload,
                contentDescription = null,
                tint = Cream,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Your $keptCount kept declarations will move to your account.",
            style = MakariosTypography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Ink
        )
    }
}

@Composable
private fun TermsAndPrivacyFooter(context: Context) {
    val annotatedString = buildAnnotatedString {
        append("By continuing you agree to the ")
        pushStringAnnotation(tag = "TERMS", annotation = "https://makarios.app/terms")
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline)) {
            append("Terms")
        }
        pop()
        append(" and the ")
        pushStringAnnotation(tag = "PRIVACY", annotation = "https://makarios.app/privacy")
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline)) {
            append("Privacy Policy")
        }
        pop()
        append(".")
    }

    Text(
        text = annotatedString,
        style = MakariosTypography.labelSmall.copy(
            fontFamily = HankenGroteskFontFamily,
            fontSize = 12.sp,
            lineHeight = 18.sp
        ),
        color = Ink.copy(alpha = 0.70f),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://makarios.app/terms"))
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
    )
}

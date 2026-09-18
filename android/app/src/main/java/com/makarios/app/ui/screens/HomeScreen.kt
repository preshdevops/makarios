package com.makarios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.AffirmationTone
import com.makarios.app.ui.components.*
import com.makarios.app.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToCreate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var promptText by remember { mutableStateOf("Doubt and fear in my new lead") }
    var selectedCategory by remember { mutableStateOf("Confidence") }
    var selectedTone by remember { mutableStateOf(AffirmationTone.RESOLUTE) }
    var currentAffirmation by remember { mutableStateOf(AffirmationRepository.starterAffirmation) }
    var isSaved by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PorcelainBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Brand Header
            HeaderBar()

            // Main Question Heading
            Text(
                text = "What are you carrying today?",
                color = TextPrimary,
                fontFamily = FontFamily.Serif,
                fontSize = 27.sp,
                lineHeight = 34.sp,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp)
            )

            // Prompt Input Pill
            PromptInput(
                value = promptText,
                onValueChange = { promptText = it },
                onSubmit = {
                    currentAffirmation = AffirmationRepository.matchAffirmation(
                        prompt = promptText,
                        category = selectedCategory,
                        tone = selectedTone
                    )
                }
            )

            // Category Filter Pills
            CategoryPills(
                categories = AffirmationRepository.categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { cat ->
                    selectedCategory = cat
                    currentAffirmation = AffirmationRepository.matchAffirmation(
                        prompt = promptText,
                        category = cat,
                        tone = selectedTone
                    )
                }
            )

            // Central Truth Card
            TruthCard(
                affirmation = currentAffirmation,
                isSaved = isSaved,
                onToggleSave = { isSaved = !isSaved },
                onCardClick = { /* Detail modal or dialog */ }
            )

            // Tone Switcher, Another & Wallpaper CTA
            ToneSelector(
                selectedTone = selectedTone,
                onToneSelected = { tone ->
                    selectedTone = tone
                    currentAffirmation = AffirmationRepository.matchAffirmation(
                        prompt = promptText,
                        category = selectedCategory,
                        tone = tone
                    )
                },
                onAnotherClicked = {
                    currentAffirmation = AffirmationRepository.getAll().random()
                },
                onCreateWallpaperClicked = {
                    onNavigateToCreate(currentAffirmation.id)
                }
            )
        }
    }
}

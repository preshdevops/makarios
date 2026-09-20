package com.makarios.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.AffirmationTone
import com.makarios.app.ui.components.*
import com.makarios.app.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToCreate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var promptText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Confidence") }
    var selectedTone by remember { mutableStateOf(AffirmationTone.RESOLUTE) }
    var currentAffirmation by remember { mutableStateOf(AffirmationRepository.starterAffirmation) }
    var isSaved by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Porcelain
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // 1. Header — wordmark + avatar + warm question
            HeaderBar()

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Prompt input — rounded pill with mic + send
            PromptInput(
                value = promptText,
                onValueChange = { promptText = it },
                onSubmit = {
                    if (promptText.isNotBlank()) {
                        currentAffirmation = AffirmationRepository.matchAffirmation(
                            prompt = promptText,
                            category = selectedCategory,
                            tone = selectedTone
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Category pills — rounded, terracotta selected
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

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Truth card — photo + declaration + scripture
            TruthCard(
                affirmation = currentAffirmation,
                isSaved = isSaved,
                onToggleSave = { isSaved = !isSaved },
                onCardClick = { /* Detail view */ }
            )

            // 5. Tone selector + Another + Create Wallpaper
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

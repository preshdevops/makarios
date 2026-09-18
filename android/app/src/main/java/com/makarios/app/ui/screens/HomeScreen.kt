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
        containerColor = ParchmentBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // Editorial Masthead
            HeaderBar()

            // Canonical Topic Index
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

            // The Centerpiece: Illuminated Broadside Plate
            TruthCard(
                affirmation = currentAffirmation,
                isSaved = isSaved,
                onToggleSave = { isSaved = !isSaved },
                onCardClick = { /* Detail view or contemplation */ }
            )

            // Literary Tone Selector & Illuminate Wallpaper Action
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

            Spacer(modifier = Modifier.height(10.dp))

            // Contemplative Inscribe Field
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
        }
    }
}

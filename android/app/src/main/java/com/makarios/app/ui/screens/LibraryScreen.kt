package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.components.AffirmationCard
import com.makarios.app.ui.theme.*

@Composable
fun LibraryScreen(
    onNavigateToDetail: (Affirmation) -> Unit,
    onNavigateToCreate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember {
        listOf("All", "Identity", "Peace", "Strength", "Purpose", "Courage", "Joy", "Provision", "Confidence", "Relationships", "Discipline")
    }

    val allAffirmations = remember { AffirmationRepository.getAll() }

    // Filter by search query and category
    val filteredAffirmations = remember(searchQuery, selectedCategory) {
        allAffirmations.filter { affirmation ->
            val matchesCategory = selectedCategory == "All" || affirmation.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    affirmation.declaration.contains(searchQuery, ignoreCase = true) ||
                    affirmation.scriptureText.contains(searchQuery, ignoreCase = true) ||
                    affirmation.reference.contains(searchQuery, ignoreCase = true) ||
                    affirmation.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Porcelain
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 72.dp)
        ) {
            // ── 1. Header Bar ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "Library",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.3).sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Biblical declarations rooted in scripture",
                    fontFamily = BodyFontFamily,
                    fontSize = 13.sp,
                    color = Stone
                )
            }

            // ── 2. Search Bar ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp), spotColor = Espresso.copy(alpha = 0.05f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = StoneMuted,
                        modifier = Modifier.size(18.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search declarations, scriptures, topics...",
                                fontFamily = BodyFontFamily,
                                fontSize = 14.sp,
                                color = StoneMuted
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                color = Espresso,
                                fontFamily = BodyFontFamily,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = StoneMuted,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── 3. Category Filter Pills ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(Espresso)
                                        .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.2f))
                                } else {
                                    Modifier
                                        .background(Surface)
                                        .border(1.dp, Border, RoundedCornerShape(20.dp))
                                }
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            fontFamily = BodyFontFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (isSelected) Surface else Espresso
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 4. Category Overview Cards (Only shown when "All" is active and search is empty) ──
            if (selectedCategory == "All" && searchQuery.isBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Life Seasons & Themes",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Espresso
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val categoryThemes = listOf(
                        Triple("Identity", "Rooted in who God declares you are", "2 declarations"),
                        Triple("Peace", "Stillness and sovereign rest amid turbulence", "2 declarations"),
                        Triple("Strength", "Endurance and power made perfect in weakness", "2 declarations"),
                        Triple("Purpose", "Intentional creation and ordered steps", "1 declaration"),
                        Triple("Confidence", "Boldness grounded in divine defense", "1 declaration"),
                        Triple("Courage", "Obedience in the presence of fear", "1 declaration"),
                        Triple("Provision", "Resting in the inexhaustible Provider", "1 declaration"),
                        Triple("Joy", "Unshakable delight from the Lord", "1 declaration")
                    )

                    categoryThemes.chunked(2).forEach { rowPairs ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowPairs.forEach { (catName, description, count) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Espresso.copy(alpha = 0.05f))
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Surface)
                                        .border(1.dp, Border, RoundedCornerShape(16.dp))
                                        .clickable { selectedCategory = catName }
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = catName.uppercase(),
                                            fontFamily = BodyFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            letterSpacing = 1.4.sp,
                                            color = Terracotta
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = description,
                                            fontFamily = BodyFontFamily,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp,
                                            color = Stone,
                                            maxLines = 2
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = count,
                                            fontFamily = BodyFontFamily,
                                            fontSize = 11.sp,
                                            color = StoneMuted
                                        )
                                    }
                                }
                            }
                            if (rowPairs.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── 5. Affirmations List ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            searchQuery.isNotBlank() -> "Search Results (${filteredAffirmations.size})"
                            selectedCategory != "All" -> "$selectedCategory Declarations (${filteredAffirmations.size})"
                            else -> "All Declarations"
                        },
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Espresso
                    )

                    if (selectedCategory != "All" || searchQuery.isNotBlank()) {
                        Text(
                            text = "Clear filter",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Terracotta,
                            modifier = Modifier.clickable {
                                selectedCategory = "All"
                                searchQuery = ""
                            }
                        )
                    }
                }

                if (filteredAffirmations.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Surface)
                            .border(1.dp, Border, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No declarations found",
                                fontFamily = DisplayFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 17.sp,
                                color = Espresso
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Write your own declaration and match with scripture.",
                                fontFamily = BodyFontFamily,
                                fontSize = 13.sp,
                                color = Stone
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onNavigateToCreate("") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Espresso,
                                    contentColor = Surface
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "Create Declaration",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    filteredAffirmations.forEach { affirmation ->
                        var isSaved by remember(affirmation.id) {
                            mutableStateOf(AffirmationRepository.isSaved(affirmation.id))
                        }

                        AffirmationCard(
                            affirmation = affirmation,
                            isSaved = isSaved,
                            onToggleSave = {
                                AffirmationRepository.toggleSave(affirmation.id)
                                isSaved = !isSaved
                            },
                            onShare = {
                                Toast.makeText(context, "Shared declaration", Toast.LENGTH_SHORT).show()
                            },
                            onCardClick = { onNavigateToDetail(affirmation) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 6. Invitation to Author / Create ──────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp), spotColor = Espresso.copy(alpha = 0.06f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(PorcelainWarm)
                    .border(1.dp, Border, RoundedCornerShape(18.dp))
                    .clickable { onNavigateToCreate("") }
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Need a declaration for your season?",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Espresso
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Write your declaration · Match with scripture",
                            fontFamily = BodyFontFamily,
                            fontSize = 12.sp,
                            color = Stone
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Espresso),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Create",
                            tint = Surface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

package com.makarios.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.Cream
import com.makarios.app.ui.theme.Ink
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.MakariosTypography
import com.makarios.app.ui.theme.lightBackground

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onOpenWallpapers: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    val screens = listOf(
        Triple(
            Light.Dawn,
            "Your words, anchored in eternal truth.",
            Triple("I am loved beyond measure.", "I have loved you with an everlasting love...", "Jeremiah 31:3")
        ),
        Triple(
            Light.Mist,
            "Quiet the noise. Find your peace.",
            Triple("My mind is at rest.", "And the peace of God, which surpasses all understanding...", "Philippians 4:7")
        ),
        Triple(
            Light.Ember,
            "Strength for the next brave step.",
            Triple("I have what it takes.", "For God gave us a spirit not of fear but of power...", "2 Timothy 1:7")
        )
    )

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val screen = screens[page]
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .lightBackground(screen.first)
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                
                // Wordmark
                Text(
                    text = "makarios",
                    style = MakariosTypography.displayMedium.copy(fontStyle = FontStyle.Italic),
                    color = screen.first.text
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // 12 words max
                Text(
                    text = screen.second,
                    style = MakariosTypography.displaySmall,
                    color = screen.first.text
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Pairing Visual
                Pairing(
                    declaration = screen.third.first,
                    verseText = screen.third.second,
                    verseReference = screen.third.third,
                    light = screen.first,
                    isCompact = true
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (page == 2) {
                    androidx.compose.material3.TextButton(onClick = onOpenWallpapers) {
                        Text("Set as wallpaper", color = screen.first.text)
                    }
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = screen.first.buttonFill, contentColor = screen.first.buttonText),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Begin", style = MakariosTypography.labelLarge)
                    }
                } else {
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }
        }
        
        // Pager indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { i ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (pagerState.currentPage == i) Ink else Ink.copy(alpha = 0.2f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
        }
    }
}



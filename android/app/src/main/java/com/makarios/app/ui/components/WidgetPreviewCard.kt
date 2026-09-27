package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.theme.*

@Composable
fun WidgetPreviewCard(
    affirmation: Affirmation,
    onAddToHomeScreen: () -> Unit,
    onSeeAllWidgets: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Espresso.copy(alpha = 0.03f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Surface)
            .border(0.5.dp, BorderSubtle, RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = Stone,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "HOME SCREEN WIDGET",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 1.6.sp,
                    color = StoneMuted
                )
            }

            Text(
                text = "Preview sizes",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Stone,
                modifier = Modifier.clickable(onClick = onSeeAllWidgets)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Clean typography without nested boxes
        Text(
            text = "“${affirmation.declaration}”",
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            color = Espresso,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = affirmation.reference.uppercase(),
            color = Terracotta,
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.5.sp,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // CTA: Add to Home Screen
        Button(
            onClick = onAddToHomeScreen,
            colors = ButtonDefaults.buttonColors(
                containerColor = Espresso,
                contentColor = Surface
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Surface,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Add to Home Screen",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = Surface
                )
            }
        }
    }
}

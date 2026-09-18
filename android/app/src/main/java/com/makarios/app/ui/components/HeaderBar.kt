package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.theme.*

@Composable
fun HeaderBar(
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Folio Date / Issue
            Column {
                Text(
                    text = (subtitle ?: "THE DAILY CANON · FOLIO XXIV").uppercase(),
                    color = RubricVermilion,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "M A K A R I O S",
                    color = InkLampblack,
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 21.sp,
                    letterSpacing = 4.sp
                )
            }

            // Right: Refined Editorial Monogram
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ParchmentWarm)
                    .border(0.75.dp, BorderBroadsheet, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✤",
                    color = GoldLeaf,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Delicate Hairline Rule separating masthead from folio content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(0.75.dp)
                .background(HairlineRule)
        )
    }
}

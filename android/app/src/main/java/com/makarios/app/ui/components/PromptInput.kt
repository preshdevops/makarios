package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.theme.*

@Composable
fun PromptInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Espresso.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(Surface) // Crisp white pill per mockup
            .border(1.dp, Border, RoundedCornerShape(28.dp))
            .padding(start = 18.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text input area
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = "Doubt and fear in my new leade...",
                    color = StoneMuted,
                    fontFamily = BodyFontFamily,
                    fontSize = 14.5.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Espresso,
                    fontFamily = BodyFontFamily,
                    fontSize = 14.5.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Mic icon
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Voice input",
            tint = StoneMuted,
            modifier = Modifier.size(19.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Send button — deep espresso-plum circle per mockup with upward arrow
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Espresso)
                .clickable(onClick = onSubmit),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Send",
                tint = Surface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

package com.makarios.app.ui.components

import android.text.format.DateFormat
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.makarios.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SacredTimePickerDialog(
    initialHour: Int = 8,
    initialMinute: Int = 30,
    title: String = "Reminder Time",
    subtitle: String = "Choose when you'd like your daily declaration to appear on your screen.",
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, BorderSubtle, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Espresso,
                    letterSpacing = (-0.2).sp
                )

                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontFamily = BodyFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = Stone,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = PorcelainWarm,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = Espresso,
                        selectorColor = Olive,
                        containerColor = Surface,
                        periodSelectorBorderColor = Border,
                        periodSelectorSelectedContainerColor = Olive,
                        periodSelectorUnselectedContainerColor = Surface,
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = Espresso,
                        timeSelectorSelectedContainerColor = OliveLight,
                        timeSelectorUnselectedContainerColor = PorcelainWarm,
                        timeSelectorSelectedContentColor = Olive,
                        timeSelectorUnselectedContentColor = Espresso
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.5.sp,
                            color = Stone
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(timePickerState.hour, timePickerState.minute)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Olive,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Set",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}

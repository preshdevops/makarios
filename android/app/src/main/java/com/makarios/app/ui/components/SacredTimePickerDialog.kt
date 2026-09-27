package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.makarios.app.ui.theme.*

@Composable
fun SacredTimePickerDialog(
    initialHour: Int = 8,
    initialMinute: Int = 30,
    title: String = "Reminder Time",
    subtitle: String = "Choose when you'd like your daily declaration to appear on your screen.",
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour24 by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    // 12-hour breakdown
    val isPm = selectedHour24 >= 12
    val hour12 = when {
        selectedHour24 == 0 -> 12
        selectedHour24 > 12 -> selectedHour24 - 12
        else -> selectedHour24
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, BorderSubtle, RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TerracottaLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Terracotta,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = Espresso,
                    letterSpacing = (-0.3).sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    fontFamily = BodyFontFamily,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = Stone,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Presets Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Dawn (6:30 AM)" to Pair(6, 30),
                        "Morning (8:30 AM)" to Pair(8, 30),
                        "Midday (12:30 PM)" to Pair(12, 30),
                        "Evening (8:30 PM)" to Pair(20, 30),
                        "Night (10:00 PM)" to Pair(22, 0)
                    ).forEach { (label, time) ->
                        val isPresetActive = selectedHour24 == time.first && selectedMinute == time.second
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isPresetActive) Espresso else PorcelainWarm)
                                .clickable {
                                    selectedHour24 = time.first
                                    selectedMinute = time.second
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isPresetActive) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 11.sp,
                                color = if (isPresetActive) Color.White else Espresso
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Time Spinners Display Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PorcelainWarm.copy(alpha = 0.6f))
                        .border(1.dp, Border, RoundedCornerShape(20.dp))
                        .padding(vertical = 18.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Hour Column
                        TimeUnitSpinner(
                            value = hour12,
                            display = "%02d".format(hour12),
                            onIncrement = {
                                val next12 = if (hour12 == 12) 1 else hour12 + 1
                                selectedHour24 = if (isPm) {
                                    if (next12 == 12) 12 else next12 + 12
                                } else {
                                    if (next12 == 12) 0 else next12
                                }
                            },
                            onDecrement = {
                                val prev12 = if (hour12 == 1) 12 else hour12 - 1
                                selectedHour24 = if (isPm) {
                                    if (prev12 == 12) 12 else prev12 + 12
                                } else {
                                    if (prev12 == 12) 0 else prev12
                                }
                            }
                        )

                        Text(
                            text = ":",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = Espresso,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )

                        // Minute Column
                        TimeUnitSpinner(
                            value = selectedMinute,
                            display = "%02d".format(selectedMinute),
                            onIncrement = {
                                selectedMinute = (selectedMinute + 5) % 60
                            },
                            onDecrement = {
                                selectedMinute = if (selectedMinute < 5) 55 else selectedMinute - 5
                            }
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // AM / PM Switcher
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isPm) Terracotta else Surface)
                                    .border(0.5.dp, if (!isPm) Terracotta else Border, RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isPm) selectedHour24 -= 12
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "AM",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (!isPm) Color.White else StoneMuted
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isPm) Terracotta else Surface)
                                    .border(0.5.dp, if (isPm) Terracotta else Border, RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (!isPm) selectedHour24 += 12
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "PM",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (isPm) Color.White else StoneMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.5.sp,
                            color = Stone
                        )
                    }

                    Button(
                        onClick = {
                            onConfirm(selectedHour24, selectedMinute)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Terracotta,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Set Reminder",
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

@Composable
private fun TimeUnitSpinner(
    value: Int,
    display: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase",
                tint = StoneMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Box(
            modifier = Modifier
                .width(62.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = display,
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                letterSpacing = (-0.5).sp,
                color = Espresso
            )
        }

        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease",
                tint = StoneMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

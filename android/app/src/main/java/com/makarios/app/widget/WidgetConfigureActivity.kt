package com.makarios.app.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.makarios.app.ui.theme.*

class WidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set result to CANCELED in case user backs out
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setContent {
            WidgetConfigureScreen(
                onSave = { selectedLight, selectedSource, selectedTopic ->
                    WidgetStore.setConfig(
                        context = this,
                        appWidgetId = appWidgetId,
                        light = selectedLight,
                        source = selectedSource,
                        topic = selectedTopic
                    )
                    val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    setResult(RESULT_OK, resultValue)
                    finish()
                }
            )
        }
    }
}

@Composable
private fun WidgetConfigureScreen(
    onSave: (String, WidgetSource, String) -> Unit
) {
    var selectedLightName by remember { mutableStateOf("AUTO") }
    var selectedSource by remember { mutableStateOf(WidgetSource.TODAY) }
    var selectedTopic by remember { mutableStateOf("Identity") }

    val allLights = listOf(
        Light.Dawn, Light.Grove, Light.Midday, Light.Ember,
        Light.Dusk, Light.Rain, Light.Mist, Light.Night
    )

    val topics = listOf(
        "Identity", "Peace", "Strength", "Purpose",
        "Courage", "Joy", "Provision", "Confidence"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F5))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Configure Widget",
            style = MakariosTypography.displaySmall,
            color = Ink
        )
        Text(
            text = "Choose light and scripture source for your home screen.",
            style = MakariosTypography.bodyMedium,
            color = Ink.copy(alpha = 0.65f),
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // 1. Light Section
        Text("Light", style = MakariosTypography.titleMedium, color = Ink)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Auto Option
            val isAuto = selectedLightName == "AUTO"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isAuto) Ink else Ink.copy(alpha = 0.08f))
                    .clickable { selectedLightName = "AUTO" }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    "Auto (by time)",
                    style = MakariosTypography.labelMedium,
                    color = if (isAuto) Color(0xFFFBF9F5) else Ink
                )
            }

            allLights.forEach { light ->
                val isSelected = selectedLightName == light.name
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(light.top)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) Ink else Ink.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .clickable { selectedLightName = light.name },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = light.text,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // 2. Content Source Section
        Text("Content Source", style = MakariosTypography.titleMedium, color = Ink)
        Spacer(Modifier.height(10.dp))

        listOf(
            WidgetSource.TODAY to "Today's declaration",
            WidgetSource.MY_DECLARATIONS to "My declarations (Kept & seed)",
            WidgetSource.TOPIC to "Specific topic",
            WidgetSource.SURPRISE to "Surprise (Daily rotation)"
        ).forEach { (source, label) ->
            val isSelected = selectedSource == source
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Ink.copy(alpha = 0.06f) else Color.Transparent)
                    .clickable { selectedSource = source }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { selectedSource = source },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFB5532B), unselectedColor = Ink.copy(alpha = 0.4f))
                )
                Spacer(Modifier.width(8.dp))
                Text(label, style = MakariosTypography.bodyLarge, color = Ink)
            }
        }

        // Topic selector if Topic source selected
        if (selectedSource == WidgetSource.TOPIC) {
            Spacer(Modifier.height(14.dp))
            Text("Select Topic", style = MakariosTypography.labelLarge, color = Ink.copy(alpha = 0.8f))
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topics.forEach { t ->
                    val isTopSel = selectedTopic == t
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isTopSel) Ink else Ink.copy(alpha = 0.08f))
                            .clickable { selectedTopic = t }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            t,
                            style = MakariosTypography.labelMedium,
                            color = if (isTopSel) Color(0xFFFBF9F5) else Ink
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onSave(selectedLightName, selectedSource, selectedTopic) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color(0xFFFBF9F5))
        ) {
            Text("Apply to Widget", style = MakariosTypography.labelLarge)
        }
    }
}

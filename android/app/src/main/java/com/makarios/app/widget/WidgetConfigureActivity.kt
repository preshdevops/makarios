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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.makarios.app.data.DeclarationSource
import com.makarios.app.data.WidgetLookMode
import com.makarios.app.data.WidgetRotateEvery
import com.makarios.app.data.WidgetSettings
import com.makarios.app.data.WidgetSettingsStore
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

        val existing = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
            WidgetSettingsStore.loadForWidget(this, appWidgetId) ?: WidgetSettingsStore.load(this)
        else WidgetSettingsStore.load(this)

        setContent {
            WidgetConfigureScreen(
                initial = existing,
                onSave = { newSettings ->
                    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                        WidgetSettingsStore.saveForWidget(this, appWidgetId, newSettings)
                    } else {
                        WidgetSettingsStore.save(this, newSettings)
                    }
                    // Also persist light for backward compat
                    WidgetStore.setFixedLight(this, newSettings.fixedLightName.ifEmpty { "AUTO" })

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
    initial: WidgetSettings,
    onSave: (WidgetSettings) -> Unit
) {
    var selectedSources by remember { mutableStateOf(initial.sources) }
    var selectedOrder by remember { mutableStateOf(initial.order) }
    var selectedRotate by remember { mutableStateOf(initial.rotateEvery) }
    var selectedLookMode by remember { mutableStateOf(initial.lookMode) }
    var selectedLightName by remember { mutableStateOf(initial.fixedLightName.ifEmpty { "AUTO" }) }

    val allLights = listOf(
        Light.Dawn, Light.Grove, Light.Midday, Light.Ember,
        Light.Dusk, Light.Rain, Light.Mist, Light.Night
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
            text = "Widget",
            style = MakariosTypography.displaySmall,
            color = Ink
        )
        Text(
            text = "Choose what appears on your home screen.",
            style = MakariosTypography.bodyMedium,
            color = Ink.copy(alpha = 0.65f),
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
        )

        // 1. Source chips — multi-select, at least one must stay on
        SectionLabel("Show from")
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SourceChip(
                label = "My declarations",
                selected = DeclarationSource.MINE in selectedSources,
                onClick = {
                    val toggled = if (DeclarationSource.MINE in selectedSources) {
                        selectedSources - DeclarationSource.MINE
                    } else {
                        selectedSources + DeclarationSource.MINE
                    }
                    if (toggled.isNotEmpty()) selectedSources = toggled
                }
            )
            SourceChip(
                label = "Saved",
                selected = DeclarationSource.KEPT in selectedSources,
                onClick = {
                    val toggled = if (DeclarationSource.KEPT in selectedSources) {
                        selectedSources - DeclarationSource.KEPT
                    } else {
                        selectedSources + DeclarationSource.KEPT
                    }
                    if (toggled.isNotEmpty()) selectedSources = toggled
                }
            )
            SourceChip(
                label = "Makarios picks",
                selected = DeclarationSource.MAKARIOS in selectedSources,
                onClick = {
                    val toggled = if (DeclarationSource.MAKARIOS in selectedSources) {
                        selectedSources - DeclarationSource.MAKARIOS
                    } else {
                        selectedSources + DeclarationSource.MAKARIOS
                    }
                    if (toggled.isNotEmpty()) selectedSources = toggled
                }
            )
        }

        Spacer(Modifier.height(28.dp))

        // 2. Rotate every
        SectionLabel("Rotate")
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                WidgetRotateEvery.ON_TAP to "On tap",
                WidgetRotateEvery.HOURLY to "Every hour",
                WidgetRotateEvery.THREE_HOUR to "Every 3 h",
                WidgetRotateEvery.DAILY to "Daily"
            ).forEach { (rotate, label) ->
                OptionChip(label, selectedRotate == rotate) { selectedRotate = rotate }
            }
        }

        Spacer(Modifier.height(28.dp))

        // 3. Look mode
        SectionLabel("Appearance")
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LookModeRow(
                label = "Use the Look it was made with",
                selected = selectedLookMode == WidgetLookMode.ORIGINAL
            ) { selectedLookMode = WidgetLookMode.ORIGINAL }
            LookModeRow(
                label = "One fixed Light",
                selected = selectedLookMode == WidgetLookMode.FIXED_LIGHT
            ) { selectedLookMode = WidgetLookMode.FIXED_LIGHT }
        }

        // Light picker when fixed mode selected
        if (selectedLookMode == WidgetLookMode.FIXED_LIGHT) {
            Spacer(Modifier.height(12.dp))
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
                        "Auto",
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
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                onSave(
                    WidgetSettings(
                        sources = selectedSources,
                        order = com.makarios.app.data.DeclarationOrder.SHUFFLE,
                        rotateEvery = selectedRotate,
                        lookMode = selectedLookMode,
                        fixedLightName = if (selectedLookMode == WidgetLookMode.FIXED_LIGHT) selectedLightName else ""
                    )
                )
            },
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

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MakariosTypography.titleMedium, color = Ink)
}

@Composable
private fun SourceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Ink else Ink.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            style = MakariosTypography.labelMedium,
            color = if (selected) Color(0xFFFBF9F5) else Ink
        )
    }
}

@Composable
private fun OptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Ink else Ink.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MakariosTypography.labelMedium,
            color = if (selected) Color(0xFFFBF9F5) else Ink
        )
    }
}

@Composable
private fun LookModeRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Ink.copy(alpha = 0.06f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFFB5532B),
                unselectedColor = Ink.copy(alpha = 0.4f)
            )
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MakariosTypography.bodyLarge, color = Ink)
    }
}

package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import com.makarios.app.ui.theme.*

/** Draws the Light gradient full-height edge-to-edge and publishes it to the bottom bar. Only screen background allowed. */
@Composable
fun LightScreen(light: Light = LocalLight.current, routeName: String = "", modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    ProvideActiveLight(light, routeName) {}
    SystemBarsController(light)
    Box(modifier.fillMaxSize().drawBehind { drawLight(light) }) { content() }
}

@Composable
fun MakChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val t = LocalLight.current.theme()
    Box(modifier.clip(RoundedCornerShape(50)).background(if (selected) t.text else t.surface)
        .border(1.dp, if (selected) t.text else t.line, RoundedCornerShape(50))
        .clickable { onClick() }.padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(label, style = MakariosTypography.labelLarge, color = if (selected) t.bottom else t.text)
    }
}

@Composable
fun MakButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = true) {
    val t = LocalLight.current.theme()
    if (primary) Button(onClick = onClick, modifier = modifier.height(50.dp), shape = RoundedCornerShape(25.dp), colors = ButtonDefaults.buttonColors(containerColor = t.text, contentColor = t.bottom)) { Text(label, style = MakariosTypography.labelLarge) }
    else OutlinedButton(onClick = onClick, modifier = modifier.height(50.dp), shape = RoundedCornerShape(25.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = t.text), border = androidx.compose.foundation.BorderStroke(1.dp, t.line)) { Text(label, style = MakariosTypography.labelLarge) }
}

/** Plain row with 1dp line divider — no card. */
@Composable
fun MakRow(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable RowScope.() -> Unit) {
    val t = LocalLight.current.theme()
    Column(modifier) {
        Row(Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable { onClick() } else Modifier).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, content = content)
        HorizontalDivider(color = t.line, thickness = 1.dp)
    }
}

@Composable
fun MakSurface(modifier: Modifier = Modifier, shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp), colors: CardColors? = null, elevation: CardElevation? = null, border: androidx.compose.foundation.BorderStroke? = null, content: @Composable BoxScope.() -> Unit) {
    val t = LocalLight.current.theme()
    Box(modifier.clip(shape).background(t.surface).then(if (border != null) Modifier.border(border, shape) else Modifier), content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakChip(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit, modifier: Modifier = Modifier, colors: SelectableChipColors? = null, enabled: Boolean = true) {
    val t = LocalLight.current.theme()
    Box(modifier.clip(RoundedCornerShape(50)).background(if (selected) t.text else t.surface).border(1.dp, if (selected) t.text else t.line, RoundedCornerShape(50)).clickable(enabled = enabled) { onClick() }.padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        CompositionLocalProvider(LocalContentColor provides if (selected) t.bottom else t.text) { label() }
    }
}

@Composable
fun MakSurface(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val t = LocalLight.current.theme()
    Box(modifier.clip(RoundedCornerShape(16.dp)).background(t.surface), content = content)
}

@Composable
fun MakSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val t = LocalLight.current.theme()
    Switch(checked, onCheckedChange, modifier, colors = SwitchDefaults.colors(checkedThumbColor = t.bottom, checkedTrackColor = t.text, uncheckedThumbColor = t.textMuted, uncheckedTrackColor = t.surface))
}

@Composable
fun MakSegmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val t = LocalLight.current.theme()
    Row(modifier.fillMaxWidth()) {
        options.forEachIndexed { i, label ->
            Column(Modifier.weight(1f).clickable { onSelect(i) }.padding(bottom = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MakariosTypography.labelLarge, color = if (i == selected) t.text else t.textMuted)
                Spacer(Modifier.height(6.dp))
                Box(Modifier.height(2.dp).fillMaxWidth().background(if (i == selected) t.accent else t.line))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val t = LocalLight.current.theme()
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = t.top, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth(), content = content)
    }
}

package com.makarios.app.ui.theme

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "All Lights", showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun AllLightsPreview() {
    Row(Modifier.fillMaxSize()) {
        Light.values().forEach {
            LightBackground(it, Modifier.weight(1f).fillMaxSize()) {}
        }
    }
}

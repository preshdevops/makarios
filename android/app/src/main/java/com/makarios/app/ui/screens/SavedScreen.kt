package com.makarios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.makarios.app.data.local.KeptEntity
import com.makarios.app.data.local.KeptRepository
import com.makarios.app.data.local.toAffirmation
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ShareHelper
import kotlinx.coroutines.launch

@Composable
fun SavedScreen(onNavigateToCreate: () -> Unit = {}, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var tab by remember { mutableStateOf("Declarations") }

    val declarations by KeptRepository.getDeclarationsFlow().collectAsState(initial = emptyList())
    val mine by KeptRepository.getMineFlow().collectAsState(initial = emptyList())
    val list = if (tab == "Mine") mine else declarations

    SystemBarsController(Light.Dawn)

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Ink,
                    contentColor = Color(0xFFFBF9F5),
                    actionColor = Color(0xFFB5532B)
                )
            }
        },
        containerColor = Color(0xFFFBF9F5),
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            Text(
                "Kept",
                style = MakariosTypography.displayLarge,
                color = Ink,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Row(
                Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                KeptTab("Declarations", tab == "Declarations") { tab = "Declarations" }
                KeptTab("Mine", tab == "Mine") { tab = "Mine" }
            }
            if (list.isEmpty()) {
                Text(
                    if (tab == "Mine") "Your written declarations will appear here." else "Nothing kept yet.",
                    color = Ink.copy(alpha = .65f),
                    modifier = Modifier.padding(32.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(list, key = { it.id }) { entity ->
                        val light = runCatching { Light.valueOf(entity.lightName) }.getOrDefault(Light.Grove)
                        KeptCard(
                            entity = entity,
                            light = light,
                            onShare = {
                                ShareHelper.shareGeneric(context, entity.toAffirmation(), light)
                            },
                            onRemove = {
                                coroutineScope.launch {
                                    KeptRepository.delete(entity)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Removed from Kept.",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        KeptRepository.save(entity)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeptCard(
    entity: KeptEntity,
    light: Light,
    onShare: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .drawBehind { drawLight(light, size, horizon = HorizonSpec(.72f, .80f, .88f)) }
            .padding(20.dp)
    ) {
        Pairing(entity.declaration, entity.verseText, entity.reference, light)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onShare) {
                Icon(Icons.Outlined.Share, "Share declaration", tint = light.text)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.Delete, "Remove from Kept", tint = light.text)
            }
        }
    }
}

@Composable
private fun KeptTab(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            label,
            color = if (selected) Ink else Ink.copy(alpha = .55f),
            style = MakariosTypography.labelLarge
        )
    }
}

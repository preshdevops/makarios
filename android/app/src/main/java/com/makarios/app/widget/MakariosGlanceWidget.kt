package com.makarios.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.makarios.app.data.AffirmationRepository

class MakariosGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val truth = AffirmationRepository.widgetAffirmation

        provideContent {
            GlanceWidgetContent(truth.declaration, truth.reference, truth.context)
        }
    }

    @Composable
    private fun GlanceWidgetContent(declaration: String, reference: String, contextText: String) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAF8F5)) // Porcelain
                .padding(16.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // Reference
                Text(
                    text = reference.uppercase(),
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFC46851)), // Terracotta
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                )

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Declaration
                Text(
                    text = "\u201C${declaration}\u201D",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF1F1118)), // Espresso
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    ),
                    maxLines = 3
                )

                Spacer(modifier = GlanceModifier.height(5.dp))

                // Context
                Text(
                    text = contextText,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF6B5E66)), // Stone
                        fontSize = 11.sp
                    ),
                    maxLines = 2
                )
            }
        }
    }
}

class MakariosGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MakariosGlanceWidget()
}

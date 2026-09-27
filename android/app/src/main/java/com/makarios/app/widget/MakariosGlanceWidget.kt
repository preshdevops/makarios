package com.makarios.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.makarios.app.MainActivity
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.util.WidgetHelper

class MakariosGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val truth = AffirmationRepository.widgetAffirmation
        val isDarkTheme = WidgetHelper.getWidgetTheme(context) == "twilight"

        provideContent {
            GlanceWidgetContent(
                declaration = truth.declaration,
                reference = truth.reference,
                scripture = truth.scriptureText,
                category = truth.category,
                isDark = isDarkTheme
            )
        }
    }

    @Composable
    private fun GlanceWidgetContent(
        declaration: String,
        reference: String,
        scripture: String,
        category: String,
        isDark: Boolean
    ) {
        // Aesthetic sacred color tokens
        val bgColor = if (isDark) Color(0xFF221C18) else Color(0xFFFAF7F2)
        val cardBg = if (isDark) Color(0xFF2D2520) else Color(0xFFFFFFFF)
        val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF2C2622)
        val secondaryText = if (isDark) Color(0xFFD4CBC4) else Color(0xFF6B625B)
        val accentColor = if (isDark) Color(0xFFDEAC46) else Color(0xFFA85842)
        val scriptureBoxBg = if (isDark) Color(0xFF1B1512) else Color(0xFFF3EDE3)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(22.dp)
                .background(bgColor)
                .clickable(actionStartActivity(MainActivity::class.java))
                .padding(14.dp)
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(18.dp)
                    .background(cardBg)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                // Header Row: Category Badge & Makarios Mark
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "M A K A R I O S   ·   ${category.uppercase()}",
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // The Declaration Quote
                Text(
                    text = "“${declaration.trim()}”",
                    style = TextStyle(
                        color = ColorProvider(primaryText),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    maxLines = 3
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Grounding Scripture Container
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .cornerRadius(10.dp)
                        .background(scriptureBoxBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (scripture.isNotBlank()) {
                            Text(
                                text = "“${scripture.trim()}”",
                                style = TextStyle(
                                    color = ColorProvider(secondaryText),
                                    fontSize = 11.5.sp
                                ),
                                maxLines = 2
                            )
                            Spacer(modifier = GlanceModifier.height(3.dp))
                        }
                        Text(
                            text = "— ${reference.uppercase()} —",
                            style = TextStyle(
                                color = ColorProvider(accentColor),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

class MakariosGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MakariosGlanceWidget()
}

package com.makarios.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
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

    companion object {
        val SIZE_2X2 = DpSize(120.dp, 120.dp)
        val SIZE_4X2 = DpSize(240.dp, 100.dp)
        val SIZE_4X3 = DpSize(240.dp, 180.dp)
        val SIZE_4X4 = DpSize(240.dp, 260.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(SIZE_2X2, SIZE_4X2, SIZE_4X3, SIZE_4X4)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val truth = AffirmationRepository.widgetAffirmation
        val isDarkTheme = WidgetHelper.getWidgetTheme(context) == "twilight"

        provideContent {
            GlanceWidgetContent(
                context = context,
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
        context: Context,
        declaration: String,
        reference: String,
        scripture: String,
        category: String,
        isDark: Boolean
    ) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val size = LocalSize.current

        // Palette tokens: crisp, warm, single-surface (no nested boxes)
        val bgColor = if (isDark) Color(0xFF1E1815) else Color(0xFFFAF7F2)
        val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF2C2622)
        val secondaryText = if (isDark) Color(0xFFC7BCB3) else Color(0xFF6B625B)
        val accentColor = if (isDark) Color(0xFFDEAC46) else Color(0xFFA85842)

        val isSmall = size.width < 180.dp
        val isBanner = !isSmall && size.height < 140.dp
        val isFeature = !isSmall && size.height in 140.dp..220.dp

        // Single clean surface without nested cards or boxes
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(22.dp)
                .background(bgColor)
                .clickable(actionStartActivity(launchIntent))
                .padding(if (isSmall) 12.dp else 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            when {
                // ── 2×2 Compact Square ──
                isSmall -> {
                    Text(
                        text = category.uppercase(),
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    Text(
                        text = "“${declaration.trim()}”",
                        style = TextStyle(
                            color = ColorProvider(primaryText),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = reference.uppercase(),
                        style = TextStyle(
                            color = ColorProvider(secondaryText),
                            fontSize = 9.sp
                        )
                    )
                }

                // ── 4×2 Horizontal Banner ──
                isBanner -> {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.uppercase(),
                            style = TextStyle(
                                color = ColorProvider(accentColor),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    Text(
                        text = "“${declaration.trim()}”",
                        style = TextStyle(
                            color = ColorProvider(primaryText),
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        maxLines = 2
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "— ${reference.uppercase()}",
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    )
                }

                // ── 4×3 Editorial Feature ──
                isFeature -> {
                    Text(
                        text = category.uppercase(),
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "“${declaration.trim()}”",
                        style = TextStyle(
                            color = ColorProvider(primaryText),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.5.sp
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    if (scripture.isNotBlank()) {
                        Text(
                            text = "“${scripture.trim()}”",
                            style = TextStyle(
                                color = ColorProvider(secondaryText),
                                fontSize = 11.5.sp
                            ),
                            maxLines = 2
                        )
                        Spacer(modifier = GlanceModifier.height(6.dp))
                    }
                    Text(
                        text = "— ${reference.uppercase()}",
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }

                // ── 4×4 Full Hero ──
                else -> {
                    Text(
                        text = category.uppercase(),
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(10.dp))
                    Text(
                        text = "“${declaration.trim()}”",
                        style = TextStyle(
                            color = ColorProvider(primaryText),
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        ),
                        maxLines = 4
                    )
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    if (scripture.isNotBlank()) {
                        Text(
                            text = "“${scripture.trim()}”",
                            style = TextStyle(
                                color = ColorProvider(secondaryText),
                                fontSize = 12.5.sp
                            ),
                            maxLines = 3
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "— ${reference.uppercase()}",
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

class MakariosGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MakariosGlanceWidget()
}

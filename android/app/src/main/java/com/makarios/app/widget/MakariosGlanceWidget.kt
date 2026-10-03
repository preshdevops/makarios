package com.makarios.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
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
import com.makarios.app.R
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.util.WidgetHelper

class ShuffleActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        WidgetHelper.shuffleWidget(context)
    }
}

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

        // Palette tokens: quiet, grounded, single-surface (no nested boxes)
        val bgColor = if (isDark) Color(0xFF161210) else Color(0xFFFAF7F2)
        val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF2C2622)
        val metadataColor = if (isDark) Color.White.copy(alpha = 0.40f) else Color(0xFF2C2622).copy(alpha = 0.40f)
        val secondaryText = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF6B625B)
        val buttonTint = if (isDark) Color.White.copy(alpha = 0.70f) else Color(0xFF2C2622).copy(alpha = 0.70f)
        val buttonBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFF2C2622).copy(alpha = 0.06f)

        val isSmall = size.width < 180.dp
        val isBanner = !isSmall && size.height < 140.dp
        val isFeature = !isSmall && size.height in 140.dp..220.dp

        // Single clean surface without nested cards or boxes
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(24.dp)
                .background(bgColor)
                .clickable(actionStartActivity(launchIntent))
                .padding(if (isSmall) 14.dp else 18.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            when {
                // ── 2×2 Compact Square ──
                isSmall -> {
                    // Top Left Metadata: Left-aligned, tiny 10pt all-caps font, 40% opacity
                    Text(
                        text = "DAILY ATTUNEMENT",
                        style = TextStyle(
                            color = ColorProvider(metadataColor),
                            fontWeight = FontWeight.Medium,
                            fontSize = 9.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // Center Main Text: Bold font
                    Text(
                        text = "“${declaration.trim()}”",
                        style = TextStyle(
                            color = ColorProvider(primaryText),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Bottom Row: Reference on left, Shuffle button tucked into bottom right
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reference.uppercase(),
                            style = TextStyle(
                                color = ColorProvider(secondaryText),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )

                        Box(
                            modifier = GlanceModifier
                                .size(24.dp)
                                .cornerRadius(12.dp)
                                .background(buttonBg)
                                .clickable(actionRunCallback<ShuffleActionCallback>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_widget_loop),
                                contentDescription = "Shuffle declaration",
                                colorFilter = ColorFilter.tint(ColorProvider(buttonTint)),
                                modifier = GlanceModifier.size(13.dp)
                            )
                        }
                    }
                }

                // ── 4×2 Horizontal Banner ──
                isBanner -> {
                    // Top Left Metadata: Left-aligned, tiny 10pt all-caps font, 40% opacity
                    Text(
                        text = "DAILY ATTUNEMENT  ·  ${category.uppercase()}",
                        style = TextStyle(
                            color = ColorProvider(metadataColor),
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // Center Main Text: Bold 18pt font
                    Text(
                        text = "“${declaration.trim()}”",
                        style = TextStyle(
                            color = ColorProvider(primaryText),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.5.sp
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Bottom Row: Reference on left, Shuffle button tucked into bottom right
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "— ${reference.uppercase()}",
                            style = TextStyle(
                                color = ColorProvider(secondaryText),
                                fontWeight = FontWeight.Medium,
                                fontSize = 9.5.sp
                            )
                        )

                        Box(
                            modifier = GlanceModifier
                                .size(28.dp)
                                .cornerRadius(14.dp)
                                .background(buttonBg)
                                .clickable(actionRunCallback<ShuffleActionCallback>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_widget_loop),
                                contentDescription = "Shuffle declaration",
                                colorFilter = ColorFilter.tint(ColorProvider(buttonTint)),
                                modifier = GlanceModifier.size(15.dp)
                            )
                        }
                    }
                }

                // ── 4×3 Editorial Feature ──
                isFeature -> {
                    // Top Left Metadata: Left-aligned, tiny 10pt all-caps font, 40% opacity
                    Text(
                        text = "DAILY ATTUNEMENT  ·  ${category.uppercase()}",
                        style = TextStyle(
                            color = ColorProvider(metadataColor),
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // Center Main Text: Bold 18pt font
                    Text(
                        text = "“${declaration.trim()}”",
                        style = TextStyle(
                            color = ColorProvider(primaryText),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.5.sp
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    if (scripture.isNotBlank()) {
                        Text(
                            text = "“${scripture.trim()}”",
                            style = TextStyle(
                                color = ColorProvider(secondaryText),
                                fontSize = 11.5.sp
                            ),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Bottom Row: Reference on left, Shuffle button tucked into bottom right
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "— ${reference.uppercase()}",
                            style = TextStyle(
                                color = ColorProvider(secondaryText),
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        )

                        Box(
                            modifier = GlanceModifier
                                .size(30.dp)
                                .cornerRadius(15.dp)
                                .background(buttonBg)
                                .clickable(actionRunCallback<ShuffleActionCallback>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_widget_loop),
                                contentDescription = "Shuffle declaration",
                                colorFilter = ColorFilter.tint(ColorProvider(buttonTint)),
                                modifier = GlanceModifier.size(16.dp)
                            )
                        }
                    }
                }

                // ── 4×4 Full Hero ──
                else -> {
                    // Top Left Metadata: Left-aligned, tiny 10pt all-caps font, 40% opacity
                    Text(
                        text = "DAILY ATTUNEMENT  ·  ${category.uppercase()}",
                        style = TextStyle(
                            color = ColorProvider(metadataColor),
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.5.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(10.dp))

                    // Center Main Text: Bold 18pt+ font
                    Text(
                        text = "“${declaration.trim()}”",
                        style = TextStyle(
                            color = ColorProvider(primaryText),
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = GlanceModifier.height(10.dp))

                    if (scripture.isNotBlank()) {
                        Text(
                            text = "“${scripture.trim()}”",
                            style = TextStyle(
                                color = ColorProvider(secondaryText),
                                fontSize = 12.5.sp
                            ),
                            maxLines = 3
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Bottom Row: Reference on left, Shuffle button tucked into bottom right
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "— ${reference.uppercase()}",
                            style = TextStyle(
                                color = ColorProvider(secondaryText),
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )

                        Box(
                            modifier = GlanceModifier
                                .size(32.dp)
                                .cornerRadius(16.dp)
                                .background(buttonBg)
                                .clickable(actionRunCallback<ShuffleActionCallback>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_widget_loop),
                                contentDescription = "Shuffle declaration",
                                colorFilter = ColorFilter.tint(ColorProvider(buttonTint)),
                                modifier = GlanceModifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

class MakariosGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MakariosGlanceWidget()
}

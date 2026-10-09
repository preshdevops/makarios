package com.makarios.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.makarios.app.R
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.Light
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

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val affirmation = AffirmationRepository.activeWidgetAffirmation 
                ?: AffirmationRepository.getAll().firstOrNull() 
                ?: return@provideContent
                
            // Resolve Light based on topic or time. For widget, perhaps topic light.
            // Let's use Mist as a fallback light if category unmapped
            val light = Light.Mist // Ideally derived from topic Light map

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(R.drawable.grain_tile)) // We can't do linear gradient easily without an image, but we can set a flat background color as fallback or use an image provider for the gradient
                    .background(light.top) // Fallback 
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start
                ) {
                    // The Pairing replication in Glance
                    Text(
                        text = affirmation.declaration,
                        style = TextStyle(
                            color = ColorProvider(light.text),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(16.dp))
                    Box(
                        modifier = GlanceModifier.width(28.dp).height(2.dp).background(light.anchorRule)
                    ) {}
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    Text(
                        text = affirmation.scriptureText,
                        style = TextStyle(
                            color = ColorProvider(light.text),
                            fontSize = 15.sp,
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = affirmation.reference,
                        style = TextStyle(
                            color = ColorProvider(light.text.copy(alpha = light.secondaryAlpha)),
                            fontSize = 12.sp,
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

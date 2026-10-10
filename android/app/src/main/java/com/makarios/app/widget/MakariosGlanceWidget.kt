package com.makarios.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.ImageProvider
import com.makarios.app.MainActivity
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.renderLightBitmap
import com.makarios.app.util.WidgetHelper
import java.time.LocalDate

enum class WidgetSize { SMALL, MEDIUM, LARGE }
enum class WidgetSource { KEPT, DAILY, PICK_ONE }

object WidgetStore {
    private const val PREFS = "makarios_widget_prefs"
    private const val LIGHT = "light"
    private const val SOURCE = "source"
    private const val PICK = "pick_id"
    fun light(context:Context):Light = context.getSharedPreferences(PREFS,0).getString(LIGHT,"AUTO")?.let { if(it=="AUTO") Light.forNow() else runCatching{Light.valueOf(it)}.getOrNull() } ?: Light.forNow()
    fun fixedLight(context:Context):String = context.getSharedPreferences(PREFS,0).getString(LIGHT,"AUTO") ?: "AUTO"
    fun source(context:Context):WidgetSource = runCatching{WidgetSource.valueOf(context.getSharedPreferences(PREFS,0).getString(SOURCE,WidgetSource.KEPT.name)!!)}.getOrDefault(WidgetSource.KEPT)
    fun setLight(context:Context,light:String){context.getSharedPreferences(PREFS,0).edit().putString(LIGHT,light).apply();WidgetScheduling.refreshNow(context)}
    fun set(context:Context,light:String,source:WidgetSource,pickId:String?){context.getSharedPreferences(PREFS,0).edit().putString(LIGHT,light).putString(SOURCE,source.name).putString(PICK,pickId).apply()}
    fun affirmation(context:Context):Affirmation {
        val repo=AffirmationRepository
        val specific = when(source(context)){
            WidgetSource.KEPT->repo.getSaved().firstOrNull()
            WidgetSource.PICK_ONE->repo.getById(context.getSharedPreferences(PREFS,0).getString(PICK,"")?:"")
            WidgetSource.DAILY->null
        }
        if (specific != null) return specific
        val pool = repo.getAll().filter{it.id!=""}.ifEmpty { listOf(repo.affirmationOfTheDay) }
        return com.makarios.app.data.OnboardingStore.getWeightedAffirmation(context, pool, kotlin.random.Random(LocalDate.now().dayOfYear))
    }
}

abstract class MakariosWidget(private val size:WidgetSize):GlanceAppWidget(){
    override suspend fun provideGlance(context:Context,id:GlanceId){
        val dp=when(size){WidgetSize.SMALL->DpSize(163.dp,163.dp);WidgetSize.MEDIUM->DpSize(342.dp,163.dp);WidgetSize.LARGE->DpSize(342.dp,342.dp)}
        val density=context.resources.displayMetrics.density
        val bitmap=renderLightBitmap(WidgetStore.light(context),(dp.width.value*density*2).toInt(),(dp.height.value*density*2).toInt())
        val affirmation=WidgetStore.affirmation(context);val light=WidgetStore.light(context)
        provideContent{WidgetContent(size,bitmap,affirmation,light)}
    }
}

@Composable private fun WidgetContent(size:WidgetSize,bitmap:android.graphics.Bitmap,affirmation:Affirmation,light:Light){
    val padding=when(size){WidgetSize.SMALL->16.dp;WidgetSize.MEDIUM->20.dp;WidgetSize.LARGE->24.dp}
    Box(GlanceModifier.fillMaxSize().cornerRadius(28.dp)){
        Image(ImageProvider(bitmap),"Makarios ${light.name}",GlanceModifier.fillMaxSize())
        Column(GlanceModifier.fillMaxSize().padding(padding).clickable(actionStartActivity<MainActivity>()),verticalAlignment=Alignment.CenterVertically){
            val declaration=affirmation.shortText
            Text(declaration,style=TextStyle(color=ColorProvider(light.text),fontSize=when(size){WidgetSize.SMALL->20.sp;WidgetSize.MEDIUM->21.sp;WidgetSize.LARGE->25.sp},fontWeight=FontWeight.Medium),maxLines=when(size){WidgetSize.SMALL,WidgetSize.MEDIUM->2;WidgetSize.LARGE->3})
            Spacer(GlanceModifier.height(10.dp));Box(GlanceModifier.width(when(size){WidgetSize.SMALL->20.dp;else->28.dp}).height(2.dp).background(ColorProvider(light.anchorRule))){ }
            if(size!=WidgetSize.SMALL){Spacer(GlanceModifier.height(9.dp));Text(affirmation.scriptureText,style=TextStyle(color=ColorProvider(light.text),fontSize=when(size){WidgetSize.MEDIUM->13.sp;else->14.sp}),maxLines=when(size){WidgetSize.MEDIUM->1;else->2})}
            Spacer(GlanceModifier.height(6.dp));Text(affirmation.reference,style=TextStyle(color=ColorProvider(light.text.copy(alpha=light.secondaryAlpha)),fontSize=when(size){WidgetSize.SMALL->11.sp;else->12.sp},fontWeight=FontWeight.Medium),maxLines=1)
            if(size==WidgetSize.LARGE){Spacer(GlanceModifier.defaultWeight());Text("makarios",style=TextStyle(color=ColorProvider(light.text.copy(alpha=.8f)),fontSize=13.sp,fontWeight=FontWeight.Medium),modifier=GlanceModifier.fillMaxWidth())}
        }
    }
}

class MakariosSmallWidget:MakariosWidget(WidgetSize.SMALL)
class MakariosMediumWidget:MakariosWidget(WidgetSize.MEDIUM)
class MakariosLargeWidget:MakariosWidget(WidgetSize.LARGE)
class MakariosSmallReceiver:GlanceAppWidgetReceiver(){override val glanceAppWidget=MakariosSmallWidget()}
class MakariosMediumReceiver:GlanceAppWidgetReceiver(){override val glanceAppWidget=MakariosMediumWidget()}
class MakariosLargeReceiver:GlanceAppWidgetReceiver(){override val glanceAppWidget=MakariosLargeWidget()}

class ShuffleActionCallback:androidx.glance.appwidget.action.ActionCallback{override suspend fun onAction(context:Context,glanceId:GlanceId,parameters:androidx.glance.action.ActionParameters){WidgetHelper.shuffleWidget(context)}}

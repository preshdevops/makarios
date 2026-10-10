package com.makarios.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.AuthManager
import com.makarios.app.ui.components.AuthDialog
import com.makarios.app.ui.components.SacredTimePickerDialog
import com.makarios.app.ui.theme.*
import com.makarios.app.widget.WidgetConfigureActivity
import com.makarios.app.widget.WidgetStore
import com.makarios.app.widget.WidgetScheduling
import java.time.LocalDate

object ProfileActivityLog {
    private const val PREFS="makarios_activity_log"
    private const val SHOWN="shown"
    private const val ACTION="action"
    private fun prefs(context:Context)=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
    private fun today()=LocalDate.now().toString()
    fun recordShown(context:Context){val p=prefs(context);p.edit().putStringSet(SHOWN,(p.getStringSet(SHOWN,emptySet())?:emptySet())+today()).apply()}
    fun recordQualified(context:Context){val p=prefs(context);val shown=p.getStringSet(SHOWN,emptySet())?:emptySet();if(today() in shown)p.edit().putStringSet(ACTION,(p.getStringSet(ACTION,emptySet())?:emptySet())+today()).apply()}
    fun recordOpened(context:Context)=recordQualified(context)
    fun recordKept(context:Context)=recordQualified(context)
    fun recordShared(context:Context)=recordQualified(context)
    fun qualifiedDates(context:Context):Set<LocalDate>=(prefs(context).getStringSet(ACTION,emptySet())?:emptySet()).mapNotNull{runCatching{LocalDate.parse(it)}.getOrNull()}.toSet()
    fun streak(context:Context):Int{val dates=qualifiedDates(context);var date=LocalDate.now();var count=0;while(date in dates){count++;date=date.minusDays(1)};return count}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onSignOut:()->Unit,onOpenWallpapers:()->Unit={},modifier:Modifier=Modifier){
    val context=LocalContext.current
    val user=AuthManager.currentUser;val guest=AuthManager.isAnonymous||user==null;val name=AuthManager.displayName
    val currentLight=remember{Light.forNow()};var widgetLight by remember{mutableStateOf(WidgetStore.fixedLight(context))}
    var showAuth by remember{mutableStateOf(false)};var signUp by remember{mutableStateOf(false)};var showTimePicker by remember{mutableStateOf(false)}
    var reminderEnabled by remember{mutableStateOf(com.makarios.app.util.ReminderManager.isDailyReminderEnabled(context))}
    var hour by remember{mutableStateOf(com.makarios.app.util.ReminderManager.getReminderHour(context))};var minute by remember{mutableStateOf(com.makarios.app.util.ReminderManager.getReminderMinute(context))}
    val kept=AffirmationRepository.getSaved().size;val written=AffirmationRepository.personalAffirmations.size;val shared=ProfileActivityLog.qualifiedDates(context).size;val streak=ProfileActivityLog.streak(context);val dates=ProfileActivityLog.qualifiedDates(context)
    val navBottom=WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val invite=remember(user){(user?.uid?:"XR79K").take(5).uppercase()}
    LazyColumn(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF4D6),Color(0xFFF6E3B0)))),contentPadding=PaddingValues(start=24.dp,end=24.dp,top=48.dp,bottom=navBottom+24.dp),verticalArrangement=Arrangement.spacedBy(0.dp)){
        item{Text("You",style=MakariosTypography.displayLarge.copy(fontSize=34.sp),color=Ink);Spacer(Modifier.height(24.dp))}
        item{ProfileLightCard(currentLight,name,guest,user?.email,onSignIn={signUp=true;showAuth=true},onSignOut=onSignOut);Spacer(Modifier.height(28.dp))}
        item{Text(streak.toString(),style=MakariosTypography.displayLarge.copy(fontSize=48.sp,fontFeatureSettings="lnum, tnum"),color=Ink);Text("days of declaring",style=MakariosTypography.bodyLarge.copy(fontFamily=NewsreaderFontFamily),color=Ink);Spacer(Modifier.height(14.dp));WeekRow(dates);Spacer(Modifier.height(28.dp))}
        item{Hairline();StatsRow(kept,written,shared);Hairline();Spacer(Modifier.height(28.dp))}
        item{Text("Daily declaration",style=MakariosTypography.bodyLarge.copy(fontFamily=NewsreaderFontFamily,fontStyle=FontStyle.Italic,fontSize=20.sp),color=Ink);Spacer(Modifier.height(4.dp));Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Receive a quiet prompt at your chosen time",style=MakariosTypography.labelSmall,color=Ink.copy(alpha=.68f),modifier=Modifier.weight(1f));Switch(checked=reminderEnabled,onCheckedChange={reminderEnabled=it;com.makarios.app.util.ReminderManager.setDailyReminderEnabled(context,it)},colors=SwitchDefaults.colors(checkedThumbColor=Cream,checkedTrackColor=Ink,uncheckedThumbColor=Ink,uncheckedTrackColor=Ink.copy(alpha=.18f)))};if(reminderEnabled){Spacer(Modifier.height(12.dp));ReminderChips(hour,minute,onSelect={h,m->hour=h;minute=m;com.makarios.app.util.ReminderManager.setReminderTime(context,h,m)},onCustom={showTimePicker=true})};Spacer(Modifier.height(28.dp))}
        item{Text("Make it yours",style=MakariosTypography.bodyLarge.copy(fontFamily=NewsreaderFontFamily,fontStyle=FontStyle.Italic,fontSize=20.sp),color=Ink);Spacer(Modifier.height(8.dp));MakeRow(Icons.Outlined.WbSunny,"Light",if(widgetLight=="AUTO")"Auto" else widgetLight){widgetLight=if(widgetLight=="AUTO")"AUTO" else widgetLight};MakeRow(Icons.Outlined.Widgets,"Widgets","3 sizes"){context.startActivity(Intent(context,WidgetConfigureActivity::class.java))};MakeRow(Icons.Outlined.Wallpaper,"Wallpapers","6 styles",onOpenWallpapers);Spacer(Modifier.height(28.dp))}
        item{InviteSection(invite,context);Spacer(Modifier.height(28.dp));Text("Makarios works without internet. Your declarations never leave this phone unless you sign in to sync.",style=MakariosTypography.labelSmall.copy(fontSize=12.sp),color=Ink.copy(alpha=.72f),modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(12.dp))}
    }
    if(showAuth)AuthDialog(initialIsSignUp=signUp,onDismiss={showAuth=false},onSuccess={showAuth=false;Toast.makeText(context,"Welcome to Makarios!",Toast.LENGTH_SHORT).show()})
    if(showTimePicker)SacredTimePickerDialog(initialHour=hour,initialMinute=minute,onConfirm={h,m->hour=h;minute=m;com.makarios.app.util.ReminderManager.setReminderTime(context,h,m);showTimePicker=false},onDismiss={showTimePicker=false})
}

@Composable private fun ProfileLightCard(light:Light,name:String,guest:Boolean,email:String?,onSignIn:()->Unit,onSignOut:()->Unit){Column(Modifier.fillMaxWidth().height(232.dp).clip(RoundedCornerShape(28.dp)).drawBehind{drawLight(light,size=size,horizon=HorizonSpec(.70f,.79f,.88f),discScale=.8f)}.padding(20.dp)){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(56.dp).clip(CircleShape).background(Ink),contentAlignment=Alignment.Center){Text(if(guest)"M" else name.firstOrNull()?.uppercase() ?: "M",style=MakariosTypography.displayMedium.copy(fontSize=26.sp),color=Cream)};Spacer(Modifier.width(14.dp));Column{Text(name,style=MakariosTypography.displaySmall.copy(fontSize=26.sp),color=light.text);Text(if(guest)"Saved on this phone only" else email?:"Synced",style=MakariosTypography.labelSmall,color=light.text.copy(alpha=.78f))}};Spacer(Modifier.weight(1f));if(guest)Button(onClick=onSignIn,shape=RoundedCornerShape(22.dp),colors=ButtonDefaults.buttonColors(containerColor=Ink,contentColor=Cream)){Text("Sign in to keep them safe",style=MakariosTypography.labelSmall)}else{Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Check,null,tint=light.text,modifier=Modifier.size(16.dp));Spacer(Modifier.width(4.dp));Text("Synced",style=MakariosTypography.labelMedium,color=light.text)}}}
}
@Composable private fun WeekRow(dates:Set<LocalDate>){val today=LocalDate.now();Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){(6 downTo 0).forEach{offset->val date=today.minusDays(offset.toLong());val done=date in dates;val isToday=date==today;Column(horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.size(28.dp).clip(CircleShape).background(if(done)Brush.verticalGradient(listOf(Light.Dawn.top,Light.Dawn.bottom)) else Brush.linearGradient(listOf(Ink.copy(alpha=.08f),Ink.copy(alpha=.08f)))).border(if(isToday)2.dp else 1.dp,if(isToday)Ink else if(done)AnchorLight else Color.Transparent,CircleShape));Text(date.dayOfWeek.name.take(1),style=MakariosTypography.labelSmall.copy(fontSize=12.sp),color=Ink.copy(alpha=.72f))}}}}
@Composable private fun Hairline(){Box(Modifier.fillMaxWidth().height(1.dp).background(AnchorLight.copy(alpha=.32f)))}
@Composable private fun StatsRow(kept:Int,written:Int,shared:Int){Row(Modifier.fillMaxWidth().padding(vertical=14.dp),horizontalArrangement=Arrangement.SpaceEvenly){listOf("$kept" to "Kept","$written" to "Written","$shared" to "Shared").forEach{(number,label)->Column(horizontalAlignment=Alignment.CenterHorizontally){Text(number,style=MakariosTypography.displaySmall.copy(fontSize=28.sp,fontFeatureSettings="lnum, tnum"),color=Ink);Text(label,style=MakariosTypography.labelSmall,color=Ink.copy(alpha=.7f))}}}}
@Composable private fun ReminderChips(hour:Int,minute:Int,onSelect:(Int,Int)->Unit,onCustom:()->Unit){Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("6 AM" to (6 to 0),"6:30 AM" to (6 to 30),"7 AM" to (7 to 0)).forEach{(label,time)->val selected=hour==time.first&&minute==time.second;TimeChip(label,selected){onSelect(time.first,time.second)}};TimeChip("Custom",hour !in listOf(6,7)||minute !in listOf(0,30),onCustom)}}
@Composable private fun TimeChip(label:String,selected:Boolean,onClick:()->Unit){Text(label,style=MakariosTypography.labelSmall,color=if(selected)Cream else Ink,modifier=Modifier.clip(RoundedCornerShape(18.dp)).background(if(selected)Ink else Ink.copy(alpha=.07f)).clickable(onClick=onClick).padding(horizontal=14.dp,vertical=9.dp),maxLines=1)}
@Composable private fun MakeRow(icon:androidx.compose.ui.graphics.vector.ImageVector,label:String,value:String,onClick:()->Unit){Column{Row(Modifier.fillMaxWidth().height(56.dp).clickable(onClick=onClick),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=Ink,modifier=Modifier.size(20.dp));Spacer(Modifier.width(14.dp));Text(label,style=MakariosTypography.bodyMedium,color=Ink,modifier=Modifier.weight(1f));Text(value,style=MakariosTypography.labelSmall,color=Ink.copy(alpha=.68f));Spacer(Modifier.width(8.dp));Icon(Icons.Outlined.ChevronRight,null,tint=Ink.copy(alpha=.55f))};Hairline()}}
@Composable private fun InviteSection(code:String,context:Context){Text("Invite a friend",style=MakariosTypography.bodyLarge.copy(fontFamily=NewsreaderFontFamily,fontStyle=FontStyle.Italic,fontSize=20.sp),color=Ink);Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.clip(RoundedCornerShape(12.dp)).background(Ink.copy(alpha=.07f)).padding(horizontal=16.dp,vertical=10.dp)){Text(code,style=MakariosTypography.bodyMedium.copy(letterSpacing=.14.em,fontFeatureSettings="tnum"),color=Ink)};Spacer(Modifier.weight(1f));OutlinedButton(onClick={val clip=ClipData.newPlainText("Makarios invite code",code);(context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)},shape=RoundedCornerShape(20.dp),border=BorderStroke(1.dp,Ink.copy(alpha=.25f)),contentPadding=PaddingValues(horizontal=14.dp)){Text("Copy",color=Ink)};Spacer(Modifier.width(8.dp));Button(onClick={context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,"Join me on Makarios: $code")},"Share link"))},shape=RoundedCornerShape(20.dp),contentPadding=PaddingValues(horizontal=14.dp),colors=ButtonDefaults.buttonColors(containerColor=Ink,contentColor=Cream)){Text("Share link")}}}




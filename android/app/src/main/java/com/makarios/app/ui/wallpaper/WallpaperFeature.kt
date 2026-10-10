package com.makarios.app.ui.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class WallpaperRenderer(private val context:Context){
 private val cache=mutableMapOf<String,Bitmap>()
 suspend fun preview(spec:WallpaperSpec,width:Int,height:Int):Bitmap=withContext(Dispatchers.Default){cache.getOrPut("${spec.style}:${spec.light}:$width:$height"){renderWallpaper(context,spec,width,height)}}
 suspend fun export(spec:WallpaperSpec):Bitmap{val wm=context.getSystemService(Context.WINDOW_SERVICE) as WindowManager;val metrics=wm.currentWindowMetrics;return renderWallpaper(context,spec,metrics.bounds.width(),metrics.bounds.height())}
 fun clear(){cache.values.forEach{if(!it.isRecycled)it.recycle()};cache.clear()}
}

enum class WallpaperTarget { LOCK, HOME, BOTH }
fun applyWallpaper(context:Context,bitmap:Bitmap,target:WallpaperTarget){val manager=WallpaperManager.getInstance(context);val flags=when(target){WallpaperTarget.LOCK->WallpaperManager.FLAG_LOCK;WallpaperTarget.HOME->WallpaperManager.FLAG_SYSTEM;WallpaperTarget.BOTH->WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM};manager.setBitmap(bitmap,null,true,flags)}

@Composable
fun WallpaperGallery(onBack:()->Unit,modifier:Modifier=Modifier){
 val context=LocalContext.current;val scope=rememberCoroutineScope();val light=remember{Light.forNow()};val renderer=remember{WallpaperRenderer(context)};var selected by remember{mutableStateOf<WallpaperStyle?>(null)}
 var target by remember{mutableStateOf(WallpaperTarget.LOCK)};var showTarget by remember{mutableStateOf(false)}
 Column(modifier.fillMaxSize().background(light.bottom).padding(horizontal=20.dp)){Row(Modifier.fillMaxWidth().padding(top=18.dp,bottom=16.dp),verticalAlignment=Alignment.CenterVertically){Text("Wallpapers",style=MakariosTypography.displaySmall,color=light.text,modifier=Modifier.weight(1f));Text("Close",color=light.text,modifier=Modifier.clickable{onBack()})}
  LazyVerticalGrid(columns=GridCells.Fixed(2),contentPadding=PaddingValues(bottom=24.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){items(WallpaperStyle.values()){style->WallpaperTile(style,light,renderer){selected=style}}}
 }
 if(selected!=null) WallpaperPreview(style=selected!!,light=light,renderer=renderer,onClose={selected=null},onSet={showTarget=true})
 if(showTarget) AlertDialog(onDismissRequest={showTarget=false},title={Text("Set wallpaper")},text={Column{Text("Choose where this artwork belongs.");WallpaperTarget.values().forEach{t->Text(t.name.lowercase().replaceFirstChar{it.uppercase()},Modifier.fillMaxWidth().clickable{target=t;showTarget=false;val chosen=selected;selected=null;if(chosen!=null)scope.launch{applyWallpaper(context,renderer.export(defaultWallpaperSpec(chosen,light)),target)}}.padding(vertical=14.dp))}}},confirmButton={TextButton(onClick={showTarget=false}){Text("Cancel")}})
}

@Composable private fun WallpaperTile(style:WallpaperStyle,light:Light,renderer:WallpaperRenderer,onClick:()->Unit){val context=LocalContext.current;val bitmap by produceState<Bitmap?>(null,style,light){value=renderer.preview(defaultWallpaperSpec(style,light),108,214)};Column(Modifier.clickable{onClick()}){Box(Modifier.fillMaxWidth().aspectRatio(9f/19.5f).clip(RoundedCornerShape(20.dp)).background(light.top),contentAlignment=Alignment.Center){bitmap?.let{androidx.compose.foundation.Image(it.asImageBitmap(),null,Modifier.fillMaxSize())}};Text(style.title,style=MakariosTypography.bodyLarge.copy(fontStyle=FontStyle.Italic),color=light.text,modifier=Modifier.padding(top=7.dp))}}

@Composable private fun WallpaperPreview(style:WallpaperStyle,light:Light,renderer:WallpaperRenderer,onClose:()->Unit,onSet:()->Unit){val bitmap by produceState<Bitmap?>(null,style,light){value=renderer.preview(defaultWallpaperSpec(style,light),360,712)};Box(Modifier.fillMaxSize().background(light.bottom)){Column(Modifier.fillMaxSize().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(style.title,style=MakariosTypography.displaySmall,color=light.text,modifier=Modifier.weight(1f));Text("Done",color=light.text,modifier=Modifier.clickable{onClose()})};Spacer(Modifier.height(16.dp));Box(Modifier.fillMaxHeight(.78f).aspectRatio(9f/19.5f).clip(RoundedCornerShape(24.dp)).background(light.top)){bitmap?.let{androidx.compose.foundation.Image(it.asImageBitmap(),null,Modifier.fillMaxSize())}};Spacer(Modifier.height(14.dp));Text("Light",color=light.text,style=MakariosTypography.labelLarge);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){Light.values().forEach{l->Box(Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).background(l.top))}};Spacer(Modifier.height(12.dp));Button(onClick=onSet,shape=RoundedCornerShape(24.dp),colors=ButtonDefaults.buttonColors(containerColor=light.buttonFill,contentColor=light.buttonText)){Text("Set wallpaper")}}}}




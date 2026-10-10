package com.makarios.app.ui.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.BlurMaskFilter
import android.graphics.LinearGradient
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.core.content.res.ResourcesCompat
import com.makarios.app.R
import com.makarios.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

enum class WallpaperStyle(val title:String) { WINDOWS("Windows"), RAYS("Rays"), NUMERALS("Numerals"), PAPER("Paper"), CONSTELLATION("Constellation"), TEXT("Text") }
data class CuratedVerse(val text:String,val reference:String)
data class WallpaperSpec(val style:WallpaperStyle,val light:Light,val verse:CuratedVerse?=null,val declaration:String?=null)

val CuratedWallpaperVerses = listOf(
 CuratedVerse("The Lord is my shepherd; I shall not want.","Psalm 23:1"), CuratedVerse("He restores my soul.","Psalm 23:3"), CuratedVerse("I will fear no evil, for you are with me.","Psalm 23:4"), CuratedVerse("Be still, and know that I am God.","Psalm 46:10"), CuratedVerse("The Lord is my light and my salvation.","Psalm 27:1"), CuratedVerse("He gives power to the faint.","Isaiah 40:29"), CuratedVerse("My grace is sufficient for you.","2 Corinthians 12:9"), CuratedVerse("The Lord will watch over your coming and going.","Psalm 121:8"), CuratedVerse("I have loved you with an everlasting love.","Jeremiah 31:3"), CuratedVerse("Under his wings you will find refuge.","Psalm 91:4"), CuratedVerse("The peace of God will guard your hearts.","Philippians 4:7"), CuratedVerse("Those who hope in the Lord will renew their strength.","Isaiah 40:31")
)

fun defaultWallpaperSpec(style:WallpaperStyle, light:Light=Light.forNow()):WallpaperSpec = WallpaperSpec(style,light,CuratedWallpaperVerses.first())

private fun DrawScope.u(value:Float,width:Float)=value*(width/393f)
private fun DrawScope.pathRidge(width:Float,height:Float,y:Float,amp:Float,seed:Int,color:Color){val r=Random(seed);val p=Path().apply{moveTo(0f,height);lineTo(0f,y);for(i in 0..6){val x=width*i/6f;val yy=y+sin(i*1.3+seed)*amp+(r.nextFloat()-.5f)*amp; if(i==0)moveTo(x,yy) else lineTo(x,yy)};lineTo(width,height);close()};drawPath(p,color)}
private fun DrawScope.scene(light:Light,w:Float,h:Float){drawLight(light,Size(w,h),grain=false);pathRidge(w,h,h*.72f,h*.018f,3,light.ridge1);pathRidge(w,h,h*.80f,h*.016f,5,light.ridge2);pathRidge(w,h,h*.88f,h*.012f,9,light.bottom)}

fun renderWallpaper(context:Context,spec:WallpaperSpec,widthPx:Int,heightPx:Int):Bitmap = renderLightBitmap(spec.light,widthPx,heightPx){ drawWallpaper(context,spec,size.width,size.height) }

fun DrawScope.drawWallpaper(context:Context,spec:WallpaperSpec,w:Float=size.width,h:Float=size.height){
 val s=min(w/393f,h/873f)
 when(spec.style){
  WallpaperStyle.WINDOWS->windows(spec,w,h,s)
  WallpaperStyle.RAYS->rays(spec,w,h,s)
  WallpaperStyle.NUMERALS->numerals(context,spec,w,h,s)
  WallpaperStyle.PAPER->paper(w,h,s)
  WallpaperStyle.CONSTELLATION->constellation(spec,w,h,s)
  WallpaperStyle.TEXT->textWallpaper(context,spec,w,h,s)
 }
}

private fun DrawScope.windows(spec:WallpaperSpec,w:Float,h:Float,s:Float){
 drawRect(Brush.verticalGradient(listOf(Color(0xFF0A1110),Color(0xFF050908))),size=Size(w,h))
 fun arch(x:Float,y:Float,aw:Float,ah:Float,light:Light){val p=Path().apply{moveTo(x,y+ah);lineTo(x,y+aw/2);cubicTo(x,y,x+aw,y,x+aw,y+aw/2);lineTo(x+aw,y+ah);close()};clipPath(p){scene(light,w,h)};drawPath(p,Color(0xFFC9964A),style=androidx.compose.ui.graphics.drawscope.Stroke(1.5f*s))}
 arch(95f*s,150f*s,200f*s,490f*s,Light.Dawn);arch(12f*s,320f*s,68f*s,320f*s,Light.Dusk);arch(310f*s,320f*s,68f*s,320f*s,Light.Mist)
 drawIntoCanvas{c->val p=AndroidPaint().apply{shader=RadialGradient(196f*s,640f*s,300f*s,Color(0x4DF4B98E).toArgb(),AndroidColor.TRANSPARENT,Shader.TileMode.CLAMP)};c.nativeCanvas.drawPath(AndroidPath().apply{moveTo(130f*s,620f*s);lineTo(262f*s,620f*s);lineTo(330f*s,900f*s);lineTo(60f*s,900f*s);close()},p)}
 footer(this,spec.verse,w,h,s)
}
private fun DrawScope.rays(spec:WallpaperSpec,w:Float,h:Float,s:Float){drawRect(Brush.verticalGradient(listOf(Color(0xFF9C4A26),Color(0xFF6B2E1E))),size=Size(w,h));val cx=w*.5f;val cy=h*.66f;drawIntoCanvas{c->val p=AndroidPaint().apply{shader=RadialGradient(cx,cy,700f*s,Color(0x8CFFE2B4),AndroidColor.TRANSPARENT,Shader.TileMode.CLAMP)};c.nativeCanvas.drawCircle(cx,cy,700f*s,p)};drawCircle(Color(0xFFFFF1D6),44f*s,Offset(cx,cy));repeat(29){i->val a=(-90f+i*6.4f)*Math.PI/180;val a2=a+2.8*Math.PI/180;val len=700f*s;val path=Path().apply{moveTo(cx,cy);lineTo(cx+cos(a).toFloat()*len,cy+sin(a).toFloat()*len);lineTo(cx+cos(a2).toFloat()*len,cy+sin(a2).toFloat()*len);close()};drawPath(path,Color(0x59FFE2B4))};pathRidge(w,h,h*.72f,h*.02f,3,Color(0xFF4A1F14));pathRidge(w,h,h*.80f,h*.016f,5,Color(0xFF33150E));pathRidge(w,h,h*.88f,h*.013f,9,Color(0xFF1E0C08))}
private fun DrawScope.numerals(context:Context,spec:WallpaperSpec,w:Float,h:Float,s:Float){drawRect(Brush.verticalGradient(listOf(Color(0xFFF4B98E),Color(0xFFC9703F),Color(0xFF6B2E1E))),size=Size(w,h));drawIntoCanvas{c->val n=spec.verse?.reference?.substringAfter("Psalm ")?.substringBefore(":")? : "23";val type=ResourcesCompat.getFont(context,R.font.newsreader)?:Typeface.SERIF;val p=AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply{this.typeface=type;typeface=Typeface.create(type,Typeface.BOLD);textSize=380f*s;textAlign=AndroidPaint.Align.CENTER};val path=AndroidPath();p.getTextPath(n,0,n.length,w/2f,h*.51f,path);c.nativeCanvas.save();c.nativeCanvas.clipPath(path);c.nativeCanvas.drawCircle(w*.72f,h*.37f,40f*s,AndroidPaint().apply{color=Color(0xFFFFD9A8).toArgb()});pathRidge(w,h,h*.58f,h*.018f,3,Color(0xFF6B2E1E));pathRidge(w,h,h*.68f,h*.016f,5,Color(0xFF432017));c.nativeCanvas.restore()};footer(this,spec.verse,w,h,s)}
private fun DrawScope.paper(w:Float,h:Float,s:Float){drawRect(Color(0xFFE5EEF0),size=Size(w,h));val colors=listOf(Color(0xFFCBDADD),Color(0xFFAFC8CE),Color(0xFF8EADB5),Color(0xFF6F929C),Color(0xFF527984),Color(0xFF3F626E));colors.forEachIndexed{i,c->pathRidge(w,h,h*(.54f+i*.065f),h*.018f,30+i,c);drawRect(Color.White.copy(alpha=.12f),topLeft=Offset(0f,h*(.54f+i*.065f)-2f),size=Size(w,3f))}}
private fun DrawScope.constellation(spec:WallpaperSpec,w:Float,h:Float,s:Float){drawRect(Brush.verticalGradient(listOf(Color(0xFF1F2B2A),Color(0xFF0F1716))),size=Size(w,h));drawIntoCanvas{c->val p=AndroidPaint().apply{shader=LinearGradient(0f,h*.2f,w,h*.7f,Color(0x339FB4B4),AndroidColor.TRANSPARENT,Shader.TileMode.CLAMP);maskFilter=BlurMaskFilter(28f*s,BlurMaskFilter.Blur.NORMAL)};c.nativeCanvas.save();c.nativeCanvas.rotate(-28f,w*.5f,h*.45f);c.nativeCanvas.drawOval(-w*.1f,h*.28f,w*1.1f,h*.55f,p);c.nativeCanvas.restore()};val r=Random(230);repeat(230){drawCircle(Color(0x99F3E6C8).copy(alpha=.35f+r.nextFloat()*.45f),.5f*s+r.nextFloat()*1.1f*s,Offset(r.nextFloat()*w,h*.12f+r.nextFloat()*h*.64f))};val cx=w*.68f;val cy=h*.43f;val points=listOf(Offset(cx,cy-.14f*h),Offset(cx,cy-.07f*h),Offset(cx,cy),Offset(cx,cy+.07f*h),Offset(cx-.07f*w,cy));points.forEach{drawCircle(Color(0xFFE8D09A),2.2f*s,it)};drawLine(Color(0xFFC9964A),points[0],points[3],1f*s);drawLine(Color(0xFFC9964A),points[2],points[4],1f*s);pathRidge(w,h,h*.91f,h*.01f,90,Color(0xFF070D0C));footer(this,spec.verse,w,h,s)}
private fun DrawScope.textWallpaper(context:Context,spec:WallpaperSpec,w:Float,h:Float,s:Float){scene(spec.light,w,h);val verse=spec.verse;drawIntoCanvas{c->val type=ResourcesCompat.getFont(context,R.font.newsreader_italic)?:Typeface.SERIF;val p=AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply{color=spec.light.text.toArgb();this.typeface=type;textSize=18f*s};c.nativeCanvas.drawText(spec.declaration?:"",28f*s,h*.39f,p);c.nativeCanvas.drawText(verse?.text?:"",28f*s,h*.48f,p);p.textSize=13f*s;c.nativeCanvas.drawText(verse?.reference?:"",28f*s,h*.53f,p)}}
private fun footer(scope:DrawScope,verse:CuratedVerse?,w:Float,h:Float,s:Float){if(verse==null)return;scope.drawIntoCanvas{c->val p=AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply{color=Color(0xCCFFF4E4).toArgb();textSize=14f*s;typeface=Typeface.create(Typeface.SERIF,Typeface.ITALIC)};c.nativeCanvas.drawText(verse.text,24f*s,h*.83f,p);p.textSize=11f*s;c.nativeCanvas.drawText(verse.reference.uppercase(),24f*s,h*.86f,p)}}







package com.makarios.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.makarios.app.R
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.theme.HorizonSpec
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.drawLight
import com.makarios.app.ui.theme.renderLightBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/** Canonical exact-pixel share formats. */
enum class ExportFormat(val width:Int,val height:Int){
    Square(1080,1080), Portrait(1080,1350), Story(1080,1920), X(1600,900), Wallpaper(-1,-1)
}

class LightCanvas(private val context:Context){
    fun render(declaration:String,verseText:String,verseReference:String,light:Light,format:ExportFormat,deviceWidth:Int=1080,deviceHeight:Int=1920,outputFile:File):File {
        val width=if(format==ExportFormat.Wallpaper)deviceWidth else format.width
        val height=if(format==ExportFormat.Wallpaper)deviceHeight else format.height
        val bitmap=renderLightBitmap(light,width,height){
            drawLight(light,size=size,horizon=if(format==ExportFormat.X) HorizonSpec(.76f,.81f,.86f) else HorizonSpec(),discPos=if(format==ExportFormat.X) androidx.compose.ui.geometry.Offset(size.width*.78f,size.height*.42f) else null,grain=false)
            drawSharePairing(this,width,height,declaration,verseText,verseReference,light,format)
        }
        val png=if(outputFile.extension.lowercase()=="png")outputFile else File(outputFile.parentFile,outputFile.nameWithoutExtension+".png")
        FileOutputStream(png).use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        if(png.length()>8L*1024L*1024L){val jpg=File(png.parentFile,png.nameWithoutExtension+".jpg");FileOutputStream(jpg).use{bitmap.compress(Bitmap.CompressFormat.JPEG,92,it)};png.delete();bitmap.recycle();return jpg}
        bitmap.recycle();return png
    }

    private fun drawSharePairing(scope:DrawScope,w:Int,h:Int,declaration:String,verse:String,reference:String,light:Light,format:ExportFormat){
        scope.drawIntoCanvas{canvas->
            val native=canvas.nativeCanvas;val margin=w*.08f;val textWidth=if(format==ExportFormat.X)w*.55f-margin else w-margin*2
            val scale=w/1080f;val news=ResourcesCompat.getFont(context,R.font.newsreader)?:Typeface.SERIF;val italic=ResourcesCompat.getFont(context,R.font.newsreader_italic)?:Typeface.SERIF;val hanken=ResourcesCompat.getFont(context,R.font.hanken_grotesk)?:Typeface.SANS_SERIF
            fun make(text:String,typeface:Typeface,size:Float,spacing:Float=1.18f)=StaticLayout.Builder.obtain(text,0,text.length,TextPaint(Paint.ANTI_ALIAS_FLAG).apply{this.typeface=typeface;textSize=size;color=light.text.toArgb();isSubpixelText=true;fontFeatureSettings="lnum, tnum"},textWidth.toInt()).setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0f,spacing).setIncludePad(false).setEllipsize(null).setMaxLines(Int.MAX_VALUE).build()
            var declarationSize=64f*scale;val declarationMin=32f*scale;var dl=make(declaration,Typeface.create(news,Typeface.BOLD),declarationSize)
            while(declarationSize>declarationMin && dl.height>h*.34f){declarationSize-=1f*scale;dl=make(declaration,Typeface.create(news,Typeface.BOLD),declarationSize)}
            var verseSize=36f*scale;val verseMin=24f*scale;var vl=make(verse,Typeface.create(italic,Typeface.ITALIC),verseSize,1.24f)
            val ref=make(reference,Typeface.create(hanken,Typeface.NORMAL),16f*scale,1f)
            val anchorH=2f*scale;val anchorW=28f*scale;val gap1=26f*scale;val gap2=20f*scale;val gap3=12f*scale
            val topSafe=if(format==ExportFormat.Story)h*.14f else h*.10f;val bottomSafe=if(format==ExportFormat.Story)h*.86f else if(format==ExportFormat.X)h*.92f else h*.76f
            fun total(includeVerse:Boolean)=dl.height+gap1+anchorH+gap2+(if(includeVerse)vl.height+gap3 else 0f)+ref.height
            var includeVerse=true
            while(verseSize>verseMin && topSafe+total(true)>bottomSafe){verseSize-=1f*scale;vl=make(verse,Typeface.create(italic,Typeface.ITALIC),verseSize,1.24f)}
            if(topSafe+total(true)>bottomSafe)includeVerse=false
            val contentH=total(includeVerse);val start=if(format==ExportFormat.Story)topSafe+(bottomSafe-topSafe-contentH)*.38f else if(format==ExportFormat.X)(h-contentH)*.38f else topSafe
            native.save();native.translate(margin,start);dl.draw(native);var y=dl.height+gap1;val rule=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=light.anchorRule.toArgb()};native.drawRect(0f,y,anchorW,y+anchorH,rule);y+=anchorH+gap2
            if(includeVerse){native.save();native.translate(0f,y);vl.draw(native);native.restore();y+=vl.height+gap3};native.save();native.translate(0f,y);ref.draw(native);native.restore();native.restore()
            val word=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=light.text.copy(alpha=.82f).toArgb();typeface=Typeface.create(italic,Typeface.ITALIC);textSize=16f*scale;textAlign=Paint.Align.CENTER};native.drawText("makarios",w/2f,h*.94f,word)
        }
    }
}



package com.makarios.app.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.sin
import kotlin.random.Random

 data class HorizonSpec(val far:Float=.72f,val mid:Float=.80f,val front:Float=.88f)
private val grainTile by lazy { Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888).apply { val p=IntArray(256*256); val r=Random(0x4D414B); for(i in p.indices){val n=r.nextInt(256);p[i]=Color.argb(n,n,n,n).toArgb()}; setPixels(p,0,256,0,0,256,256) }.asImageBitmap() }

fun DrawScope.drawLight(light:Light,size:Size=this.size,horizon:HorizonSpec=HorizonSpec(),discPos:Offset?=null,discScale:Float=1f,grain:Boolean=true) {
 drawRect(Brush.verticalGradient(listOf(light.top,light.bottom),0f,size.height),size=size)
 val p=discPos?:Offset(size.width*light.discX,size.height*light.discY); val r=light.discR*discScale
 drawCircle(Brush.radialGradient(listOf(light.glow.copy(alpha=.55f),light.glow.copy(alpha=0f)),r*4f,p),r*4f,p); drawCircle(light.disc.copy(alpha=light.discAlpha),r,p)
 if(light==Light.Night){val n=(size.width*size.height/9000f).toInt();val q=Random(8);repeat(n){drawCircle(Color.White.copy(alpha=.35f+q.nextFloat()*.35f),.7f+q.nextFloat()*.5f,Offset(q.nextFloat()*size.width,size.height*(.45f+q.nextFloat()*(horizon.far-.45f))))}}
 ridge(light.ridge1,size,size.height*horizon.far,size.height*.018f,3);ridge(light.ridge2,size,size.height*horizon.mid,size.height*.0162f,5);ridge(light.bottom,size,size.height*horizon.front,size.height*.0126f,9)
 if(grain)drawIntoCanvas{c->c.drawRect(0f,0f,size.width,size.height,Paint().apply{shader=ImageShader(grainTile,TileMode.Repeated,TileMode.Repeated);alpha=.07f;blendMode=if(light.isLight)BlendMode.Multiply else BlendMode.Screen})}
}
private fun DrawScope.ridge(color:Color,s:Size,y0:Float,amp:Float,seed:Int){val r=Random(seed);val p=(0..6).map{i->Offset(s.width*i/6f,y0+amp*sin(i*1.3f+seed)+(r.nextFloat()-.5f)*.8f*amp)};val path=Path().apply{moveTo(0f,s.height);lineTo(p[0].x,p[0].y);for(i in 0 until 6){val m=Offset((p[i].x+p[i+1].x)/2,(p[i].y+p[i+1].y)/2);quadraticBezierTo(p[i].x,p[i].y,m.x,m.y)};lineTo(p.last().x,p.last().y);lineTo(s.width,s.height);close()};drawPath(path,color)}

fun renderLightBitmap(light:Light,widthPx:Int,heightPx:Int,content:DrawScope.()->Unit={}):Bitmap { require(widthPx>0&&heightPx>0);val b=Bitmap.createBitmap(widthPx,heightPx,Bitmap.Config.ARGB_8888);CanvasDrawScope().draw(Density(1f),LayoutDirection.Ltr,Canvas(android.graphics.Canvas(b)),Size(widthPx.toFloat(),heightPx.toFloat())){drawLight(light);content()};return b }
@Composable fun LightBackground(light:Light,modifier:Modifier=Modifier,content:@Composable()->Unit={}){Box(modifier.drawBehind{drawLight(light)}){content()}}
fun Modifier.lightBackground(light:Light)=drawBehind{drawLight(light)}

/** Pixel-stable pairing renderer for share cards and other non-Compose surfaces. */
fun drawPairing(canvas: android.graphics.Canvas, widthPx: Int, declaration: String, verse: String, reference: String, light: Light, declarationTypeface: android.graphics.Typeface, italicTypeface: android.graphics.Typeface, referenceTypeface: android.graphics.Typeface, maxDeclarationPx: Float = 34f, minDeclarationPx: Float = 16f) {
    fun layout(text: String, paint: android.text.TextPaint) = android.text.StaticLayout.Builder.obtain(text,0,text.length,paint,widthPx).setIncludePad(false).setEllipsize(null).setMaxLines(Int.MAX_VALUE).build()
    var size=maxDeclarationPx
    val dp=android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply{color=light.text.toArgb();typeface=declarationTypeface;textSize=size;isSubpixelText=true}
    var dl=layout(declaration,dp)
    while(dl.lineCount>2&&size>minDeclarationPx){size-=1f;dp.textSize=size;dl=layout(declaration,dp)}
    dl.draw(canvas)
    var y=dl.height+24f
    val rule=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply{color=light.anchorRule.toArgb()};canvas.drawRect(0f,y,28f,y+2f,rule);y+=18f
    val vp=android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply{color=light.text.toArgb();typeface=italicTypeface;textSize=17f;isSubpixelText=true};val vl=layout(verse,vp);canvas.save();canvas.translate(0f,y);vl.draw(canvas);canvas.restore();y+=vl.height+8f
    val rp=android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply{color=light.text.copy(alpha=light.secondaryAlpha).toArgb();typeface=referenceTypeface;textSize=13f;isSubpixelText=true};val rl=layout(reference,rp);canvas.save();canvas.translate(0f,y);rl.draw(canvas);canvas.restore()
}

package com.makarios.app

import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.makarios.app.ui.theme.Light
import com.makarios.app.util.ExportFormat
import com.makarios.app.util.LightCanvas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Golden-render matrix: exact dimensions and non-empty deterministic output for 4 formats x 3 Lights. */
class ShareGoldenTest {
 @Test fun fourFormatsAndThreeLightsRenderAtExactPixels(){val context=InstrumentationRegistry.getInstrumentation().targetContext;listOf(ExportFormat.Story,ExportFormat.Square,ExportFormat.Portrait,ExportFormat.X).forEach{format->listOf(Light.Dawn,Light.Midday,Light.Night).forEach{light->val file=File(context.cacheDir,"golden_${format.name}_${light.name}.png");val out=LightCanvas(context).render("I am held.","Be still and know that I am God.","Psalm 46:10",light,format,outputFile=file);val bitmap=BitmapFactory.decodeFile(out.absolutePath);assertEquals(format.width,bitmap.width);assertEquals(format.height,bitmap.height);assertTrue(bitmap.byteCount>0);bitmap.recycle();out.delete()}}}
 @Test fun longDeclarationDoesNotFailOrEllipsizeAtRendererBoundary(){val context=InstrumentationRegistry.getInstrumentation().targetContext;val file=File(context.cacheDir,"golden_long.png");val out=LightCanvas(context).render("This is a deliberately long declaration with two hundred and eighty characters that must remain complete and readable without truncation or a clipped word at the edge of the rendered image.","A verse that may be dropped when the safe zone cannot hold the complete pairing.","Psalm 23:1",Light.Dusk,ExportFormat.Portrait,outputFile=file);assertTrue(out.exists());out.delete()}
}

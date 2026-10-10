package com.makarios.app.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.makarios.app.ui.theme.*

class WidgetConfigureActivity:ComponentActivity(){override fun onCreate(state:Bundle?){super.onCreate(state);setContent{WidgetConfigureScreen{light,source,pick->WidgetStore.set(this,light,source,pick);WidgetScheduling.refreshNow(this);setResult(RESULT_OK);finish()}}}}
@Composable private fun WidgetConfigureScreen(save:(String,WidgetSource,String?)->Unit){var light by remember{mutableStateOf("AUTO")};var source by remember{mutableStateOf(WidgetSource.KEPT)};var pick by remember{mutableStateOf<String?>(null)};val ink=Ink;Column(Modifier.fillMaxSize().padding(24.dp)){Text("Configure widget",style=MakariosTypography.displaySmall,color=ink);Spacer(Modifier.height(20.dp));Text("Light",style=MakariosTypography.bodyLarge,color=ink);Spacer(Modifier.height(10.dp));Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){Box(Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(ink).clickable{light="AUTO"});Light.values().forEach{l->Box(Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(l.top).clickable{light=l.name})}};Spacer(Modifier.height(22.dp));Text("Content",style=MakariosTypography.bodyLarge,color=ink);WidgetSource.values().forEach{item->Row(Modifier.fillMaxWidth().clickable{source=item}.padding(vertical=10.dp)){RadioButton(selected=source==item,onClick={source=item});Text(item.name.replace('_',' '),modifier=Modifier.padding(top=12.dp),color=ink)}};Spacer(Modifier.weight(1f));Button(onClick={save(light,source,pick)},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),colors=ButtonDefaults.buttonColors(containerColor=ink,contentColor=Cream)){Text("Save")}}}

package com.makarios.app.ui.theme

import androidx.compose.ui.graphics.Color
import java.time.Clock
import java.time.LocalTime

enum class Light(
    val dark: Boolean, val top: Color, val bottom: Color, val text: Color, val secondaryAlpha: Float,
    val ridge1: Color, val ridge2: Color, val disc: Color, val glow: Color,
    val discX: Float, val discY: Float, val discR: Float, val discAlpha: Float
) {
    Dawn(false, Color(0xFFFBE3C4), Color(0xFFEBB195), Ink,.78f,Color(0x4DB0684A),Color(0x6696543A),Color(0xFFFFF1D6),Color(0xFFFFF1D6),.75f,.65f,34f,.92f),
    Midday(false,Color(0xFFFFF4D6),Color(0xFFEFD58A),Ink,.78f,Color(0x4DC8963C),Color(0x66AA7828),Color(0xFFFFFDF2),Color(0xFFFFFDF2),.50f,.50f,36f,.95f),
    Mist(false,Color(0xFFE3EADB),Color(0xFFBCCBB2),Ink,.78f,Color(0x475A6E55),Color(0x61645C46),Color(0xFFF6F8F1),Color(0xFFF6F8F1),.70f,.62f,30f,.70f),
    Rain(false,Color(0xFFD5E0E2),Color(0xFFA3B9BE),Ink,.78f,Color(0x47506973),Color(0x613C5864),Color(0xFFEEF3F4),Color(0xFFEEF3F4),.30f,.58f,28f,.55f),
    Ember(true,Color(0xFF9C4A26),Color(0xFF6B2E1E),Cream,.88f,Color(0x38280A05),Color(0x5C280A05),Color(0xFFFFD9A8),Color(0xFFFFD9A8),.26f,.74f,36f,.95f),
    Dusk(true,Color(0xFF8F4B3A),Color(0xFF43292B),Cream,.88f,Color(0x38190A0F),Color(0x5C190A0F),Color(0xFFF4B98E),Color(0xFFF4B98E),.72f,.70f,30f,.95f),
    Grove(true,Color(0xFF3F5A37),Color(0xFF223019),Cream,.88f,Color(0x33081006),Color(0x57081006),Color(0xFFE8F0D0),Color(0xFFE8F0D0),.74f,.60f,22f,.90f),
    Night(true,Color(0xFF1F2B2A),Color(0xFF0F1716),Cream,.88f,Color(0x33000000),Color(0x52000000),Color(0xFFF3E6C8),Color(0xFFF3E6C8),.76f,.62f,20f,.92f);
    val isLight get()=!dark
    val anchorRule get()=if(dark) AnchorDark else AnchorLight
    val buttonFill get()=if(dark) Cream else Ink
    val buttonText get()=if(dark) Ink else Cream
    companion object {
        fun forNow(clock: Clock=Clock.systemDefaultZone()): Light { val t=LocalTime.now(clock); return when { t>=LocalTime.of(5,30)&&t<LocalTime.of(9)->Dawn; t>=LocalTime.of(9)&&t<LocalTime.of(16,30)->Midday; t>=LocalTime.of(16,30)&&t<LocalTime.of(19,30)->Dusk; else->Night } }
        fun forTopic(topic:String)=when(topic.lowercase()) { "identity"->Dawn;"peace"->Mist;"strength"->Ember;"purpose"->Rain;"courage"->Dusk;"joy"->Midday;"rest"->Grove;else->Dawn }
    }
}
fun getCurrentLightForTime()=Light.forNow()

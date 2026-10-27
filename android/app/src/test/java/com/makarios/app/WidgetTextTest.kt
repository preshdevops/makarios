package com.makarios.app

import com.makarios.app.data.toWidgetShortText
import com.makarios.app.data.widgetTextFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WidgetTextTest {
 @Test fun shortDeclarationsAreKept(){assertEquals("I am held.","I am held.".toWidgetShortText())}
 @Test fun firstShortSentenceIsUsed(){assertEquals("I am held.","I am held. This longer second sentence is not needed for a widget.".toWidgetShortText())}
 @Test fun longDeclarationFallsBackToFirstSixtyChars(){
   val input="This is a declaration with no sentence punctuation and more than forty characters in total length here"
   assertEquals(input.take(60).trimEnd(), input.toWidgetShortText())
   assertFalse(input.toWidgetShortText() == "Short version for widgets")
 }
 @Test fun emptyShortTextUsesDeclaration(){
   assertEquals("I am loved and held by God.", widgetTextFor("I am loved and held by God.", ""))
 }
 @Test fun uiLabelNeverUsedAsWidgetContent(){
   assertEquals("I am held.", widgetTextFor("I am held.", "Short version for widgets"))
 }
}

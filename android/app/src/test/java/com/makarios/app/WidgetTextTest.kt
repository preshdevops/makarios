package com.makarios.app

import com.makarios.app.data.toWidgetShortText
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetTextTest {
 @Test fun shortDeclarationsAreKept(){assertEquals("I am held.","I am held.".toWidgetShortText())}
 @Test fun firstShortSentenceIsUsed(){assertEquals("I am held.","I am held. This longer second sentence is not needed for a widget.".toWidgetShortText())}
 @Test fun longUnsentenceDeclarationUsesEditPrompt(){assertEquals("Short version for widgets","This is a declaration with no sentence punctuation and more than forty characters".toWidgetShortText())}
}

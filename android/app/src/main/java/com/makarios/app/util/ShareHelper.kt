package com.makarios.app.util

import android.content.Context
import android.content.Intent
import com.makarios.app.data.Affirmation

object ShareHelper {

    fun shareAffirmation(context: Context, affirmation: Affirmation) {
        val shareText = buildString {
            append("“${affirmation.declaration}”\n\n")
            append("“${affirmation.scriptureText}”\n")
            append("— ${affirmation.reference}\n\n")
            append("Shared via Makarios · Sacred Declarations & Scripture")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Makarios — Daily Declaration")
            type = "text/plain"
        }

        val chooser = Intent.createChooser(sendIntent, "Share Declaration")
        context.startActivity(chooser)
    }

    fun shareText(context: Context, title: String, text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, title)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Share via")
        context.startActivity(chooser)
    }
}

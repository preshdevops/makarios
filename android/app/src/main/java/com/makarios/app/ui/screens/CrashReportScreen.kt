package com.makarios.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.theme.Ink
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.MakariosTypography
import com.makarios.app.ui.theme.SystemBarsController
import com.makarios.app.util.CrashReporter

@Composable
fun CrashReportScreen(
    reportText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    SystemBarsController(Light.Dawn)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F5))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Makarios stopped last time",
            style = MakariosTypography.displaySmall.copy(fontSize = 26.sp),
            color = Ink
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Here is what happened right before the app closed. You can copy or share this report to help fix the issue.",
            style = MakariosTypography.bodyMedium,
            color = Ink.copy(alpha = 0.75f)
        )

        Spacer(Modifier.height(18.dp))

        // Scrollable trace container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF0ECE1))
                .padding(14.dp)
        ) {
            val vScroll = rememberScrollState()
            val hScroll = rememberScrollState()

            Text(
                text = reportText,
                style = MakariosTypography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                ),
                color = Ink,
                modifier = Modifier
                    .verticalScroll(vScroll)
                    .horizontalScroll(hScroll)
            )
        }

        Spacer(Modifier.height(20.dp))

        // Two primary buttons: Copy report and Share report
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Makarios Crash Report", reportText))
                    Toast.makeText(context, "Crash report copied", Toast.LENGTH_SHORT).show()
                    CrashReporter.clearCrashReport(context)
                    onDismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ink,
                    contentColor = Color.White
                )
            ) {
                Text("Copy report", style = MakariosTypography.labelLarge)
            }

            OutlinedButton(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, reportText)
                        putExtra(Intent.EXTRA_SUBJECT, "Makarios Crash Report")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Crash Report")
                    context.startActivity(shareIntent)
                    CrashReporter.clearCrashReport(context)
                    onDismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Ink
                )
            ) {
                Text("Share report", style = MakariosTypography.labelLarge)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = {
                    CrashReporter.clearCrashReport(context)
                    onDismiss()
                }
            ) {
                Text("Dismiss", style = MakariosTypography.labelMedium, color = Ink.copy(alpha = 0.6f))
            }
        }
    }
}

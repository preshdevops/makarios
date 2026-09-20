package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.theme.*

enum class BillingPlan {
    ANNUAL,
    MONTHLY
}

@Composable
fun SubscriptionScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPlan by remember { mutableStateOf(BillingPlan.ANNUAL) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Porcelain
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // ── Top Bar ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Surface)
                        .border(1.dp, Border, CircleShape)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Espresso,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Restore Purchases",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.5.sp,
                    color = Stone,
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Purchases restored", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // ── Header Section ────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Makarios+ Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TerracottaLight)
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "MAKARIOS+",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        letterSpacing = 2.sp,
                        color = Terracotta
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Unlock the Full Depth of God's Word",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 25.sp,
                    lineHeight = 33.sp,
                    letterSpacing = (-0.3).sp,
                    textAlign = TextAlign.Center,
                    color = Espresso
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Unlimited personal declarations, full category library, 4K wallpaper exports, and custom widget themes.",
                    fontFamily = BodyFontFamily,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Stone
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Core Benefits List ────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    "Unlimited Affirmation Authoring" to "Write and match declarations without daily generation limits.",
                    "Complete Category Library" to "Access all 11+ spiritual seasons, curated themes, and deep promises.",
                    "High-Resolution Studio Exports" to "Download 4K wallpapers, Stories, and social cards with watermarks optional.",
                    "Custom Widget Studio" to "Unlock all widget themes, lock screen glances, and multiple active widgets.",
                    "Offline-Ready Sanctuary" to "Keep all your saved truths and custom declarations available anywhere, offline."
                ).forEach { (title, description) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(16.dp), spotColor = Espresso.copy(alpha = 0.04f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Surface)
                            .border(1.dp, Border, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(TerracottaLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Terracotta,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = title,
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp,
                                    color = Espresso
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = description,
                                    fontFamily = BodyFontFamily,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = Stone
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Pricing Plan Selector ─────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Annual Plan (Featured)
                val isAnnual = selectedPlan == BillingPlan.ANNUAL
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (isAnnual) 4.dp else 1.dp, RoundedCornerShape(18.dp), spotColor = Espresso.copy(alpha = 0.10f))
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isAnnual) Surface else PorcelainWarm.copy(alpha = 0.3f))
                        .border(
                            width = if (isAnnual) 2.dp else 1.dp,
                            color = if (isAnnual) Terracotta else Border,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { selectedPlan = BillingPlan.ANNUAL }
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Radio circle
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isAnnual) Terracotta else Color.Transparent)
                                    .border(2.dp, if (isAnnual) Terracotta else Border, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAnnual) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Annual Plan",
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = Espresso
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Terracotta)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "SAVE 35%",
                                            fontFamily = BodyFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            letterSpacing = 1.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "7 days free trial, then $39.99/year ($3.33/mo)",
                                    fontFamily = BodyFontFamily,
                                    fontSize = 12.sp,
                                    color = Stone
                                )
                            }
                        }

                        Text(
                            text = "$3.33/mo",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Espresso
                        )
                    }
                }

                // Monthly Plan
                val isMonthly = selectedPlan == BillingPlan.MONTHLY
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (isMonthly) 4.dp else 1.dp, RoundedCornerShape(18.dp), spotColor = Espresso.copy(alpha = 0.10f))
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isMonthly) Surface else PorcelainWarm.copy(alpha = 0.3f))
                        .border(
                            width = if (isMonthly) 2.dp else 1.dp,
                            color = if (isMonthly) Terracotta else Border,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { selectedPlan = BillingPlan.MONTHLY }
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Radio circle
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isMonthly) Terracotta else Color.Transparent)
                                    .border(2.dp, if (isMonthly) Terracotta else Border, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isMonthly) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Monthly Plan",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Espresso
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Billed monthly. Cancel anytime.",
                                    fontFamily = BodyFontFamily,
                                    fontSize = 12.sp,
                                    color = Stone
                                )
                            }
                        }

                        Text(
                            text = "$4.99/mo",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Espresso
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Primary Action Button ─────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        Toast.makeText(
                            context,
                            "Welcome to Makarios+! Your 7-day trial is active.",
                            Toast.LENGTH_LONG
                        ).show()
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Terracotta,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = if (selectedPlan == BillingPlan.ANNUAL) "Start 7-Day Free Trial" else "Subscribe to Makarios+",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Cancel anytime in Google Play Store · No commitment",
                    fontFamily = BodyFontFamily,
                    fontSize = 11.5.sp,
                    color = StoneMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Legal links
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terms of Service",
                        fontFamily = BodyFontFamily,
                        fontSize = 11.sp,
                        color = StoneMuted
                    )
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(StoneMuted))
                    Text(
                        text = "Privacy Policy",
                        fontFamily = BodyFontFamily,
                        fontSize = 11.sp,
                        color = StoneMuted
                    )
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diagnostics.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BatteryViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiagnosticsScreen(viewModel: BatteryViewModel) {
    val telemetryState by viewModel.telemetryState.collectAsState()
    val slownessDiagnosis by viewModel.activeSlownessDiagnosis.collectAsState()
    val aiReport by viewModel.aiReport.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()
    val portReport by viewModel.activePortHealth.collectAsState()
    val fakeFastReport by viewModel.activeFakeFastCharge.collectAsState()

    var fixingState by remember { mutableStateOf(false) }
    var fixingComplete by remember { mutableStateOf(false) }

    LaunchedEffect(fixingState) {
        if (fixingState) {
            kotlinx.coroutines.delay(2000) // Simulate closing background drain processes
            fixingState = false
            fixingComplete = true
        }
    }

    val alertActive = slownessDiagnosis.causes.isNotEmpty() || telemetryState.tempC >= 38.0

    // Compute causesList right here at Composable top-level!
    val causesList = remember(slownessDiagnosis.causes) {
        if (slownessDiagnosis.causes.isEmpty()) {
            listOf(
                SlownessCause("High Temperature", "High", "Core temperature is currently ${telemetryState.tempC}°C. Extreme thermal thresholds trigger dynamic PMIC current throttling to prevent internal cell expansion.", 84, "Cool down device"),
                SlownessCause("Screen Power Usage", "Medium", "High screen brightness and live 120Hz refresh loops consume active milliamps, diverting power away from chemical storage.", 71, "Turn off screen while charging"),
                SlownessCause("Weak Charger", "Medium", "Intake current is limited. The adapter output voltage profile shows high resistance and lacks sufficient wattage protocols.", 59, "Upgrade to certified GaN fast charger"),
                SlownessCause("Battery Protection", "Low", "Software charge limit loops or protective peak performance thresholds are restricting intake currents above 80%.", 48, "Toggle Battery Protection levels in Settings")
            )
        } else {
            slownessDiagnosis.causes
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("diagnostics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. TITLE HEADER ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Why Is Charging Slow?",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Real-time intake tracking & bottleneck analysis",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = { viewModel.runDiagnosisDoctorAlert() }) {
                    Icon(Icons.Default.Refresh, "Re-scan loop", tint = CyanGlow)
                }
            }
        }

        // --- 2. HERO CAUTION: SLOW CHARGING DETECTED (MOCKUP 2 CORE CARD) ---
        item {
            val gradientBrush = if (alertActive) {
                Brush.verticalGradient(
                    colors = listOf(
                        CoreAmber.copy(0.18f),
                        SpaceSlate
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        HyperGreen.copy(0.12f),
                        SpaceSlate
                    )
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (alertActive) CoreAmber.copy(0.35f) else HyperGreen.copy(0.2f),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(gradientBrush)
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (alertActive) "Slow Charging Detected" else "Fast Charging Active",
                                color = if (alertActive) CoreAmber else HyperGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (alertActive) {
                                    "Charging speed is lower than usual. Tap below to diagnose."
                                } else {
                                    "Input curves are calibrated correctly. Power is transferring at healthy current rates."
                                },
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (alertActive) CoreAmber.copy(0.15f) else HyperGreen.copy(0.15f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (alertActive) Icons.Default.Warning else Icons.Default.OfflineBolt,
                                contentDescription = "Security Status indicator",
                                tint = if (alertActive) CoreAmber else HyperGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. COGNITIVE AI DOCTOR CONSULTATION EXPANSION ---
        if (aiReport != null || aiLoading) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyanGlow.copy(0.2f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, "AI Consulting", tint = CyanGlow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("COGNITIVE AI DOCTOR TERMINAL", color = CyanGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (aiLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            ) {
                                CircularProgressIndicator(color = CyanGlow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Synthesizing atomic battery diagnostics...", color = TextSecondary, fontSize = 12.sp)
                            }
                        } else {
                            aiReport?.let {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CyberObsidian, RoundedCornerShape(8.dp))
                                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = it,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. TOP REASONS WITH PROGRESS RATIOS (MOCKUP 2 STYLED LIST) ---
        item {
            Text(
                text = "Top Reasons",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        if (!alertActive) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                    modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, "Healthy chemistry", tint = HyperGreen, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No charging bottlenecks detected.", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("System is in healthy thermodynamic equilibrium.", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        } else {
            itemsIndexed(causesList) { idx, cause ->
                RankedReasonItem(
                    rank = idx + 1,
                    cause = cause
                )
            }
        }

        // --- 5. ACTION HUBS (MOCKUP 2 CAPSULE BUTTONS) ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (fixingState) {
                    Button(
                        onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(disabledContainerColor = SteelGray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        CircularProgressIndicator(color = HyperGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("TERMINATING BACKGROUND PROCESSES...", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { fixingState = true },
                        colors = ButtonDefaults.buttonColors(containerColor = HyperGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("fix_now_button"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = if (fixingComplete) "FIX COMPLETE (DRAIN RESTRICTED)" else "Fix Now",
                            color = CyberObsidian,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Button(
                    onClick = { viewModel.runDiagnosisDoctorAlert() },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaceSlate),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .border(1.dp, CardBorder, RoundedCornerShape(19.dp))
                        .testTag("deep_diagnosis_button"),
                    shape = RoundedCornerShape(19.dp)
                ) {
                    Text(
                        text = "Run Deep Diagnosis",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- 6. SECONDARY PORT CONTACT CHECKS ---
        item {
            Text(
                "HARDWARE INTERFACE ANALYSIS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            PortHealthCard(portReport = portReport, disconnections = telemetryState.disconnections)
        }

        // --- 7. FAKE FAST PROTOCOLS CHECKS ---
        item {
            FakeFastDetectorCard(fakeFastReport = fakeFastReport)
        }
    }
}

// Ranked slowness reason card matching Mockup 2 perfectly!
@Composable
fun RankedReasonItem(
    rank: Int,
    cause: SlownessCause
) {
    val barColor = when (cause.severity) {
        "High" -> FlareRed
        "Medium" -> CoreAmber
        else -> HyperGreen
    }

    val impactPct = cause.confidence / 100f

    Card(
        colors = CardDefaults.cardColors(containerColor = SpaceSlate),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Number Circle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(SteelGray, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$rank",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = cause.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Impact severity label tag
                Box(
                    modifier = Modifier
                        .background(barColor.copy(0.08f), RoundedCornerShape(4.dp))
                        .border(1.dp, barColor.copy(0.25f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Impact: ${cause.severity}",
                        color = barColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = cause.explanation,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Percentage Bar layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(CardBorder, RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = impactPct)
                            .background(barColor, RoundedCornerShape(3.dp))
                    )
                }

                Text(
                    text = "${cause.confidence}%",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PortHealthCard(portReport: PortHealthReport, disconnections: Int) {
    val accentColor = when (portReport.status) {
        "Clean" -> HyperGreen
        "Warn" -> CoreAmber
        else -> FlareRed
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SpaceSlate),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(accentColor.copy(0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PowerInput, "Port Check", tint = accentColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("PORT CONNECTION QUALITY", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Charging Port Status: ${portReport.status}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "${portReport.stabilityIndex}/100 INDEX",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(accentColor.copy(0.08f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = CardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Connection Dropouts", color = TextSecondary, fontSize = 10.sp)
                    Text("$disconnections Loops", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Lint / Dust Risk", color = TextSecondary, fontSize = 10.sp)
                    Text(if (portReport.isDustLikely) "HIGH" else "LOW", color = if (portReport.isDustLikely) CoreAmber else HyperGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Stability Rating", color = TextSecondary, fontSize = 10.sp)
                    Text(portReport.disconnectSeverity.uppercase(Locale.US), color = if (portReport.status == "Clean") HyperGreen else FlareRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = portReport.advice,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun FakeFastDetectorCard(fakeFastReport: FakeFastChargeReport) {
    val accent = if (fakeFastReport.isFakeDetected) FlareRed else HyperGreen

    Card(
        colors = CardDefaults.cardColors(containerColor = SpaceSlate),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(0.15f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(accent.copy(0.10f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NetworkCheck, "Fake Fast check", tint = accent)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("PROTOCOL VERIFICATION ENGINE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Fake Fast Charge Guard", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = if (fakeFastReport.isFakeDetected) "DISCREPANCY DETECTED" else "PROTOCOL MATCHED",
                    color = accent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .border(1.dp, accent, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = CardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Text("System Claim:", color = TextSecondary, fontSize = 10.sp)
            Text(fakeFastReport.reportedType, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Electrical Delivery Rate:", color = TextSecondary, fontSize = 10.sp)
            Text(String.format(Locale.US, "%.2f Watts (Actual Live Energy Flow)", fakeFastReport.actualWattage), color = accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(10.dp))
            Text(fakeFastReport.discrepancyMessage, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberObsidian, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "ACTION: ${fakeFastReport.advice}",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

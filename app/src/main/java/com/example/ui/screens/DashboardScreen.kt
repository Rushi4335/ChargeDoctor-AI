package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.BatteryViewModel
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: BatteryViewModel,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val telemetry by viewModel.telemetryState.collectAsState()
    val score by viewModel.activeChargeScore.collectAsState()
    val curMaAbs = abs(telemetry.currentMa)
    val powerWatts = (curMaAbs / 1000.0) * (telemetry.voltageMv / 1000.0)
    val thermalReport by viewModel.activeThermalReport.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 76.dp, bottom = 16.dp), // Spacer for Dynamic Island
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // --- 1. BRANDED APP HEADER WITH HIGHLIGHTED AI ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "CHARGEDOCTOR ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .background(HyperGreen.copy(0.12f), RoundedCornerShape(4.dp))
                            .border(1.dp, HyperGreen.copy(0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = HyperGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Charging Intelligence & Diagnostics",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }
        }

        // --- 2. HERO CHARGING CIRCULAR RADIAL GAUGE (MOCKUP 1 STYLE) ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularBatteryGauge(
                        level = telemetry.level,
                        isCharging = telemetry.isCharging,
                        powerWatts = powerWatts,
                        stateLabel = telemetry.pluggedState,
                        color = score.color
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Floating Green/Amber Pill Active State Banner
                    Box(
                        modifier = Modifier
                            .background(
                                if (telemetry.isCharging) HyperGreen.copy(0.08f) else SteelGray.copy(0.4f),
                                RoundedCornerShape(20.dp)
                            )
                            .border(
                                1.dp,
                                if (telemetry.isCharging) HyperGreen.copy(0.35f) else CardBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (telemetry.isCharging) HyperGreen else CoreAmber,
                                        RoundedCornerShape(3.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (telemetry.isCharging) {
                                    if (powerWatts > 15.0) "⚡ FAST CHARGING ACTIVE" else "🔌 CHARGING STANDARD"
                                } else {
                                    "🔋 DISCHARGING LIVE STATUS"
                                },
                                color = if (telemetry.isCharging) HyperGreen else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 3. DUAL-COLUMN HIGH-FIDELITY GRID WITH TREND SPARK WAVE (MOCKUP 1 CORES) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // LEFT GRID CARD 1: Charging Power (with tiny custom canvas sparkline)
                    HighFidelityGridCard(
                        title = "Charging Power",
                        value = String.format(Locale.US, "%.1f W", powerWatts),
                        subtitle = if (telemetry.isCharging) "Intake rate" else "Net system drain",
                        icon = Icons.Default.ElectricBolt,
                        iconColor = HyperGreen,
                        drawSparkwave = true,
                        sparkwaveColor = HyperGreen
                    )

                    // LEFT GRID CARD 2: Estimated Time to Full/Empty
                    HighFidelityGridCard(
                        title = "Time to Full",
                        value = if (telemetry.isCharging) "${(100 - telemetry.level) * 45 / 60}m" else "Discharging",
                        subtitle = if (telemetry.isCharging) "Estimate to 100%" else "Saturating drain loops",
                        icon = Icons.Default.AccessTime,
                        iconColor = CyanGlow
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // RIGHT GRID CARD 1: Temperature (with tiny thermocouple orange sparkwave)
                    HighFidelityGridCard(
                        title = "Temperature",
                        value = String.format(Locale.US, "%.1f °C", telemetry.tempC),
                        subtitle = "Operational thermals",
                        icon = Icons.Default.DeviceThermostat,
                        iconColor = CoreAmber,
                        drawSparkwave = true,
                        sparkwaveColor = CoreAmber
                    )

                    // RIGHT GRID CARD 2: Battery Health (with heart icon)
                    HighFidelityGridCard(
                        title = "Battery Health",
                        value = "91 %",
                        subtitle = "Cell chemical integrity",
                        icon = Icons.Default.Favorite,
                        iconColor = HyperGreen
                    )
                }
            }
        }

        // --- 4. DYNAMIC PROTECTIVE ACCREDITED STATUS BANNER (MOCKUP 1 SHIELD CHECK) ---
        item {
            val systemHealthPercent = (88 + (telemetry.tempC - 28.0).coerceIn(0.0, 10.0) * -1.5).toInt()
            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HyperGreen.copy(0.2f), RoundedCornerShape(16.dp))
                    .clickable { onNavigateToDiagnostics() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(HyperGreen.copy(0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Shield Guard",
                            tint = HyperGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "STATUS",
                            color = HyperGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (telemetry.isCharging) {
                                if (telemetry.tempC > 40) {
                                    "Thermal throttling active. Your phone is charging at 38% of expected speed."
                                } else {
                                    "Everything looks good. Your phone is charging at $systemHealthPercent% of expected speed."
                                }
                            } else {
                                "Unplugged. Idle drain loops safe. Battery core registers normal thermoelectrical stability."
                            },
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // --- 5. COGNITIVE AI DIAGNOSTIC DOCK ---
        item {
            AIDiagnosticCard(viewModel = viewModel, onNavigateToDiagnostics = onNavigateToDiagnostics)
        }

        // --- 6. SIMULATOR CONTROLS PREVIEW CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SteelGray.copy(0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .clickable { onNavigateToSettings() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Tune,
                        "Profile Config",
                        tint = CyanGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SIMULATOR CABINET ACTIVE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(telemetry.simulatorProfileName, color = CyanGlow, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .background(SteelGray, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("SIMULATE", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } // Closes item (SIMULATOR CONTROLS PREVIEW CARD)
    } // Closes LazyColumn

    // --- FLOATING DYNAMIC ISLAND WIDGET ---
    val remainingMin = if (telemetry.isCharging) (100 - telemetry.level) * 45 / 60 else 0
    DynamicIslandWidget(
        isCharging = telemetry.isCharging,
        powerWatts = powerWatts,
        level = telemetry.level,
        tempC = telemetry.tempC,
        timeToFullMin = remainingMin
    )
} // Closes parent Box
} // Closes DashboardScreen


@Composable
fun DynamicIslandWidget(
    isCharging: Boolean,
    powerWatts: Double,
    level: Int,
    tempC: Double,
    timeToFullMin: Int
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Smooth capsule transitions mimicking Apple's Liquid effect
    val targetWidth by animateDpAsState(
        targetValue = if (isExpanded) 340.dp else 180.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "IslandWidth"
    )
    val targetHeight by animateDpAsState(
        targetValue = if (isExpanded) 80.dp else 36.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "IslandHeight"
    )
    val targetCornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 20.dp else 18.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "IslandCorners"
    )

    // Pulsing charging glow indicator
    val infiniteTransition = rememberInfiniteTransition(label = "Pulsating Glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    Box(
        modifier = Modifier
            .padding(top = 12.dp)
            .width(targetWidth)
            .height(targetHeight)
            .clip(RoundedCornerShape(targetCornerRadius))
            .background(Color.Black)
            .border(1.dp, Color(0xFF2C2C2E), RoundedCornerShape(targetCornerRadius))
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isExpanded) {
            // Collapsed pill
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Power",
                        tint = AppleGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format(Locale.US, "%.1fW", powerWatts),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Pulsing green charging dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            if (isCharging) AppleGreen.copy(alpha = pulseAlpha) else Color.White.copy(0.4f),
                            CircleShape
                        )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$level%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "Battery",
                        tint = AppleGreen,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        } else {
            // Expanded capsule displaying rich telemetry specs
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Feature 1: Live Intake Watts
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ElectricBolt, null, tint = AppleGreen, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("POWER", color = AppleTextSecondary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(String.format(Locale.US, "%.1fW", powerWatts), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }

                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF2C2C2E)))

                // Feature 2: Battery Percentage
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BatteryChargingFull, null, tint = AppleGreen, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("BATTERY", color = AppleTextSecondary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("$level%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }

                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF2C2C2E)))

                // Feature 3: Live Cores Temp
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeviceThermostat, null, tint = SoftOrange, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("THERMAL", color = AppleTextSecondary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(String.format(Locale.US, "%.1f°C", tempC), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }

                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF2C2C2E)))

                // Feature 4: Energy ETA
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.3f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, tint = AppleBlue, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("TIME LEFT", color = AppleTextSecondary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(if (isCharging) "${timeToFullMin}m remaining" else "Discharging", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

// Sparkline & Mini Grid Component matching the beautiful cards on Mockup 1
@Composable
fun HighFidelityGridCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    drawSparkwave: Boolean = false,
    sparkwaveColor: Color = HyperGreen
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SpaceSlate),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title.uppercase(Locale.US),
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )

            if (drawSparkwave) {
                Spacer(modifier = Modifier.height(10.dp))
                // Beautiful micro custom sparkline wave embedded directly in grid cards!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val path = Path()

                        // Draw a premium mini dynamic line curve
                        val coordinates = listOf(0.1f, 0.45f, 0.35f, 0.72f, 0.51f, 0.88f)
                        val stepX = width / (coordinates.size - 1)

                        coordinates.forEachIndexed { i, pt ->
                            val x = i * stepX
                            val y = height - (pt * height)
                            if (i == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = sparkwaveColor,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Translucent background gradient fill
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(sparkwaveColor.copy(0.12f), Color.Transparent)
                            )
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
fun CircularBatteryGauge(
    level: Int,
    isCharging: Boolean,
    powerWatts: Double,
    stateLabel: String,
    color: Color
) {
    val levelAnimated by animateFloatAsState(
        targetValue = level / 100f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "Level Hoop"
    )

    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val canvasSize = size.width
            val radius = (canvasSize - strokeWidth) / 2
            val center = Offset(canvasSize / 2, canvasSize / 2)

            // Faint background ring
            drawCircle(
                color = CardBorder,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Neon sweep ring representing capacity
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        color.copy(0.3f),
                        color,
                        color,
                        color.copy(0.3f)
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f * levelAnimated,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center content mapping Mockup 1
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$level%",
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Icon(
                imageVector = Icons.Default.OfflineBolt,
                contentDescription = null,
                tint = if (isCharging) HyperGreen else TextSecondary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isCharging) "Charging" else "Discharging",
                color = if (isCharging) HyperGreen else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun AIDiagnosticCard(viewModel: BatteryViewModel, onNavigateToDiagnostics: () -> Unit) {
    val aiLoading by viewModel.aiLoading.collectAsState()
    val aiReport by viewModel.aiReport.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = SpaceSlate),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onNavigateToDiagnostics() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        "AI Consulting",
                        tint = CyanGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "COGNITIVE AI DIAGNOSTIC DOCK",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Consult AI Battery Doctor",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = { viewModel.runDiagnosisDoctorAlert() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow),
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("CONSULT", color = CyberObsidian, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (aiLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyanGlow, modifier = Modifier.size(20.dp))
                }
            } else if (aiReport != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberObsidian, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = aiReport!!,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )
                }
            } else {
                Text(
                    text = "No diagnostic summary generated. Tap to analyze your current chemical stress levels, physical adapter efficiency and connector wear indices.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}


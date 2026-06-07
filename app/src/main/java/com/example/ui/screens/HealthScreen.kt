package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diagnostics.*
import com.example.ui.theme.*
import java.util.Locale
import com.example.ui.viewmodel.BatteryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    viewModel: BatteryViewModel,
    onBack: () -> Unit
) {
    val telemetryState by viewModel.telemetryState.collectAsState()
    val agingPrediction by viewModel.activeAgingPrediction.collectAsState()
    val careScore by viewModel.activeCareScore.collectAsState()

    val batteryHealthPct = (telemetryState.estimatedCapacityMah.toDouble() / telemetryState.designCapacityMah.toDouble() * 100).toInt()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CyberObsidian,
                    titleContentColor = Color.White
                ),
                title = {
                    Text(
                        "BATTERY HEALTH",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Diagnostics", tint = CyanGlow)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Info, "Health guidelines", tint = TextSecondary)
                    }
                }
            )
        },
        containerColor = CyberObsidian
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("health_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. HERO CIRCULAR GAUGE (MOCKUP 4 STYLE) ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(190.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Thickness ring hoop
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 14.dp.toPx()
                                val canvasSize = size.width
                                val radius = (canvasSize - strokeWidth) / 2
                                val center = Offset(canvasSize / 2, canvasSize / 2)

                                // Draw baseline gray ring
                                drawCircle(
                                    color = CardBorder,
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = strokeWidth)
                                )

                                // Draw glowing green charging health progress
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            HyperGreen.copy(0.4f),
                                            HyperGreen,
                                            HyperGreen,
                                            HyperGreen.copy(0.4f)
                                        )
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = 360f * (batteryHealthPct / 100f),
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            // Center stats values
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$batteryHealthPct%",
                                    color = Color.White,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = HyperGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Good",
                                    color = HyperGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Descriptive caption
                        Text(
                            text = "Battery Health is Good. Keep following good charging habits.",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }

            // --- 2. THE 2x2 INTEGRITY GRID (MOCKUP 4 STYLE) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HealthMetricCard(
                            label = "Battery Age",
                            value = "11 Months",
                            annotation = "Manufactured 11 mo ago"
                        )

                        HealthMetricCard(
                            label = "Battery Wear",
                            value = "${100 - batteryHealthPct} %",
                            annotation = "Cell attenuation"
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HealthMetricCard(
                            label = "Estimated Cap.",
                            value = "${telemetryState.estimatedCapacityMah} mAh",
                            annotation = "Residual volume"
                        )

                        HealthMetricCard(
                            label = "Cycle Count",
                            value = "${telemetryState.cyclesEstimate} Cycles",
                            annotation = "Charge events logged"
                        )
                    }
                }
            }

            // --- 3. HEALTH OVER TIME CUSTOM CANVAS SPARK LINE (MOCKUP 4) ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Health Over Time",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom line chart showing decay over Jan, Apr, Jul, Oct!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(CyberObsidian, RoundedCornerShape(8.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .padding(14.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height

                                // Draw faint grid horizontal lines
                                val grids = 3
                                for (i in 0..grids) {
                                    val y = (height / grids) * i
                                    drawLine(
                                        color = CardBorder.copy(0.4f),
                                        start = Offset(0f, y),
                                        end = Offset(width, y)
                                    )
                                }

                                // Plot the actual decay path: from 100% (Jan) down to 91% (Oct)
                                val pts = listOf(1.0f, 0.96f, 0.93f, 0.91f) // Health multipliers
                                val stepX = width / (pts.size - 1)
                                val path = Path()

                                pts.forEachIndexed { i, pt ->
                                    // Scale y offset between 80% (bottom) and 100% (top)
                                    val normY = (pt - 0.80f) / 0.20f
                                    val x = i * stepX
                                    val y = height - (normY * height)

                                    if (i == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }

                                    // Small bubble indicators
                                    drawCircle(
                                        color = HyperGreen,
                                        radius = 4.dp.toPx(),
                                        center = Offset(x, y)
                                    )
                                }

                                drawPath(
                                    path = path,
                                    color = HyperGreen,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Linear vertical fill
                                val fillPath = Path().apply {
                                    addPath(path)
                                    lineTo(width, height)
                                    lineTo(0f, height)
                                    close()
                                }
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(HyperGreen.copy(0.2f), Color.Transparent)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Time stamps labels under chart ticks
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Jan", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("Apr", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("Jul", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("Oct", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // --- 4. LIFESPAN HABIT COMPILER ACCRUAL DOCK ---
            item {
                val scoreColor = when {
                    careScore.score >= 90 -> HyperGreen
                    careScore.score >= 78 -> CyanGlow
                    else -> CoreAmber
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
                                        .background(scoreColor.copy(0.1f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.VolunteerActivism, "Habit checklist", tint = scoreColor)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("LIFESPAN HABIT ASSESSMENT", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("Battery Care Score: ${careScore.score}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = "GRADE ${careScore.grade}",
                                color = scoreColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .border(1.dp, scoreColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = CardBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        careScore.tips.forEach { tip ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.OfflineBolt, null, tint = scoreColor, modifier = Modifier.size(13.dp).offset(y = 1.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(tip, color = TextPrimary, fontSize = 11.sp, lineHeight = 15.sp)
                            }
                        }
                    }
                }
            }

            // --- 5. ELECTROCHEMICAL ELASTIC TIME DECIDENCY ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AGING ATTEUATION PREDICTIONS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Future chemical decay projections", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AgingDecayItem(
                                timeSpan = "T+3 MONTHS FORECAST",
                                capacityMah = (telemetryState.designCapacityMah * (agingPrediction.health3MonthsPercent / 100)).toInt(),
                                healthPct = agingPrediction.health3MonthsPercent,
                                color = HyperGreen
                            )

                            AgingDecayItem(
                                timeSpan = "T+6 MONTHS FORECAST",
                                capacityMah = (telemetryState.designCapacityMah * (agingPrediction.health6MonthsPercent / 100)).toInt(),
                                healthPct = agingPrediction.health6MonthsPercent,
                                color = CyanGlow
                            )

                            AgingDecayItem(
                                timeSpan = "T+12 MONTHS FORECAST",
                                capacityMah = (telemetryState.designCapacityMah * (agingPrediction.health12MonthsPercent / 100)).toInt(),
                                healthPct = agingPrediction.health12MonthsPercent,
                                color = CoreAmber
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HealthMetricCard(
    label: String,
    value: String,
    annotation: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SpaceSlate),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label.uppercase(Locale.US), color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(annotation, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun AgingDecayItem(
    timeSpan: String,
    capacityMah: Int,
    healthPct: Double,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberObsidian),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(timeSpan, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text("$capacityMah mAh Projected", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .background(color.copy(0.12f), RoundedCornerShape(6.dp))
                    .border(1.dp, color.copy(0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%.1f %%", healthPct),
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}


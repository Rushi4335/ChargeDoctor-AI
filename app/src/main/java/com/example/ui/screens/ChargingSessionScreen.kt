package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ChargingSession
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargingSessionScreen(
    session: ChargingSession,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CyberObsidian,
                    titleContentColor = Color.White
                ),
                title = {
                    Text(
                        "CHARGING SESSION",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to History list", tint = CyanGlow)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, "Share Diagnostics Brief", tint = CyanGlow)
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
                .testTag("charging_session_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. SESSION SCORE RADIAL DOUBLE GAUGE (MOCKUP 3) ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(
                                "Session Score",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Great Session! 🔥",
                                color = HyperGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Voltage stayed stable under rapid-charge current curves.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }

                        // Circular arc chart gauge
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 8.dp.toPx()
                                val canvasSize = size.width
                                val radius = (canvasSize - strokeWidth) / 2
                                val center = Offset(canvasSize / 2, canvasSize / 2)

                                // Faint dark path
                                drawCircle(
                                    color = CardBorder,
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = strokeWidth)
                                )

                                // Glowing segment mapping score
                                drawArc(
                                    color = HyperGreen,
                                    startAngle = -90f,
                                    sweepAngle = 360f * (session.chargeScore / 100f),
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${session.chargeScore}",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "/100",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // --- 2. 2x3 METRIC VALUES GRID (MOCKUP 3) ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricCell(label = "Start", value = "${session.startPercentage}%", modifier = Modifier.weight(1f))
                            Dividervertical()
                            MetricCell(label = "End", value = "${session.endPercentage}%", modifier = Modifier.weight(1f))
                            Dividervertical()
                            MetricCell(label = "Duration", value = "42m", modifier = Modifier.weight(1f))
                        }

                        Divider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricCell(label = "Avg Power", value = String.format(Locale.US, "%.1f W", session.averageWattage), modifier = Modifier.weight(1f), valueColor = HyperGreen)
                            Dividervertical()
                            MetricCell(label = "Peak Power", value = String.format(Locale.US, "%.1f W", session.peakWattage), modifier = Modifier.weight(1f), valueColor = HyperGreen)
                            Dividervertical()
                            MetricCell(label = "Max Temp", value = String.format(Locale.US, "%.1f °C", session.maxTemperature), modifier = Modifier.weight(1f), valueColor = if (session.maxTemperature > 38) FlareRed else Color.White)
                        }
                    }
                }
            }

            // --- 3. THE DUAL-LINE CHARGING GRAPH (MOCKUP 3 MASTERPIECE) ---
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
                            "Charging Graph",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Legends Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(HyperGreen, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Power (W)", color = TextSecondary, fontSize = 10.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(CoreAmber, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Temp (°C)", color = TextSecondary, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // High fidelity Dual axes canvas drawing!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(CyberObsidian, RoundedCornerShape(8.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .padding(14.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height

                                // Draw vertical grid markings
                                val gridCols = 4
                                val stepCol = width / gridCols
                                for (i in 0..gridCols) {
                                    drawLine(
                                        color = CardBorder.copy(0.3f),
                                        start = Offset(i * stepCol, 0f),
                                        end = Offset(i * stepCol, height)
                                    )
                                }

                                // Draw horizontal grid markings
                                val gridRows = 3
                                val stepRow = height / gridRows
                                for (i in 0..gridRows) {
                                    drawLine(
                                        color = CardBorder.copy(0.3f),
                                        start = Offset(0f, i * stepRow),
                                        end = Offset(width, i * stepRow)
                                    )
                                }

                                // 1. Plot Power Line (Green Wave)
                                val powerPoints = listOf(0.15f, 0.58f, 0.72f, 0.61f, 0.38f)
                                val stepX = width / (powerPoints.size - 1)
                                val powerPath = Path()

                                powerPoints.forEachIndexed { i, pt ->
                                    val x = i * stepX
                                    val y = height - (pt * height)
                                    if (i == 0) powerPath.moveTo(x, y) else powerPath.lineTo(x, y)
                                    drawCircle(color = HyperGreen, radius = 2.5f.dp.toPx(), center = Offset(x, y))
                                }
                                drawPath(path = powerPath, color = HyperGreen, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

                                // 2. Plot Temperature Line (Orange/Amber Wave)
                                val tempPoints = listOf(0.35f, 0.41f, 0.52f, 0.55f, 0.51f)
                                val tempPath = Path()

                                tempPoints.forEachIndexed { i, pt ->
                                    val x = i * stepX
                                    val y = height - (pt * height)
                                    if (i == 0) tempPath.moveTo(x, y) else tempPath.lineTo(x, y)
                                    drawCircle(color = CoreAmber, radius = 2.5f.dp.toPx(), center = Offset(x, y))
                                }
                                drawPath(path = tempPath, color = CoreAmber, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dual axises numbers labelling under chart (Mockup 3 axis labels)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0m", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("10m", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("20m", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("30m", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("40m", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // --- 4. ENERGY STORAGE EFFICIENCY METRIC (MOCKUP 3) ---
            item {
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
                            Text(
                                "Efficiency",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Good",
                                color = HyperGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Horizontal layout progress bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .background(CardBorder, RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = 0.72f)
                                        .background(HyperGreen, RoundedCornerShape(4.dp))
                                )
                            }

                            Text(
                                text = "72%",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(Locale.US),
            color = TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun Dividervertical() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(34.dp)
            .background(CardBorder)
    )
}

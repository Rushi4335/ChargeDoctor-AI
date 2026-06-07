package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.viewmodel.BatteryViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: BatteryViewModel,
    onNavigateToSessionDetail: (ChargingSession) -> Unit
) {
    val sessions by viewModel.allSessions.collectAsState()
    var selectedTab by remember { mutableStateOf("Month") } // Day, Week, Month, Year

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CyberObsidian,
                    titleContentColor = Color.White
                ),
                title = {
                    Text(
                        "ANALYTICS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            )
        },
        containerColor = CyberObsidian
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("analytics_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. FILTER TABS CAPSULES (MOCKUP 5 STYLED) ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SpaceSlate, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("Day", "Week", "Month", "Year")
                    tabs.forEach { t ->
                        val active = selectedTab == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) HyperGreen else Color.Transparent)
                                .clickable { selectedTab = t }
                                .wrapContentHeight(Alignment.CenterVertically),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t,
                                color = if (active) CyberObsidian else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- 2. AVERAGE CHARGING SPEED MODULE (MOCKUP 5) ---
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
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("Average Charging Speed", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("20.4 W", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            }

                            // Dynamic green status bubble comparison
                            Box(
                                modifier = Modifier
                                    .background(HyperGreen.copy(0.08f), RoundedCornerShape(4.dp))
                                    .border(1.dp, HyperGreen.copy(0.3f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("+8% vs last month", color = HyperGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Speed path waveform Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .background(CyberObsidian, RoundedCornerShape(8.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height

                                // Draw baseline markings
                                drawLine(color = CardBorder, start = Offset(0f, height * 0.7f), end = Offset(width, height * 0.7f))
                                drawLine(color = CardBorder, start = Offset(0f, height * 0.3f), end = Offset(width, height * 0.3f))

                                val coordinates = listOf(0.18f, 0.45f, 0.38f, 0.72f, 0.65f, 0.82f, 0.76f)
                                val stepX = width / (coordinates.size - 1)
                                val path = Path()

                                coordinates.forEachIndexed { i, pt ->
                                    val x = i * stepX
                                    val y = height - (pt * height)
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

                                    drawCircle(color = HyperGreen, radius = 3.dp.toPx(), center = Offset(x, y))
                                }

                                drawPath(path = path, color = HyperGreen, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                                val fillPath = Path().apply {
                                    addPath(path)
                                    lineTo(width, height)
                                    lineTo(0f, height)
                                    close()
                                }
                                drawPath(fillPath, brush = Brush.verticalGradient(listOf(HyperGreen.copy(0.2f), Color.Transparent)))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1 May", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("15 May", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("31 May", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // --- 3. HEAT TREND THERMAL DIAGRAM (MOCKUP 5 THERMAL CARD) ---
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
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("Heat Trend", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("34.6 °C", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            }

                            Box(
                                modifier = Modifier
                                    .background(HyperGreen.copy(0.08f), RoundedCornerShape(4.dp))
                                    .border(1.dp, HyperGreen.copy(0.3f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Good", color = HyperGreen, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Heat curve Canvas (Orange)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .background(CyberObsidian, RoundedCornerShape(8.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height

                                drawLine(color = CardBorder, start = Offset(0f, height * 0.5f), end = Offset(width, height * 0.5f))

                                val thermals = listOf(0.35f, 0.42f, 0.58f, 0.48f, 0.31f, 0.34f, 0.28f)
                                val stepX = width / (thermals.size - 1)
                                val path = Path()

                                thermals.forEachIndexed { i, pt ->
                                    val x = i * stepX
                                    val y = height - (pt * height)
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

                                    drawCircle(color = CoreAmber, radius = 3.dp.toPx(), center = Offset(x, y))
                                }

                                drawPath(path = path, color = CoreAmber, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                                val fillPath = Path().apply {
                                    addPath(path)
                                    lineTo(width, height)
                                    lineTo(0f, height)
                                    close()
                                }
                                drawPath(fillPath, brush = Brush.verticalGradient(listOf(CoreAmber.copy(0.18f), Color.Transparent)))
                            }
                        }
                    }
                }
            }

            // --- 3B. BATTERY HEALTH DEGRADATION TREND (MOCKUP 5 EXTRA STOCKS GRAPH) ---
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
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("Battery Health Trend", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("91.0 %", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            }

                            Box(
                                modifier = Modifier
                                    .background(FlareRed.copy(0.08f), RoundedCornerShape(4.dp))
                                    .border(1.dp, FlareRed.copy(0.3f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("-9% decay", color = FlareRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Health decay plot
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .background(CyberObsidian, RoundedCornerShape(8.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height

                                drawLine(color = CardBorder, start = Offset(0f, height * 0.5f), end = Offset(width, height * 0.5f))

                                val healths = listOf(1.00f, 0.98f, 0.96f, 0.95f, 0.93f, 0.92f, 0.91f)
                                val stepX = width / (healths.size - 1)
                                val path = Path()

                                healths.forEachIndexed { i, pt ->
                                    val x = i * stepX
                                    val localPct = (pt - 0.85f) / 0.15f
                                    val y = height - (localPct * height)
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

                                    drawCircle(color = HyperGreen, radius = 3.dp.toPx(), center = Offset(x, y))
                                }

                                drawPath(path = path, color = HyperGreen, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                                val fillPath = Path().apply {
                                    addPath(path)
                                    lineTo(width, height)
                                    lineTo(0f, height)
                                    close()
                                }
                                drawPath(fillPath, brush = Brush.verticalGradient(listOf(HyperGreen.copy(0.18f), Color.Transparent)))
                            }
                        }
                    }
                }
            }

            // --- 3C. CONVERSION EFFICIENCY TREND (MOCKUP 5 EXTRA STOCKS GRAPH) ---
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
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("Charging Conversion Efficiency", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("92.4 %", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            }

                            Box(
                                modifier = Modifier
                                    .background(HyperGreen.copy(0.08f), RoundedCornerShape(4.dp))
                                    .border(1.dp, HyperGreen.copy(0.3f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Stable High", color = HyperGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Efficiency fluctuating plot
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .background(CyberObsidian, RoundedCornerShape(8.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height

                                drawLine(color = CardBorder, start = Offset(0f, height * 0.5f), end = Offset(width, height * 0.5f))

                                val effs = listOf(0.92f, 0.94f, 0.91f, 0.93f, 0.92f, 0.95f, 0.92f)
                                val stepX = width / (effs.size - 1)
                                val path = Path()

                                effs.forEachIndexed { i, pt ->
                                    val x = i * stepX
                                    val localEff = (pt - 0.80f) / 0.20f
                                    val y = height - (localEff * height)
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

                                    drawCircle(color = AppleGreen, radius = 3.dp.toPx(), center = Offset(x, y))
                                }

                                drawPath(path = path, color = AppleGreen, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                                val fillPath = Path().apply {
                                    addPath(path)
                                    lineTo(width, height)
                                    lineTo(0f, height)
                                    close()
                                }
                                drawPath(fillPath, brush = Brush.verticalGradient(listOf(AppleGreen.copy(0.18f), Color.Transparent)))
                            }
                        }
                    }
                }
            }

            // --- 4. TOTAL SESSIONS & TIME COLUMNS COMPOSITE (MOCKUP 5 DOCK) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Sessions
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, tint = HyperGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Total Sessions", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("28", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text("+5 vs last month", color = HyperGreen, fontSize = 10.sp)
                        }
                    }

                    // Total Time
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, null, tint = HyperGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Total Time", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("18h 42m", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text("+2h vs last month", color = HyperGreen, fontSize = 10.sp)
                        }
                    }
                }
            }

            // --- 5. PAST CHARGE SESSIONS HEADER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "CHARGING SESSIONS HISTORY",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        "ADD MOCK CYCLE",
                        color = CyanGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.submitCurrentSessionTestRecord() }
                            .border(1.dp, CyanGlow, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (sessions.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No session records intercepts in local database", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(sessions) { session ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                            .clickable { onNavigateToSessionDetail(session) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US).format(Date(session.startTime)),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Box(
                                    modifier = Modifier
                                        .background(HyperGreen.copy(0.12f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${session.chargeScore}/100 Score",
                                        color = HyperGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = CardBorder, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Intake Gain", color = TextSecondary, fontSize = 10.sp)
                                    Text("${session.startPercentage}% → ${session.endPercentage}%", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Average Power", color = TextSecondary, fontSize = 10.sp)
                                    Text(String.format(Locale.US, "%.1f W", session.averageWattage), color = HyperGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Max Temp", color = TextSecondary, fontSize = 10.sp)
                                    Text(String.format(Locale.US, "%.1f °C", session.maxTemperature), color = if (session.maxTemperature > 38.0) FlareRed else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

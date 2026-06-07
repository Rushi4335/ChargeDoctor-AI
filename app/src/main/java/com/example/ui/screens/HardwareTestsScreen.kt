package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.BatteryViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareTestsScreen(
    viewModel: BatteryViewModel,
    onBack: () -> Unit
) {
    val cableState by viewModel.activeCableQuality.collectAsState()
    val chargerState by viewModel.activeChargerFingerprint.collectAsState()
    val telemetryState by viewModel.telemetryState.collectAsState()

    var testingActive by remember { mutableStateOf(false) }
    var testComplete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(testingActive) {
        if (testingActive) {
            delay(2000) // Realistic loading measuring voltage droops
            testingActive = false
            testComplete = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CyberObsidian,
                    titleContentColor = Color.White
                ),
                title = {
                    Text(
                        "CHARGER TEST",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Diagnostics More page", tint = CyanGlow)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Info, "Calibrating", tint = TextSecondary)
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
                .testTag("hardware_tests_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. RADIAL GAUGE SEMI-ARC (MOCKUP 8 GAUGE) ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier.size(200.dp, 120.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 12.dp.toPx()
                                val width = size.width
                                val height = size.height
                                val radius = (width - strokeWidth) / 2
                                val center = Offset(width / 2, height)

                                // Faint dark semi-circle loop path
                                drawArc(
                                    color = CardBorder,
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                // Glowing green charging performance arc
                                drawArc(
                                    color = HyperGreen,
                                    startAngle = 180f,
                                    sweepAngle = 180f * 0.86f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.offset(y = (-10).dp)
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "8.6",
                                        color = Color.White,
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "/10",
                                        color = TextSecondary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                                    )
                                }

                                Text(
                                    text = "Good",
                                    color = HyperGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Charger is good and performing well.",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // --- 2. 2x2 STABILITY ATTRIBUTES GRID (MOCKUP 8 STABILITY CARD) ---
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
                            "Stability Ratings",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            StabilityGridItem(label = "Power Stability", value = "Good", color = HyperGreen, modifier = Modifier.weight(1f))
                            StabilityGridItem(label = "Voltage Stability", value = "Good", color = HyperGreen, modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            StabilityGridItem(label = "Efficiency", value = "Good", color = HyperGreen, modifier = Modifier.weight(1f))
                            StabilityGridItem(label = "Heat Output", value = "Low", color = CyanGlow, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // --- 3. HARDWARE TESTING TRIGGERS AND LOOPS ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("IMPEDANCE & CABLE METRICS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Copper Conduit Evaluation", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Wire Impedance", color = TextSecondary, fontSize = 10.sp)
                                Text("0.11 Ω (Optimal)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Voltage Droop", color = TextSecondary, fontSize = 10.sp)
                                Text(String.format(Locale.US, "%.1f mV", cableState.estimatedVoltageDropMv), color = HyperGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Thermal Watt Loss", color = TextSecondary, fontSize = 10.sp)
                                Text(String.format(Locale.US, "%.3f W", cableState.powerLossWatts), color = HyperGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Estimated resistance in current loop is standard. No localized corrosion or physical connector damage isolated.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // --- 4. START TEST CAPSULE BUTTON (MOCKUP 8 FOOTER) ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (testingActive) {
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
                            Text("SCANNING ELECTRICAL RIPPLE...", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { testingActive = true },
                            colors = ButtonDefaults.buttonColors(containerColor = HyperGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("run_cable_impedance_button"),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = if (testComplete) "Start Test Again" else "START COPPER IMPEDANCE RE-EVALUATION",
                                color = CyberObsidian,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StabilityGridItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

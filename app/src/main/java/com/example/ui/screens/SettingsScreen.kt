package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TelemetryState
import com.example.ui.theme.*
import com.example.ui.viewmodel.BatteryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: BatteryViewModel) {
    val telemetryState by viewModel.telemetryState.collectAsState()
    val scope = rememberCoroutineScope()

    var showWipeConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. SIMULATOR STUDIO COMMAND CENTRE ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyanGlow.copy(0.3f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, "Simulator Studio", tint = CyanGlow)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("ENGINEERING SIMULATION STUDIO", color = CyanGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("Mock High-Stress Charging States", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Activate a system profile to test AI Doctor diagnosing protocols under varying physical electrical conditions.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = CardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .selectableGroup()
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SimulatorRadioOption(
                            title = "Raw Hardware Sensors (No Mocking)",
                            desc = "Reads live Broadcaster intents directly from kernel PMIC registers.",
                            selected = !telemetryState.simulatorActive,
                            onClick = { viewModel.toggleSimulator(false) }
                        )

                        SimulatorRadioOption(
                            title = "Perfect Power Delivery (GaN PD)",
                            desc = "Simulates authentic 40W+ high volt Super Charging loops under absolute safe cold states.",
                            selected = telemetryState.simulatorActive && telemetryState.simulatorProfileName.contains("Perfect", true),
                            onClick = { viewModel.toggleSimulator(true, "PERFECT") }
                        )

                        SimulatorRadioOption(
                            title = "Thermal Protection Limits (Hot)",
                            desc = "Simulates active cell core temperatures at 42°C, causing automated PMIC throttlings to 550mA.",
                            selected = telemetryState.simulatorActive && telemetryState.simulatorProfileName.contains("Thermal", true),
                            onClick = { viewModel.toggleSimulator(true, "THERMAL") }
                        )

                        SimulatorRadioOption(
                            title = "Damaged Cable (High Loss)",
                            desc = "Imposes a simulated high resistance wire, droping voltage drops and bottle-necking inputs to 380mA trickle.",
                            selected = telemetryState.simulatorActive && telemetryState.simulatorProfileName.contains("Cable", true),
                            onClick = { viewModel.toggleSimulator(true, "CABLE") }
                        )

                        SimulatorRadioOption(
                            title = "Dirty / Loose Connection (Unstable Port)",
                            desc = "Interferes delivery lines with rapid on-and-off disconnections to check socket contacts dust levels.",
                            selected = telemetryState.simulatorActive && telemetryState.simulatorProfileName.contains("Port", true),
                            onClick = { viewModel.toggleSimulator(true, "PORT") }
                        )

                        SimulatorRadioOption(
                            title = "Intensive Task Gaming Load (Heavy CPU)",
                            desc = "Heats CPU to 58°C with concurrent charging. Intercepts siphoned power consumption anomalies.",
                            selected = telemetryState.simulatorActive && telemetryState.simulatorProfileName.contains("Gaming", true),
                            onClick = { viewModel.toggleSimulator(true, "GAMING") }
                        )
                    }
                }
            }
        }

        // --- 2. THE CALIBRATION CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("GAS GAUGE CALIBRATION", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Chemical Level Synchronizations", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Force re-align chemical gas thresholds to match physical charge counters in hardware registers. Useful if level percentages drop abruptly.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    var calibrating by remember { mutableStateOf(false) }
                    AnimatedContent(targetState = calibrating, label = "Calib cross") { isCalibrating ->
                        if (isCalibrating) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = CyanGlow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Re-aligning Coulomb Counters...", color = CyanGlow, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            LaunchedEffect(Unit) {
                                delay(1500)
                                calibrating = false
                            }
                        } else {
                            Button(
                                onClick = { calibrating = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SteelGray),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Sync, "Force Calibration", tint = TextPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SYNCHRONIZE COULOMB COUNTERS", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- 3. HARDWARE HISTORY PURGER ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (showWipeConfirm) FlareRed.copy(0.4f) else CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("LOCAL SYSTEM MAINTENANCE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Clear Room SQLite Database", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Erase all historic log points, charger profiles, and charging sessions from SQLite database. Re-starts default analytic charts calibrations.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!showWipeConfirm) {
                        Button(
                            onClick = { showWipeConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SteelGray),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteForever, "Clear DB", tint = FlareRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WIPE ENTIRE DATA LAB", color = FlareRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Are you absolutely sure physically? This erases all SQLite records permanently.", color = FlareRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.clearAllDiagnosticHistory()
                                        showWipeConfirm = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FlareRed),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("CONFIRM DELETION", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showWipeConfirm = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = SteelGray),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("CANCEL", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SIMULATOR INDIVIDUAL CARD SELECT FIELD ---
@Composable
fun SimulatorRadioOption(
    title: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) SteelGray.copy(0.4f) else Color.Transparent)
            .border(1.dp, if (selected) CyanGlow.copy(0.4f) else CardBorder, RoundedCornerShape(12.dp))
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = CyanGlow,
                    unselectedColor = TextSecondary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (selected) CyanGlow else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

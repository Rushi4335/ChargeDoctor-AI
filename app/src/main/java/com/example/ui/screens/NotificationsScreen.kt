package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.BatteryViewModel
import java.util.Locale

// Custom Notification Item structure mapping Mockup 6
data class StaticNotification(
    val title: String,
    val text: String,
    val time: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val category: String // Alerts, Tips, All
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: BatteryViewModel,
    onBack: () -> Unit
) {
    var activeFilter by remember { mutableStateOf("Alerts") } // All, Alerts, Tips

    var activeModalTitle by remember { mutableStateOf<String?>(null) }
    var activeModalBody by remember { mutableStateOf<String?>(null) }
    var activeModalColor by remember { mutableStateOf(CyanGlow) }
    var activeModalIcon by remember { mutableStateOf(Icons.Default.Notifications) }

    val notificationsList = remember {
        listOf(
            StaticNotification(
                title = "Overheat Alert",
                text = "Temperature reached 43°C. Charging speed may be reduced.",
                time = "10:30 AM",
                color = FlareRed,
                icon = Icons.Default.Whatshot,
                category = "Alerts"
            ),
            StaticNotification(
                title = "Slow Charging Alert",
                text = "Charging speed is lower than usual. Tap to diagnose.",
                time = "Yesterday",
                color = CoreAmber,
                icon = Icons.Default.Warning,
                category = "Alerts"
            ),
            StaticNotification(
                title = "Fast Charging Started",
                text = "Fast charging (21.4W) is active.",
                time = "Yesterday",
                color = ElectricBlue,
                icon = Icons.Default.OfflineBolt,
                category = "All"
            ),
            StaticNotification(
                title = "Fast Charging Lost",
                text = "Fast charging (Power Delivery) lost due to high core thermal stress indices.",
                time = "Yesterday",
                color = CoreAmber,
                icon = Icons.Default.Power,
                category = "Alerts"
            ),
            StaticNotification(
                title = "Charge Complete",
                text = "Your phone is fully charged. Unplug to protect battery.",
                time = "Yesterday",
                color = HyperGreen,
                icon = Icons.Default.CheckCircle,
                category = "All"
            ),
            StaticNotification(
                title = "Weekly Report Ready",
                text = "Your weekly battery report is ready. Tap to view.",
                time = "2 days ago",
                color = ElectricBlue,
                icon = Icons.Default.Assessment,
                category = "Tips"
            )
        )
    }

    // Filter notification records
    val filteredList = notificationsList.filter {
        activeFilter == "All" || it.category == activeFilter || (activeFilter == "Alerts" && (it.color == FlareRed || it.color == CoreAmber))
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
                        "NOTIFICATIONS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to More list Dashboard", tint = CyanGlow)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, "Filter alerts list", tint = CyanGlow)
                    }
                }
            )
        },
        containerColor = CyberObsidian
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("notifications_screen")
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- 1. FILTER CAPSULES TABS (MOCKUP 6 CAPSULES) ---
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SpaceSlate, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val filters = listOf("All", "Alerts", "Tips")
                        filters.forEach { f ->
                            val active = activeFilter == f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) HyperGreen else Color.Transparent)
                                    .clickable { activeFilter = f }
                                    .wrapContentHeight(Alignment.CenterVertically),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = f,
                                    color = if (active) CyberObsidian else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // --- 2. LIST OF NOTIFICATIONS (MOCKUP 6 CARDS) ---
                if (filteredList.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No alerts active inside this category", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    items(filteredList) { notif ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                                .clickable {
                                    activeModalIcon = notif.icon
                                    activeModalColor = notif.color
                                    activeModalTitle = notif.title
                                    activeModalBody = notif.text + "\n\nSystem verification has verified this alert state. Charging Doctor advises monitoring operational heats carefully."
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Circular Notification Category Bubble Icon
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(notif.color.copy(0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = notif.icon,
                                        contentDescription = null,
                                        tint = notif.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = notif.title,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = notif.time,
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = notif.text,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 3. ALERTS PREVIEWS CABINET (SIMULATOR TESTING PLAYGROUND) ---
                item {
                    Text(
                        "ALERTS DIAGNOSTIC SIMULATOR",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Intelligent Alerts Preview Cabinet",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tap any category to simulate systems alerts instantly on the floating overlay.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AlertTriggerBtn(
                                    label = "Overheat",
                                    color = FlareRed,
                                    onClick = {
                                        activeModalIcon = Icons.Default.Whatshot
                                        activeModalColor = FlareRed
                                        activeModalTitle = "🌡️ CRITICAL EMERGENCY: CELL OVERHEAT"
                                        activeModalBody = "WARNING: Core thermocouple recorded temperature of 44.5°C! Charging rate was downgraded to absolute zero to prevent safety swelling of delicate cathode components."
                                    }
                                )

                                AlertTriggerBtn(
                                    label = "Slow Charging",
                                    color = CoreAmber,
                                    onClick = {
                                        activeModalIcon = Icons.Default.Warning
                                        activeModalColor = CoreAmber
                                        activeModalTitle = "🐌 BOTTLE-NECK: SLOW CHARGING BLOCKED"
                                        activeModalBody = "Device has been charging for 30 minutes at sub-3.5W currents. This is typical of cheap non-coaxial wires, dirty socket terminals, or uncertified USB adapters."
                                    }
                                )

                                AlertTriggerBtn(
                                    label = "Loose Port",
                                    color = FlareRed,
                                    onClick = {
                                        activeModalIcon = Icons.Default.PowerInput
                                        activeModalColor = FlareRed
                                        activeModalTitle = "🔌 CONTACTS DIRTY: LOOSE PORT IDENTIFIED"
                                        activeModalBody = "Interruption cycles captured (3 times). Pocket lint or debris inside the female USB controller is preventing stable electrical handshakings."
                                    }
                                )

                                AlertTriggerBtn(
                                    label = "Sleep保護 report",
                                    color = CyanGlow,
                                    onClick = {
                                        activeModalIcon = Icons.Default.NightsStay
                                        activeModalColor = CyanGlow
                                        activeModalTitle = "🛌 OVERNIGHT TRICKLE COMP保护"
                                        activeModalBody = "Night calibration summary: Handset spent 5.2 hours saturated fully at maximum standard cellular voltage. Disconnect active screen loads to help protect plates."
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // --- 4. FLOATING OVERLAY MODAL SHEET ---
            AnimatedVisibility(
                visible = activeModalTitle != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, activeModalColor, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .testTag("simulated_notification_sheet")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(activeModalIcon, null, tint = activeModalColor)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("ALERTS BROADCAST RADAR", color = activeModalColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }

                            IconButton(onClick = {
                                activeModalTitle = null
                                activeModalBody = null
                            }) {
                                Icon(Icons.Default.Close, "Dismiss Simulation overlay", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = activeModalTitle ?: "",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberObsidian)
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = activeModalBody ?: "",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                activeModalTitle = null
                                activeModalBody = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = activeModalColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ACKNOWLEDGE HARDWARE WARNING", color = CyberObsidian, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertTriggerBtn(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(0.12f),
            contentColor = color
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.35f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(32.dp),
        contentPadding = PaddingValues(horizontal = 10.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}


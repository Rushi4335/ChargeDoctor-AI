package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.BatteryViewModel
import java.io.FileReader

@Composable
fun ReportsScreen(viewModel: BatteryViewModel) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf("brief") } // brief or csv
    var showSuccessToast by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. CONFIGURATION HEADER ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ENGINEERING EXPORTER CORE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Service Documentation Center", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Compile physical diagnostics, historical curves, and aging projections to export clean, technician-readable datasets.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TabSelectionButton("Brief Summary", active = selectedFormat == "brief", onClick = { selectedFormat = "brief" }, modifier = Modifier.weight(1f))
                        TabSelectionButton("CSV Spreadsheet", active = selectedFormat == "csv", onClick = { selectedFormat = "csv" }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // --- 2. REPORT GRAPHICAL PREVIEW WINDOW ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DOCUMENT COMPILED TRANSLATOR PREVIEW", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberObsidian)
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        // Generate a report right now on cache to display active live values
                        var reportContent by remember(selectedFormat) { mutableStateOf("") }
                        LaunchedEffect(selectedFormat) {
                            try {
                                val file = viewModel.generateExportFile(selectedFormat)
                                val text = FileReader(file).readText().take(400) + "\n\n... (Report truncated, ready for full export compilation) ..."
                                reportContent = text
                            } catch (e: Exception) {
                                reportContent = "Generating active fields..."
                            }
                        }

                        Text(
                            text = reportContent,
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            try {
                                val file = viewModel.generateExportFile(selectedFormat)
                                val textContent = FileReader(file).readText()

                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, textContent)
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "ChargeDoctor AI Diagnostics Report")
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Diagnostics Brief with:")
                                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(shareIntent)
                                showSuccessToast = true
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, "Share out Report", tint = CyberObsidian, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COMPILE & COMPANION SHARE REPORT", color = CyberObsidian, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 3. EXPLAINER ON SHARINGS ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SteelGray.copy(0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        "Document info",
                        tint = CyanGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("TECHNICIAN DISCLOSURE DOCUMENTATION", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Exported reports can be handed over directly to service center agents or battery swap stations. The detailed resistance indices (Impedance values) allow technicians to isolate cable deficiencies from physical charging IC hardware failures.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabSelectionButton(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) HyperGreen else SpaceSlate,
            contentColor = if (active) CyberObsidian else TextPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (active) HyperGreen else CardBorder),
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

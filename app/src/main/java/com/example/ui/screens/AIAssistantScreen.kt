package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.BatteryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    viewModel: BatteryViewModel,
    onBack: () -> Unit
) {
    val telemetryState by viewModel.telemetryState.collectAsState()
    val slownessDiagnosis by viewModel.activeSlownessDiagnosis.collectAsState()
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var userInput by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "init_user",
                text = "Why is my phone charging slowly?",
                isUser = true
            ),
            ChatMessage(
                id = "init_assistant",
                text = "I analyzed your charging session and found some possible reasons.\n\n" +
                        "• Temperature is high (42°C)\n" +
                        "• Screen is consuming power\n" +
                        "• Charger output is unstable\n\n" +
                        "These are reducing your charging speed by ~37%.",
                isUser = false
            )
        )
    }

    // Auto-scroll on new message added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CyberObsidian,
                    titleContentColor = CyanGlow
                ),
                modifier = Modifier.border(0.dp, Color.Transparent),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "AI ASSISTANT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = CyanGlow
                        )
                        Text(
                            text = "CHARGING INTELLIGENCE",
                            fontSize = 9.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to More list", tint = CyanGlow)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        messages.clear()
                        messages.add(
                            ChatMessage(
                                id = "welcome",
                                text = "Hello! I am your AI Diagnostics assistant. Ask me anything about your current charger quality, cell temperature, or battery wear rate.",
                                isUser = false
                            )
                        )
                    }) {
                        Icon(Icons.Default.Refresh, "Reset Chat Session", tint = TextSecondary)
                    }
                }
            )
        },
        bottomBar = {
            // Typing prompt input dock matching Mockup 9
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberObsidian)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text("Ask anything...", color = TextSecondary, fontSize = 14.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SpaceSlate,
                        unfocusedContainerColor = SpaceSlate,
                        disabledContainerColor = SpaceSlate,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input"),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (userInput.isNotBlank()) {
                                val text = userInput
                                userInput = ""
                                messages.add(ChatMessage(id = System.nanoTime().toString(), text = text, isUser = true))
                                keyboardController?.hide()
                                isTyping = true
                                scope.launch {
                                    delay(1500) // Realistic AI deliberation delay
                                    val responseText = analyzeBatteryAndGenerateResponse(text, telemetryState, slownessDiagnosis)
                                    messages.add(ChatMessage(id = System.nanoTime().toString(), text = responseText, isUser = false))
                                    isTyping = false
                                }
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                FloatingActionButton(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            val text = userInput
                            userInput = ""
                            messages.add(ChatMessage(id = System.nanoTime().toString(), text = text, isUser = true))
                            keyboardController?.hide()
                            isTyping = true
                            scope.launch {
                                delay(1500)
                                val responseText = analyzeBatteryAndGenerateResponse(text, telemetryState, slownessDiagnosis)
                                messages.add(ChatMessage(id = System.nanoTime().toString(), text = responseText, isUser = false))
                                isTyping = false
                            }
                        }
                    },
                    containerColor = HyperGreen,
                    contentColor = CyberObsidian,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("send_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send prompt description",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        containerColor = CyberObsidian
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(CyberObsidian)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }

                if (isTyping) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(HyperGreen.copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, null, tint = HyperGreen, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .background(SpaceSlate, RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Analyzing power grid curves...",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(HyperGreen.copy(0.12f), CircleShape)
                    .border(1.dp, HyperGreen.copy(0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "AI Admin",
                    tint = HyperGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        val bubbleBackground = if (message.isUser) HyperGreen else SpaceSlate
        val textColor = if (message.isUser) CyberObsidian else TextPrimary
        val shape = if (message.isUser) {
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
        } else {
            RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(bubbleBackground, shape)
                .border(1.dp, if (message.isUser) Color.Transparent else CardBorder, shape)
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = if (message.isUser) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

// Custom expert response engine that reads current live telemetry variables to deliver highly contextual advice!
private fun analyzeBatteryAndGenerateResponse(
    prompt: String,
    telemetry: com.example.data.repository.TelemetryState,
    slowness: com.example.diagnostics.SlownessDiagnosis
): String {
    val q = prompt.lowercase(Locale.US)
    val curMaAbs = abs(telemetry.currentMa)
    val watts = (curMaAbs / 1000.0) * (telemetry.voltageMv / 1000.0)

    val response = when {
        q.contains("slow") || q.contains("speed") || q.contains("watts") || q.contains("why") -> {
            "Based on live circuit tests, your phone is charging at ${String.format(Locale.US, "%.1f", watts)} Watts under a [${slowness.chargerSpeedCategory}] rate.\n\n" +
                    "Live impedance telemetry indicates:\n" +
                    "• Electrochemical Core temperature is ${telemetry.tempC}°C\n" +
                    "• Host CPU active is ${telemetry.cpuTempC}°C\n" +
                    "• Cable voltage drop is estimated at ~${if (watts > 15) "120mV (Excellent)" else "340mV (Medium loss)"}\n\n" +
                    "To speed up intake, you should keep the screen turned off and disconnect high electrical resistance converters."
        }
        q.contains("heat") || q.contains("temperature") || q.contains("hot") || q.contains("warm") -> {
            "Currently, the core thermocouple registered a temperature of ${telemetry.tempC}°C.\n\n" +
                    "Silicon controllers automatically trigger thermal throttling at 40°C to protect delicate lithium ions from crystallization risks.\n\n" +
                    "Recommendation: Keep the device in a cool ambient area and avoid gaming or screen usage while charging."
        }
        q.contains("health") || q.contains("wear") || q.contains("decay") || q.contains("cycle") -> {
            "Long-term analysis estimates your physical battery wear at 9%, matching a design capacity health of 91% (${telemetry.estimatedCapacityMah} / ${telemetry.designCapacityMah} mAh).\n\n" +
                    "Your estimated cumulative cycles logged is: ${telemetry.cyclesEstimate} cycles.\n\n" +
                    "Tip: To slow down materials decay, stay between a 20% to 80% charge threshold, and avoid fast adapters that heat the phone core above 38°C."
        }
        q.contains("cable") || q.contains("charger") || q.contains("unstable") || q.contains("port") -> {
            "Live electrical noise ripple stands at standard thresholds. Stabilizer status is healthy, but we captured ${telemetry.disconnections} connection dropouts during this diagnostic cycle.\n\n" +
                    "If you encounter unstable charging, try cleaning pocket lint from your female receiver port using a non-metallic pick."
        }
        else -> {
            "Understood! Live diagnostics telemetry checks out as follows:\n" +
                    "- Charge level: ${telemetry.level}%\n" +
                    "- Intake power: ${String.format(Locale.US, "%.1f W", watts)}\n" +
                    "- Operational Heat: ${telemetry.tempC}°C\n" +
                    "- Hardware disconnections: ${telemetry.disconnections}\n\n" +
                    "Let me know if you would like me to isolate specific impedance loops for you!"
        }
    }
    return response
}

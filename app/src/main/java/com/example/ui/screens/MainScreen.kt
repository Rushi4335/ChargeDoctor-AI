package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ChargingSession
import com.example.ui.theme.*
import com.example.ui.viewmodel.BatteryViewModel
import kotlinx.coroutines.launch

enum class MainScreenPage(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    DIAGNOSTICS("AI Diagnostics", Icons.Default.AutoAwesome),
    TESTS("Circuit Tests", Icons.Default.Cable),
    HEALTH("Battery Health", Icons.Default.Favorite),
    ANALYTICS("Analytics Trends", Icons.Default.BarChart),
    ASSISTANT("AI Assistant", Icons.Default.Chat),
    NOTIFICATIONS("Alerts Lab", Icons.Default.Notifications),
    SETTINGS("Control Panel", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: BatteryViewModel) {
    var currentPage by remember { mutableStateOf(MainScreenPage.DASHBOARD) }
    var selectedSessionForDetail by remember { mutableStateOf<ChargingSession?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Handle physical hardware back buttons safely
    BackHandler(enabled = currentPage != MainScreenPage.DASHBOARD || selectedSessionForDetail != null) {
        if (selectedSessionForDetail != null) {
            selectedSessionForDetail = null
        } else {
            currentPage = MainScreenPage.DASHBOARD
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SpaceSlate,
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .testTag("app_navigation_drawer")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberObsidian)
                        .drawBehind { drawLine(color = CardBorder, start = Offset(0f, size.height), end = Offset(size.width, size.height), strokeWidth = 1.dp.toPx()) }
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Text(
                        text = "CHARGEDOCTOR AI",
                        color = CyanGlow,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Diagnostics & Intelligence Platform",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Drawer navigation entries
                MainScreenPage.values().forEach { page ->
                    NavigationDrawerItem(
                        icon = { Icon(page.icon, contentDescription = page.title, tint = if (currentPage == page) CyberObsidian else CyanGlow) },
                        label = { Text(page.title, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        selected = currentPage == page,
                        onClick = {
                            selectedSessionForDetail = null
                            currentPage = page
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = CyanGlow,
                            selectedTextColor = CyberObsidian,
                            unselectedTextColor = TextPrimary,
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .height(48.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Divider(color = CardBorder, modifier = Modifier.padding(16.dp))

                Text(
                    text = "v1.0.0 (Release-Engine v2)",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .align(Alignment.Start)
                )
            }
        }
    ) {
        val showMainAppBars = selectedSessionForDetail == null

        Scaffold(
            topBar = {
                if (showMainAppBars) {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = CyberObsidian,
                            titleContentColor = CyanGlow
                        ),
                        modifier = Modifier.drawBehind { drawLine(color = CardBorder, start = Offset(0f, size.height), end = Offset(size.width, size.height), strokeWidth = 1.dp.toPx()) },
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = currentPage.title.uppercase(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = CyanGlow
                                )
                                Text(
                                    text = "CHARGEDOCTOR AI",
                                    fontSize = 9.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, "Open Menu Drawer", tint = CyanGlow)
                            }
                        },
                        actions = {
                            IconButton(onClick = { currentPage = MainScreenPage.SETTINGS }) {
                                Icon(Icons.Default.Settings, "Open Settings control panel", tint = TextSecondary)
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (showMainAppBars) {
                    NavigationBar(
                        containerColor = CyberObsidian,
                        modifier = Modifier
                            .drawBehind { drawLine(color = CardBorder, start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = 1.dp.toPx()) }
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        val primaryPages = listOf(
                            MainScreenPage.DASHBOARD,
                            MainScreenPage.DIAGNOSTICS,
                            MainScreenPage.TESTS,
                            MainScreenPage.HEALTH,
                            MainScreenPage.ANALYTICS
                        )

                        primaryPages.forEach { page ->
                            NavigationBarItem(
                                icon = { Icon(page.icon, contentDescription = page.title) },
                                selected = currentPage == page,
                                onClick = {
                                    selectedSessionForDetail = null
                                    currentPage = page
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberObsidian,
                                    selectedTextColor = CyanGlow,
                                    indicatorColor = CyanGlow,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CyberObsidian)
                    .padding(if (showMainAppBars) paddingValues else PaddingValues(0.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                // If nested Session Detail screen has been select, render it directly
                if (selectedSessionForDetail != null) {
                    ChargingSessionScreen(
                        session = selectedSessionForDetail!!,
                        onBack = { selectedSessionForDetail = null }
                    )
                } else {
                    AnimatedContent(
                        targetState = currentPage,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "Screen switch animations core"
                    ) { targetPage ->
                        when (targetPage) {
                            MainScreenPage.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToDiagnostics = { currentPage = MainScreenPage.DIAGNOSTICS },
                                onNavigateToSettings = { currentPage = MainScreenPage.SETTINGS }
                            )
                            MainScreenPage.DIAGNOSTICS -> DiagnosticsScreen(viewModel = viewModel)
                            MainScreenPage.TESTS -> HardwareTestsScreen(
                                viewModel = viewModel,
                                onBack = { currentPage = MainScreenPage.DASHBOARD }
                            )
                            MainScreenPage.HEALTH -> HealthScreen(
                                viewModel = viewModel,
                                onBack = { currentPage = MainScreenPage.DASHBOARD }
                            )
                            MainScreenPage.ANALYTICS -> AnalyticsScreen(
                                viewModel = viewModel,
                                onNavigateToSessionDetail = { selectSess ->
                                    selectedSessionForDetail = selectSess
                                }
                            )
                            MainScreenPage.ASSISTANT -> AIAssistantScreen(
                                viewModel = viewModel,
                                onBack = { currentPage = MainScreenPage.DASHBOARD }
                            )
                            MainScreenPage.NOTIFICATIONS -> NotificationsScreen(
                                viewModel = viewModel,
                                onBack = { currentPage = MainScreenPage.DASHBOARD }
                            )
                            MainScreenPage.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

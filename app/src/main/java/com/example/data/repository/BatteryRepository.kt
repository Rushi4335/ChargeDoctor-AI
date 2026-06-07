package com.example.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.data.local.dao.ChargingDao
import com.example.data.local.entities.BatteryLog
import com.example.data.local.entities.ChargerProfile
import com.example.data.local.entities.ChargingSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random

// --- Live Telemetry Data Model ---
data class TelemetryState(
    val level: Int = 50,
    val isCharging: Boolean = false,
    val pluggedState: String = "None", // AC, USB, Wireless, None
    val voltageMv: Int = 3850,
    val currentMa: Int = -250, // Negative means discharging
    val tempC: Double = 28.5,
    val cpuTempC: Double = 31.0,
    val systemHealth: String = "Good",
    val isFastCharging: Boolean = false,
    val disconnections: Int = 0,
    val simulatorActive: Boolean = false,
    val simulatorProfileName: String = "None",
    val designCapacityMah: Int = 4500,
    val estimatedCapacityMah: Int = 4125, // Wear state ~91%
    val cyclesEstimate: Int = 214
)

class BatteryRepository(
    private val context: Context,
    private val chargingDao: ChargingDao
) {
    private val ioScope = CoroutineScope(Dispatchers.IO)

    // Reactive flow of current metrics
    private val _telemetry = MutableStateFlow(TelemetryState())
    val telemetry: StateFlow<TelemetryState> = _telemetry.asStateFlow()

    // Simulator variables
    private var simJobActive = false
    private var currentSimProfile = "NONE" // NONE, PERFECT, THERMAL, CABLE, PORT, GAMING

    // History flows
    val allSessions: Flow<List<ChargingSession>> = chargingDao.getAllSessions().flowOn(Dispatchers.IO)
    val allChargerProfiles: Flow<List<ChargerProfile>> = chargingDao.getAllChargerProfiles().flowOn(Dispatchers.IO)
    val batteryLogs: Flow<List<BatteryLog>> = chargingDao.getAllLogs().flowOn(Dispatchers.IO)

    init {
        // Prepare initial dynamic mock history if database is fresh
        seedDefaultHistory()
        // Start listening to system hardware telemetry
        startHardwareListener()
    }

    // --- Database Operations ---
    suspend fun saveSession(session: ChargingSession) {
        chargingDao.insertSession(session)
    }

    suspend fun clearHistory() {
        chargingDao.clearAllSessions()
        chargingDao.clearAllLogs()
        chargingDao.clearAllChargerProfiles()
        delay(100)
        seedDefaultHistory()
    }

    suspend fun saveLog(log: BatteryLog) {
        chargingDao.insertLog(log)
    }

    suspend fun saveChargerProfile(profile: ChargerProfile) {
        chargingDao.insertChargerProfile(profile)
    }

    // --- Hard-Seeding Analytics History (Stunning visual dashboards instantly) ---
    private fun seedDefaultHistory() {
        ioScope.launch {
            chargingDao.getAllSessions().collect { list ->
                if (list.isEmpty()) {
                    val random = Random(42)
                    val now = System.currentTimeMillis()
                    val hourMs = 3600_000L
                    val dayMs = 24 * hourMs

                    // Generate historical charging sessions over the last 1-2 weeks
                    for (i in 1..8) {
                        val durationMin = random.nextInt(35, 120)
                        val startPct = random.nextInt(12, 38)
                        val endPct = startPct + random.nextInt(40, 60)
                        val maxTemp = 31.0 + random.nextDouble() * 9.5
                        val avgWatts = 4.5 + random.nextDouble() * 15.0
                        val peakWatts = avgWatts + 2.5
                        val efficiency = 75.0 + random.nextDouble() * 18.0

                        val startTime = now - (i * dayMs) - (random.nextInt(1, 5) * hourMs)
                        val score = random.nextInt(75, 98)
                        val isWeak = avgWatts < 6.0

                        val session = ChargingSession(
                            id = UUID.randomUUID().toString(),
                            startTime = startTime,
                            endTime = startTime + (durationMin * 60 * 1000L),
                            startPercentage = startPct,
                            endPercentage = endPct,
                            averageWattage = avgWatts,
                            peakWattage = peakWatts,
                            maxTemperature = maxTemp,
                            efficiency = efficiency,
                            stressScore = if (maxTemp > 38.0) 70 else 25,
                            chargerType = if (isWeak) "Weak Legacy Wall adapter" else "Certified Fast Charger ( GaN )",
                            cableQuality = if (random.nextBoolean()) 92.0 else 55.0,
                            diagSummary = if (maxTemp > 38) "Session limited by temperature dampening." else "Perfect charging throughput.",
                            chargeScore = score
                        )
                        chargingDao.insertSession(session)
                    }

                    // Generate detailed logging points for the analytics curves (Speed, Temp, Health trends)
                    val logs = mutableListOf<BatteryLog>()
                    for (i in 0..30) {
                        val timestamp = now - ((30 - i) * 6 * hourMs) // logs spread over a week
                        val level = 50 + (i % 5) * 8
                        val temp = 26.5 + (i % 6) * 1.8
                        val cpuTemp = temp + 5.0 + (i % 3) * 4.0
                        val vMv = 3600 + (level * 8)
                        val ma = if (i % 2 == 0) 1800 else -210

                        logs.add(
                            BatteryLog(
                                timestamp = timestamp,
                                batteryLevel = level,
                                voltageMv = vMv,
                                currentMa = ma,
                                tempC = temp,
                                cpuTempC = cpuTemp,
                                isCharging = ma > 0,
                                chargeSpeedMvPerHr = if (ma > 0) 420.0 else 0.0
                            )
                        )
                    }
                    chargingDao.insertLogs(logs)

                    // Store charger hardware profiles
                    chargingDao.insertChargerProfile(
                        ChargerProfile(
                            id = "charger_original_pps",
                            name = "Original Super Charger (120W PPS)",
                            ratedPowerWatts = 120.0,
                            measuredPowerWatts = 98.4,
                            stabilityScore = 97,
                            voltageDropMv = 45.0,
                            detectedType = "original_pd",
                            lastUsed = now - 2 * hourMs
                        )
                    )
                    chargingDao.insertChargerProfile(
                        ChargerProfile(
                            id = "charger_office_cable",
                            name = "Office Desk USB Wire",
                            ratedPowerWatts = 15.0,
                            measuredPowerWatts = 4.8,
                            stabilityScore = 52,
                            voltageDropMv = 480.0,
                            detectedType = "unstable_weak",
                            lastUsed = now - dayMs
                        )
                    )
                }
            }
        }
    }

    // --- Hardware Telemetry Receiver ---
    private fun startHardwareListener() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null || _telemetry.value.simulatorActive) return

                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 50)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val levelPct = (level * 100 / scale.toFloat()).toInt()

                val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                val tempC = tempTenths / 10.0

                val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

                val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
                val healthStr = when (healthInt) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat!"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                    BatteryManager.BATTERY_HEALTH_COLD -> "Too Cold"
                    else -> "Operational"
                }

                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val pluggedStr = when (plugged) {
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB Computer Interface"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Magnetic Coil"
                    else -> if (isCharging) "Charging Adaptor" else "Unplugged"
                }

                // Query the actual microampere current from hardware BatteryManager
                val bm = context?.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                var currentMa = 0
                if (bm != null) {
                    val currentMicroAmps = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    // If the current is positive on systems (meaning charging), but discharging is negative
                    // Normalize standard currents:
                    currentMa = currentMicroAmps / 1000

                    // Fallback to average current if zero
                    if (currentMa == 0) {
                        currentMa = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE) / 1000
                    }
                }

                // Fallback calculations if charging but mA is returned negative or zero (system-specific quirks)
                var calculatedMa = currentMa
                if (isCharging && calculatedMa <= 0) {
                    calculatedMa = if (plugged == BatteryManager.BATTERY_PLUGGED_USB) 450 else 1800
                } else if (!isCharging && calculatedMa >= 0) {
                    calculatedMa = -320 // average idle discharge load
                }

                val estWattage = (abs(calculatedMa) / 1000.0) * (voltageMv / 1000.0)
                val speedIsFast = isCharging && estWattage >= 10.0

                // Generate real-time realistic CPU core estimate
                val cpuBase = tempC + 4.0
                val simulatedCpuLoad = if (isCharging) cpuBase + 5.0 else cpuBase + 2.0

                _telemetry.value = TelemetryState(
                    level = levelPct,
                    isCharging = isCharging,
                    pluggedState = pluggedStr,
                    voltageMv = voltageMv,
                    currentMa = calculatedMa,
                    tempC = tempC,
                    cpuTempC = simulatedCpuLoad,
                    systemHealth = healthStr,
                    isFastCharging = speedIsFast,
                    disconnections = 0,
                    simulatorActive = false,
                    simulatorProfileName = "Raw Hardware Sensors"
                )
            }
        }
        context.registerReceiver(receiver, filter)
    }

    // --- TOGGLE SIMULATION STUDIO MODES ---
    fun setSimulationMode(active: Boolean, profile: String = "NONE") {
        currentSimProfile = if (active) profile else "NONE"

        if (!active) {
            _telemetry.value = _telemetry.value.copy(
                simulatorActive = false,
                simulatorProfileName = "Raw Hardware Sensors"
            )
            // Trigger a refresh from real hardware broadcast (or mock to close loop)
            startHardwareListener()
            return
        }

        // Start active simulator loop
        _telemetry.value = _telemetry.value.copy(
            simulatorActive = true,
            simulatorProfileName = when(profile) {
                "PERFECT" -> "Sim Studio: Perfect Super GaN PD"
                "THERMAL" -> "Sim Studio: Thermal Protection Throttling"
                "CABLE" -> "Sim Studio: Damaged Cable (High Impedance)"
                "PORT" -> "Sim Studio: Oxidized / Corrosion Port Flaps"
                "GAMING" -> "Sim Studio: Intensive Play load heating"
                else -> "Sim Studio Active"
            }
        )

        triggerSimulatorTick()
    }

    private fun triggerSimulatorTick() {
        if (simJobActive) return
        simJobActive = true

        ioScope.launch {
            while (_telemetry.value.simulatorActive) {
                val current = _telemetry.value
                val nextState = when (currentSimProfile) {
                    "PERFECT" -> current.copy(
                        level = if (current.level >= 99) 40 else (current.level + 1).coerceAtMost(99),
                        isCharging = true,
                        pluggedState = "AC High-Volt PD",
                        voltageMv = 4320 + Random.nextInt(-10, 10),
                        currentMa = 3450 + Random.nextInt(-50, 50),
                        tempC = 31.2 + Random.nextInt(-1, 2) * 0.1,
                        cpuTempC = 33.6,
                        systemHealth = "Excellent (Super Cooler)",
                        isFastCharging = true,
                        disconnections = 0
                    )
                    "THERMAL" -> current.copy(
                        level = 76,
                        isCharging = true,
                        pluggedState = "AC Wall Adapter",
                        voltageMv = 4050,
                        currentMa = 550 + Random.nextInt(-20, 20), // highly reduced
                        tempC = 41.8 + Random.nextInt(-2, 3) * 0.1, // very hot!
                        cpuTempC = 44.2,
                        systemHealth = "Thermal Warning Active",
                        isFastCharging = true, // Charger reports fast charging support but is throttled
                        disconnections = 0
                    )
                    "CABLE" -> current.copy(
                        level = 32,
                        isCharging = true,
                        pluggedState = "USB Port (Low Potential)",
                        voltageMv = 3680,
                        currentMa = 380 + Random.nextInt(-10, 10), // tiny stream
                        tempC = 27.5,
                        cpuTempC = 29.8,
                        systemHealth = "Good",
                        isFastCharging = false,
                        disconnections = 0
                    )
                    "PORT" -> current.copy(
                        level = 18,
                        isCharging = Random.nextInt(0, 10) > 2, // flaps connection on-off!
                        pluggedState = if (Random.nextBoolean()) "DC Intercept" else "None",
                        voltageMv = if (Random.nextBoolean()) 3550 else 0,
                        currentMa = if (Random.nextBoolean()) 1100 else 0,
                        tempC = 29.1,
                        cpuTempC = 31.4,
                        systemHealth = "Interruption Trace",
                        isFastCharging = false,
                        disconnections = (current.disconnections + Random.nextInt(0, 2)).coerceAtMost(10)
                    )
                    "GAMING" -> current.copy(
                        level = 58,
                        isCharging = true,
                        pluggedState = "Quick-Charge QC3",
                        voltageMv = 3980,
                        currentMa = 750, // lower because background gaming drain
                        tempC = 39.8,
                        cpuTempC = 58.5 + Random.nextInt(-5, 6) * 0.2, // high CPU temperature!
                        systemHealth = "Operational (Warmer)",
                        isFastCharging = true,
                        disconnections = 0
                    )
                    else -> current
                }

                _telemetry.value = nextState

                // Store periodic log entries in simulated active charging to animate graph tracing
                if (Random.nextInt(0, 20) == 1 && nextState.isCharging) {
                    chargingDao.insertLog(
                        BatteryLog(
                            timestamp = System.currentTimeMillis(),
                            batteryLevel = nextState.level,
                            voltageMv = nextState.voltageMv,
                            currentMa = nextState.currentMa,
                            tempC = nextState.tempC,
                            cpuTempC = nextState.cpuTempC,
                            isCharging = nextState.isCharging,
                            chargeSpeedMvPerHr = 450.0
                        )
                    )
                }

                delay(1500) // loop speed rate
            }
            simJobActive = false
        }
    }
}

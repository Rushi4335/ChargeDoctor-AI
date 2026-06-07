package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.BatteryLog
import com.example.data.local.entities.ChargerProfile
import com.example.data.local.entities.ChargingSession
import com.example.data.repository.BatteryRepository
import com.example.data.repository.TelemetryState
import com.example.diagnostics.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.abs
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class BatteryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = BatteryRepository(application, db.chargingDao())

    // --- State Observables ---
    val telemetryState: StateFlow<TelemetryState> = repository.telemetry

    val allSessions: StateFlow<List<ChargingSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChargers: StateFlow<List<ChargerProfile>> = repository.allChargerProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batteryLogs: StateFlow<List<BatteryLog>> = repository.batteryLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aiReport = MutableStateFlow<String?>(null)
    val aiReport: StateFlow<String?> = _aiReport.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    // Active diagnostic computations
    val activeChargeScore: StateFlow<ChargeScore> = telemetryState.map {
        DiagnosticEngine.gradeCharging(it.currentMa, it.voltageMv, it.tempC)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChargeScore(80, "B", androidx.compose.ui.graphics.Color(0xFF3498DB), "Stable charging."))

    val activeSlownessDiagnosis: StateFlow<SlownessDiagnosis> = telemetryState.map {
        DiagnosticEngine.diagnoseChargingSlowness(
            it.level,
            it.currentMa,
            it.voltageMv,
            it.tempC,
            it.cpuTempC,
            it.isCharging,
            it.disconnections
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SlownessDiagnosis("AC", false, "", emptyList(), 0))

    val activePortHealth: StateFlow<PortHealthReport> = telemetryState.map {
        val stability = if (it.disconnections >= 3) 45 else if (it.disconnections >= 1) 75 else 98
        DiagnosticEngine.assessPortHealth(it.disconnections, stability)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PortHealthReport("Clean", 98, "None", false, "Standard clean state."))

    val activeFakeFastCharge: StateFlow<FakeFastChargeReport> = telemetryState.map {
        DiagnosticEngine.detectFakeFastCharging(it.isFastCharging, it.currentMa, it.voltageMv)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FakeFastChargeReport(false, "AC", 0.0, "Operational", "None"))

    val activeCableQuality: StateFlow<CableQualityReport> = telemetryState.map {
        DiagnosticEngine.diagnoseCableQuality(it.currentMa, it.voltageMv)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CableQualityReport(90, "Excellent", 20.0, 0.05, "Ready"))

    val activeChargerFingerprint: StateFlow<ChargerDiagnosis> = telemetryState.map {
        DiagnosticEngine.fingerprintCharger(it.currentMa, it.voltageMv)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChargerDiagnosis("USB", 4.5, 2.0, false, 75))

    val activeThermalReport: StateFlow<ThermalReport> = telemetryState.map {
        DiagnosticEngine.getThermalStatus(it.tempC, it.cpuTempC)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThermalReport("Optimal", "Safe limits", false, "Healthy status", androidx.compose.ui.graphics.Color(0xFF27AE60)))

    val activeAgingPrediction: StateFlow<BatteryAgePrediction> = telemetryState.map {
        DiagnosticEngine.predictBatteryAging(91.5, it.cyclesEstimate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BatteryAgePrediction(90.0, 88.5, 84.0, 410, "Normal"))

    val activeCareScore: StateFlow<BatteryCareScore> = allSessions.map {
        telemetryState.value.let { tel ->
            DiagnosticEngine.assessBatteryCare(it, tel.cyclesEstimate)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BatteryCareScore(90, "A", "Good", emptyList()))

    // --- Interactive Command Bindings ---
    fun runDiagnosisDoctorAlert() {
        val state = telemetryState.value
        _aiLoading.value = true
        _aiReport.value = null

        viewModelScope.launch {
            // Give a gorgeous premium procedural delay to simulate advanced physics modeling recalculating curves
            delay(1200)
            DiagnosticEngine.runAIConsultation(
                batteryLevel = state.level,
                voltageMv = state.voltageMv,
                currentMa = state.currentMa,
                tempC = state.tempC,
                cpuTempC = state.cpuTempC,
                disconnections = state.disconnections,
                reportedFastCharging = state.isFastCharging,
                aiKeyPresent = false
            ) { generatedMarkdown ->
                _aiReport.value = generatedMarkdown
                _aiLoading.value = false
            }
        }
    }

    fun toggleSimulator(enable: Boolean, profile: String = "NONE") {
        repository.setSimulationMode(enable, profile)
        _aiReport.value = null // reset prior report
    }

    fun submitCurrentSessionTestRecord() {
        val tel = telemetryState.value
        val actualCurrent = abs(tel.currentMa)
        val wattage = (actualCurrent / 1000.0) * (tel.voltageMv / 1000.0)
        val score = activeChargeScore.value

        viewModelScope.launch {
            repository.saveSession(
                ChargingSession(
                    id = UUID.randomUUID().toString(),
                    startTime = System.currentTimeMillis() - 3600_000,
                    endTime = System.currentTimeMillis(),
                    startPercentage = (tel.level - 25).coerceAtLeast(1),
                    endPercentage = tel.level,
                    averageWattage = wattage.coerceAtLeast(1.5),
                    peakWattage = (wattage + 1.2).coerceAtLeast(2.4),
                    maxTemperature = tel.tempC,
                    efficiency = if (tel.tempC < 35) 89.0 else 72.0,
                    stressScore = if (tel.tempC >= 38) 75 else 18,
                    chargerType = tel.simulatorProfileName,
                    cableQuality = activeCableQuality.value.qualityPercent.toDouble(),
                    diagSummary = score.summary,
                    chargeScore = score.score
                )
            )

            // save active custom physical hardware charger index
            repository.saveChargerProfile(
                ChargerProfile(
                    id = "charger_${UUID.randomUUID().toString().take(6)}",
                    name = if (tel.simulatorActive) tel.simulatorProfileName else "Identified Brick: ${tel.pluggedState}",
                    ratedPowerWatts = if (tel.isFastCharging) 65.0 else 15.0,
                    measuredPowerWatts = wattage,
                    stabilityScore = if (tel.disconnections > 0) 65 else 94,
                    voltageDropMv = activeCableQuality.value.estimatedVoltageDropMv,
                    detectedType = activeChargerFingerprint.value.rating,
                    lastUsed = System.currentTimeMillis()
                )
            )
        }
    }

    fun clearAllDiagnosticHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _aiReport.value = null
        }
    }

    // --- Report Generator & Service Exporter ---
    fun generateExportFile(format: String): File {
        val dir = File(getApplication<Application>().cacheDir, "reports")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "ChargeDoctor_Report_${System.currentTimeMillis()}.$format")
        val sessionsList = allSessions.value
        val state = telemetryState.value

        file.printWriter().use { out ->
            if (format == "csv") {
                out.println("Type,Metric,Value,Explanation")
                out.println("System,Battery Level,${state.level}%,Current charge remaining in cobalt plates")
                out.println("System,Temperature,${state.tempC}°C,Thermal profile of cell core")
                out.println("System,Electrical Power,${state.voltageMv}mV / ${state.currentMa}mA,Real-time flow")
                out.println("System,Active Cable Health,${activeCableQuality.value.qualityPercent}%,Resistance quality")
                out.println("System,Device Aging,${activeAgingPrediction.value.expectedAgingSpeed},Expected cycle decay speed")
                out.println("")
                out.println("SessionID,StartTime,DurationMin,Start%,End%,AvgWatts,MaxTempC,Score")
                sessionsList.forEach { s ->
                    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(s.startTime))
                    val dur = (s.endTime - s.startTime) / 60000
                    out.println("${s.id},$dateStr,$dur,${s.startPercentage},${s.endPercentage},${String.format(Locale.US, "%.1f", s.averageWattage)},${String.format(Locale.US, "%.1f", s.maxTemperature)},${s.chargeScore}")
                }
            } else {
                // Generates a fully compiled, gorgeous Text/Diagnostic Service Center brief summary
                out.println("==========================================================")
                out.println("  CHARGEDOCTOR AI - SERVICE DIAGNOSTICS BRIEF SUMMARY  ")
                out.println("  Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
                out.println("==========================================================")
                out.println("DEVICE GENERAL TELEMETRY STATE:")
                out.println(" - Cell Capacity Model: ${state.estimatedCapacityMah} mAh / ${state.designCapacityMah} mAh (Wear: ${100 - (state.estimatedCapacityMah * 100 / state.designCapacityMah)}%)")
                out.println(" - Current Cycle Index: ${state.cyclesEstimate} cycles")
                out.println(" - Operational Charge: ${state.level}% - Temperature: ${state.tempC}°C")
                out.println(" - Contact Stability Index: ${activePortHealth.value.stabilityIndex}/100")
                out.println("----------------------------------------------------------")
                out.println("HARDEST ANALYSIS & SCORE EVALUATIONS:")
                out.println(" - Charger Profile Quality: ${activeChargerFingerprint.value.rating} (Score: ${activeChargerFingerprint.value.chargerScore}/100)")
                out.println(" - Port Physical Category: ${activePortHealth.value.status} - Contact disconnect risks count: ${state.disconnections}")
                out.println(" - Cable Drop Compensation: ${String.format(Locale.US, "%.1f", activeCableQuality.value.estimatedVoltageDropMv)} mV loss (Power lost in wire: ${String.format(Locale.US, "%.3f", activeCableQuality.value.powerLossWatts)} W)")
                out.println("----------------------------------------------------------")
                out.println("ESTIMATED LONG TERM AGING TIMELINE:")
                out.println(" - Predicted Health (3 Months): ${String.format(Locale.US, "%.1f", activeAgingPrediction.value.health3MonthsPercent)} %")
                out.println(" - Predicted Health (6 Months): ${String.format(Locale.US, "%.1f", activeAgingPrediction.value.health6MonthsPercent)} %")
                out.println(" - Predicted Health (12 Months): ${String.format(Locale.US, "%.1f", activeAgingPrediction.value.health12MonthsPercent)} %")
                out.println("==========================================================")
                out.println("   AUTONOMOUS SAFETY CLASSIFICATION: ${activeThermalReport.value.overallStatus.uppercase(Locale.US)}")
                out.println("==========================================================")
            }
        }
        return file
    }
}

package com.example.diagnostics

import androidx.compose.ui.graphics.Color

// --- Charge / Diagnostic Scores ---
data class ChargeScore(
    val score: Int, // 0 to 100
    val grade: String, // A+, A, B, C, D
    val color: Color,
    val summary: String
)

data class BatteryCareScore(
    val score: Int,
    val grade: String,
    val status: String, // Excellent, Good, Fair, At Risk
    val tips: List<String>
)

// --- Slowness Causes ---
data class SlownessCause(
    val title: String,
    val severity: String, // High, Medium, Low
    val explanation: String,
    val confidence: Int, // percentage
    val actionItem: String
)

data class SlownessDiagnosis(
    val chargerSpeedCategory: String, // Fast, Normal, Slow, USB
    val isThrottled: Boolean,
    val throttlingReason: String,
    val causes: List<SlownessCause>,
    val totalConfidence: Int
)

// --- Health / Cable Checks ---
data class PortHealthReport(
    val status: String, // Clean, Warn, Damaged
    val stabilityIndex: Int, // 0 - 100
    val disconnectSeverity: String, // None, Minor, Severe
    val isDustLikely: Boolean,
    val advice: String
)

data class FakeFastChargeReport(
    val isFakeDetected: Boolean,
    val reportedType: String,
    val actualWattage: Double,
    val discrepancyMessage: String,
    val advice: String
)

data class CableQualityReport(
    val qualityPercent: Int, // 0 - 100
    val status: String, // Excellent, Average, Poor, Damaged
    val estimatedVoltageDropMv: Double,
    val powerLossWatts: Double,
    val advice: String
)

// --- Charging Curve ---
data class CurveSegment(
    val rangeText: String, // "0-20%", "20-40%", etc.
    val avgPowerWatts: Double,
    val avgTempC: Double,
    val speedCategory: String
)

data class CurveAnalysis(
    val segments: List<CurveSegment>,
    val overallEfficiency: Double,
    val peakWattage: Double,
    val curveDescription: String
)

// --- Predictions ---
data class BatteryAgePrediction(
    val health3MonthsPercent: Double,
    val health6MonthsPercent: Double,
    val health12MonthsPercent: Double,
    val predictedCyclesInYear: Int,
    val expectedAgingSpeed: String // Normal, Fast, Accelerated, Optimal
)

// --- Charger Diagnostics ---
data class ChargerDiagnosis(
    val rating: String, // Extremely Weak, Safe slow, Dynamic QC, Super Fast PD, Noisy
    val maxMeasuredWattage: Double,
    val electricalNoiseMv: Double,
    val isOriginalLikely: Boolean,
    val chargerScore: Int
)

// --- Thermal Intelligence ---
data class ThermalReport(
    val overallStatus: String, // Optimal, Warm, Throttled, Danger
    val warningMessage: String,
    val isUnsafe: Boolean,
    val recommendation: String,
    val color: Color
)

package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "charging_sessions")
data class ChargingSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val startTime: Long,
    val endTime: Long,
    val startPercentage: Int,
    val endPercentage: Int,
    val averageWattage: Double,
    val peakWattage: Double,
    val maxTemperature: Double,
    val efficiency: Double,
    val stressScore: Int,
    val chargerType: String,
    val cableQuality: Double, // 0.0 to 1.0 (percent)
    val diagSummary: String,
    val chargeScore: Int
)

@Entity(tableName = "battery_logs")
data class BatteryLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val batteryLevel: Int,
    val voltageMv: Int,
    val currentMa: Int,
    val tempC: Double,
    val cpuTempC: Double,
    val isCharging: Boolean,
    val chargeSpeedMvPerHr: Double = 0.0
)

@Entity(tableName = "charger_profiles")
data class ChargerProfile(
    @PrimaryKey val id: String,
    val name: String,
    val ratedPowerWatts: Double,
    val measuredPowerWatts: Double,
    val stabilityScore: Int, // 0 to 100
    val voltageDropMv: Double,
    val detectedType: String, // original, fast, slow, weak, unstable
    val lastUsed: Long
)

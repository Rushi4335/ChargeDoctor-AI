package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.BatteryLog
import com.example.data.local.entities.ChargerProfile
import com.example.data.local.entities.ChargingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargingDao {

    // --- Charging Sessions ---
    @Query("SELECT * FROM charging_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ChargingSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChargingSession)

    @Delete
    suspend fun deleteSession(session: ChargingSession)

    @Query("DELETE FROM charging_sessions")
    suspend fun clearAllSessions()

    // --- Battery Logs ---
    @Query("SELECT * FROM battery_logs ORDER BY timestamp ASC")
    fun getAllLogs(): Flow<List<BatteryLog>>

    @Query("SELECT * FROM battery_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getLogsSince(sinceTimestamp: Long): Flow<List<BatteryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BatteryLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<BatteryLog>)

    @Query("DELETE FROM battery_logs")
    suspend fun clearAllLogs()

    // --- Charger Profiles ---
    @Query("SELECT * FROM charger_profiles ORDER BY lastUsed DESC")
    fun getAllChargerProfiles(): Flow<List<ChargerProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChargerProfile(profile: ChargerProfile)

    @Query("DELETE FROM charger_profiles")
    suspend fun clearAllChargerProfiles()
}

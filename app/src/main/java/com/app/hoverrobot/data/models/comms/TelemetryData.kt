package com.app.hoverrobot.data.models.comms

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TelemetryData(
    val type: String,
    val cpu: CpuData,
    val fans: Map<String, Int>,
    val memory: MemoryData,
    val disk: DiskData,
    val uptime: Long,
)

@Serializable
data class CpuData(
    val usage: Float,
    @SerialName("per_core")
    val perCore: List<Float>,
    val temp: Float
)

@Serializable
data class MemoryData(
    val usage: Float,
    @SerialName("total_mb")
    val totalMb: Int,
    @SerialName("available_mb")
    val availableMb: Int
)

@Serializable
data class DiskData(
    val usage: Float,
    @SerialName("total_gb")
    val totalGb: Float,
    @SerialName("free_gb")
    val freeGb: Float
)
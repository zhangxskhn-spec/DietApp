package com.dietapp.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.data.HealthMqttConnectionState
import com.dietapp.data.HealthMqttRepository
import com.dietapp.data.HealthStreamPacket
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class HeartRateSample(
    val timestamp: Long,
    val bpm: Int
)

data class HealthMonitorUiState(
    val connectionLabel: String = "连接中",
    val streamSource: String = "本地演示流",
    val deviceName: String = "Vital Sense S1",
    val heartRate: Int = 82,
    val heartStatus: String = "稳定监测",
    val heartTrendLabel: String = "最近 30 秒",
    val bloodPressureSystolic: Int = 118,
    val bloodPressureDiastolic: Int = 76,
    val bloodGlucose: Float = 5.8f,
    val steps: Int = 4860,
    val calories: Int = 286,
    val cadence: Int = 96,
    val activityLabel: String = "轻度活动",
    val stressScore: Int = 42,
    val lastUpdated: String = "--:--:--",
    val heartRateSamples: List<HeartRateSample> = emptyList()
)

class HealthMonitorViewModel : ViewModel() {

    private val random = Random(System.currentTimeMillis())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.CHINA)

    private var currentHeartRate = 82
    private var currentSystolic = 118
    private var currentDiastolic = 76
    private var currentGlucose = 5.8f
    private var currentSteps = 4860
    private var currentCalories = 286
    private var currentCadence = 96
    private var currentStress = 42
    private var hasReceivedMqttData = false

    private val _uiState = MutableStateFlow(
        HealthMonitorUiState(
            heartRateSamples = seedSamples()
        )
    )
    val uiState: StateFlow<HealthMonitorUiState> = _uiState.asStateFlow()

    init {
        HealthMqttRepository.ensureStarted()

        viewModelScope.launch {
            HealthMqttRepository.connectionState.collectLatest { state ->
                applyConnectionState(state)
            }
        }

        viewModelScope.launch {
            HealthMqttRepository.streamPackets.collectLatest { packet ->
                hasReceivedMqttData = true
                applyMqttPacket(packet)
            }
        }

        viewModelScope.launch {
            delay(700)
            while (isActive) {
                if (!hasReceivedMqttData) {
                    emitDemoTick()
                }
                delay(900)
            }
        }
    }

    private fun seedSamples(): List<HeartRateSample> {
        val now = System.currentTimeMillis()
        return List(36) { index ->
            val value = 78 + random.nextInt(-6, 7)
            HeartRateSample(
                timestamp = now - (35 - index) * 900L,
                bpm = value
            )
        }
    }

    private fun applyConnectionState(state: HealthMqttConnectionState) {
        val source = when {
            hasReceivedMqttData && state.connected -> "MQTT 实时流"
            state.connected -> "MQTT 已连接 · 等待数据"
            else -> "本地演示流"
        }
        _uiState.value = _uiState.value.copy(
            connectionLabel = state.statusLabel,
            streamSource = source
        )
    }

    private fun applyMqttPacket(packet: HealthStreamPacket) {
        currentHeartRate = packet.heartRate.value.takeIf { it > 0 } ?: currentHeartRate
        currentSystolic = packet.bloodPressure.systolic.takeIf { it > 0 } ?: currentSystolic
        currentDiastolic = packet.bloodPressure.diastolic.takeIf { it > 0 } ?: currentDiastolic
        currentGlucose = packet.bloodGlucose.value.takeIf { it > 0f } ?: currentGlucose
        currentSteps = packet.exercise.steps.takeIf { it > 0 } ?: currentSteps
        currentCalories = packet.exercise.calories.takeIf { it > 0 } ?: currentCalories
        currentCadence = packet.exercise.cadence.takeIf { it > 0 } ?: currentCadence
        currentStress = deriveStress(currentHeartRate)

        val samples = (_uiState.value.heartRateSamples + HeartRateSample(
            timestamp = packet.ts,
            bpm = currentHeartRate
        )).takeLast(40)

        _uiState.value = _uiState.value.copy(
            connectionLabel = "实时流在线",
            streamSource = "MQTT 实时流",
            heartRate = currentHeartRate,
            heartStatus = heartStatus(currentHeartRate),
            bloodPressureSystolic = currentSystolic,
            bloodPressureDiastolic = currentDiastolic,
            bloodGlucose = currentGlucose,
            steps = currentSteps,
            calories = currentCalories,
            cadence = currentCadence,
            activityLabel = activityLabel(currentCadence),
            stressScore = currentStress,
            lastUpdated = packet.time.ifBlank { timeFormat.format(Date()) },
            heartRateSamples = samples
        )
    }

    private var demoTick = 0

    private fun emitDemoTick() {
        demoTick++
        val sin = kotlin.math.sin(demoTick * 0.15)

        currentHeartRate = clamp((78 + sin * 15 + random.nextDouble(-3.0, 3.0)).toInt(), 58, 130)
        currentSystolic = clamp((118 + sin * 10 + random.nextDouble(-2.0, 2.0)).toInt(), 95, 150)
        currentDiastolic = clamp((76 + sin * 6 + random.nextDouble(-2.0, 2.0)).toInt(), 55, 95)
        currentGlucose = clampF((5.6 + kotlin.math.sin(demoTick * 0.08) * 1.2 + random.nextDouble(-0.2, 0.2)).toFloat(), 3.8f, 9.5f)
        currentSteps += random.nextInt(20, 65)
        currentCalories += random.nextInt(1, 4)
        currentCadence = clamp((92 + sin * 18 + random.nextDouble(-4.0, 4.0)).toInt(), 60, 135)
        currentStress = clamp((45 + sin * 20 + random.nextDouble(-3.0, 3.0)).toInt(), 20, 85)

        val samples = (_uiState.value.heartRateSamples + HeartRateSample(
            timestamp = System.currentTimeMillis(),
            bpm = currentHeartRate
        )).takeLast(40)

        _uiState.value = _uiState.value.copy(
            connectionLabel = "等待 MQTT 数据",
            streamSource = "本地演示流",
            heartRate = currentHeartRate,
            heartStatus = heartStatus(currentHeartRate),
            bloodPressureSystolic = currentSystolic,
            bloodPressureDiastolic = currentDiastolic,
            bloodGlucose = currentGlucose,
            steps = currentSteps,
            calories = currentCalories,
            cadence = currentCadence,
            activityLabel = activityLabel(currentCadence),
            stressScore = currentStress,
            lastUpdated = timeFormat.format(Date()),
            heartRateSamples = samples
        )
    }

    private fun deriveStress(bpm: Int): Int = min(90, max(24, (bpm - 55) * 2))

    private fun heartStatus(bpm: Int): String = when {
        bpm < 72 -> "静息恢复"
        bpm < 96 -> "稳定监测"
        bpm < 112 -> "轻度活动"
        else -> "高负荷"
    }

    private fun activityLabel(cadence: Int): String = when {
        cadence < 86 -> "放松恢复"
        cadence < 102 -> "轻度活动"
        cadence < 116 -> "节奏训练"
        else -> "高强度输出"
    }

    private fun clamp(value: Int, min: Int, max: Int): Int = max(min, min(value, max))
    private fun clampF(value: Float, min: Float, max: Float): Float = max(min, min(value, max))

    private fun nextInt(current: Int, minValue: Int, maxValue: Int, delta: Int): Int {
        val next = current + random.nextInt(-delta, delta + 1)
        return min(max(next, minValue), maxValue)
    }

    private fun nextFloat(current: Float, minValue: Float, maxValue: Float, delta: Float): Float {
        val next = current + random.nextFloat() * (delta * 2) - delta
        return min(max(next, minValue), maxValue)
    }
}

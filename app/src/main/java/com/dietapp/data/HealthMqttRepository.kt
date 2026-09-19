package com.dietapp.data

import com.dietapp.DietApp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.charset.StandardCharsets
import java.util.UUID

data class HealthStreamPacket(
    val ts: Long = 0L,
    val time: String = "",
    @SerializedName("heart_rate") val heartRate: HeartRatePayload = HeartRatePayload(),
    @SerializedName("blood_pressure") val bloodPressure: BloodPressurePayload = BloodPressurePayload(),
    @SerializedName("blood_glucose") val bloodGlucose: BloodGlucosePayload = BloodGlucosePayload(),
    val exercise: ExercisePayload = ExercisePayload()
)

data class HeartRatePayload(
    val ts: Long = 0L,
    val time: String = "",
    val value: Int = 0,
    val unit: String = "bpm",
    val status: String = ""
)

data class BloodPressurePayload(
    val ts: Long = 0L,
    val time: String = "",
    val systolic: Int = 0,
    val diastolic: Int = 0,
    val unit: String = "mmHg",
    val status: String = ""
)

data class BloodGlucosePayload(
    val ts: Long = 0L,
    val time: String = "",
    val value: Float = 0f,
    val unit: String = "mmol/L",
    val status: String = ""
)

data class ExercisePayload(
    val ts: Long = 0L,
    val time: String = "",
    val steps: Int = 0,
    val calories: Int = 0,
    val cadence: Int = 0,
    val status: String = ""
)

data class HealthMqttConnectionState(
    val connected: Boolean = false,
    val statusLabel: String = "连接中",
    val hostLabel: String = "MQTT 10.0.2.2:1883",
    val errorMessage: String? = null
)

object HealthMqttRepository {

    private const val BROKER_HOST = "10.0.2.2"
    private const val BROKER_PORT = 1883
    private const val TOPIC_ALL = "health/live/all"
    private const val RECONNECT_INTERVAL_MS = 5000L

    private val gson = Gson()
    private val clientId = "dietapp-monitor-${UUID.randomUUID()}"

    private val client: Mqtt3AsyncClient by lazy {
        MqttClient.builder()
            .useMqttVersion3()
            .identifier(clientId)
            .serverHost(BROKER_HOST)
            .serverPort(BROKER_PORT)
            .automaticReconnectWithDefaultConfig()
            .buildAsync()
    }

    private val _connectionState = MutableStateFlow(
        HealthMqttConnectionState(
            connected = false,
            statusLabel = "等待连接",
            hostLabel = "MQTT $BROKER_HOST:$BROKER_PORT"
        )
    )
    val connectionState: StateFlow<HealthMqttConnectionState> = _connectionState.asStateFlow()

    private val _streamPackets = MutableSharedFlow<HealthStreamPacket>(replay = 1, extraBufferCapacity = 4)
    val streamPackets: SharedFlow<HealthStreamPacket> = _streamPackets.asSharedFlow()

    @Volatile
    private var started = false

    @Volatile
    private var isConnected = false

    fun ensureStarted() {
        if (started) return
        synchronized(this) {
            if (started) return
            started = true
            _connectionState.value = _connectionState.value.copy(statusLabel = "连接中")
            connectAndSubscribe()
        }
    }

    private fun connectAndSubscribe() {
        client.connectWith()
            .cleanSession(true)
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    isConnected = false
                    _connectionState.value = _connectionState.value.copy(
                        connected = false,
                        statusLabel = "连接失败，重试中...",
                        errorMessage = throwable.message
                    )
                    scheduleReconnect()
                } else {
                    isConnected = true
                    _connectionState.value = _connectionState.value.copy(
                        connected = true,
                        statusLabel = "MQTT在线",
                        errorMessage = null
                    )
                    subscribeAllTopic()
                }
            }
    }

    private fun scheduleReconnect() {
        Thread {
            try {
                Thread.sleep(RECONNECT_INTERVAL_MS)
                if (!isConnected) {
                    _connectionState.value = _connectionState.value.copy(statusLabel = "重新连接中...")
                    connectAndSubscribe()
                }
            } catch (_: InterruptedException) {}
        }.start()
    }

    private fun subscribeAllTopic() {
        client.subscribeWith()
            .topicFilter(TOPIC_ALL)
            .qos(MqttQos.AT_MOST_ONCE)
            .callback { publish ->
                try {
                    val payload = publish.payload.orElse(null)?.let { byteBuffer ->
                        val bytes = ByteArray(byteBuffer.remaining())
                        byteBuffer.get(bytes)
                        String(bytes, StandardCharsets.UTF_8)
                    } ?: return@callback

                    val packet = gson.fromJson(payload, HealthStreamPacket::class.java)
                    _streamPackets.tryEmit(packet)
                    _connectionState.value = _connectionState.value.copy(
                        connected = true,
                        statusLabel = "实时流在线",
                        errorMessage = null
                    )
                } catch (e: Exception) {
                    _connectionState.value = _connectionState.value.copy(
                        errorMessage = e.message ?: "消息解析失败"
                    )
                }
            }
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    _connectionState.value = _connectionState.value.copy(
                        connected = false,
                        statusLabel = "订阅失败",
                        errorMessage = throwable.message
                    )
                }
            }
    }
}

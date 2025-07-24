package com.example.wearos_project.presentation

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HeartRateViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    val serverUrl = "ws://169.254.152.52:8765" // watch server
    //val serverUrl = "ws://129.6.249.48:8765" // emulator server
    //val serverUrl = "ws://192.168.8.234:8765" // home router server
    val timestampFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var sendJob: Job? = null

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    // Uncomment the next line for real device, comment for simulation
    private val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    // Force value to be null for simulation testing
    //private val heartRateSensor: Sensor? = null

    private val _heartRate = mutableStateOf(0f)
    val heartRate: State<Float> get() = _heartRate

    private var simulationJob: Job? = null

    fun startHeartRateMonitoring() {
        if (heartRateSensor == null) { // Simulate heart rate on emulator, needs above part to be commented out/in
            simulationJob?.cancel()
            simulationJob = viewModelScope.launch {
                while (true) {
                    _heartRate.value = (60..100).random().toFloat()
                    delay(1000)
                }
            }
        } else {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_UI) // Non-simulation
        }
        startDataTransfer()
    }

    fun stopHeartRateMonitoring() {
        simulationJob?.cancel()
        simulationJob = null
        sensorManager.unregisterListener(this)
        stopDataTransfer()
        closeWebSocket()
    }

    fun resetHeartRate() {
        _heartRate.value = 0f
        stopHeartRateMonitoring()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
            _heartRate.value = event.values[0]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        stopHeartRateMonitoring()
        closeWebSocket()
    }

    private val healthDataSender = HealthDataSender(
        serverUrl = serverUrl,
        timestampFormat = timestampFormat
    )

    fun connectWebSocket() {
        healthDataSender.connect(viewModelScope)
    }

    fun closeWebSocket() {
        healthDataSender.disconnect(viewModelScope)
    }

    private fun startDataTransfer() {
        if (sendJob == null) {
            sendJob = viewModelScope.launch {
                while (isActive) {
                    healthDataSender.sendHeartData(
                        scope = this,
                        heartRate = _heartRate.value.toInt()
                    )
                    delay(500L) // sends data every second, mutable
                }
            }
        }
    }

    private fun stopDataTransfer() {
        sendJob?.cancel()
        sendJob = null
    }
}


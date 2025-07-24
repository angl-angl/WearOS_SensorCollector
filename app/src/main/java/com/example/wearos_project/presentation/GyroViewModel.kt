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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GyroViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    val serverUrl = "ws://169.254.152.52:8765" // watch server
    //val serverUrl = "ws://129.6.249.48:8765" // emulator server
    //val serverUrl = "ws://192.168.8.234:8765" // home router server
    val timestampFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var sendJob: Job? = null

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _gyro = mutableStateOf(Triple(0f, 0f, 0f))
    val gyro: State<Triple<Float, Float, Float>> get() = _gyro

    fun startGyroMonitoring() {
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        startDataTransfer()
    }

    fun stopGyroMonitoring() {
        sensorManager.unregisterListener(this)
        stopDataTransfer()
        closeWebSocket()
    }

    fun resetGyro() {
        stopGyroMonitoring()
        _gyro.value = Triple(0f, 0f, 0f)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            _gyro.value = Triple(event.values[0], event.values[1], event.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        stopGyroMonitoring()
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
                    healthDataSender.sendMotionData(
                        scope = this,
                        sensorType = "gyroscope",
                        valueX = gyro.value.first.toDouble(),
                        valueY = gyro.value.second.toDouble(),
                        valueZ = gyro.value.third.toDouble()
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
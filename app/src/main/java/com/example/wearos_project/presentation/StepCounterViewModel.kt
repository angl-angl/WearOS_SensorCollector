package com.example.wearos_project.presentation

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.IntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.Locale

class StepCounterViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    val serverUrl = "ws://169.254.152.52:8765" // watch server
    //val serverUrl = "ws://129.6.249.48:8765" // emulator server
    //val serverUrl = "ws://192.168.8.234:8765" // home router server
    val timestampFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var sendJob: Job? = null

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _steps = mutableIntStateOf(0)
    val steps: IntState get() = _steps

    private var initialSteps: Float = -1f
    private var accumulatedSteps: Int = 0
    private var isCounting = false

    fun startStepCounting() {
        if (!isCounting) {
            isCounting = true
            initialSteps = -1f // Will be set on first sensor event after resume
            stepSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
        startDataTransfer()
    }

    fun stopStepCounting() {
        if (isCounting) {
            isCounting = false
            sensorManager.unregisterListener(this)
            accumulatedSteps = _steps.intValue // Save steps so far
        }
        stopDataTransfer()
        closeWebSocket()
    }

    fun resetSteps() {
        _steps.intValue = 0
        accumulatedSteps = 0
        initialSteps = -1f
        stopStepCounting()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (isCounting) {
            if (initialSteps < 0f) {
                initialSteps = event.values[0]
            }
            _steps.intValue = accumulatedSteps + (event.values[0] - initialSteps).toInt()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
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
                    healthDataSender.sendStepData(
                        scope = this,
                        stepCount = _steps.intValue
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
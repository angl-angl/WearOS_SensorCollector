package com.example.wearos_project.presentation

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AccelerometerViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    val serverUrl = "ws://169.254.152.52:8765" // watch server
    //val serverUrl = "ws://129.6.249.48:8765" // emulator server
    //val serverUrl = "ws://192.168.8.234:8765" // home router server
    val timestampFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _x = MutableStateFlow(0f)
    val x: StateFlow<Float> = _x.asStateFlow()
    private val _y = MutableStateFlow(0f)
    val y: StateFlow<Float> = _y.asStateFlow()
    private val _z = MutableStateFlow(0f)
    val z: StateFlow<Float> = _z.asStateFlow()

    private val healthDataSender = HealthDataSender(
        serverUrl = serverUrl,
        timestampFormat = timestampFormat
    )

    private var sendJob: Job? = null

    fun connectWebSocket() {
        healthDataSender.connect(viewModelScope)
    }

    fun closeWebSocket() {
        healthDataSender.disconnect(viewModelScope)
    }

    fun startAccelerometerMonitoring() {
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        startDataTransfer()
    }

    fun stopAccelerometerMonitoring() {
        sensorManager.unregisterListener(this)
        stopDataTransfer()
        closeWebSocket()
    }

    fun resetAccelerometer() {
        stopAccelerometerMonitoring()
        _x.value = 0f
        _y.value = 0f
        _z.value = 0f
    }

    private fun startDataTransfer() {
        if (sendJob == null) {
            sendJob = viewModelScope.launch {
                while (isActive) {
                    healthDataSender.sendMotionData(
                        scope = this,
                        sensorType = "accelerometer",
                        valueX = _x.value.toDouble(),
                        valueY = _y.value.toDouble(),
                        valueZ = _z.value.toDouble()
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

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            _x.value = event.values[0]
            _y.value = event.values[1]
            _z.value = event.values[2]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        stopAccelerometerMonitoring()
        closeWebSocket()
    }
}

//package com.example.wearos_project.presentation
//
//import android.app.Application
//import android.content.Context
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import java.text.SimpleDateFormat
//import java.util.*
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.launch
//
//class AccelerometerViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
//    private val serverUrl = "ws://129.6.249.48:8765"
//    private val timestampFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
//
//    private val sensorManager =
//        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//
//    private val _x = MutableStateFlow(0f)
//    val x: StateFlow<Float> = _x.asStateFlow()
//    private val _y = MutableStateFlow(0f)
//    val y: StateFlow<Float> = _y.asStateFlow()
//    private val _z = MutableStateFlow(0f)
//    val z: StateFlow<Float> = _z.asStateFlow()
//
//    // HealthDataSender instance
//    private val healthDataSender = HealthDataSender(
//        serverUrl = serverUrl,
//        timestampFormat = timestampFormat
//    )
//
//    fun connectWebSocket() {
//        healthDataSender.connect(viewModelScope)
//    }
//
//    fun closeWebSocket() {
//        healthDataSender.disconnect(viewModelScope)
//    }
//
//    fun startAccelerometerMonitoring() {
//        accelerometerSensor?.let {
//            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
//        }
//    }
//
//    fun stopAccelerometerMonitoring() {
//        sensorManager.unregisterListener(this)
//    }
//
//    fun resetAccelerometer() {
//        stopAccelerometerMonitoring()
//        _x.value = 0f
//        _y.value = 0f
//        _z.value = 0f
//    }
//
//    override fun onSensorChanged(event: SensorEvent) {
//        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//            _x.value = event.values[0]
//            _y.value = event.values[1]
//            _z.value = event.values[2]
//            // Send data as JSON via WebSocket
//
//            healthDataSender.sendMotionData(
//                scope = viewModelScope,
//                dataType = "accelerometer",
//                valueX = event.values[0].toDouble(),
//                valueY = event.values[1].toDouble(),
//                valueZ = event.values[2].toDouble()
//            )
//        }
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//
//    override fun onCleared() {
//        super.onCleared()
//        stopAccelerometerMonitoring()
//        closeWebSocket()
//    }
//
//}



//package com.example.wearos_project.presentation
//
//import android.app.Application
//import android.content.Context
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import androidx.lifecycle.AndroidViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//
//class AccelerometerViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
//    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//
//    private val _x = MutableStateFlow(0f)
//    val x: StateFlow<Float> = _x.asStateFlow()
//
//    private val _y = MutableStateFlow(0f)
//    val y: StateFlow<Float> = _y.asStateFlow()
//
//    private val _z = MutableStateFlow(0f)
//    val z: StateFlow<Float> = _z.asStateFlow()
//
//    fun startAccelerometerMonitoring() {
//        accelerometerSensor?.let {
//            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
//        }
//    }
//
//    fun stopAccelerometerMonitoring() {
//        sensorManager.unregisterListener(this)
//    }
//
//    fun resetAccelerometer() {
//        stopAccelerometerMonitoring()
//        _x.value = 0f
//        _y.value = 0f
//        _z.value = 0f
//    }
//
//    override fun onSensorChanged(event: SensorEvent) {
//        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//            _x.value = event.values[0]
//            _y.value = event.values[1]
//            _z.value = event.values[2]
//        }
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//
//    override fun onCleared() {
//        super.onCleared()
//        stopAccelerometerMonitoring()
//    }
//}
//

//package com.example.wearos_project.presentation
//
//import android.app.Application
//import android.content.Context
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import androidx.lifecycle.AndroidViewModel
//import androidx.compose.runtime.State
//import androidx.compose.runtime.mutableStateOf
//
//
//// TODO: Defunct for the Galaxy Watch 6, as it does not support skin temperature sensors.
//class SkinTempViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
//    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//    private val skinTempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) // ambient temperature sensor is used as a placeholder
//    //private val skinTempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_SKIN_TEMPERATURE)
//
//    private val _skinTemp = mutableStateOf(0f)
//    val skinTemp: State<Float> get() = _skinTemp
//
//    fun startSkinTempMonitoring() {
//        skinTempSensor?.let {
//            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
//        }
//    }
//
//    fun stopSkinTempMonitoring() {
//        sensorManager.unregisterListener(this)
//    }
//
//    override fun onSensorChanged(event: SensorEvent) {
//        if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
//            _skinTemp.value = event.values[0]
//        }
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//
//    override fun onCleared() {
//        super.onCleared()
//        stopSkinTempMonitoring()
//    }
//}
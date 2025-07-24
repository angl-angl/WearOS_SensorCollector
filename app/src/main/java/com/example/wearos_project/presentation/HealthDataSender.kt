package com.example.wearos_project.presentation

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class HealthDataSender(
    private val serverUrl: String,
    private val timestampFormat: SimpleDateFormat
) {
    private var webSocketClient: WebSocketClient? = null

    fun connect(scope: CoroutineScope, onConnected: (() -> Unit)? = null) {
        scope.launch(Dispatchers.IO) {
            try {
                val uri = URI(serverUrl)
                webSocketClient = object : WebSocketClient(uri) {
                    override fun onOpen(handshakedata: ServerHandshake?) {
                        scope.launch(Dispatchers.Main) {
                            Log.d("HealthDataSender", "WebSocket Connected!")
                            onConnected?.invoke()
                        }
                    }
                    override fun onMessage(message: String?) {}
                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        scope.launch(Dispatchers.Main) {
                            Log.d("HealthDataSender", "WebSocket Closed!")
                        }
                    }
                    // Error handling, close connection safely
                    override fun onError(ex: Exception?) {
                        scope.launch(Dispatchers.Main) {
                            Log.e("HealthDataSender", "WebSocket Error: ${ex?.message}", ex)
                            try {
                                webSocketClient?.close()
                            } catch (e: Exception) {
                                Log.e("HealthDataSender", "Disconnect Error: ${e.message}", e)
                            }
                        }
                    }
                }
                webSocketClient?.connectBlocking()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("HealthDataSender", "Connection Error: ${e.message}", e)
                }
            }
        }
    }

    fun sendMotionData(
        scope: CoroutineScope,
        sensorType: String,
        valueX: Double,
        valueY: Double,
        valueZ: Double
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val jsonPayload = JSONObject().apply {
                    put("sensor", sensorType)
                    put("x", valueX)
                    put("y", valueY)
                    put("z", valueZ)
                    put("time", timestampFormat.format(Date()))
                }
                webSocketClient?.send(jsonPayload.toString())
                withContext(Dispatchers.Main) {
                    Log.d("HealthDataSender", "Motion data sent")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("HealthDataSender", "Send Error: ${e.message}", e)
                }
            }
        }
    }

    fun sendStepData(
        scope: CoroutineScope,
        stepCount: Int
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val jsonPayload = JSONObject().apply {
                    put("sensor", "step_counter")
                    put("steps", stepCount)
                    put("time", timestampFormat.format(Date()))
                }
                webSocketClient?.send(jsonPayload.toString())
                withContext(Dispatchers.Main) {
                    Log.d("HealthDataSender", "Step data sent")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("HealthDataSender", "Send Step Error: ${e.message}", e)
                }
            }
        }
    }

    fun sendHeartData(
        scope: CoroutineScope,
        heartRate: Int
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val jsonPayload = JSONObject().apply {
                    put("sensor", "heart_monitor")
                    put("bpm", heartRate)
                    put("time", timestampFormat.format(Date()))
                }
                webSocketClient?.send(jsonPayload.toString())
                withContext(Dispatchers.Main) {
                    Log.d("HealthDataSender", "Heart rate data sent")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("HealthDataSender", "Send Heart Rate Error: ${e.message}", e)
                }
            }
        }
    }

    fun disconnect(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                webSocketClient?.close()
                withContext(Dispatchers.Main) {
                    Log.d("HealthDataSender", "Disconnected")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("HealthDataSender", "Disconnect Error: ${e.message}", e)
                }
            }
        }
    }
}
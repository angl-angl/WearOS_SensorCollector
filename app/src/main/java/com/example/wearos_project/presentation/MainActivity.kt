package com.example.wearos_project.presentation

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                PERMISSION_REQUEST_CODE
            )
        }

        if (checkSelfPermission(android.Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.BODY_SENSORS), 0)
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.BODY_SENSORS),
                PERMISSION_REQUEST_CODE
            )
        }

        setContent {
            val stopWatchViewModel = viewModel<StopWatchViewModel>()
            val stepCounterViewModel = viewModel<StepCounterViewModel>()
            val heartRateViewModel = viewModel<HeartRateViewModel>()
            val accelerometerViewModel = viewModel<AccelerometerViewModel>()
            val gyroViewModel = viewModel<GyroViewModel>()

            val timerState by stopWatchViewModel.timerState.collectAsStateWithLifecycle()
            val stopWatchText by stopWatchViewModel.stopWatchText.collectAsStateWithLifecycle()

            Scaffold(
                timeText = {
                    TimeText(
                        timeTextStyle = TimeTextDefaults.timeTextStyle(
                            fontSize = 10.sp
                        )
                    )
                },
                vignette = {
                    Vignette(vignettePosition = VignettePosition.TopAndBottom)
                }
            ) {}

            StopWatch(
                state = timerState,
                text = stopWatchText,
                onToggleRunning = {
                    stopWatchViewModel.toggleIsRunning()
                    if (timerState == TimerState.RUNNING) {
                        // Pause
                        stepCounterViewModel.stopStepCounting()
                        gyroViewModel.stopGyroMonitoring()
                        heartRateViewModel.stopHeartRateMonitoring()
                        accelerometerViewModel.stopAccelerometerMonitoring()
                    } else {
                        // Start/Resume
                        stepCounterViewModel.startStepCounting()
                        gyroViewModel.startGyroMonitoring()
                        heartRateViewModel.startHeartRateMonitoring()
                        accelerometerViewModel.startAccelerometerMonitoring()

                        accelerometerViewModel.connectWebSocket()
                        gyroViewModel.connectWebSocket()
                        stepCounterViewModel.connectWebSocket()
                        heartRateViewModel.connectWebSocket()
                    }
                },
                onReset = {
                    stopWatchViewModel.resetTimer()
                    stepCounterViewModel.resetSteps()
                    stepCounterViewModel.stopStepCounting()
                    heartRateViewModel.stopHeartRateMonitoring()
                    heartRateViewModel.resetHeartRate()
                    accelerometerViewModel.resetAccelerometer()
                    accelerometerViewModel.stopAccelerometerMonitoring()
                    gyroViewModel.resetGyro()
                    gyroViewModel.stopGyroMonitoring()
                },
                stepCounterViewModel = stepCounterViewModel,
                heartRateViewModel = heartRateViewModel,
                accelerometerViewModel = accelerometerViewModel,
                gyroViewModel = gyroViewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun StopWatch(
    state: TimerState,
    text: String,
    onToggleRunning: () -> Unit,
    onReset: () -> Unit,
    stepCounterViewModel: StepCounterViewModel,
    heartRateViewModel: HeartRateViewModel,
    gyroViewModel: GyroViewModel,
    //skinTempViewModel: SkinTempViewModel,
    accelerometerViewModel: AccelerometerViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(45.dp))
        Text(
            text = "Data collection time:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = Color(0xFFAECBFA)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = onToggleRunning) {
                Icon(
                    imageVector = if (state == TimerState.RUNNING) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onReset,
                enabled = state != TimerState.RESET,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null
                )
            }
        }
        // ADD SCREENS TO WATCH
        Spacer(modifier = Modifier.height(15.dp))
        StepCounterScreen(viewModel = stepCounterViewModel)
        HeartRateScreen(viewModel = heartRateViewModel)
        GyroScreen(viewModel = gyroViewModel)
        AccelerometerScreen(viewModel = accelerometerViewModel)
        //SkinTempScreen(viewModel = skinTempViewModel)
    }
}

@Composable
fun StepCounterScreen(viewModel: StepCounterViewModel) {
    val steps by viewModel.steps

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(18.dp))
        Text("Steps", style = MaterialTheme.typography.title3)
        Text("$steps", style = MaterialTheme.typography.display1)
    }
}

@Composable
fun HeartRateScreen(viewModel: HeartRateViewModel) {
    val heartRate by viewModel.heartRate
    Spacer(Modifier.height(18.dp))
    Text("Heart Rate", style = MaterialTheme.typography.title3)
    Text(
        if (heartRate > 0) "${heartRate.toInt()} bpm" else "--",
        style = MaterialTheme.typography.display1
    )
}

////@Composable
////fun SkinTempScreen(viewModel: SkinTempViewModel) {
////    val skinTemp by viewModel.skinTemp
////    Spacer(Modifier.height(18.dp))
////    Text("Skin Temperature", style = MaterialTheme.typography.title3)
////    Text(
////        if (skinTemp > 0) "${skinTemp}°C" else "--",
////        style = MaterialTheme.typography.display1
////    )
//}

@Composable
fun AccelerometerScreen(viewModel: AccelerometerViewModel) {
    val x by viewModel.x.collectAsStateWithLifecycle()
    val y by viewModel.y.collectAsStateWithLifecycle()
    val z by viewModel.z.collectAsStateWithLifecycle()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(18.dp))
        Text("Accelerometer", style = MaterialTheme.typography.title3)
        Spacer(Modifier.height(5.dp))
        Text("X: $x", fontSize = 18.sp, textAlign = TextAlign.Center)
        Text("Y: $y", fontSize = 18.sp, textAlign = TextAlign.Center)
        Text("Z: $z", fontSize = 18.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun GyroScreen(viewModel: GyroViewModel) {
    val gyro by viewModel.gyro

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(18.dp))
        Text("Gyroscope", style = MaterialTheme.typography.title3)
        Spacer(Modifier.height(5.dp))
        Text("X: ${gyro.first}", fontSize = 18.sp, textAlign = TextAlign.Center)
        Text("Y: ${gyro.second}", fontSize = 18.sp, textAlign = TextAlign.Center)
        Text("Z: ${gyro.third}", fontSize = 18.sp, textAlign = TextAlign.Center)
    }
}
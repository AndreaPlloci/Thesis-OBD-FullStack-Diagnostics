package com.andreaplloci.thesisobdapp.ui

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.andreaplloci.thesisobdapp.ObdData
import com.andreaplloci.thesisobdapp.ObdManager
import com.andreaplloci.thesisobdapp.ReportRequest
import com.andreaplloci.thesisobdapp.data.VehicleProfileStore
import com.andreaplloci.thesisobdapp.ui.screens.*

@Composable
fun AppNavigation(obdManager: ObdManager) {
    val context = LocalContext.current
    val profileStore = remember { VehicleProfileStore(context) }

    var currentScreen by remember { mutableStateOf("connection") }
    var reportData by remember { mutableStateOf<ReportRequest?>(null) }
    var liveObdData by remember { mutableStateOf(ObdData()) }
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }

    when (currentScreen) {
        "connection" -> ConnectionScreen(obdManager) { device ->
            selectedDevice = device
            currentScreen = "selection"
        }
        "selection" -> SelectionScreen(profileStore) { partialReport ->
            reportData = partialReport
            currentScreen = "reading"
        }
        "reading" -> ReadingScreen(obdManager, selectedDevice, reportData, onBack = { currentScreen = "selection" }) { scannedData ->
            liveObdData = scannedData
            currentScreen = "diagnostic"
        }
        "diagnostic" -> DiagnosticScreen(
            initialReport = reportData?.copy(datiDallaCentralina = liveObdData),
            profileStore = profileStore,
            onSendSuccess = { currentScreen = "success" },
            onBack = { currentScreen = "selection" }
        )
        "success" -> SuccessScreen(
            marca = reportData?.marca ?: "Alfa Romeo",
            onDone = { currentScreen = "connection" }
        )
    }
}

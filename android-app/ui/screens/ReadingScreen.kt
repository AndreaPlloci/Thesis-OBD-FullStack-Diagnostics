package com.andreaplloci.thesisobdapp.ui.screens

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andreaplloci.thesisobdapp.ObdData
import com.andreaplloci.thesisobdapp.ObdManager
import com.andreaplloci.thesisobdapp.ReportRequest
import com.andreaplloci.thesisobdapp.ui.AutomotiveRed
import kotlinx.coroutines.delay

@Composable
fun ReadingScreen(
    obdManager: ObdManager,
    device: BluetoothDevice?,
    reportData: ReportRequest?,
    onBack: () -> Unit,
    onScanComplete: (ObdData) -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    var status by remember { mutableStateOf("Preparazione Protocollo...") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (device == null) {
            errorMessage = "Nessun dispositivo selezionato. Torna alla connessione."
            return@LaunchedEffect
        }

        val targetProtocol = if (reportData?.modello == "159") "5" else "0"
        val connected = obdManager.connect(device, targetProtocol)
        if (!connected) {
            errorMessage = "Connessione fallita. Verifica il dispositivo OBD e riprova."
            return@LaunchedEffect
        }
        delay(2000)

        suspend fun pollUntilData(pid: String, label: String, startP: Float, endP: Float, maxRetries: Int = 15): String {
            status = "Lettura $label..."
            progress = startP
            var value = "N/A"
            var attempt = 0
            while (value == "N/A" && attempt < maxRetries) {
                value = obdManager.getParam(pid)
                if (value != "N/A") break
                status = "Lettura $label (Tentativo ${attempt + 1}/$maxRetries)"
                attempt++
                delay(1200)
            }
            progress = endP
            return value
        }

        val volt = obdManager.readVoltage()
        val temp = pollUntilData("0105", "Temperatura ECT", 0.15f, 0.30f)
        val load = pollUntilData("0104", "Carico Motore", 0.30f, 0.48f)
        val maf  = pollUntilData("0110", "Massa Aria",     0.48f, 0.64f)
        val iat  = pollUntilData("010F", "Temp Aspirazione", 0.64f, 0.80f, maxRetries = 8)
        val rail = pollUntilData("0123", "Pressione Rail", 0.80f, 0.92f)

        status = "Scansione DTC..."
        progress = 0.95f
        val dtcs = obdManager.readDtc()

        onScanComplete(ObdData(volt, dtcs, temp, maf, load, iat, rail))
    }

    val error = errorMessage
    if (error != null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(error, color = AutomotiveRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 32.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBack) { Text("Torna Indietro") }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(progress = progress, modifier = Modifier.size(100.dp), color = AutomotiveRed, strokeWidth = 8.dp)
        Spacer(modifier = Modifier.height(20.dp))
        Text(status, fontWeight = FontWeight.Bold)
    }
}

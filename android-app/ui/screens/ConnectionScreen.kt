package com.andreaplloci.thesisobdapp.ui.screens

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.andreaplloci.thesisobdapp.ObdManager
import com.andreaplloci.thesisobdapp.ui.AutomotiveRed
import kotlinx.coroutines.launch

@Composable
fun ConnectionScreen(obdManager: ObdManager, onConnected: (BluetoothDevice) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isConnecting by remember { mutableStateOf(false) }
    var showDeviceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 70.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Jarvis Hub", style = MaterialTheme.typography.headlineMedium, color = AutomotiveRed, fontWeight = FontWeight.Bold)
        Text("Hardware Check: connetti l'interfaccia OBD II per iniziare.", color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { showDeviceDialog = true },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            enabled = !isConnecting
        ) {
            if (isConnecting) CircularProgressIndicator(color = Color.White)
            else Text("CERCA E CONNETTI OBD", fontWeight = FontWeight.Bold)
        }

        if (showDeviceDialog) {
            Dialog(onDismissRequest = { showDeviceDialog = false }) {
                Card {
                    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                    val devices = bluetoothManager?.adapter?.bondedDevices?.toList() ?: emptyList()
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Seleziona Dispositivo", color = AutomotiveRed, style = MaterialTheme.typography.titleLarge)
                        if (devices.isEmpty()) {
                            Text(
                                "Nessun dispositivo abbinato. Abbina il tuo OBD nelle impostazioni Bluetooth del telefono.",
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = Color.Gray
                            )
                        } else {
                            LazyColumn {
                                items(devices) { dev ->
                                    Text(
                                        dev.name ?: dev.address,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                showDeviceDialog = false
                                                isConnecting = true
                                                scope.launch {
                                                    if (obdManager.connect(dev, "0")) onConnected(dev)
                                                    else {
                                                        isConnecting = false
                                                        Toast.makeText(context, "OBD non risponde. Verifica che il dispositivo sia acceso e in range.", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                            .padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

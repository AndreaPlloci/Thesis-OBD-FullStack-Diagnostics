package com.example.automotive.obdapp

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- BRANDING & DESIGN SYSTEM ---
val AutomotiveRed = Color(0xFFB71C1C)
val SuccessGreen = Color(0xFF2E7D32)

class MainActivity : ComponentActivity() {
    private val obdManager = ObdManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = AutomotiveRed)) {
                PermissionHandler {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                        AppNavigation(obdManager)
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(obdManager: ObdManager) {
    var currentScreen by remember { mutableStateOf("connection") }
    var reportData by remember { mutableStateOf<ReportRequest?>(null) }
    var liveObdData by remember { mutableStateOf(ObdData()) }
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }

    when (currentScreen) {
        "connection" -> ConnectionScreen(obdManager) { device ->
            selectedDevice = device
            currentScreen = "selection"
        }
        "selection" -> SelectionScreen { partialReport ->
            reportData = partialReport
            currentScreen = "reading"
        }
        "reading" -> ReadingScreen(obdManager, selectedDevice, reportData) { scannedData ->
            liveObdData = scannedData
            currentScreen = "diagnostic"
        }
        "diagnostic" -> DiagnosticScreen(
            initialReport = reportData?.copy(datiDallaCentralina = liveObdData),
            onSendSuccess = { currentScreen = "success" },
            onBack = { currentScreen = "selection" }
        )
        "success" -> SuccessScreen(
            marca = reportData?.marca ?: "Alfa Romeo",
            onDone = { currentScreen = "connection" }
        )
    }
}

@Composable
fun ConnectionScreen(obdManager: ObdManager, onConnected: (BluetoothDevice) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isConnecting by remember { mutableStateOf(false) }
    var showDeviceDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(top = 70.dp, start = 20.dp, end = 20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Jarvis Hub", style = MaterialTheme.typography.headlineMedium, color = AutomotiveRed, fontWeight = FontWeight.Bold)
        Text("Hardware Check: Connect the OBD-II interface to begin.", color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = { showDeviceDialog = true }, modifier = Modifier.fillMaxWidth().height(60.dp), enabled = !isConnecting) {
            if (isConnecting) CircularProgressIndicator(color = Color.White)
            else Text("SCAN & CONNECT OBD", fontWeight = FontWeight.Bold)
        }

        if (showDeviceDialog) {
            Dialog(onDismissRequest = { showDeviceDialog = false }) {
                Card {
                    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                    val devices = bluetoothManager.adapter.bondedDevices
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select Device", color = AutomotiveRed, style = MaterialTheme.typography.titleLarge)
                        LazyColumn {
                            items(devices.toList()) { dev ->
                                Text(dev.name ?: dev.address, modifier = Modifier.fillMaxWidth().clickable {
                                    showDeviceDialog = false; isConnecting = true
                                    scope.launch {
                                        if (obdManager.connect(dev, "0")) onConnected(dev)
                                        else { isConnecting = false; Toast.makeText(context, "OBD not responding", Toast.LENGTH_SHORT).show() }
                                    }
                                }.padding(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(onFormComplete: (ReportRequest) -> Unit) {
    val lingue = listOf("Italiano", "English", "Deutsch")
    val marche = listOf("Audi", "Alfa Romeo")
    val modelliPerMarca = mapOf("Audi" to listOf("A3"), "Alfa Romeo" to listOf("159", "Giulia"))

    // VEHICLE DATA CONFIGURATION
    val databaseAuto = mapOf(
        "A3" to mapOf(
            "1.6 TDI (105/110cv)" to listOf("Manuale (5/6m)", "S-tronic (7m)"),
            "2.0 TDI (140/150/170/184cv)" to listOf("Manuale (6m)", "S-tronic (6m)"),
            "1.4 TFSI (122/125/150cv)" to listOf("Manuale (6m)", "S-tronic (7m)"),
            "1.8 TFSI (160/180cv)" to listOf("Manuale (6m)", "S-tronic (7m)"),
            "2.0 TFSI (S3 - 200/220/300cv)" to listOf("Manuale", "S-tronic"),
            "RS3 2.5 TFSI" to listOf("S-tronic (7m)")
        ),
        "159" to mapOf(
            "1.9 JTDM 8v (120cv)" to listOf("Manuale (6m)"),
            "1.9 JTDM 16v (150cv)" to listOf("Manuale (6m)", "Q-Tronic (6m)"),
            "2.0 JTDM (170cv)" to listOf("Manuale (6m)"),
            "2.4 JTDM (200/210cv)" to listOf("Manuale (6m)", "Q-Tronic (6m)"),
            "1.750 TBi (200cv)" to listOf("Manuale (6m)"),
            "1.9 JTS (160cv)" to listOf("Manuale (6m)"),
            "2.2 JTS (185cv)" to listOf("Manuale (6m)", "Selespeed"),
            "3.2 V6 JTS (260cv)" to listOf("Manuale (6m)", "Q-Tronic (6m)")
        ),
        "Giulia" to mapOf(
            "2.0 Turbo (200cv)" to listOf("ZF 8HP (8m)"),
            "2.0 Turbo (280cv)" to listOf("ZF 8HP (8m)"),
            "2.2 JTDM (150/160cv)" to listOf("Manuale (6m)", "ZF 8HP (8m)"),
            "2.2 JTDM (180/190cv)" to listOf("Manuale (6m)", "ZF 8HP (8m)"),
            "2.2 JTDM (210cv)" to listOf("ZF 8HP (8m)"),
            "2.9 V6 Quadrifoglio (510cv)" to listOf("Manuale (6m)", "ZF 8HP (8m)")
        )
    )

    var lingua by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modello by remember { mutableStateOf("") }
    var anno by remember { mutableStateOf("") }
    var motore by remember { mutableStateOf("") }
    var cambio by remember { mutableStateOf("") }
    var kmAttuali by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(top = 70.dp, start = 20.dp, end = 20.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Vehicle Details", style = MaterialTheme.typography.headlineMedium, color = AutomotiveRed, fontWeight = FontWeight.Bold)
        MenuTendina("Report Language", lingua, lingue) { lingua = it }
        MenuTendina("Make", marca, marche) { if (marca != it) { marca = it; modello = ""; anno = ""; motore = ""; cambio = "" } }
        if (marca.isNotEmpty()) MenuTendina("Model", modello, modelliPerMarca[marca] ?: emptyList()) { if (modello != it) { modello = it; anno = ""; motore = ""; cambio = "" } }
        if (modello.isNotEmpty()) OutlinedTextField(value = anno, onValueChange = { if (it.length <= 4) anno = it }, label = { Text("Year") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        if (anno.length == 4) {
            val motori = databaseAuto[modello]?.keys?.toList() ?: listOf("Standard")
            MenuTendina("Engine Type", motore, motori) { motore = it; cambio = "" }
        }
        if (motore.isNotEmpty()) {
            val cambi = databaseAuto[modello]?.get(motore) ?: listOf("Manuale")
            MenuTendina("Transmission Type", cambio, cambi) { cambio = it }
        }
        if (cambio.isNotEmpty()) OutlinedTextField(value = kmAttuali, onValueChange = { kmAttuali = it }, label = { Text("Current Mileage (KM)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { onFormComplete(ReportRequest("", lingua, marca, modello, anno, motore, cambio, kmAttuali, emptyList(), ObdData())) },
            enabled = kmAttuali.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text("START ECU SCAN", fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun ReadingScreen(obdManager: ObdManager, device: BluetoothDevice?, reportData: ReportRequest?, onScanComplete: (ObdData) -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    var status by remember { mutableStateOf("Preparing Protocol...") }

    LaunchedEffect(Unit) {
        // Specific protocol selection: Alfa 159 often requires K-Line (ISO 14230-4) or specific CAN settings
        val targetProtocol = if (reportData?.modello == "159") "5" else "0"
        obdManager.connect(device!!, targetProtocol)
        delay(2000)

        // POLLING LOGIC (Ensuring data retrieval for mission-critical PIDs)
        suspend fun pollUntilData(pid: String, label: String, startP: Float, endP: Float): String {
            status = "Reading $label..."; progress = startP
            var value = "N/A"
            var attempt = 1
            while (value == "N/A") {
                value = obdManager.getParam(pid)
                if (value != "N/A") break
                status = "Reading $label (Retry... $attempt)"
                attempt++
                delay(1200) // Critical delay for stable communication over older K-Line buses
            }
            progress = endP
            return value
        }

        val volt = obdManager.readVoltage()
        val temp = pollUntilData("0105", "ECT (Coolant Temp)", 0.15f, 0.35f)
        val load = pollUntilData("0104", "Engine Load", 0.35f, 0.55f)
        val maf = pollUntilData("0110", "MAF (Mass Air Flow)", 0.55f, 0.75f)
        val rail = pollUntilData("0123", "Fuel Rail Pressure", 0.75f, 0.90f)

        status = "Scanning DTC Memory..."; progress = 0.95f
        val dtcs = obdManager.readDtc()

        onScanComplete(ObdData(volt, dtcs, temp, maf, load, "32°C", rail))
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(progress = progress, modifier = Modifier.size(100.dp), color = AutomotiveRed, strokeWidth = 8.dp)
        Spacer(modifier = Modifier.height(20.dp)); Text(status, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DiagnosticScreen(initialReport: ReportRequest?, onSendSuccess: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    val works = remember { mutableStateListOf(MaintenanceWork()) }
    var isSending by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(top = 70.dp, start = 20.dp, end = 20.dp)) {
        Text("Final Step", style = MaterialTheme.typography.headlineSmall, color = AutomotiveRed, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Your Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(works) { idx, w ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    OutlinedTextField(value = w.km, onValueChange = { works[idx] = w.copy(km = it) }, modifier = Modifier.weight(0.3f), label = { Text("KM") })
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(value = w.descrizione, onValueChange = { works[idx] = w.copy(descrizione = it) }, modifier = Modifier.weight(0.7f), label = { Text("Service Description") })
                    IconButton(onClick = { if (works.size > 1) works.removeAt(idx) }) { Icon(Icons.Default.Delete, null) }
                }
            }
            item { TextButton(onClick = { works.add(MaintenanceWork()) }) { Icon(Icons.Default.Add, null); Text("Add Previous Maintenance Record") } }
        }
        Button(onClick = {
            isSending = true
            scope.launch {
                // Masked endpoint for system privacy
                val url = "https://YOUR_PRIVATE_SERVER.ts.net/webhook/obd-diagnostic"
                if (RetrofitClient.instance.sendReport(url, initialReport!!.copy(email = email, storicoLavori = works.toList())).isSuccessful) onSendSuccess()
                else { isSending = false; Toast.makeText(context, "Cloud Connection Error", Toast.LENGTH_SHORT).show() }
            }
        }, enabled = email.contains("@") && !isSending, modifier = Modifier.fillMaxWidth().height(60.dp)) {
            if (isSending) CircularProgressIndicator(color = Color.White) else Text("GENERATE AI REPORT")
        }
    }
}

@Composable
fun SuccessScreen(marca: String, onDone: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (startAnimation) 1.0f else 0f, animationSpec = tween(1000))
    val logoRes: Int = if (marca.contains("Alfa", true)) R.drawable.logo_alfa else R.drawable.logo_audi

    LaunchedEffect(Unit) { startAnimation = true }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).background(Color.White), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(painter = painterResource(id = logoRes), modifier = Modifier.size(220.dp).scale(scale).padding(bottom = 32.dp), contentDescription = null)
        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(50.dp).scale(scale), tint = SuccessGreen)
        Spacer(modifier = Modifier.height(24.dp))
        Text("REPORT DISPATCHED!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = SuccessGreen)
        Text("Jarvis AI has completed the analysis for your $marca.", textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(modifier = Modifier.height(60.dp))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
            Text("BACK TO DASHBOARD", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuTendina(label: String, selezione: String, opzioni: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(value = if (selezione.isEmpty()) label else selezione, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            opzioni.forEach { op -> DropdownMenuItem(text = { Text(op) }, onClick = { onSelect(op); expanded = false }) }
        }
    }
}

@Composable
fun PermissionHandler(content: @Composable () -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    LaunchedEffect(Unit) {
        // Essential automotive diagnostics permissions
        launcher.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION))
    }
    content()
}

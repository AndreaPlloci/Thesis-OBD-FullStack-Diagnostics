package com.andreaplloci.thesisobdapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.andreaplloci.thesisobdapp.ObdData
import com.andreaplloci.thesisobdapp.ReportRequest
import com.andreaplloci.thesisobdapp.data.VehicleProfileStore
import com.andreaplloci.thesisobdapp.data.databaseAuto
import com.andreaplloci.thesisobdapp.data.lingue
import com.andreaplloci.thesisobdapp.data.marche
import com.andreaplloci.thesisobdapp.data.modelliPerMarca
import com.andreaplloci.thesisobdapp.ui.AutomotiveRed
import com.andreaplloci.thesisobdapp.ui.components.MenuTendina

@Composable
fun SelectionScreen(profileStore: VehicleProfileStore, onFormComplete: (ReportRequest) -> Unit) {
    val savedProfiles = remember { profileStore.getAll() }

    var lingua by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modello by remember { mutableStateOf("") }
    var anno by remember { mutableStateOf("") }
    var motore by remember { mutableStateOf("") }
    var cambio by remember { mutableStateOf("") }
    var kmAttuali by remember { mutableStateOf("") }
    var savedEmail by remember { mutableStateOf("") }
    var savedWorks by remember { mutableStateOf(emptyList<com.andreaplloci.thesisobdapp.MaintenanceWork>()) }
    var selectedProfileName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 70.dp, start = 20.dp, end = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dati Veicolo", style = MaterialTheme.typography.headlineMedium, color = AutomotiveRed, fontWeight = FontWeight.Bold)

        // Sezione profili salvati
        if (savedProfiles.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Profili Salvati", style = MaterialTheme.typography.labelLarge, color = AutomotiveRed)
                    MenuTendina(
                        label = "Carica profilo",
                        selezione = selectedProfileName,
                        opzioni = savedProfiles.map { it.displayName }
                    ) { name ->
                        selectedProfileName = name
                        val p = savedProfiles.firstOrNull { it.displayName == name } ?: return@MenuTendina
                        lingua = p.lingua
                        marca = p.marca
                        modello = p.modello
                        anno = p.anno
                        motore = p.motore
                        cambio = p.cambio
                        savedEmail = p.email
                        savedWorks = p.storicoLavori
                    }
                    if (selectedProfileName.isNotEmpty()) {
                        Text(
                            "Profilo caricato. Inserisci i km attuali e avvia.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            HorizontalDivider()
        }

        MenuTendina("Lingua Report", lingua, lingue) { lingua = it }
        MenuTendina("Marca", marca, marche) {
            if (marca != it) { marca = it; modello = ""; anno = ""; motore = ""; cambio = "" }
        }
        if (marca.isNotEmpty()) {
            MenuTendina("Modello", modello, modelliPerMarca[marca] ?: emptyList()) {
                if (modello != it) { modello = it; anno = ""; motore = ""; cambio = "" }
            }
        }
        if (modello.isNotEmpty()) {
            OutlinedTextField(
                value = anno,
                onValueChange = { if (it.length <= 4) anno = it },
                label = { Text("Anno") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        if (anno.length == 4) {
            val motori = databaseAuto[modello]?.keys?.toList() ?: listOf("Standard")
            MenuTendina("Motorizzazione", motore, motori) { motore = it; cambio = "" }
        }
        if (motore.isNotEmpty()) {
            val cambi = databaseAuto[modello]?.get(motore) ?: listOf("Manuale")
            MenuTendina("Tipo di Cambio", cambio, cambi) { cambio = it }
        }
        if (cambio.isNotEmpty()) {
            OutlinedTextField(
                value = kmAttuali,
                onValueChange = { kmAttuali = it },
                label = { Text("KM Attuali") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                onFormComplete(ReportRequest(savedEmail, lingua, marca, modello, anno, motore, cambio, kmAttuali, savedWorks, ObdData()))
            },
            enabled = kmAttuali.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text("AVVIA SCANSIONE", fontWeight = FontWeight.Bold) }
    }
}

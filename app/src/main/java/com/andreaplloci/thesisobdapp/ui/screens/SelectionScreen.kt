package com.andreaplloci.thesisobdapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andreaplloci.thesisobdapp.ObdData
import com.andreaplloci.thesisobdapp.ReportRequest
import com.andreaplloci.thesisobdapp.data.VehicleProfileStore
import com.andreaplloci.thesisobdapp.data.databaseAuto
import com.andreaplloci.thesisobdapp.data.lingue
import com.andreaplloci.thesisobdapp.data.marche
import com.andreaplloci.thesisobdapp.data.modelliPerMarca
import com.andreaplloci.thesisobdapp.ui.ActionBlue
import com.andreaplloci.thesisobdapp.ui.GradientButton
import com.andreaplloci.thesisobdapp.ui.LocalAppStrings
import com.andreaplloci.thesisobdapp.ui.components.MenuTendina

@Composable
fun SelectionScreen(
    profileStore: VehicleProfileStore,
    onLanguageChange: (String) -> Unit,
    onFormComplete: (ReportRequest) -> Unit
) {
    val s = LocalAppStrings.current
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

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ActionBlue,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp, start = 20.dp, end = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            s.vehicleDataTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 2.sp
        )

        // Saved profiles section
        if (savedProfiles.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        s.savedProfiles,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = ActionBlue,
                        letterSpacing = 1.sp
                    )
                    MenuTendina(
                        label = s.loadProfile,
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
                            s.profileLoaded,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Form card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MenuTendina(s.reportLanguage, lingua, lingue) {
                    lingua = it
                    onLanguageChange(it)
                }
                MenuTendina(s.brand, marca, marche) {
                    if (marca != it) { marca = it; modello = ""; anno = ""; motore = ""; cambio = "" }
                }
                if (marca.isNotEmpty()) {
                    MenuTendina(s.model, modello, modelliPerMarca[marca] ?: emptyList()) {
                        if (modello != it) { modello = it; anno = ""; motore = ""; cambio = "" }
                    }
                }
                if (modello.isNotEmpty()) {
                    OutlinedTextField(
                        value = anno,
                        onValueChange = { if (it.length <= 4) anno = it },
                        label = { Text(s.year) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(4.dp)
                    )
                }
                if (anno.length == 4) {
                    val motori = databaseAuto[modello]?.keys?.toList() ?: listOf("Standard")
                    MenuTendina(s.engine, motore, motori) { motore = it; cambio = "" }
                }
                if (motore.isNotEmpty()) {
                    val cambi = databaseAuto[modello]?.get(motore) ?: listOf("Manuale")
                    MenuTendina(s.gearbox, cambio, cambi) { cambio = it }
                }
                if (cambio.isNotEmpty()) {
                    OutlinedTextField(
                        value = kmAttuali,
                        onValueChange = { kmAttuali = it },
                        label = { Text(s.currentKm) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }
        }

        GradientButton(
            text = s.startScan,
            onClick = {
                onFormComplete(ReportRequest(savedEmail, lingua, marca, modello, anno, motore, cambio, kmAttuali, savedWorks, ObdData()))
            },
            enabled = kmAttuali.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
    }
}

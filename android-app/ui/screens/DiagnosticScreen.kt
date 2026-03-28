package com.andreaplloci.thesisobdapp.ui.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andreaplloci.thesisobdapp.MaintenanceWork
import com.andreaplloci.thesisobdapp.ReportRequest
import com.andreaplloci.thesisobdapp.data.VehicleProfile
import com.andreaplloci.thesisobdapp.data.VehicleProfileStore
import com.andreaplloci.thesisobdapp.repository.ReportRepository
import com.andreaplloci.thesisobdapp.ui.AutomotiveRed
import kotlinx.coroutines.launch

@Composable
fun DiagnosticScreen(
    initialReport: ReportRequest?,
    profileStore: VehicleProfileStore,
    onSendSuccess: () -> Unit,
    onBack: () -> Unit
) {
    if (initialReport == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text("Errore: dati mancanti. Torna indietro e riprova.", color = AutomotiveRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) { Text("Torna Indietro") }
        }
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ReportRepository() }
    var email by remember { mutableStateOf(initialReport.email) }
    val works = remember {
        val saved = initialReport.storicoLavori
        if (saved.isNotEmpty()) mutableStateListOf(*saved.toTypedArray()) else mutableStateListOf(MaintenanceWork())
    }
    var isSending by remember { mutableStateOf(false) }
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    Column(modifier = Modifier.fillMaxSize().padding(top = 70.dp, start = 20.dp, end = 20.dp)) {
        Text("Dati Finali Jarvis", style = MaterialTheme.typography.headlineSmall, color = AutomotiveRed, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Tua Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(works) { idx, w ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    OutlinedTextField(
                        value = w.km,
                        onValueChange = { works[idx] = w.copy(km = it) },
                        modifier = Modifier.weight(0.3f),
                        label = { Text("KM") }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(
                        value = w.descrizione,
                        onValueChange = { works[idx] = w.copy(descrizione = it) },
                        modifier = Modifier.weight(0.7f),
                        label = { Text("Descrizione") }
                    )
                    IconButton(onClick = { if (works.size > 1) works.removeAt(idx) }) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
            }
            item {
                TextButton(onClick = { works.add(MaintenanceWork()) }) {
                    Icon(Icons.Default.Add, null)
                    Text("Aggiungi Lavoro")
                }
            }
        }
        Button(
            onClick = {
                isSending = true
                scope.launch {
                    val finalWorks = works.toList()
                    val success = repository.sendReport(
                        initialReport.copy(email = email, storicoLavori = finalWorks)
                    )
                    if (success) {
                        profileStore.save(VehicleProfile(
                            lingua = initialReport.lingua,
                            marca = initialReport.marca,
                            modello = initialReport.modello,
                            anno = initialReport.anno,
                            motore = initialReport.motore,
                            cambio = initialReport.cambio,
                            email = email,
                            storicoLavori = finalWorks
                        ))
                        onSendSuccess()
                    }
                    else { isSending = false; Toast.makeText(context, "Invio fallito. Controlla la connessione e riprova.", Toast.LENGTH_LONG).show() }
                }
            },
            enabled = isEmailValid && !isSending,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            if (isSending) CircularProgressIndicator(color = Color.White) else Text("GENERA REPORT")
        }
    }
}

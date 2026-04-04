package com.andreaplloci.thesisobdapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andreaplloci.thesisobdapp.MaintenanceWork
import com.andreaplloci.thesisobdapp.ReportRequest
import com.andreaplloci.thesisobdapp.data.VehicleProfile
import com.andreaplloci.thesisobdapp.data.VehicleProfileStore
import com.andreaplloci.thesisobdapp.repository.ReportRepository
import com.andreaplloci.thesisobdapp.ui.ActionBlue
import com.andreaplloci.thesisobdapp.ui.GradientButton
import com.andreaplloci.thesisobdapp.ui.LocalAppStrings
import kotlinx.coroutines.launch
import android.util.Patterns

@Composable
fun DiagnosticScreen(
    initialReport: ReportRequest?,
    profileStore: VehicleProfileStore,
    onSendSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val s = LocalAppStrings.current

    if (initialReport == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    s.missingData,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onBack,
                border = BorderStroke(1.dp, ActionBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ActionBlue)
            ) { Text(s.back) }
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

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ActionBlue,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp, start = 20.dp, end = 20.dp)
    ) {
        Text(
            s.finalDataTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(16.dp))

        // Info callout with left blue accent border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(ActionBlue)
                .padding(start = 4.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(12.dp)
        ) {
            Text(
                s.infoCallout,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(s.emailLabel) },
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors,
            shape = RoundedCornerShape(4.dp),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                s.serviceHistory,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 1.sp
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    "${works.size}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = ActionBlue
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(works) { idx, w ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = w.km,
                            onValueChange = { works[idx] = w.copy(km = it) },
                            modifier = Modifier.weight(0.25f),
                            label = { Text(s.km) },
                            singleLine = true,
                            colors = fieldColors,
                            shape = RoundedCornerShape(4.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        OutlinedTextField(
                            value = w.descrizione,
                            onValueChange = { works[idx] = w.copy(descrizione = it) },
                            modifier = Modifier.weight(0.65f),
                            label = { Text(s.description) },
                            singleLine = true,
                            colors = fieldColors,
                            shape = RoundedCornerShape(4.dp)
                        )
                        IconButton(onClick = { if (works.size > 1) works.removeAt(idx) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { works.add(MaintenanceWork()) },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ActionBlue),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(s.addWork)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        GradientButton(
            text = s.generateReport,
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
                    } else {
                        isSending = false
                        Toast.makeText(context, s.sendFailed, Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = isEmailValid && !isSending,
            loading = isSending,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
    }
}

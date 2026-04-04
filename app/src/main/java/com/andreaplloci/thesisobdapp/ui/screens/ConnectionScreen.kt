package com.andreaplloci.thesisobdapp.ui.screens

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.andreaplloci.thesisobdapp.ObdManager
import com.andreaplloci.thesisobdapp.ui.ActionBlue
import com.andreaplloci.thesisobdapp.ui.GradientButton
import com.andreaplloci.thesisobdapp.ui.LocalAppStrings
import com.andreaplloci.thesisobdapp.ui.SuccessGreen
import kotlinx.coroutines.launch

@Composable
fun ConnectionScreen(obdManager: ObdManager, onConnected: (BluetoothDevice) -> Unit) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isConnecting by remember { mutableStateOf(false) }
    var showDeviceDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart, StartOffset(0, StartOffsetType.FastForward)),
        label = "p1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart, StartOffset(467, StartOffsetType.FastForward)),
        label = "p2"
    )
    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart, StartOffset(934, StartOffsetType.FastForward)),
        label = "p3"
    )

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.6f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val baseRadius = 56.dp.toPx()
                    listOf(pulse1, pulse2, pulse3).forEach { p ->
                        drawCircle(
                            color = ActionBlue.copy(alpha = 0.35f * (1f - p)),
                            radius = baseRadius + 48.dp.toPx() * p,
                            center = center
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(112.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "OBD",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = ActionBlue
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                s.connectTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                s.connectSubtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.weight(1f))

            GradientButton(
                text = if (isConnecting) s.connecting else s.connectButton,
                onClick = { showDeviceDialog = true },
                enabled = !isConnecting,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(40.dp))
        }
    }

    if (showDeviceDialog) {
        Dialog(onDismissRequest = { showDeviceDialog = false }) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 0.dp
            ) {
                val bluetoothManager = LocalContext.current
                    .getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                val devices = bluetoothManager?.adapter?.bondedDevices?.toList() ?: emptyList()
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        s.selectDevice,
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = ActionBlue,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    if (devices.isEmpty()) {
                        Text(
                            s.noDevices,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyColumn {
                            items(devices) { dev ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            showDeviceDialog = false
                                            isConnecting = true
                                            scope.launch {
                                                if (obdManager.connect(dev, "0")) onConnected(dev)
                                                else {
                                                    isConnecting = false
                                                    Toast.makeText(context, s.obdNotResponding, Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(ActionBlue)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        dev.name ?: dev.address,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntegrityCard(label: String, ready: Boolean, modifier: Modifier = Modifier) {
    val s = LocalAppStrings.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (ready) SuccessGreen else ActionBlue)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                if (ready) s.ready else s.search,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = if (ready) SuccessGreen else ActionBlue
            )
        }
    }
}

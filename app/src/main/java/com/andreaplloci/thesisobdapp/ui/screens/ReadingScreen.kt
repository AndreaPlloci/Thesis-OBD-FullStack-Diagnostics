package com.andreaplloci.thesisobdapp.ui.screens

import android.bluetooth.BluetoothDevice
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andreaplloci.thesisobdapp.ObdData
import com.andreaplloci.thesisobdapp.ObdManager
import com.andreaplloci.thesisobdapp.ReportRequest
import com.andreaplloci.thesisobdapp.ui.ActionBlue
import com.andreaplloci.thesisobdapp.ui.LocalAppStrings
import kotlinx.coroutines.delay

@Composable
fun ReadingScreen(
    obdManager: ObdManager,
    device: BluetoothDevice?,
    reportData: ReportRequest?,
    onBack: () -> Unit,
    onScanComplete: (ObdData) -> Unit
) {
    val s = LocalAppStrings.current

    var progress by remember { mutableStateOf(0f) }
    var status by remember { mutableStateOf(s.preparingProtocol) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var voltDisplay by remember { mutableStateOf("—") }
    var tempDisplay by remember { mutableStateOf("—") }
    var loadDisplay by remember { mutableStateOf("—") }
    var mafDisplay by remember { mutableStateOf("—") }

    LaunchedEffect(Unit) {
        if (device == null) {
            errorMessage = s.noDeviceSelected
            return@LaunchedEffect
        }

        val targetProtocol = if (reportData?.modello == "159") "5" else "0"
        val connected = obdManager.connect(device, targetProtocol)
        if (!connected) {
            errorMessage = s.connectionFailed
            return@LaunchedEffect
        }
        delay(2000)

        suspend fun pollUntilData(pid: String, label: String, startP: Float, endP: Float, maxRetries: Int = 15): String {
            status = "${s.reading} $label..."
            progress = startP
            var value = "N/A"
            var attempt = 0
            while (value == "N/A" && attempt < maxRetries) {
                value = obdManager.getParam(pid)
                if (value != "N/A") break
                status = "${s.reading} $label (${s.attempt} ${attempt + 1}/$maxRetries)"
                attempt++
                delay(1200)
            }
            progress = endP
            return value
        }

        val volt = obdManager.readVoltage()
        voltDisplay = volt

        val temp = pollUntilData("0105", s.labelEct, 0.15f, 0.30f)
        tempDisplay = temp

        val load = pollUntilData("0104", s.labelLoad, 0.30f, 0.48f)
        loadDisplay = load

        val maf = pollUntilData("0110", s.labelMaf, 0.48f, 0.64f)
        mafDisplay = maf

        val iat = pollUntilData("010F", s.labelIat, 0.64f, 0.80f, maxRetries = 8)

        val rail = pollUntilData("0123", s.labelRail, 0.80f, 0.92f)

        status = s.scanningDtc
        progress = 0.95f
        val dtcs = obdManager.readDtc()

        onScanComplete(ObdData(volt, dtcs, temp, maf, load, iat, rail))
    }

    val error = errorMessage
    if (error != null) {
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
                    error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = onBack,
                border = androidx.compose.foundation.BorderStroke(1.dp, ActionBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ActionBlue)
            ) { Text(s.back) }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        CircularGauge(
            progress = progress,
            modifier = Modifier.size(200.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            status,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(label = s.labelEct, value = tempDisplay, modifier = Modifier.weight(1f))
            MetricCard(label = s.labelLoad, value = loadDisplay, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(label = s.labelMaf, value = mafDisplay, modifier = Modifier.weight(1f))
            MetricCard(label = s.labelBattery, value = voltDisplay, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun CircularGauge(progress: Float, modifier: Modifier = Modifier) {
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val primaryColor = MaterialTheme.colorScheme.primary

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "gauge"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            if (animatedProgress > 0.01f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color(0xFFADC7FF), Color(0xFF1A73E8), Color(0xFF1A73E8)),
                        center = Offset(size.width / 2, size.height / 2)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        Text(
            "${(progress * 100).toInt()}%",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = primaryColor
        )
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

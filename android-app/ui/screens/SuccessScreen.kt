package com.andreaplloci.thesisobdapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.andreaplloci.thesisobdapp.R
import com.andreaplloci.thesisobdapp.ui.SuccessGreen

@Composable
fun SuccessScreen(marca: String, onDone: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0f,
        animationSpec = tween(1000)
    )
    val logoRes: Int = if (marca.contains("Alfa", true)) R.drawable.logo_alfa else R.drawable.logo_audi

    LaunchedEffect(Unit) { startAnimation = true }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = logoRes),
            modifier = Modifier.size(220.dp).scale(scale).padding(bottom = 32.dp),
            contentDescription = null
        )
        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(50.dp).scale(scale), tint = SuccessGreen)
        Spacer(modifier = Modifier.height(24.dp))
        Text("REPORT INVIATO!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = SuccessGreen)
        Text("Analisi Jarvis completata per $marca.", textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(modifier = Modifier.height(60.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("TORNA ALLA DASHBOARD", fontWeight = FontWeight.Bold)
        }
    }
}

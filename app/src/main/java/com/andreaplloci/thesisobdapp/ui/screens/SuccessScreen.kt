package com.andreaplloci.thesisobdapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andreaplloci.thesisobdapp.R
import com.andreaplloci.thesisobdapp.ui.GradientButton
import com.andreaplloci.thesisobdapp.ui.LocalAppStrings
import com.andreaplloci.thesisobdapp.ui.SuccessGreen

@Composable
fun SuccessScreen(marca: String, onDone: () -> Unit) {
    val s = LocalAppStrings.current

    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0f,
        animationSpec = tween(1000),
        label = "scale"
    )
    val logoRes: Int = if (marca.contains("Alfa", true)) R.drawable.logo_alfa else R.drawable.logo_audi

    LaunchedEffect(Unit) { startAnimation = true }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x141A73E8), Color.Transparent),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.minDimension * 0.8f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = null,
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale)
                    .padding(bottom = 24.dp)
            )

            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp).scale(scale),
                tint = SuccessGreen
            )

            Spacer(Modifier.height(20.dp))

            Text(
                s.reportSent,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                color = SuccessGreen,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                s.analysisComplete(marca),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(56.dp))

            GradientButton(
                text = s.backToDashboard,
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

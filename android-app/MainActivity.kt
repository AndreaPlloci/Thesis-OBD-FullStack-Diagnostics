package com.andreaplloci.thesisobdapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.andreaplloci.thesisobdapp.ui.AppNavigation
import com.andreaplloci.thesisobdapp.ui.AutomotiveRed
import com.andreaplloci.thesisobdapp.ui.components.PermissionHandler

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

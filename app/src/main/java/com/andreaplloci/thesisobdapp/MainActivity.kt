package com.andreaplloci.thesisobdapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.andreaplloci.thesisobdapp.ui.AppNavigation
import com.andreaplloci.thesisobdapp.ui.components.PermissionHandler
import com.andreaplloci.thesisobdapp.ui.theme.ThesisOBDAppTheme

class MainActivity : ComponentActivity() {
    private val obdManager = ObdManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThesisOBDAppTheme {
                PermissionHandler {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavigation(obdManager)
                    }
                }
            }
        }
    }
}

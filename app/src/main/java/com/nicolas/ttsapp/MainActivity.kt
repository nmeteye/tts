package com.nicolas.ttsapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.nicolas.ttsapp.ui.TTSNavGraph
import com.nicolas.ttsapp.ui.theme.TTSAppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Récupère le texte partagé depuis d'autres apps (intent SEND)
        val sharedText = if (intent?.action == Intent.ACTION_SEND) {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else null

        setContent {
            TTSAppTheme {
                TTSNavGraph(sharedText = sharedText)
            }
        }
    }
}

package com.nicolas.ttsapp.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicolas.ttsapp.domain.model.TtsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit
) {
    val inputText  by viewModel.inputText.collectAsState()
    val ttsState   by viewModel.ttsState.collectAsState()
    val settings   by viewModel.settings.collectAsState()

    val charCount  = inputText.length
    val maxChars   = 5000

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lecteur vocal", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Zone de saisie ──────────────────────────────────────────────
            OutlinedTextField(
                value = inputText,
                onValueChange = { if (it.length <= maxChars) viewModel.onTextChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("Entrez le texte à lire...") },
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearInput) {
                            Icon(Icons.Default.Clear, contentDescription = "Effacer")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                supportingText = {
                    Text(
                        "$charCount / $maxChars caractères",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (charCount > maxChars * 0.9) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            // ── Info vitesse / pitch ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = onNavigateToSettings,
                    label = { Text("Vitesse ×${String.format("%.1f", settings.speechRate)}") },
                    leadingIcon = { Icon(Icons.Default.Speed, null, Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = onNavigateToSettings,
                    label = { Text("Hauteur ×${String.format("%.1f", settings.pitch)}") },
                    leadingIcon = { Icon(Icons.Default.GraphicEq, null, Modifier.size(16.dp)) }
                )
                if (settings.voiceName.isNotEmpty()) {
                    AssistChip(
                        onClick = onNavigateToSettings,
                        label = { Text(settings.voiceName.take(12), maxLines = 1) },
                        leadingIcon = { Icon(Icons.Default.RecordVoiceOver, null, Modifier.size(16.dp)) }
                    )
                }
            }

            // ── Contrôles de lecture ────────────────────────────────────────
            PlaybackControls(
                state    = ttsState,
                enabled  = inputText.isNotBlank(),
                onPlay   = viewModel::speak,
                onPause  = viewModel::pause,
                onResume = viewModel::resume,
                onStop   = viewModel::stop
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    state: TtsState,
    enabled: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bouton Stop
        AnimatedVisibility(visible = state == TtsState.SPEAKING || state == TtsState.PAUSED) {
            OutlinedIconButton(
                onClick = onStop,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Arrêter",
                    modifier = Modifier.size(28.dp))
            }
        }

        // Bouton principal Play / Pause / Resume
        FilledIconButton(
            onClick = when (state) {
                TtsState.IDLE, TtsState.ERROR -> onPlay
                TtsState.SPEAKING             -> onPause
                TtsState.PAUSED               -> onResume
                TtsState.LOADING              -> { {} }
            },
            enabled  = enabled && state != TtsState.LOADING,
            modifier = Modifier.size(72.dp),
            colors   = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            AnimatedContent(targetState = state, label = "play_icon") { s ->
                when (s) {
                    TtsState.LOADING  -> CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color    = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                    TtsState.SPEAKING -> Icon(Icons.Default.Pause, "Pause",
                        modifier = Modifier.size(36.dp))
                    TtsState.PAUSED   -> Icon(Icons.Default.PlayArrow, "Reprendre",
                        modifier = Modifier.size(36.dp))
                    else              -> Icon(Icons.Default.PlayArrow, "Lire",
                        modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

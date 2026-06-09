package com.nicolas.ttsapp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val voices   by viewModel.availableVoices.collectAsState()
    var voiceExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ── Vitesse ────────────────────────────────────────────────────
            SettingsSection(title = "Vitesse de lecture") {
                SliderWithValue(
                    value    = settings.speechRate,
                    onValueChange = viewModel::setSpeechRate,
                    valueRange = 0.25f..4.0f,
                    steps    = 14,   // 0.25 increments
                    label    = "×${String.format("%.2f", settings.speechRate)}",
                    icon     = Icons.Default.Speed
                )
            }

            // ── Hauteur ────────────────────────────────────────────────────
            SettingsSection(title = "Hauteur de la voix (pitch)") {
                SliderWithValue(
                    value    = settings.pitch,
                    onValueChange = viewModel::setPitch,
                    valueRange = 0.5f..2.0f,
                    steps    = 5,
                    label    = "×${String.format("%.2f", settings.pitch)}",
                    icon     = Icons.Default.GraphicEq
                )
            }

            // ── Voix TTS ───────────────────────────────────────────────────
            SettingsSection(title = "Voix TTS") {
                if (voices.isEmpty()) {
                    Text("Voix système par défaut",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    ExposedDropdownMenuBox(
                        expanded  = voiceExpanded,
                        onExpandedChange = { voiceExpanded = !voiceExpanded }
                    ) {
                        OutlinedTextField(
                            value = settings.voiceName.ifEmpty { "Par défaut" },
                            onValueChange = {},
                            readOnly = true,
                            label    = { Text("Voix sélectionnée") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(voiceExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = voiceExpanded,
                            onDismissRequest = { voiceExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Par défaut") },
                                onClick = { viewModel.setVoice(""); voiceExpanded = false }
                            )
                            voices.forEach { voiceName ->
                                DropdownMenuItem(
                                    text = { Text(voiceName) },
                                    onClick = { viewModel.setVoice(voiceName); voiceExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            // ── Historique ─────────────────────────────────────────────────
            SettingsSection(title = "Taille max de l'historique") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Slider(
                        value = settings.maxHistorySize.toFloat(),
                        onValueChange = { viewModel.setMaxHistory(it.roundToInt()) },
                        valueRange = 20f..500f,
                        steps = 23,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("${settings.maxHistorySize}", style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(40.dp))
                }
            }

            // ── Reset ──────────────────────────────────────────────────────
            OutlinedButton(
                onClick = viewModel::resetDefaults,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.RestartAlt, null)
                Spacer(Modifier.width(8.dp))
                Text("Réinitialiser les paramètres")
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary)
        HorizontalDivider()
        content()
    }
}

@Composable
private fun SliderWithValue(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(56.dp))
    }
}

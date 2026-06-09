package com.nicolas.ttsapp.ui.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicolas.ttsapp.domain.model.HistoryEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onSelectEntry: (String) -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    var editingEntry by remember { mutableStateOf<HistoryEntry?>(null) }
    var labelInput   by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoris") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FavoriteBorder, null, Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("Aucun favori", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Ajoutez des textes depuis l'historique",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites, key = { it.id }) { entry ->
                    FavoriteCard(
                        entry    = entry,
                        onSelect = { onSelectEntry(entry.text) },
                        onEditLabel = { editingEntry = entry; labelInput = entry.label },
                        onRemove = { viewModel.removeFavorite(entry) }
                    )
                }
            }
        }
    }

    // ── Dialogue édition libellé ─────────────────────────────────────────────
    editingEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { editingEntry = null },
            title = { Text("Libellé du favori") },
            text = {
                OutlinedTextField(
                    value = labelInput,
                    onValueChange = { labelInput = it },
                    label = { Text("Libellé (optionnel)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateLabel(entry, labelInput)
                    editingEntry = null
                }) { Text("Enregistrer") }
            },
            dismissButton = {
                TextButton(onClick = { editingEntry = null }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun FavoriteCard(
    entry: HistoryEntry,
    onSelect: () -> Unit,
    onEditLabel: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                if (entry.label.isNotEmpty()) {
                    Text(entry.label, style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    text = entry.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onEditLabel) {
                Icon(Icons.Default.Edit, "Modifier le libellé",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.FavoriteBorder, "Retirer des favoris",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

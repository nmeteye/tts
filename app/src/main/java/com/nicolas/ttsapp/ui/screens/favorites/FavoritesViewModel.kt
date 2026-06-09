package com.nicolas.ttsapp.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicolas.ttsapp.data.repository.HistoryRepository
import com.nicolas.ttsapp.domain.model.HistoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repo: HistoryRepository
) : ViewModel() {

    val favorites = repo.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFavorite(entry: HistoryEntry) = viewModelScope.launch {
        repo.toggleFavorite(entry) // bascule isFavorite → false
    }

    fun updateLabel(entry: HistoryEntry, label: String) = viewModelScope.launch {
        repo.updateLabel(entry, label)
    }
}

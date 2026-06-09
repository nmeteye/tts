package com.nicolas.ttsapp.ui.screens.history

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
class HistoryViewModel @Inject constructor(
    private val repo: HistoryRepository
) : ViewModel() {

    val history = repo.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavorite(entry: HistoryEntry) = viewModelScope.launch {
        repo.toggleFavorite(entry)
    }

    fun delete(entry: HistoryEntry) = viewModelScope.launch {
        repo.delete(entry)
    }

    fun clearHistory() = viewModelScope.launch {
        repo.clearHistory()
    }
}

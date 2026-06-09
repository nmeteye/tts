package com.nicolas.ttsapp.ui.screens.home

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nicolas.ttsapp.data.repository.HistoryRepository
import com.nicolas.ttsapp.data.repository.SettingsRepository
import com.nicolas.ttsapp.domain.model.TtsSettings
import com.nicolas.ttsapp.domain.model.TtsState
import com.nicolas.ttsapp.service.TTSService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val historyRepo: HistoryRepository,
    private val settingsRepo: SettingsRepository
) : AndroidViewModel(application) {

    // ── State ─────────────────────────────────────────────────────────────────

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText

    val settings: StateFlow<TtsSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TtsSettings())

    private val _ttsState = MutableStateFlow(TtsState.IDLE)
    val ttsState: StateFlow<TtsState> = _ttsState

    val availableVoices: StateFlow<List<String>> = MutableStateFlow(emptyList())

    // ── Service binding ───────────────────────────────────────────────────────

    private var ttsService: TTSService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            ttsService = (binder as TTSService.LocalBinder).getService()
            isBound = true
            observeServiceState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            ttsService = null
            isBound = false
        }
    }

    init {
        val app = getApplication<Application>()
        val intent = Intent(app, TTSService::class.java)
        app.startService(intent)
        app.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        if (isBound) getApplication<Application>().unbindService(connection)
        super.onCleared()
    }

    private fun observeServiceState() {
        viewModelScope.launch {
            ttsService?.ttsState?.collect { state ->
                _ttsState.value = state
            }
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    fun onTextChange(text: String) {
        _inputText.value = text
    }

    fun speak() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return
        val s = settings.value
        ttsService?.setParams(s.speechRate, s.pitch, s.voiceName)
        ttsService?.speak(text)
        viewModelScope.launch {
            historyRepo.add(text, s.maxHistorySize)
        }
    }

    fun pause() = ttsService?.pause()

    fun resume() = ttsService?.resume(_inputText.value)

    fun stop() = ttsService?.stop()

    fun clearInput() { _inputText.value = "" }

    fun loadFromHistory(text: String) { _inputText.value = text }

    // Paramètres
    fun setSpeechRate(v: Float)  = viewModelScope.launch { settingsRepo.updateSpeechRate(v) }
    fun setPitch(v: Float)       = viewModelScope.launch { settingsRepo.updatePitch(v) }
    fun setVoice(name: String)   = viewModelScope.launch { settingsRepo.updateVoiceName(name) }
}

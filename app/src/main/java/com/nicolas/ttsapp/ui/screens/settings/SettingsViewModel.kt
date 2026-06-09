package com.nicolas.ttsapp.ui.screens.settings

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nicolas.ttsapp.data.repository.SettingsRepository
import com.nicolas.ttsapp.domain.model.TtsSettings
import com.nicolas.ttsapp.service.TTSService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsRepo: SettingsRepository
) : AndroidViewModel(application) {

    val settings: StateFlow<TtsSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TtsSettings())

    private val _voices = MutableStateFlow<List<String>>(emptyList())
    val availableVoices: StateFlow<List<String>> = _voices

    private var ttsService: TTSService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            ttsService = (binder as TTSService.LocalBinder).getService()
            viewModelScope.launch {
                ttsService?.availableVoices?.collect { voices ->
                    _voices.value = voices
                }
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) { ttsService = null }
    }

    init {
        val app = getApplication<Application>()
        app.bindService(
            Intent(app, TTSService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onCleared() {
        getApplication<Application>().unbindService(connection)
        super.onCleared()
    }

    fun setSpeechRate(v: Float) = viewModelScope.launch { settingsRepo.updateSpeechRate(v) }
    fun setPitch(v: Float)      = viewModelScope.launch { settingsRepo.updatePitch(v) }
    fun setVoice(name: String)  = viewModelScope.launch { settingsRepo.updateVoiceName(name) }
    fun setMaxHistory(n: Int)   = viewModelScope.launch { settingsRepo.updateMaxHistory(n) }
    fun resetDefaults()         = viewModelScope.launch { settingsRepo.resetDefaults() }
}

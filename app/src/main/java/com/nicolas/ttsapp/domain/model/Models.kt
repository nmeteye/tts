package com.nicolas.ttsapp.domain.model

import java.time.LocalDateTime

data class HistoryEntry(
    val id: Long = 0,
    val text: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isFavorite: Boolean = false,
    val label: String = ""      // libellé optionnel pour les favoris
)

data class TtsSettings(
    val speechRate: Float = 1.0f,       // 0.25 – 4.0
    val pitch: Float = 1.0f,            // 0.1 – 2.0
    val voiceName: String = "",         // nom TTS système sélectionné
    val volumeGain: Float = 1.0f,       // réservé usage futur
    val maxHistorySize: Int = 200
)

enum class TtsState { IDLE, LOADING, SPEAKING, PAUSED, ERROR }

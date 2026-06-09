package com.nicolas.ttsapp.service

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import com.nicolas.ttsapp.MainActivity
import com.nicolas.ttsapp.R
import com.nicolas.ttsapp.domain.model.TtsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TTSService : Service(), TextToSpeech.OnInitListener {

    inner class LocalBinder : Binder() {
        fun getService(): TTSService = this@TTSService
    }

    private val binder = LocalBinder()

    private lateinit var tts: TextToSpeech

    private val _ttsState = MutableStateFlow(TtsState.IDLE)
    val ttsState: StateFlow<TtsState> = _ttsState

    private val _progress = MutableStateFlow(0f)  // 0..1
    val progress: StateFlow<Float> = _progress

    private val _availableVoices = MutableStateFlow<List<String>>(emptyList())
    val availableVoices: StateFlow<List<String>> = _availableVoices

    private var speechRate = 1.0f
    private var pitch      = 1.0f
    private var voiceName  = ""

    companion object {
        const val CHANNEL_ID     = "tts_channel"
        const val NOTIFICATION_ID = 1
        private const val UTTERANCE_ID = "TTS_UTTERANCE"
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        tts = TextToSpeech(this, this)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }

    // ── TTS Init ──────────────────────────────────────────────────────────────

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.FRENCH
            refreshVoices()
            setupProgressListener()
        } else {
            _ttsState.value = TtsState.ERROR
        }
    }

    private fun refreshVoices() {
        val voices = tts.voices
            ?.filter { it.locale.language == "fr" }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
        _availableVoices.value = voices
    }

    private fun setupProgressListener() {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _ttsState.value = TtsState.SPEAKING
                _progress.value = 0f
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                // Progression mot par mot si disponible (API 26+)
            }

            override fun onDone(utteranceId: String?) {
                _ttsState.value = TtsState.IDLE
                _progress.value = 1f
                stopForeground(STOP_FOREGROUND_REMOVE)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _ttsState.value = TtsState.ERROR
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        })
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun speak(text: String) {
        if (text.isBlank()) return
        applyParams()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
        startForeground(NOTIFICATION_ID, buildNotification(text.take(60)))
    }

    fun pause() {
        if (_ttsState.value == TtsState.SPEAKING) {
            tts.stop()
            _ttsState.value = TtsState.PAUSED
        }
    }

    fun resume(text: String) {
        if (_ttsState.value == TtsState.PAUSED) {
            speak(text)
        }
    }

    fun stop() {
        tts.stop()
        _ttsState.value = TtsState.IDLE
        _progress.value = 0f
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun setParams(rate: Float, pitch: Float, voice: String) {
        this.speechRate = rate
        this.pitch      = pitch
        this.voiceName  = voice
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private fun applyParams() {
        tts.setSpeechRate(speechRate)
        tts.setPitch(pitch)
        if (voiceName.isNotEmpty()) {
            tts.voices?.find { it.name == voiceName }?.let { tts.voice = it }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Lecture TTS",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Lecture vocale en cours" }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(previewText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, TTSService::class.java).apply { action = "STOP" },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tts_notification)
            .setContentTitle("Lecture en cours")
            .setContentText(previewText)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stop, "Arrêter", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") stop()
        return START_NOT_STICKY
    }
}

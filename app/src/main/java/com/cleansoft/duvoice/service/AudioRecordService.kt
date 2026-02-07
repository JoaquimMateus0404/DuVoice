package com.cleansoft.duvoice.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cleansoft.duvoice.MainActivity
import com.cleansoft.duvoice.R
import com.cleansoft.duvoice.data.model.AudioQuality
import com.cleansoft.duvoice.util.audio.AudioRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class AudioRecordService : Service() {

    companion object {
        const val CHANNEL_ID = "audio_recording_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.cleansoft.duvoice.action.START"
        const val ACTION_PAUSE = "com.cleansoft.duvoice.action.PAUSE"
        const val ACTION_RESUME = "com.cleansoft.duvoice.action.RESUME"
        const val ACTION_STOP = "com.cleansoft.duvoice.action.STOP"

        const val EXTRA_OUTPUT_PATH = "output_path"
        const val EXTRA_QUALITY = "quality"
        const val EXTRA_IS_STEREO = "is_stereo"
    }

    private val binder = LocalBinder()
    private val audioRecorder = AudioRecorder()
    private var notificationUpdateJob: Job? = null

    val state: StateFlow<AudioRecorder.State> get() = audioRecorder.state
    val amplitude: StateFlow<Int> get() = audioRecorder.amplitude
    val elapsedTime: StateFlow<Long> get() = audioRecorder.elapsedTime

    inner class LocalBinder : Binder() {
        fun getService(): AudioRecordService = this@AudioRecordService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val outputPath = intent.getStringExtra(EXTRA_OUTPUT_PATH)
                val qualityName = intent.getStringExtra(EXTRA_QUALITY) ?: AudioQuality.MEDIUM.name
                val isStereo = intent.getBooleanExtra(EXTRA_IS_STEREO, false)

                if (outputPath != null) {
                    startRecording(outputPath, AudioQuality.valueOf(qualityName), isStereo)
                }
            }
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_STICKY
    }

    private fun startRecording(outputPath: String, quality: AudioQuality, isStereo: Boolean) {
        val file = File(outputPath)
        if (audioRecorder.prepare(file, quality, isStereo)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    createNotification("A gravar..."),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(NOTIFICATION_ID, createNotification("A gravar..."))
            }

            audioRecorder.start()
            startNotificationUpdates()
        }
    }

    private fun pauseRecording() {
        audioRecorder.pause()
        updateNotification("Gravação pausada")
        stopNotificationUpdates()
    }

    private fun resumeRecording() {
        audioRecorder.start()
        startNotificationUpdates()
    }

    fun stopRecording(): File? {
        stopNotificationUpdates()
        val file = audioRecorder.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        return file
    }

    fun getRecordedFile(): File? {
        return audioRecorder.stop()
    }

    fun getAudioRecorder(): AudioRecorder = audioRecorder

    fun getDuration(): Long = audioRecorder.getDuration()

    fun getSampleRate(): Int = audioRecorder.getSampleRate()

    fun getChannels(): Int = audioRecorder.getChannels()

    fun getAmplitudeHistory(): List<Int> = audioRecorder.getAmplitudeHistory()

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Gravação de Áudio",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Mostra quando o app está a gravar áudio"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Ação de parar
        val stopIntent = Intent(this, AudioRecordService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DuVoice")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Parar", stopPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(text))
    }

    private fun startNotificationUpdates() {
        notificationUpdateJob?.cancel()
        notificationUpdateJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val elapsed = elapsedTime.value
                val formatted = formatTime(elapsed)
                updateNotification("A gravar: $formatted")
                delay(1000)
            }
        }
    }

    private fun stopNotificationUpdates() {
        notificationUpdateJob?.cancel()
        notificationUpdateJob = null
    }

    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecorder.release()
        notificationUpdateJob?.cancel()
    }
}


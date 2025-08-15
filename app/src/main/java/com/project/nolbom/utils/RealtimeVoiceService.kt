// utils/RealtimeVoiceService.kt - 에러 수정 버전
package com.project.nolbom.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.project.nolbom.data.repository.STTRepository
import kotlinx.coroutines.*
import kotlin.math.sqrt

class RealtimeVoiceService : Service() {

    companion object {
        private const val TAG = "RealtimeVoiceService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "realtime_voice_channel"

        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_DURATION_MS = 1000 // 1초 버퍼
        private const val VOICE_THRESHOLD = 500 // 음성 감지 임계값
        private const val SILENCE_DURATION = 2000 // 2초 침묵 후 음성 세그먼트 완료
    }

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var serviceJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val sttRepository = STTRepository()

    // 음성 버퍼링
    private val audioBuffer = mutableListOf<Short>()
    private var lastVoiceTime = 0L
    private var isVoiceSegmentActive = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
        Log.d(TAG, "🎤 실시간 음성 감지 서비스 생성")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            "STOP_SERVICE" -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                Log.d(TAG, "🎤 실시간 음성 감지 서비스 시작")

                val notification = createNotification()
                startForeground(NOTIFICATION_ID, notification)

                startRealtimeVoiceMonitoring()

                return START_STICKY // 서비스가 종료되어도 자동 재시작
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopVoiceMonitoring()
        releaseWakeLock()
        Log.d(TAG, "🛑 실시간 음성 감지 서비스 종료")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "실시간 음성 응급 감지",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "실시간으로 응급 상황을 감지합니다"
                setSound(null, null)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        // 🔧 중지 버튼 추가 (아이콘 없이)
        val stopIntent = Intent(this, RealtimeVoiceService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val stopPendingIntent = android.app.PendingIntent.getService(
            this, 0, stopIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("놀봄 - 실시간 음성 감지 중 🎤")
            .setContentText("응급 상황을 실시간으로 감지하고 있습니다")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now) // 🔧 기본 안드로이드 아이콘 사용
            .setOngoing(true)
            .setSilent(true)
            .addAction(
                android.R.drawable.ic_media_pause, // 🔧 기본 안드로이드 아이콘 사용
                "중지",
                stopPendingIntent
            )
            .build()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "NolbomApp::RealtimeVoiceWakeLock"
        )
        wakeLock?.acquire(24*60*60*1000L /*24 hours*/) // 하루 종일 유지
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    private fun startRealtimeVoiceMonitoring() {
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "🎤 실시간 음성 모니터링 시작")

            // 권한 확인
            if (ActivityCompat.checkSelfPermission(
                    this@RealtimeVoiceService,
                    android.Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "❌ 음성 녹음 권한 없음")
                return@launch
            }

            try {
                val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 2

                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "❌ AudioRecord 초기화 실패")
                    return@launch
                }

                audioRecord?.startRecording()
                isRecording = true

                Log.d(TAG, "✅ 실시간 음성 녹음 시작됨")

                val buffer = ShortArray(bufferSize / 2)

                while (isActive && isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        processAudioBuffer(buffer, read)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ 실시간 모니터링 오류: ${e.message}")
            } finally {
                stopCurrentRecording()
            }
        }
    }

    private suspend fun processAudioBuffer(buffer: ShortArray, readBytes: Int) {
        val amplitude = calculateAmplitude(buffer, readBytes)
        val currentTime = System.currentTimeMillis()

        // 음성이 감지되었을 때
        if (amplitude > VOICE_THRESHOLD) {
            lastVoiceTime = currentTime

            if (!isVoiceSegmentActive) {
                isVoiceSegmentActive = true
                audioBuffer.clear()
                Log.d(TAG, "🔊 음성 세그먼트 시작 (레벨: $amplitude)")
            }

            // 오디오 데이터를 버퍼에 추가
            for (i in 0 until readBytes) {
                audioBuffer.add(buffer[i])
            }

        } else {
            // 침묵 상태
            if (isVoiceSegmentActive) {
                // 음성 버퍼에 침묵도 약간 포함 (자연스러운 음성 인식을 위해)
                for (i in 0 until readBytes) {
                    audioBuffer.add(buffer[i])
                }

                // 일정 시간 침묵이 지속되면 음성 세그먼트 완료
                if (currentTime - lastVoiceTime > SILENCE_DURATION) {
                    isVoiceSegmentActive = false

                    Log.d(TAG, "🔇 음성 세그먼트 완료 - 서버로 전송 (${audioBuffer.size} 샘플)")

                    // 음성 데이터를 서버로 전송
                    if (audioBuffer.isNotEmpty()) {
                        sendVoiceSegmentToServer()
                    }

                    audioBuffer.clear()
                }
            }
        }
    }

    private fun calculateAmplitude(buffer: ShortArray, readBytes: Int): Int {
        var sum = 0.0
        for (i in 0 until readBytes) {
            sum += buffer[i] * buffer[i]
        }
        return if (readBytes > 0) sqrt(sum / readBytes).toInt() else 0
    }

    private suspend fun sendVoiceSegmentToServer() {
        try {
            // Short 배열을 byte 배열로 변환
            val byteArray = ByteArray(audioBuffer.size * 2)
            for (i in audioBuffer.indices) {
                val value = audioBuffer[i]
                byteArray[i * 2] = (value.toInt() and 0xff).toByte() // 🔧 and 연산자 수정
                byteArray[i * 2 + 1] = ((value.toInt() shr 8) and 0xff).toByte()
            }

            val base64Audio = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            Log.d(TAG, "📡 음성 세그먼트 서버 전송 중...")

            val result = sttRepository.recognizeVoice(base64Audio)

            result.fold(
                onSuccess = { response ->
                    Log.d(TAG, "✅ 실시간 음성 인식 성공: ${response.transcript}")

                    if (response.keywordDetected) {
                        Log.w(TAG, "🚨 응급 키워드 감지! SMS 전송됨")

                        // 응급 상황 알림 (권한 확인 후)
                        if (hasNotificationPermission()) {
                            showEmergencyNotification(response.transcript)
                        }
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ 실시간 음성 인식 실패: ${error.message}")
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ 서버 전송 오류: ${e.message}")
        }
    }

    // 🔧 알림 권한 확인 함수 추가
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13 미만에서는 알림 권한이 필요 없음
        }
    }

    private fun showEmergencyNotification(transcript: String) {
        try {
            val emergencyNotification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("🚨 응급 상황 감지!")
                .setContentText("\"$transcript\" - SMS 전송 완료")
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // 🔧 기본 안드로이드 아이콘 사용
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID + 1, emergencyNotification)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 응급 알림 표시 실패: ${e.message}")
        }
    }

    private fun stopCurrentRecording() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.d(TAG, "🛑 실시간 녹음 중지")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 녹음 중지 오류: ${e.message}")
        }
    }

    private fun stopVoiceMonitoring() {
        serviceJob?.cancel()
        stopCurrentRecording()
        audioBuffer.clear()
        Log.d(TAG, "🛑 실시간 음성 모니터링 완전 중지")
    }
}
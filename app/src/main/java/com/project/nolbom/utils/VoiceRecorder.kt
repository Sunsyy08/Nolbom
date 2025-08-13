// utils/VoiceRecorder.kt - testMicrophone 함수 추가
package com.project.nolbom.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import kotlin.math.sqrt

class VoiceRecorder(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    companion object {
        private const val TAG = "VoiceRecorder"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 2
    }

    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
    ) * BUFFER_SIZE_FACTOR

    init {
        Log.d(TAG, "🎤 VoiceRecorder 초기화")
        Log.d(TAG, "버퍼 크기: $bufferSize bytes")
        debugAudioSettings()
    }

    private fun debugAudioSettings() {
        try {
            Log.d(TAG, "=== 오디오 설정 디버깅 ===")
            Log.d(TAG, "마이크 음소거: ${audioManager.isMicrophoneMute}")
            Log.d(TAG, "음성통화 볼륨: ${audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)}")
            Log.d(TAG, "시스템 볼륨: ${audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)}")
            Log.d(TAG, "미디어 볼륨: ${audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)}")

            // 마이크 하드웨어 체크
            val packageManager = context.packageManager
            val hasMicrophone = packageManager.hasSystemFeature("android.hardware.microphone")
            Log.d(TAG, "마이크 하드웨어: $hasMicrophone")

        } catch (e: Exception) {
            Log.e(TAG, "오디오 설정 디버깅 오류: ${e.message}")
        }
    }

    /**
     * 권한 확인
     */
    fun hasRecordPermission(): Boolean {
        val hasPermission = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "녹음 권한 상태: $hasPermission")
        return hasPermission
    }

    /**
     * 마이크 테스트
     */
    suspend fun testMicrophone(): String = withContext(Dispatchers.IO) {
        if (!hasRecordPermission()) {
            return@withContext "❌ 마이크 권한 없음"
        }

        try {
            val testRecord = createAudioRecord()
            if (testRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return@withContext "❌ AudioRecord 초기화 실패"
            }

            Log.d(TAG, "🧪 마이크 테스트 시작")

            val buffer = ByteArray(bufferSize)
            testRecord.startRecording()

            var totalAmplitude = 0.0
            var maxAmplitude = 0
            val testDuration = 1000 // 1초 테스트
            val startTime = System.currentTimeMillis()
            var sampleCount = 0

            while (System.currentTimeMillis() - startTime < testDuration) {
                val readBytes = testRecord.read(buffer, 0, buffer.size)
                if (readBytes > 0) {
                    // 음성 레벨 계산
                    val amplitude = calculateAmplitude(buffer, readBytes)
                    totalAmplitude += amplitude
                    sampleCount++
                    if (amplitude > maxAmplitude) {
                        maxAmplitude = amplitude
                    }

                    // 100ms마다 로그
                    if (sampleCount % 10 == 0) {
                        Log.d(TAG, "📊 현재 음성 레벨: $amplitude")
                    }
                }
            }

            testRecord.stop()
            testRecord.release()

            val avgAmplitude = if (sampleCount > 0) totalAmplitude / sampleCount else 0.0

            Log.d(TAG, "🧪 테스트 완료 - 평균: $avgAmplitude, 최대: $maxAmplitude, 샘플: $sampleCount")

            return@withContext when {
                maxAmplitude < 50 -> "❌ 마이크 입력 없음 (레벨: $maxAmplitude)"
                maxAmplitude < 500 -> "⚠️ 마이크 입력 약함 (레벨: $maxAmplitude)"
                else -> "✅ 마이크 정상 작동 (레벨: $maxAmplitude)"
            }

        } catch (e: Exception) {
            Log.e(TAG, "마이크 테스트 오류: ${e.message}")
            return@withContext "❌ 테스트 실패: ${e.message}"
        }
    }

    private fun calculateAmplitude(buffer: ByteArray, readBytes: Int): Int {
        var sum = 0.0
        for (i in 0 until readBytes step 2) {
            if (i + 1 < readBytes) {
                val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
                sum += sample * sample
            }
        }
        return if (readBytes > 0) sqrt(sum / (readBytes / 2)).toInt() else 0
    }

    /**
     * AudioRecord 생성 (권한 체크 포함)
     */
    private fun createAudioRecord(): AudioRecord? {
        return if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                Log.d(TAG, "AudioRecord 생성 중...")
                Log.d(TAG, "샘플레이트: $SAMPLE_RATE, 버퍼크기: $bufferSize")

                val record = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
                )

                Log.d(TAG, "AudioRecord 상태: ${record.state}")
                Log.d(TAG, "AudioRecord 레코딩 상태: ${record.recordingState}")

                record

            } catch (e: SecurityException) {
                Log.e(TAG, "❌ AudioRecord 생성 권한 오류: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e(TAG, "❌ AudioRecord 생성 오류: ${e.message}")
                e.printStackTrace()
                null
            }
        } else {
            Log.e(TAG, "❌ 음성 녹음 권한이 없습니다")
            null
        }
    }

    /**
     * 짧은 음성 녹음 (3초)
     */
    suspend fun recordShortAudio(durationMs: Long = 3000): String? = withContext(Dispatchers.IO) {
        if (!hasRecordPermission()) {
            Log.e(TAG, "❌ 음성 녹음 권한이 없습니다")
            return@withContext null
        }

        try {
            // AudioRecord 초기화
            audioRecord = createAudioRecord()
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "❌ AudioRecord 초기화 실패")
                return@withContext null
            }

            Log.d(TAG, "🎤 음성 녹음 시작 (${durationMs}ms)")

            val outputStream = ByteArrayOutputStream()
            val buffer = ByteArray(bufferSize)
            var totalAmplitude = 0.0
            var sampleCount = 0

            audioRecord?.startRecording()
            isRecording = true

            val endTime = System.currentTimeMillis() + durationMs

            while (isRecording && System.currentTimeMillis() < endTime) {
                val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readBytes > 0) {
                    outputStream.write(buffer, 0, readBytes)

                    // 음성 레벨 모니터링
                    val amplitude = calculateAmplitude(buffer, readBytes)
                    totalAmplitude += amplitude
                    sampleCount++

                    if (sampleCount % 10 == 0) { // 100ms마다 로그
                        Log.d(TAG, "📊 현재 음성 레벨: $amplitude")
                    }
                }
            }

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            isRecording = false

            val audioData = outputStream.toByteArray()
            outputStream.close()

            val avgAmplitude = if (sampleCount > 0) totalAmplitude / sampleCount else 0.0
            Log.d(TAG, "✅ 음성 녹음 완료: ${audioData.size} bytes, 평균 레벨: $avgAmplitude")

            if (avgAmplitude < 50) {
                Log.w(TAG, "⚠️ 녹음된 음성 레벨이 매우 낮습니다. 마이크를 확인해주세요.")
            }

            // Base64 인코딩
            return@withContext Base64.encodeToString(audioData, Base64.NO_WRAP)

        } catch (e: SecurityException) {
            Log.e(TAG, "❌ 음성 녹음 권한 오류: ${e.message}")
            stopRecording()
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "❌ 음성 녹음 오류: ${e.message}")
            e.printStackTrace()
            stopRecording()
            return@withContext null
        }
    }

    /**
     * 연속 음성 녹음 시작
     */
    fun startContinuousRecording(
        onAudioChunk: (String) -> Unit,
        chunkDurationMs: Long = 2000
    ) {
        if (!hasRecordPermission()) {
            Log.e(TAG, "❌ 음성 녹음 권한이 없습니다")
            return
        }

        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                audioRecord = createAudioRecord()
                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "❌ AudioRecord 초기화 실패")
                    return@launch
                }

                audioRecord?.startRecording()
                isRecording = true

                Log.d(TAG, "🎤 연속 음성 녹음 시작")

                val buffer = ByteArray(bufferSize)
                val chunkSize = (SAMPLE_RATE * chunkDurationMs / 1000).toInt() * 2 // 16bit = 2 bytes
                val outputStream = ByteArrayOutputStream()
                var chunkCount = 0

                while (isRecording) {
                    val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readBytes > 0) {
                        outputStream.write(buffer, 0, readBytes)

                        // 음성 레벨 모니터링
                        val amplitude = calculateAmplitude(buffer, readBytes)
                        if (amplitude > 100) { // 임계값 이상일 때만 로그
                            Log.d(TAG, "🔊 음성 감지: $amplitude")
                        }

                        // 청크 크기가 되면 전송
                        if (outputStream.size() >= chunkSize) {
                            val audioData = outputStream.toByteArray()
                            val base64Audio = Base64.encodeToString(audioData, Base64.NO_WRAP)

                            chunkCount++
                            Log.d(TAG, "📦 청크 #$chunkCount 전송: ${audioData.size} bytes")

                            // 메인 스레드에서 콜백 실행
                            withContext(Dispatchers.Main) {
                                onAudioChunk(base64Audio)
                            }

                            outputStream.reset()
                        }
                    }
                }

                outputStream.close()

            } catch (e: SecurityException) {
                Log.e(TAG, "❌ 연속 음성 녹음 권한 오류: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ 연속 음성 녹음 오류: ${e.message}")
                e.printStackTrace()
            } finally {
                stopRecording()
            }
        }
    }

    /**
     * 녹음 중지
     */
    fun stopRecording() {
        isRecording = false
        recordingJob?.cancel()

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.d(TAG, "🛑 음성 녹음 중지")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 녹음 중지 오류: ${e.message}")
        }
    }

    /**
     * 리소스 정리
     */
    fun release() {
        stopRecording()
        Log.d(TAG, "🧹 VoiceRecorder 리소스 정리")
    }
}
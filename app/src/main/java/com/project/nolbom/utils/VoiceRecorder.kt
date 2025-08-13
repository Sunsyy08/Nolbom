// utils/VoiceRecorder.kt - 권한 체크 개선
package com.project.nolbom.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class VoiceRecorder(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 2
    }

    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
    ) * BUFFER_SIZE_FACTOR

    /**
     * 권한 확인
     */
    fun hasRecordPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
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
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
                )
            } catch (e: SecurityException) {
                println("❌ AudioRecord 생성 권한 오류: ${e.message}")
                null
            } catch (e: Exception) {
                println("❌ AudioRecord 생성 오류: ${e.message}")
                null
            }
        } else {
            println("❌ 음성 녹음 권한이 없습니다")
            null
        }
    }

    /**
     * 짧은 음성 녹음 (3초)
     */
    suspend fun recordShortAudio(durationMs: Long = 3000): String? = withContext(Dispatchers.IO) {
        if (!hasRecordPermission()) {
            println("❌ 음성 녹음 권한이 없습니다")
            return@withContext null
        }

        try {
            // AudioRecord 초기화
            audioRecord = createAudioRecord()
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                println("❌ AudioRecord 초기화 실패")
                return@withContext null
            }

            println("🎤 음성 녹음 시작 (${durationMs}ms)")

            val outputStream = ByteArrayOutputStream()
            val buffer = ByteArray(bufferSize)

            audioRecord?.startRecording()
            isRecording = true

            val endTime = System.currentTimeMillis() + durationMs

            while (isRecording && System.currentTimeMillis() < endTime) {
                val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readBytes > 0) {
                    outputStream.write(buffer, 0, readBytes)
                }
            }

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            isRecording = false

            val audioData = outputStream.toByteArray()
            outputStream.close()

            println("✅ 음성 녹음 완료: ${audioData.size} bytes")

            // Base64 인코딩
            return@withContext Base64.encodeToString(audioData, Base64.NO_WRAP)

        } catch (e: SecurityException) {
            println("❌ 음성 녹음 권한 오류: ${e.message}")
            stopRecording()
            return@withContext null
        } catch (e: Exception) {
            println("❌ 음성 녹음 오류: ${e.message}")
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
            println("❌ 음성 녹음 권한이 없습니다")
            return
        }

        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                audioRecord = createAudioRecord()
                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    println("❌ AudioRecord 초기화 실패")
                    return@launch
                }

                audioRecord?.startRecording()
                isRecording = true

                println("🎤 연속 음성 녹음 시작")

                val buffer = ByteArray(bufferSize)
                val chunkSize = (SAMPLE_RATE * chunkDurationMs / 1000).toInt() * 2 // 16bit = 2 bytes
                val outputStream = ByteArrayOutputStream()

                while (isRecording) {
                    val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readBytes > 0) {
                        outputStream.write(buffer, 0, readBytes)

                        // 청크 크기가 되면 전송
                        if (outputStream.size() >= chunkSize) {
                            val audioData = outputStream.toByteArray()
                            val base64Audio = Base64.encodeToString(audioData, Base64.NO_WRAP)

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
                println("❌ 연속 음성 녹음 권한 오류: ${e.message}")
            } catch (e: Exception) {
                println("❌ 연속 음성 녹음 오류: ${e.message}")
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
            println("🛑 음성 녹음 중지")
        } catch (e: Exception) {
            println("❌ 녹음 중지 오류: ${e.message}")
        }
    }

    /**
     * 리소스 정리
     */
    fun release() {
        stopRecording()
    }
}
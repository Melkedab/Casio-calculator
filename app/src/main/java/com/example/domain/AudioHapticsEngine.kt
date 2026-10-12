package com.example.domain

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class AudioHapticsEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    // Pre-generated PCM audio buffer for ultra-low-latency mechanical key click
    private val sampleRate = 22050
    private val clickSoundData: ShortArray by lazy {
        // 8 milliseconds duration
        val numSamples = (sampleRate * 0.008).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            // Exponential decaying envelope
            val envelope = Math.exp(-progress * 6.0)
            // Two blended frequencies: 2400Hz and 3200Hz for crisp plastic click
            val tone1 = sin(2.0 * Math.PI * 2400.0 * i / sampleRate)
            val tone2 = sin(2.0 * Math.PI * 3400.0 * i / sampleRate)
            val sample = ((tone1 * 0.7 + tone2 * 0.3) * envelope * Short.MAX_VALUE * 0.35).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        buffer
    }

    fun playKeyClick() {
        if (isHapticsEnabled) {
            triggerHapticClick()
        }
        if (isSoundEnabled) {
            triggerSoundClick()
        }
    }

    private fun triggerHapticClick() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(15)
                }
            }
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    private fun triggerSoundClick() {
        scope.launch {
            try {
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(clickSoundData.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(clickSoundData, 0, clickSoundData.size)
                audioTrack.play()
                // Let it play, then release
                Thread.sleep(15)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio output errors
            }
        }
    }
}

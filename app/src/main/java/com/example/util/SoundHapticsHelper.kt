package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.Executors

enum class CyberSoundType {
    TAP, STEP_DOWN, CONFIRM, WARN
}

class SoundHapticsHelper(context: Context) {
    private val appContext = context.applicationContext
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "TakaVault-SoundHaptics").apply { isDaemon = true }
    }

    private var toneGenerator: ToneGenerator? = null

    private val vibrator: Vibrator? by lazy(LazyThreadSafetyMode.NONE) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    private val tapEffect: VibrationEffect? by lazy(LazyThreadSafetyMode.NONE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
        } else null
    }

    private val confirmEffect: VibrationEffect? by lazy(LazyThreadSafetyMode.NONE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect.createWaveform(longArrayOf(0, 16, 30, 22), -1)
        } else null
    }

    init {
        executor.execute {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 40)
            } catch (_: Exception) {
                toneGenerator = null
            }
        }
    }

    fun play(type: CyberSoundType, enabled: Boolean) {
        if (!enabled) return
        executor.execute {
            try {
                vibrateInternal(type)
                val tone = when (type) {
                    CyberSoundType.TAP -> ToneGenerator.TONE_PROP_BEEP
                    CyberSoundType.STEP_DOWN -> ToneGenerator.TONE_PROP_NACK
                    CyberSoundType.CONFIRM -> ToneGenerator.TONE_PROP_ACK
                    CyberSoundType.WARN -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
                }
                val duration = when (type) {
                    CyberSoundType.CONFIRM -> 120
                    CyberSoundType.WARN -> 140
                    else -> 35
                }
                toneGenerator?.startTone(tone, duration)
            } catch (_: Exception) {
                // Safe ignore on environments without audio/vibrator hardware
            }
        }
    }

    private fun vibrateInternal(type: CyberSoundType) {
        try {
            val vib = vibrator ?: return
            if (vib.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = if (type == CyberSoundType.CONFIRM) confirmEffect else tapEffect
                    if (effect != null) vib.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(12)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun release() {
        executor.execute {
            try {
                toneGenerator?.release()
                toneGenerator = null
            } catch (_: Exception) {
            }
        }
        executor.shutdown()
    }
}

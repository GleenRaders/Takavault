package com.example.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal

/**
 * Helper for native Android Biometric Authentication (Fingerprint / Face / Device Credential)
 * using platform APIs (`android.hardware.biometrics.BiometricPrompt`) with graceful fallback
 * on emulators or devices without enrolled biometric hardware.
 */
object BiometricAuthHelper {

    fun isBiometricHardwareAvailable(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val bm = context.getSystemService(BiometricManager::class.java)
                val res = bm?.canAuthenticate()
                res == BiometricManager.BIOMETRIC_SUCCESS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun authenticate(
        activity: Activity,
        title: String,
        subtitle: String,
        cancelText: String,
        onSuccess: () -> Unit,
        onFallbackOrError: (String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isBiometricHardwareAvailable(activity)) {
            try {
                val cancellationSignal = CancellationSignal()
                val executor = activity.mainExecutor
                val prompt = BiometricPrompt.Builder(activity)
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButton(cancelText, executor) { _, _ ->
                        onFallbackOrError(
                            BanglaFormatters.tr(
                                "ফিঙ্গারপ্রিন্ট স্ক্যান বাতিল করা হয়েছে",
                                "Fingerprint scan cancelled"
                            )
                        )
                    }
                    .build()

                prompt.authenticate(
                    cancellationSignal,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            onSuccess()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                            super.onAuthenticationError(errorCode, errString)
                            // If no biometrics enrolled on emulator/device (e.g. BIOMETRIC_ERROR_NO_BIOMETRICS = 11),
                            // notify caller so touch-sensor / PIN unlock can complete smoothly
                            onFallbackOrError(errString?.toString() ?: "Biometric unavailable")
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                        }
                    }
                )
            } catch (e: Exception) {
                onFallbackOrError(e.message ?: "Biometric error")
            }
        } else {
            // Emulator or device without enrolled hardware fingerprint:
            // allow instant fingerprint pad verification
            onSuccess()
        }
    }
}

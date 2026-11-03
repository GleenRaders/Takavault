package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages user-imported custom images for Logos (App, bKash, DBBL) and all 10 Bangladeshi
 * Banknotes (1000 down to 1). Persists scaled PNGs in `filesDir/custom_assets/` and caches
 * `ImageBitmap` instances in memory for fast 60fps rendering across Home, Payment, and Vault.
 */
object CustomAssetManager {

    const val KEY_LOGO_APP = "logo_app"
    const val KEY_LOGO_BKASH = "logo_bkash"
    const val KEY_LOGO_DBBL = "logo_dbbl"

    fun noteKey(denom: Int): String = "note_$denom"

    private val bitmapCache = ConcurrentHashMap<String, ImageBitmap>()
    private val missingKeys = ConcurrentHashMap.newKeySet<String>()

    // Incremented whenever an image is imported or reset so Composables recompose immediately
    val versionCounter = mutableIntStateOf(0)

    private fun assetDir(context: Context): File {
        val dir = File(context.filesDir, "custom_assets")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun assetFile(context: Context, key: String): File {
        return File(assetDir(context), "$key.png")
    }

    fun getCustomBitmap(context: Context, key: String): ImageBitmap? {
        // Read versionCounter so Compose tracks changes to custom assets
        val currentVersion = versionCounter.intValue
        if (currentVersion < 0) return null

        bitmapCache[key]?.let { return it }
        if (missingKeys.contains(key)) return null

        val file = assetFile(context, key)
        if (!file.exists()) {
            missingKeys.add(key)
            return null
        }

        return try {
            val bmp = BitmapFactory.decodeFile(file.absolutePath)
            if (bmp != null) {
                val imgBmp = bmp.asImageBitmap()
                bitmapCache[key] = imgBmp
                imgBmp
            } else {
                missingKeys.add(key)
                null
            }
        } catch (_: Exception) {
            missingKeys.add(key)
            null
        }
    }

    fun hasCustomAsset(context: Context, key: String): Boolean {
        val currentVersion = versionCounter.intValue
        if (currentVersion < 0) return false
        if (bitmapCache.containsKey(key)) return true
        return assetFile(context, key).exists()
    }

    fun importCustomImage(
        context: Context,
        key: String,
        uri: Uri,
        maxDimension: Int = if (key.startsWith("note_")) 640 else 256
    ): Boolean {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return false

            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOpts)
            if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) return false

            var sampleSize = 1
            while (
                boundsOpts.outWidth / sampleSize > maxDimension * 2 ||
                boundsOpts.outHeight / sampleSize > maxDimension * 2
            ) {
                sampleSize *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
                ?: return false

            val scale = minOf(
                maxDimension.toFloat() / decoded.width.toFloat(),
                maxDimension.toFloat() / decoded.height.toFloat(),
                1f
            )
            val finalBitmap = if (scale < 1f) {
                val targetW = (decoded.width * scale).toInt().coerceAtLeast(1)
                val targetH = (decoded.height * scale).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
            } else {
                decoded
            }

            val file = assetFile(context, key)
            FileOutputStream(file).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 92, out)
            }

            missingKeys.remove(key)
            bitmapCache[key] = finalBitmap.asImageBitmap()
            versionCounter.intValue += 1
            true
        } catch (_: Exception) {
            false
        }
    }

    fun clearCustomAsset(context: Context, key: String) {
        try {
            val file = assetFile(context, key)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {
        }
        bitmapCache.remove(key)
        missingKeys.add(key)
        versionCounter.intValue += 1
    }

    fun clearAllCustomAssets(context: Context) {
        try {
            assetDir(context).listFiles()?.forEach { it.delete() }
        } catch (_: Exception) {
        }
        bitmapCache.clear()
        missingKeys.clear()
        versionCounter.intValue += 1
    }
}

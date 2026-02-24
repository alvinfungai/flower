package com.alvinfungai.flower.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale


class ImageCompressor(private val context: Context) {
    // Crop image dimens and compress image quality to reduce file size
    fun compressImage(uri: Uri, maxWidth: Int = 500, quality: Int = 75): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Calculate new dimens but maintain aspect ratio
            val ratio = originalBitmap.height.toFloat() / originalBitmap.width.toFloat()
            val newHeight = (maxWidth * ratio).toInt()
            val scaledBitmap = originalBitmap.scale(maxWidth, newHeight)
            val outputStream = ByteArrayOutputStream()
            // JPEG is best for photos; PNG doesn't support quality compression well
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val result = outputStream.toByteArray()

            // clean up memory
            originalBitmap.recycle()
            scaledBitmap.recycle()

            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
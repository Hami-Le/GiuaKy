package com.example.gkapp

import android.content.Context
import android.net.Uri

fun getBase64FromUri(uri: Uri, context: Context): String {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            val maxImageSize = 300f
            val ratio = Math.min(maxImageSize / originalBitmap.width, maxImageSize / originalBitmap.height)
            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(
                originalBitmap,
                Math.round(ratio * originalBitmap.width),
                Math.round(ratio * originalBitmap.height),
                true
            )
            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, outputStream)
            "base64," + android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
        } ?: ""
    } catch (e: Exception) { "" }
}
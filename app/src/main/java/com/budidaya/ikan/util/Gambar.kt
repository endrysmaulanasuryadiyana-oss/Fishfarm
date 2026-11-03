package com.budidaya.ikan.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

/** Mengolah logo dari galeri: diperkecil, lalu disimpan sebagai teks Base64 (ikut ter-backup). */
object Gambar {

    fun keBase64(context: Context, uri: Uri, maks: Int = 512): String? = try {
        val asli: Bitmap? = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }
        if (asli == null) {
            null
        } else {
            val skala = minOf(1f, maks.toFloat() / maxOf(asli.width, asli.height))
            val w = (asli.width * skala).toInt().coerceAtLeast(1)
            val h = (asli.height * skala).toInt().coerceAtLeast(1)
            val kecil = Bitmap.createScaledBitmap(asli, w, h, true)
            // latar putih agar logo PNG transparan tetap rapi saat disimpan sebagai JPEG
            val hasil = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            Canvas(hasil).apply {
                drawColor(Color.WHITE)
                drawBitmap(kecil, 0f, 0f, null)
            }
            val out = ByteArrayOutputStream()
            hasil.compress(Bitmap.CompressFormat.JPEG, 88, out)
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        }
    } catch (e: Exception) {
        null
    }

    fun dariBase64(teks: String): ImageBitmap? = try {
        val b = Base64.decode(teks, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

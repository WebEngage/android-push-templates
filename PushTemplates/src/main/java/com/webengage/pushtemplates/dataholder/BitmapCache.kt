package com.webengage.pushtemplates.dataholder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*

class BitmapCache(context: Context) {
    private var cacheDir: File = context.cacheDir

    fun writeToCache(
        url: String, bitmap: Bitmap
    ) {
        val fileName = File(cacheDir, "TEMP_${url}")
        try {
            val out = FileOutputStream(
                fileName
            )
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                100, out
            )
            out.flush()
            out.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getBitmapFromCache(url: String): Bitmap? {
        val fileName = File(cacheDir, "TEMP_${url}")
        var fis: FileInputStream? = null
        var bitmap: Bitmap? = null
        try {
            fis = FileInputStream(fileName)
            bitmap = BitmapFactory.decodeStream(fis)
        } catch (e: FileNotFoundException) {
        } finally {
            fis?.close()
        }
        return bitmap
    }
}
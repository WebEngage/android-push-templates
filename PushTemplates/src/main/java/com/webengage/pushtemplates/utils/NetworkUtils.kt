package com.webengage.pushtemplates.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL

class NetworkUtils {

    suspend fun getBitmapFromURL(urlString: String): Bitmap? {
        var bitmap: Bitmap?

        withContext(Dispatchers.IO) {
            bitmap = downloadBitmap(urlString)
        }
        return bitmap
    }


    /**
     * Downloads the bitmap from the given URL
     */
    private fun downloadBitmap(urlString: String): Bitmap? {
        val inputStream: InputStream?
        var bitmap: Bitmap? = null
        var bitmapSampled: Bitmap? = null

        try {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.doInput = true
            connection.connect()
            if (connection.contentLength > 0) {
                Log.d("PushTemplates", "Downloading image from network")
                inputStream = connection.inputStream
                bitmap = BitmapFactory.decodeStream(inputStream)
                Log.d("PushTemplates", "Bitmap size = ${bitmap.byteCount}")
                if ((bitmap.byteCount > Constants.REMOTE_VIEW_MAX_SIZE)
                    && Build.VERSION.SDK_INT > Build.VERSION_CODES.R
                ) {
                    Log.d("PushTemplates", "Downscaling image")
                    val options = BitmapFactory.Options()

                    options.inSampleSize =
                        ImageUtils().getSampleSize(
                            Constants.REMOTE_VIEW_MAX_SIZE,
                            options.outHeight,
                            options.outWidth
                        )

                    options.inJustDecodeBounds = false
                    bitmapSampled = BitmapFactory.decodeStream(inputStream, null, options)
                }
                inputStream!!.close()
            }
        } catch (ex: Exception) {
            Log.e("PushTemplates", ex.toString())
        }
        return bitmapSampled ?: bitmap
    }

    /**
     * Get String from inputStream and close the inputStream
     */
    fun readEntireStream(inputStream: InputStream): String? {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line: String? = ""
        return try {
            while (bufferedReader.readLine().also { line = it } != null) {
                sb.append(line)
            }
//            inputStream.close()
            sb.toString()
        } catch (e: java.lang.Exception) {
            null
        }
    }
}

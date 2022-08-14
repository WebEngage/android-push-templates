package com.webengage.pushtemplates.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.InputStream
import java.net.URL


class NetworkUtils {

    fun getBitmapFromURL(urlString : String): Bitmap?{
        var inputStream: InputStream?
        var bitmap: Bitmap? = null
        try {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.doInput = true
            connection.connect()
            if (connection.contentLength > 0) {
                inputStream = connection.inputStream
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
            }
        } catch (ex: Exception) {
            Log.e("PushTemplates", ex.toString())
        }
        return bitmap
    }


}
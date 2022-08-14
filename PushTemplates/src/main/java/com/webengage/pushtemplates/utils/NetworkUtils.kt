package com.webengage.pushtemplates.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.Log
import com.webengage.pushtemplates.dataholder.BitmapCache
import kotlinx.coroutines.*
import java.io.InputStream
import java.net.URL
import java.net.URLEncoder

class NetworkUtils {

    fun getBitmapFromURL(context: Context, urlString: String): Bitmap? {
        val bitmapCache = BitmapCache(context)
        val urlFileName = URLEncoder.encode(urlString, Charsets.UTF_8.toString())
        val cacheBitmap = bitmapCache.getBitmapFromCache(urlFileName)
        if (cacheBitmap != null) {
            return cacheBitmap
        }
        var bitmap: Bitmap? = null
        CoroutineScope(Dispatchers.IO).launch {
            bitmap = downloadBitmap(urlString)
            if (bitmap != null)
                bitmapCache.writeToCache(urlFileName, bitmap!!)
        }
        return bitmap
    }

    fun getBitmapArrayList(context: Context, urlList: ArrayList<String>): ArrayList<Bitmap?> {
        val bitmapList = ArrayList<Bitmap?>()
        for (iterator in 0..urlList.size) {
            if (!TextUtils.isEmpty(urlList[iterator])) {
                val bitmapFromUrl = getBitmapFromURL(context, urlList[iterator])
                bitmapList.add(bitmapFromUrl)
            }
        }
        return bitmapList
    }

    private fun downloadBitmap(urlString: String): Bitmap? {
        val inputStream: InputStream?
        var bitmap: Bitmap? = null
        try {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.doInput = true
            connection.connect()
            if (connection.contentLength > 0) {
                Log.d("PushTemplates", "Downloading image from network")
                inputStream = connection.inputStream
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream!!.close()
            }
        } catch (ex: Exception) {
            Log.e("PushTemplates", ex.toString())
        }
        return bitmap
    }
}
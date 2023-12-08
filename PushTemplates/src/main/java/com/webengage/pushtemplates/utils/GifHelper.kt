package com.webengage.pushtemplates.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.webengage.pushtemplates.utils.animpush.UpshotGifDecoder
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URL
import java.nio.charset.StandardCharsets


class GifHelper {
    suspend fun downloadGif(urlString: String): InputStream? {
        var inputStream: InputStream? = null

        try {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.doInput = true
            connection.connect()
            if (connection.contentLength > 0) {
                Log.d("PushTemplates", "Downloading gif from network")
                inputStream = connection.inputStream
                return inputStream;
            }
        } catch (ex: Exception) {
            Log.e("PushTemplates", ex.toString())
        }
        return inputStream
    }

    fun decodeGif(context: Context, string: InputStream) : ArrayList<Bitmap?>{
        val decoder = UpshotGifDecoder.decode(string)
        val bitmapList = ArrayList<Bitmap?> ()
        var looper = decoder.frameCount - 1;
        while (looper > 0){
            bitmapList.add(decoder.getFrame(decoder.frameCount - looper))
            looper--
        }
        Log.e("PushTemplates", "decoder.frameCount ${decoder.frameCount}")
        Log.e("PushTemplates", "decoder.loopCount ${decoder.loopCount}")
        return bitmapList
    }

}
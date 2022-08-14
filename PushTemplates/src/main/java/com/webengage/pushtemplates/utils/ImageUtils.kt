package com.webengage.pushtemplates.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class ImageUtils {

    private fun getSampledBitmap(context: Context, bitmap: Bitmap, maxSize: Int): Bitmap {
        val bitmapStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream)
        val imageByteArray = bitmapStream.toByteArray()
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size, options)

        options.inSampleSize = getSampleSize(
            maxSize,
            options.outHeight,
            options.outWidth
        )

        options.inJustDecodeBounds = false

        return BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size, options)
    }

    fun getSampleSize(
        maxSize: Int,
        currentHeightInPixels: Int,
        currentWidthInPixels: Int
    ): Int {
        var sampleSize = 1
        var bitmapSize =
            currentHeightInPixels * currentWidthInPixels * 4 / (sampleSize * sampleSize)
        while (maxSize < bitmapSize) {
            sampleSize *= 2
            bitmapSize /= 4
        }
        return sampleSize
    }

    fun getSampledBitmapList(
        context: Context,
        bitmapList: ArrayList<Bitmap>,
        availableSize: Int
    ): ArrayList<Bitmap> {
        val sampledBitmapList = ArrayList<Bitmap>()
        for (iterator in bitmapList) {
            sampledBitmapList.add(
                getSampledBitmap(
                    context,
                    iterator,
                    availableSize / bitmapList.size
                )
            )
        }
        return sampledBitmapList
    }

}
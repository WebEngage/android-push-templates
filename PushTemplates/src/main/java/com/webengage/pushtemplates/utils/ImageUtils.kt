package com.webengage.pushtemplates.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.text.TextUtils
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.sdk.android.utils.WebEngageConstant
import java.io.ByteArrayOutputStream

class ImageUtils {

    /**
     * Returns the sampled Bitmap Image to fit under the byte limit provided by {maxSize} parameter
     */
    private fun getSampledBitmap(bitmap: Bitmap?, maxSize: Int): Bitmap {
        val bitmapStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream)
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

    /**
     *Returns the amount of sampling required for the bitmap to fit under the byte limit
     *  provided by {maxSize} parameter
     */
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

    /**
     * Returns list of bitmap which can be rendered in the notification.
     */
    private fun getSampledBitmapList(
        bitmapList: ArrayList<Bitmap?>,
    ): ArrayList<Bitmap?> {
        //return bitmaps without sampling if Android version is less than Android 10
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            return bitmapList

        var totalBitmapSize = 0
        for (iterator in bitmapList) {
            if (iterator != null)
                totalBitmapSize += iterator.byteCount
        }

        //return bitmaps without sampling if under max limit
        if (totalBitmapSize < Constants.REMOTE_VIEW_MAX_SIZE)
            return bitmapList


        val sampledBitmapList = ArrayList<Bitmap?>()
        var countOfSampledBitmaps = 0
        for (iterator in 0 until bitmapList.size) {
            if (totalBitmapSize > Constants.REMOTE_VIEW_MAX_SIZE) {
                val bitmap = getSampledBitmap(
                    bitmapList[iterator],
                    Constants.REMOTE_VIEW_MAX_SIZE / bitmapList.size
                )
                sampledBitmapList.add(bitmap)
                totalBitmapSize -= bitmap.byteCount
                countOfSampledBitmaps++
            } else break
        }
        for (iterator in (countOfSampledBitmaps) until bitmapList.size) {
            sampledBitmapList.add(bitmapList[iterator])
        }

        return sampledBitmapList
    }

    suspend fun getBitmapArrayList(
        pushNotificationData: PushNotificationData
    ): ArrayList<Bitmap?> {
        val urlList = ArrayList<String>()
        if (pushNotificationData.style == WebEngageConstant.STYLE.BIG_PICTURE) {
            urlList.add(pushNotificationData.bigPictureStyleData.bigPictureUrl)
        }

        val bitmapList = ArrayList<Bitmap?>()
        for (iterator in 0 until urlList.size) {
            if (!TextUtils.isEmpty(urlList[iterator])) {
                val bitmapFromUrl = NetworkUtils().getBitmapFromURL(urlList[iterator])
                bitmapList.add(bitmapFromUrl)
            }
        }
        return getSampledBitmapList(
            bitmapList,
        )
    }

}
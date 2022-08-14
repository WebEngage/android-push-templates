package com.webengage.pushtemplates.templates

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.webengage.pushtemplates.utils.Constants
import com.webengage.pushtemplates.services.NotificationService
import com.webengage.pushtemplates.models.TimerStyleData
import com.webengage.sdk.android.WebEngage.startService
import com.webengage.sdk.android.actions.render.PushNotificationData

class ProgressBarRenderer {

    private lateinit var context: Context
    private lateinit var mBuilder: NotificationCompat.Builder
    private lateinit var pushData: TimerStyleData
    private var whenTime: Long = 0

    fun onRender(mContext: Context?, pushNotificationData: PushNotificationData?): Boolean {
        this.context = mContext!!
        this.pushData = TimerStyleData(mContext, pushNotificationData!!)
        this.mBuilder =
            NotificationCompat.Builder(mContext, pushNotificationData.channelId)
        this.whenTime = System.currentTimeMillis()

        //If the provided future time is less that the system time, then do not render notification
        if (pushData.futureTime < System.currentTimeMillis())
            return false
        attachToService(mContext, pushData)
        return true

    }

    /**
    Create a foreground service to periodically update the notification progress bar at set intervals.
     */
    private fun attachToService(context: Context, pushData: TimerStyleData?) {
        val intent = Intent(context, NotificationService::class.java)
        intent.action = Constants.PROGRESS_BAR_ACTION
        intent.putExtra(Constants.PAYLOAD, pushData!!.pushNotification.pushPayloadJSON.toString())
        intent.putExtra(Constants.WHEN_TIME, whenTime)
        startService(intent, context)
    }

}
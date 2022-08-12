package com.webengage.pushtemplates.callbacks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.webengage.pushtemplates.utils.Constants
import com.webengage.pushtemplates.services.NotificationService
import com.webengage.pushtemplates.models.TimerStyleData
import com.webengage.sdk.android.WebEngage.startService
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.sdk.android.callbacks.CustomPushRender
import com.webengage.sdk.android.callbacks.CustomPushRerender

class ProgressBarRenderer : CustomPushRender, CustomPushRerender {

    private lateinit var context: Context
    private lateinit var mBuilder: NotificationCompat.Builder
    private lateinit var pushData: TimerStyleData
    private var whenTime : Long = 0

    override fun onRender(mContext: Context?, pushNotificationData: PushNotificationData?): Boolean {

        this.context = mContext!!
        this.pushData = TimerStyleData(mContext, pushNotificationData!!)
        this.mBuilder =
            NotificationCompat.Builder(mContext, pushNotificationData.channelId)
        this.whenTime = System.currentTimeMillis()
        if(pushData.timerTime < System.currentTimeMillis())
            return false
        initRender(mContext, pushNotificationData)
        return true

    }

    override fun onRerender(
        context: Context?,
        pushNotificationData: PushNotificationData?,
        extras: Bundle?
    ): Boolean {
        //TODO("Implement when the notification is supposed to me rendered with new content")
        return false
    }

    fun initRender(mContext: Context,pushNotificationData: PushNotificationData?) {
        attachToService(mContext, pushData)
    }


    private fun attachToService(context: Context, pushData: TimerStyleData?) {
        var intent = Intent(context, NotificationService::class.java)
        intent.action = Constants.PROGRESSBAR_ACTION
        intent.putExtra(Constants.PAYLOAD,pushData!!.pushNotification.pushPayloadJSON.toString())
        intent.putExtra(Constants.WHEN_TIME,whenTime)
        startService(intent,context)
    }

}
package com.webengage.pushtemplates.models

import android.content.Context
import com.webengage.sdk.android.actions.render.PushNotificationData
import android.os.Bundle
import com.webengage.pushtemplates.utils.Constants

class TimerStyleData(context: Context?, pushNotificationData : PushNotificationData) {
    var timerTime : Long = System.currentTimeMillis() + 1*Constants.MINUTE
    var pushNotification : PushNotificationData = pushNotificationData
    var timerFormat = "%s"
    init {
        val customData = pushNotification.customData
        if (customData.containsKey(Constants.TIMER_DATE) && customData[Constants.TIMER_DATE] != null) {
            timerTime =  customData.getString(Constants.TIMER_DATE)!!.toLong()
        }
        if (customData.containsKey(Constants.TIMER_FORMAT) && customData[Constants.TIMER_FORMAT] != null) {
            timerFormat =  customData.getString(Constants.TIMER_FORMAT)!!
        }
    }
}
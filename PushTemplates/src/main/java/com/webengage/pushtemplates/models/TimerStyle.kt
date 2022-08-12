package com.webengage.pushtemplates.models

import android.content.Context
import com.webengage.sdk.android.actions.render.PushNotificationData
import android.os.Bundle
import com.webengage.pushtemplates.utils.Constants

class TimerStyle( context: Context?, pushNotificationData : PushNotificationData) {
    var timerData: Bundle
    var timerFormat : String = "%s"
    var timerTime : Long = System.currentTimeMillis() + 1*Constants.MINUTE
    var timerType : String = "DISMISS"
    var pushNotification : PushNotificationData = pushNotificationData
    private fun setTimerDataBundle(customData: Bundle): Bundle {
        val timerBundle = Bundle()
        if (customData.containsKey(Constants.TIMER_TITLE) && customData[Constants.TIMER_TITLE] != null) {
            timerBundle.putString(
                Constants.TIMER_TITLE,
                customData.getString(Constants.TIMER_TITLE)
            )
            timerType = "NOTIFICATION"
        }
        else{
            timerType = "DISMISS"
        }

        if (customData.containsKey(Constants.TIMER_DESCRIPTION) && customData[Constants.TIMER_DESCRIPTION] != null) {
            timerBundle.putString(
                Constants.TIMER_DESCRIPTION,
                customData.getString(Constants.TIMER_DESCRIPTION)
            )
        }
        if (customData.containsKey(Constants.TIMER_SUMMARY) && customData[Constants.TIMER_SUMMARY] != null) {
            timerBundle.putString(
                Constants.TIMER_SUMMARY,
                customData.getString(Constants.TIMER_SUMMARY)
            )
        }
        return timerBundle
    }

    init {
        val customData = pushNotification.customData
        timerData = setTimerDataBundle(customData)

        if(customData.containsKey("format") && customData.getString("format") != null)
            timerFormat = customData.getString("format")!!

        if (customData.containsKey(Constants.TIMER_DATE) && customData[Constants.TIMER_DATE] != null) {
            timerTime =  customData.getString(Constants.TIMER_DATE)!!.toLong()
        }
        if(customData.containsKey(Constants.TIMER_TYPE) && customData.getString(Constants.TIMER_TYPE) != null){
            timerType = customData.getString(Constants.TIMER_TYPE)!!
        }

    }
}
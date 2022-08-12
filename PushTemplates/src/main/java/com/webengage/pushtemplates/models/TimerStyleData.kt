package com.webengage.pushtemplates.models

import android.content.Context
import android.graphics.Color
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.pushtemplates.utils.Constants

class TimerStyleData(context: Context?, pushNotificationData: PushNotificationData) {

    /** Future time provided in custom data */
    var futureTime: Long = System.currentTimeMillis() + 1 * Constants.MINUTE

    /** PushNotificationData object provided by the WebEngage SDK in the callbacks */
    var pushNotification: PushNotificationData = pushNotificationData

    /** Count Down Timer format provided in custom data */
    var timerFormat = "%s"

    var timerColor : Int? = null

    init {
        val customData = pushNotification.customData
        if (customData.containsKey(Constants.FUTURE_TIME) && customData[Constants.FUTURE_TIME] != null) {
            futureTime = customData.getString(Constants.FUTURE_TIME)!!.toLong()
        }
        if (customData.containsKey(Constants.TIMER_FORMAT) && customData[Constants.TIMER_FORMAT] != null) {
            timerFormat = customData.getString(Constants.TIMER_FORMAT)!!
        }

        if (customData.containsKey(Constants.TIMER_COLOR) && customData[Constants.TIMER_COLOR] != null) {
            timerColor = Color.parseColor(customData.getString(Constants.TIMER_COLOR)!!)
        }
    }
}
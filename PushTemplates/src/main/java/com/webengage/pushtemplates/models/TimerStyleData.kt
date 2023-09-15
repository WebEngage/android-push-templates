package com.webengage.pushtemplates.models

import android.graphics.Color
import android.util.Log
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.pushtemplates.utils.Constants
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException

class TimerStyleData(pushNotificationData: PushNotificationData) {

    /** Future time provided in custom data */
    var futureTime: Long = System.currentTimeMillis() + 1 * Constants.MINUTE

    /** PushNotificationData object provided by the WebEngage SDK in the callbacks */
    var pushNotification: PushNotificationData = pushNotificationData

    /** Count Down Timer format provided in custom data */
    var timerFormat = "%s"

    /** color for the CountDown Timer provided in custom data */
    var timerColor: Int? = null

    var progressBarColor: Int? = null

    var progressBarBackgroundColor: Int? = null

    var showDismissCTA: Boolean = false

    //duration in min
    var duration: Int? = null

    var lockscreenVisibility: String? = null

    init {
        val customData = pushNotification.customData
        if (customData.containsKey(Constants.FUTURE_TIME) && customData[Constants.FUTURE_TIME] != null) {
            try {
                futureTime = customData.getString(Constants.FUTURE_TIME)!!.toLong()
            }
            catch (exception :NumberFormatException){
                Log.d("PushTemplates","FUTURE_TIME is not a numerical value")
            }
        }

        if(customData.containsKey(Constants.DURATION)) {
            try {
                duration = customData.getString(Constants.DURATION)!!.toInt()
            }
            catch (exception :NumberFormatException){
                Log.d("PushTemplates","DURATION is not a int value")
            }
            //calculating future time if duration is present
            if(duration != null){
                futureTime = System.currentTimeMillis() + (duration!! * Constants.MINUTE)
            }
        }

        if (customData.containsKey(Constants.TIMER_FORMAT) && customData[Constants.TIMER_FORMAT] != null) {
            timerFormat = customData.getString(Constants.TIMER_FORMAT)!!
        }

        if (customData.containsKey(Constants.TIMER_COLOR) && customData[Constants.TIMER_COLOR] != null) {
            try {
                timerColor = Color.parseColor(customData.getString(Constants.TIMER_COLOR)!!)
            }
            catch (e : IllegalArgumentException){
                Log.d("PushTemplates","PROGRESS_BAR_BACKGROUND_COLOR is not a hex color value")
            }
        }

        if (customData.containsKey(Constants.PROGRESS_BAR_COLOR) && customData[Constants.PROGRESS_BAR_COLOR] != null) {
            try {
                progressBarColor =
                    Color.parseColor(customData.getString(Constants.PROGRESS_BAR_COLOR)!!)
            }
            catch (e : IllegalArgumentException){
                Log.d("PushTemplates","PROGRESS_BAR_BACKGROUND_COLOR is not a hex color value")
            }
        }

        if (customData.containsKey(Constants.PROGRESS_BAR_BACKGROUND_COLOR) && customData[Constants.PROGRESS_BAR_BACKGROUND_COLOR] != null) {
            try {
                progressBarBackgroundColor =
                    Color.parseColor(customData.getString(Constants.PROGRESS_BAR_BACKGROUND_COLOR)!!)
            }
            catch (e : IllegalArgumentException){
                Log.d("PushTemplates","PROGRESS_BAR_BACKGROUND_COLOR is not a hex color value")
            }
        }

        if (customData.containsKey(Constants.SHOW_DISMISS_CTA) && customData[Constants.SHOW_DISMISS_CTA] != null) {
            showDismissCTA =
                customData.getString(Constants.SHOW_DISMISS_CTA)!!.toBoolean()
        }

        if(pushNotificationData.customData.containsKey(Constants.LOCK_SCREEN_VISIBILITY)){
            lockscreenVisibility = pushNotificationData.customData.getString(Constants.LOCK_SCREEN_VISIBILITY, null)
        }
    }
}
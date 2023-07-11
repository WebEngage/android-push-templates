package com.webengage.pushtemplates.models

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.webengage.pushtemplates.R
import com.webengage.pushtemplates.utils.Constants
import com.webengage.sdk.android.actions.render.PushNotificationData
import java.lang.Exception

class BannerStyleData(context: Context, pushNotificationData: PushNotificationData) {
    //use this to set how the image should be shown in expanded mode
    var expandedMode: String = Constants.DEFAULT_MODE

    //use this to set how the image should be shown in collapsed mode
    var collapsedMode: String = Constants.DEFAULT_MODE

    //use this to set image in collapsed mode.
    var collapsedImageURL: String? = null

    //Use this for appName and time. Title, desc, summary colors should come from the HTML string itself
    var fontColor: Int = context.resources.getColor(R.color.we_hard_black, context.theme)

    // PushNotificationData object provided by the WebEngage SDK in the callbacks
    var pushNotification: PushNotificationData = pushNotificationData

    var showDismissCTA: Boolean = false

    //For push layout selection i.e. 1 - em=full_bg cm=default
    var layout: Int = 1

    init {

        collapsedImageURL =
            pushNotificationData.customData.getString(Constants.COLLAPSED_MODE_IMAGE_URL, null)


        try {
            layout =
                pushNotificationData.customData.getString(Constants.LAYOUT)?.toInt()?:1
        } catch (ex: Exception) {
            Log.d("PushTemplates", "LAYOUT is not a int value")
        }

        when(layout){
            1 -> {
                expandedMode = Constants.FULL_BACKGROUND_MODE
                collapsedMode = Constants.DEFAULT_MODE
            }
            2 -> {
                expandedMode = Constants.FULL_BACKGROUND_MODE
                collapsedMode = Constants.FULL_BACKGROUND_MODE
            }
            3 -> {
                expandedMode = Constants.FULL_BACKGROUND_MODE
                collapsedMode = Constants.HALF_BACKGROUND_MODE
            }
            4 -> {
                expandedMode = Constants.DEFAULT_MODE
                collapsedMode = Constants.HALF_BACKGROUND_MODE
            }
            5 -> {
                expandedMode = Constants.DEFAULT_MODE
                collapsedMode = Constants.FULL_BACKGROUND_MODE
            }
        }

        try {
            fontColor =
                Color.parseColor(pushNotificationData.customData.getString(Constants.FONT_COLOR))
        } catch (ex: Exception) {
            Log.d("PushTemplates", "FONT_COLOR is not a hex color value")
        }

        if (pushNotificationData.customData.containsKey(Constants.SHOW_DISMISS_CTA)) {
            showDismissCTA =
                pushNotificationData.customData.getString(Constants.SHOW_DISMISS_CTA)!!.toBoolean()
        }
    }
}
package com.webengage.pushtemplates.models

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.webengage.pushtemplates.R
import com.webengage.pushtemplates.utils.Constants
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.sdk.android.utils.WebEngageConstant

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

    var lockscreenVisibility: String? = null

    init {

        collapsedImageURL =
            pushNotificationData.customData.getString(Constants.COLLAPSED_MODE_IMAGE_URL, null)

        when(pushNotificationData.customData.getString(Constants.TEMPLATE_TYPE)){
            Constants.BANNER_1 -> {
                expandedMode = Constants.FULL_BACKGROUND_MODE
                collapsedMode = Constants.DEFAULT_MODE
            }
            Constants.BANNER_2 -> {
                expandedMode = Constants.FULL_BACKGROUND_MODE
                collapsedMode = Constants.FULL_BACKGROUND_MODE
            }
            Constants.BANNER_3 -> {
                expandedMode = Constants.FULL_BACKGROUND_MODE
                collapsedMode = Constants.HALF_BACKGROUND_MODE
            }
            Constants.BANNER_4 -> {
                expandedMode = Constants.DEFAULT_MODE
                collapsedMode = Constants.HALF_BACKGROUND_MODE
            }
            Constants.BANNER_5 -> {
                expandedMode = Constants.DEFAULT_MODE
                collapsedMode = Constants.FULL_BACKGROUND_MODE
            }
            else -> {
                expandedMode = Constants.DEFAULT_MODE
                collapsedMode = Constants.DEFAULT_MODE
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

        if(pushNotificationData.customData.containsKey(Constants.LOCK_SCREEN_VISIBILITY)){
            lockscreenVisibility = pushNotificationData.customData.getString(Constants.LOCK_SCREEN_VISIBILITY, null)
        }
    }
}
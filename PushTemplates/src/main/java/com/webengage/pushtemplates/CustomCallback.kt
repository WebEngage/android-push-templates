package com.webengage.pushtemplates

import android.content.Context
import android.os.Bundle
import com.webengage.pushtemplates.templates.BannerRenderer
import com.webengage.pushtemplates.templates.CountDownRenderer
import com.webengage.pushtemplates.templates.ProgressBarRenderer
import com.webengage.pushtemplates.utils.Constants
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.sdk.android.callbacks.CustomPushRender
import com.webengage.sdk.android.callbacks.CustomPushRerender

open class CustomCallback : CustomPushRerender, CustomPushRender {

    /**
     * Callback received for Pending Intent created using
     * PendingIntentFactory.constructRerenderPendingIntent()
     * Render and return true if your template requires re-rendering of the notification.
     */
    override fun onRerender(
        context: Context?,
        pushNotificationData: PushNotificationData?,
        extras: Bundle?
    ): Boolean {

        return false
    }

    /**
     * Render the notification and return true.
     */
    override fun onRender(context: Context?, pushNotificationData: PushNotificationData?): Boolean {
        if (pushNotificationData!!.customData.containsKey(Constants.TEMPLATE_TYPE) &&
            pushNotificationData.customData.getString(Constants.TEMPLATE_TYPE)
                .equals(Constants.COUNTDOWN)
        )
            return CountDownRenderer().onRender(context, pushNotificationData)
        else
            if (pushNotificationData.customData.containsKey(Constants.TEMPLATE_TYPE) &&
                pushNotificationData.customData.getString(Constants.TEMPLATE_TYPE)
                    .equals(Constants.PROGRESS_BAR)
            )
                return ProgressBarRenderer().onRender(context, pushNotificationData)
            else if (pushNotificationData.customData.containsKey(Constants.TEMPLATE_TYPE) &&
                (pushNotificationData.customData.getString(Constants.TEMPLATE_TYPE)
                    .equals(Constants.BANNER_1) || pushNotificationData.customData.getString(Constants.TEMPLATE_TYPE)
                    .equals(Constants.BANNER_2) || pushNotificationData.customData.getString(Constants.TEMPLATE_TYPE)
                    .equals(Constants.BANNER_3) || pushNotificationData.customData.getString(Constants.TEMPLATE_TYPE)
                    .equals(Constants.BANNER_4) || pushNotificationData.customData.getString(Constants.TEMPLATE_TYPE)
                    .equals(Constants.BANNER_5))
            )
                return BannerRenderer().onRender(context!!, pushNotificationData)

        return false
    }
}
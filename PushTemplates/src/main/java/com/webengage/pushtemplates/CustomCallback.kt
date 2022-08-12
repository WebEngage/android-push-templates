package com.webengage.pushtemplates

import android.content.Context
import android.os.Bundle
import com.webengage.pushtemplates.templates.CountDownRenderer
import com.webengage.pushtemplates.templates.ProgressBarRenderer
import com.webengage.pushtemplates.utils.Constants
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.sdk.android.callbacks.CustomPushRender
import com.webengage.sdk.android.callbacks.CustomPushRerender

class CustomCallback : CustomPushRerender, CustomPushRender {

    override fun onRerender(
        context: Context?,
        pushNotificationData: PushNotificationData?,
        extras: Bundle?
    ): Boolean {
            return false
    }

    override fun onRender(context: Context?, pushNotificationData: PushNotificationData?): Boolean {
        if(pushNotificationData!!.customData.containsKey(Constants.TEMPLATE_TYPE) && pushNotificationData.customData.getString(
                Constants.TEMPLATE_TYPE
            ).equals(Constants.COUNTDOWN))
            return CountDownRenderer().onRender(context, pushNotificationData)
        else
            if(pushNotificationData.customData.containsKey(Constants.TEMPLATE_TYPE) && pushNotificationData.customData.getString(
                    Constants.TEMPLATE_TYPE
                ).equals(Constants.PROGRESS_BAR))
                return ProgressBarRenderer().onRender(context,pushNotificationData)

        return false
    }
}
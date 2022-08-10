package com.webengage.pushtemplates.Utils

import android.content.Context
import android.os.Bundle
import com.webengage.pushtemplates.CallBacks.CountDownRenderer
import com.webengage.pushtemplates.CallBacks.ProgressBarRenderer
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.sdk.android.callbacks.CustomPushRender
import com.webengage.sdk.android.callbacks.CustomPushRerender

class CustomCallback : CustomPushRerender, CustomPushRender {
    override fun onRerender(
        context: Context?,
        pushNotificationData: PushNotificationData?,
        extras: Bundle?
    ): Boolean {
        if(pushNotificationData!!.customData.containsKey(Constants.TYPE) && pushNotificationData.customData.getString(Constants.TYPE).equals(Constants.COUNTDOWN))
            return CountDownRenderer().onRerender(context, pushNotificationData, extras)
        else
            if(pushNotificationData.customData.containsKey(Constants.TYPE) && pushNotificationData.customData.getString(Constants.TYPE).equals(Constants.PROGRESS_BAR))
                return ProgressBarRenderer().onRerender(context,pushNotificationData,extras)
            return false
    }

    override fun onRender(context: Context?, pushNotificationData: PushNotificationData?): Boolean {
        if(pushNotificationData!!.customData.containsKey(Constants.TYPE) && pushNotificationData.customData.getString(Constants.TYPE).equals(Constants.COUNTDOWN))
            return CountDownRenderer().onRender(context, pushNotificationData)
        else
            if(pushNotificationData.customData.containsKey(Constants.TYPE) && pushNotificationData.customData.getString(Constants.TYPE).equals(Constants.PROGRESS_BAR))
                return ProgressBarRenderer().onRender(context,pushNotificationData)

        return false
    }
}
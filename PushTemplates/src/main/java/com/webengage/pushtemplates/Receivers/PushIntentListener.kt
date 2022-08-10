package com.webengage.pushtemplates.Receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.DataTypes.TimerStyle
import com.webengage.pushtemplates.Services.NotificationService
import com.webengage.pushtemplates.Utils.Constants
import com.webengage.sdk.android.PendingIntentFactory
import com.webengage.sdk.android.actions.render.PushNotificationData
import org.json.JSONObject

class PushIntentListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action.equals(Constants.DELETE_ACTION)) {
            Log.d("Timer", "Delete Intent received")
            if (intent.extras != null && intent.extras!!.containsKey(Constants.PAYLOAD)) {
                val pushData =
                    TimerStyle(PushNotificationData(intent.extras!!.getString(Constants.PAYLOAD)
                        ?.let { JSONObject(it) }, context!!
                    ), context
                    )

                val dismissIntent = PendingIntentFactory.constructPushDeletePendingIntent(
                    context,
                    pushData.pushNotification
                )
                dismissIntent.send()


                if (pushData.pushNotification.customData.containsKey(Constants.TYPE) && pushData.pushNotification.customData.getString(
                        Constants.TYPE
                    ).equals(Constants.PROGRESS_BAR)
                ) {
                    val notificationServiceIntent =
                        Intent(context, NotificationService::class.java)
                    context.stopService(notificationServiceIntent)
                } else if (pushData.pushNotification.customData.containsKey(Constants.TYPE) && pushData.pushNotification.customData.getString(
                        Constants.TYPE
                    ).equals(Constants.COUNTDOWN)
                ) {

                    with(NotificationManagerCompat.from(context!!)) {
                        this.cancel(pushData.pushNotification.experimentId.hashCode())
                    }
                }
            }
        }
    }
}
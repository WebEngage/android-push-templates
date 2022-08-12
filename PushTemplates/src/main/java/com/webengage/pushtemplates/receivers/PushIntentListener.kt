package com.webengage.pushtemplates.receivers

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.services.NotificationService
import com.webengage.pushtemplates.utils.Constants
import com.webengage.pushtemplates.utils.NotificationConfigurator
import com.webengage.sdk.android.PendingIntentFactory
import com.webengage.sdk.android.actions.render.PushNotificationData
import org.json.JSONObject
import java.util.*

class PushIntentListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("PushTemplates","Action Received")
        if (intent!!.action.equals(Constants.DELETE_ACTION)) {
            Log.d("PushTemplates","Delete Action Received")
            dismissNotification(context!!, intent)
        }
    }

    private fun dismissNotificationWithId(context: Context, id: Int) {
            with(NotificationManagerCompat.from(context)) {
                Log.d("PushTemplates","Dismissing Notification")
                this.cancel(id)
            }
        }

    private fun dismissNotification(context: Context, intent: Intent){
        if (intent.extras != null && intent.extras!!.containsKey(Constants.PAYLOAD)) {
            val pushData = PushNotificationData(intent.extras!!.getString(Constants.PAYLOAD)
                ?.let { JSONObject(it) }, context
            )


            if(intent.extras!!.containsKey(Constants.LOG_DISMISS) && intent.extras!!.getBoolean(Constants.LOG_DISMISS)){
                val dismissIntent = PendingIntentFactory.constructPushDeletePendingIntent(
                    context,
                    pushData
                )
                Log.d("PushTemplates","Manually Dismissed. Tracking Dismiss Event. ${intent.extras!!.getBoolean(Constants.LOG_DISMISS)}")
                dismissIntent.send()
            }

            if (pushData.customData.containsKey(Constants.TYPE) && pushData.customData.getString(
                    Constants.TYPE
                ).equals(Constants.PROGRESS_BAR)
            ) {
                val notificationServiceIntent =
                    Intent(context, NotificationService::class.java)
                context.stopService(notificationServiceIntent)
            } else if (pushData.customData.containsKey(Constants.TYPE) && pushData.customData.getString(
                    Constants.TYPE
                ).equals(Constants.COUNTDOWN)
            ) {
                Log.d("PushTemplates","Dismissing Timer")
                dismissNotificationWithId(context,pushData.variationId.hashCode())
            }
        }
    }
}
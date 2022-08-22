package com.webengage.pushtemplates.receivers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.services.NotificationService
import com.webengage.pushtemplates.utils.Constants
import com.webengage.sdk.android.PendingIntentFactory
import com.webengage.sdk.android.WebEngage
import com.webengage.sdk.android.actions.render.PushNotificationData
import org.json.JSONObject

class PushTransparentActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("PushTemplates","Starting PushTransparentActivity")
        try {
            WebEngage.get()
            if (intent!!.action.equals(Constants.DELETE_ACTION)) {
                Log.d("PushTemplates", "Starting PushTransparentActivity for action DELETE_ACTION")
                dismissNotification(this, intent)
            }
            if (intent.action.equals(Constants.CLICK_ACTION)) {
                Log.d("PushTemplates", "Starting PushTransparentActivity for action CLICK_ACTION")

                sendClickEvent(this, intent)
            }
        }
        catch (e : Exception){}
        finally {
            finish()
        }
    }

    /**
     * Send the notification click event for the provided cta ID and stop the foreground service
     */
    private fun sendClickEvent(context: Context, intent: Intent) {
        if (intent.extras != null && intent.extras!!.containsKey(Constants.PAYLOAD)) {
            val pushData = PushNotificationData(
                intent.extras!!.getString(Constants.PAYLOAD)
                    ?.let { JSONObject(it) }, context
            )
            if (intent.extras!!.containsKey(Constants.CTA_ID)) {
                val ctaID = intent.extras!!.getString(Constants.CTA_ID)
                val cta = pushData.getCallToActionById(ctaID)
                val clickIntent = PendingIntentFactory.constructPushClickPendingIntent(
                    context,
                    pushData,
                    cta,
                    false
                )
                clickIntent.send()
            }
            if (pushData.customData.containsKey(Constants.TEMPLATE_TYPE) && pushData.customData.getString(
                    Constants.TEMPLATE_TYPE
                ).equals(Constants.PROGRESS_BAR)
            ) {
                val notificationServiceIntent =
                    Intent(context, NotificationService::class.java)
                context.stopService(notificationServiceIntent)
            }else{
                dismissNotificationWithId(context, pushData.variationId.hashCode())
            }
        }
    }


    /**
     * Dismiss the notification with the provided notification ID
     */
    private fun dismissNotificationWithId(context: Context, id: Int) {
        with(NotificationManagerCompat.from(context)) {
            this.cancel(id)
        }
    }

    /**
     * Used to listen to the DISMISS CTA button clicks
     * If LOG_DISMISS is true then the notification close event will be logged.
     * If TEMPLATE_TYPE is ProgressBar, then the NotificationService will be stopped.
     * If TEMPLATE_TYPE is ProgressBar, then the Notification will be cancelled.
     */
    private fun dismissNotification(context: Context, intent: Intent) {
        if (intent.extras != null && intent.extras!!.containsKey(Constants.PAYLOAD)) {
            val pushData = PushNotificationData(
                intent.extras!!.getString(Constants.PAYLOAD)
                    ?.let { JSONObject(it) }, context
            )

            if (intent.extras!!.containsKey(Constants.LOG_DISMISS) && intent.extras!!.getBoolean(
                    Constants.LOG_DISMISS
                )
            ) {
                val dismissIntent = PendingIntentFactory.constructPushDeletePendingIntent(
                    context,
                    pushData
                )
                dismissIntent.send()
            }
            if (pushData.customData.containsKey(Constants.TEMPLATE_TYPE) && pushData.customData.getString(
                    Constants.TEMPLATE_TYPE
                ).equals(Constants.PROGRESS_BAR)
            ) {
                val notificationServiceIntent =
                    Intent(context, NotificationService::class.java)
                context.stopService(notificationServiceIntent)
            } else if (pushData.customData.containsKey(Constants.TEMPLATE_TYPE) && pushData.customData.getString(
                    Constants.TEMPLATE_TYPE
                ).equals(Constants.COUNTDOWN)
            ) {
                dismissNotificationWithId(context, pushData.variationId.hashCode())
            }
        }
    }
}
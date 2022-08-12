package com.webengage.pushtemplates.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.models.TimerStyleData
import com.webengage.pushtemplates.R
import com.webengage.pushtemplates.receivers.PushIntentListener
import com.webengage.sdk.android.PendingIntentFactory
import com.webengage.sdk.android.WebEngage
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.sdk.android.utils.htmlspanner.WEHtmlParserInterface

class NotificationConfigurator {


    /**
     * Get the id of the channel to be used for the showing the notification.
     * */
    fun getDefaultNotificationChannelID(
        context: Context, pushData: PushNotificationData
    ): String {
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        var channelId =
            WebEngage.get().webEngageConfig.defaultPushChannelConfiguration.notificationChannelId
        if (notificationManagerCompat.getNotificationChannel(pushData.channelId) != null)
            channelId = pushData.channelId

        return channelId
    }


    /**
     * Set the click intent on the remote view of the notification.
     * The click intent will be set for the R.id.we_notification_content view provided in the remote view.
     */
    fun setClickIntent(context: Context, remoteView: RemoteViews, pushData: PushNotificationData) {
        val clickIntent = PendingIntentFactory.constructPushClickPendingIntent(
            context,
            pushData,
            pushData.primeCallToAction,
            true
        )
        remoteView.setOnClickPendingIntent(R.id.we_notification_content, clickIntent)
    }

    /**
     * Returns the pending intent which will dismiss the notification as well as do the click action
     * provided in the CTA.
     * The notification will be dismissed without logging the dismiss event.
     * Click event will be logged for the notification
     */
    fun getClickAndDismissPendingIntent(
        context: Context,
        pushData: PushNotificationData,
        ctaID: String
    ): PendingIntent {
        val intent = Intent(context, PushIntentListener::class.java)
        intent.action = Constants.CLICK_ACTION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.identifier = (pushData.variationId + "_" + ctaID)
        }
        intent.addCategory(context.packageName)
        intent.putExtra(Constants.PAYLOAD, pushData.pushPayloadJSON.toString())
        intent.putExtra(Constants.CTA_ID, ctaID)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                context,
                (pushData.variationId + "_" + ctaID).hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                (pushData.variationId + "_" + ctaID).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        return pendingIntent
    }

    /**
     * Sets the dismiss pending intent for notification. This will log the push dismiss event.
     */
    fun setDismissIntent(
        context: Context,
        mBuilder: NotificationCompat.Builder,
        pushData: PushNotificationData
    ) {
        val deleteIntent = PendingIntentFactory.constructPushDeletePendingIntent(
            context,
            pushData
        )
        mBuilder.setDeleteIntent(deleteIntent)
    }

    /**
     * Sets the click pending intent for notification. This will log the push click event.
     */
    fun setClickIntent(
        context: Context,
        mBuilder: NotificationCompat.Builder,
        pushData: PushNotificationData
    ) {
        val clickIntent = PendingIntentFactory.constructPushClickPendingIntent(
            context,
            pushData,
            pushData.primeCallToAction,
            true
        )
        mBuilder.setContentIntent(clickIntent)
    }


    /**
     * This should be used when the background colour of the notification is transparent or not set.
     * Sets the CTA for the remote views. The font color of the CTA buttons will change according
     * to the theme.
     */
    private fun setAdaptiveCTAs(
        context: Context,
        remoteViews: RemoteViews,
        pushData: PushNotificationData
    ) {
        val dismissIntent =
            getNotificationDismissPendingIntent(context, pushData, true)
        remoteViews.setViewVisibility(R.id.actions_container, View.VISIBLE)

        if (pushData.callToActions != null && pushData.callToActions.size > 1) {
            if (pushData.callToActions[1] != null) {
                val cta = pushData.callToActions[1]
                val clickIntent =
                    getClickAndDismissPendingIntent(context, pushData, cta.id)

                remoteViews.setViewVisibility(R.id.action1_adaptive, View.VISIBLE)
                remoteViews.setTextViewText(
                    R.id.action1_adaptive,
                    cta.text
                )

                remoteViews.setOnClickPendingIntent(R.id.action1_adaptive, clickIntent)
            } else {
                remoteViews.setViewVisibility(R.id.action1_adaptive, View.VISIBLE)
                remoteViews.setTextViewText(R.id.action1_adaptive, Constants.DISMISS_CTA)
                remoteViews.setOnClickPendingIntent(R.id.action1_adaptive, dismissIntent)
            }
            if (pushData.callToActions.size > 2) {
                remoteViews.setViewVisibility(R.id.action2_adaptive, View.VISIBLE)
                remoteViews.setViewVisibility(R.id.action3_adaptive, View.VISIBLE)
                val cta = pushData.callToActions[2]
                val clickIntent =
                    getClickAndDismissPendingIntent(context, pushData, cta.id)

                remoteViews.setTextViewText(
                    R.id.action2_adaptive,
                    cta.text
                )
                remoteViews.setTextViewText(R.id.action3_adaptive, Constants.DISMISS_CTA)

                remoteViews.setOnClickPendingIntent(R.id.action2_adaptive, clickIntent)
                remoteViews.setOnClickPendingIntent(R.id.action3_adaptive, dismissIntent)
            } else {
                remoteViews.setViewVisibility(R.id.action2_adaptive, View.VISIBLE)
                remoteViews.setTextViewText(R.id.action2_adaptive, Constants.DISMISS_CTA)
                remoteViews.setOnClickPendingIntent(R.id.action2_adaptive, dismissIntent)

            }
        } else {
            remoteViews.setViewVisibility(R.id.action1_adaptive, View.VISIBLE)
            remoteViews.setTextViewText(R.id.action1_adaptive, Constants.DISMISS_CTA)
            remoteViews.setOnClickPendingIntent(R.id.action1_adaptive, dismissIntent)
        }
    }


    /**
     * This should be used when the background colour of the notification is set.
     * Sets the CTA for the remote views. The font color of the CTA buttons will remain static.
     */
    private fun setNativeCTAs(
        context: Context,
        remoteViews: RemoteViews,
        pushData: PushNotificationData
    ) {
        val dismissIntent =
            getNotificationDismissPendingIntent(context, pushData, true)

        remoteViews.setViewVisibility(R.id.actions_container, View.VISIBLE)

        if (pushData.callToActions != null && pushData.callToActions.size > 1) {
            if (pushData.callToActions[1] != null) {
                remoteViews.setViewVisibility(R.id.action1_native, View.VISIBLE)
                val cta = pushData.callToActions[1]
                val clickIntent =
                    getClickAndDismissPendingIntent(context, pushData, cta.id)

                remoteViews.setTextViewText(
                    R.id.action1_native,
                    cta.text
                )

                remoteViews.setOnClickPendingIntent(R.id.action1_native, clickIntent)
            } else {
                remoteViews.setViewVisibility(R.id.action1_native, View.VISIBLE)
                remoteViews.setTextViewText(R.id.action1_native, Constants.DISMISS_CTA)
                remoteViews.setOnClickPendingIntent(R.id.action1_native, dismissIntent)
            }
            if (pushData.callToActions.size > 2) {
                remoteViews.setViewVisibility(R.id.action2_native, View.VISIBLE)
                remoteViews.setViewVisibility(R.id.action3_native, View.VISIBLE)

                val cta = pushData.callToActions[1]
                val clickIntent =
                    getClickAndDismissPendingIntent(context, pushData, cta.id)

                remoteViews.setTextViewText(
                    R.id.action2_native,
                    cta.text
                )
                remoteViews.setTextViewText(R.id.action3_native, Constants.DISMISS_CTA)


                remoteViews.setOnClickPendingIntent(R.id.action2_native, clickIntent)
                remoteViews.setOnClickPendingIntent(R.id.action3_native, dismissIntent)
            } else {
                remoteViews.setViewVisibility(R.id.action2_native, View.VISIBLE)
                remoteViews.setTextViewText(R.id.action2_native, Constants.DISMISS_CTA)
                remoteViews.setOnClickPendingIntent(R.id.action2_native, dismissIntent)
            }
        } else {
            remoteViews.setViewVisibility(R.id.action1_native, View.VISIBLE)
            remoteViews.setTextViewText(R.id.action1_native, Constants.DISMISS_CTA)
            remoteViews.setOnClickPendingIntent(R.id.action1_native, dismissIntent)
        }
    }

    /**
     * Sets the CTA for the remote views.
     */
    fun setCTAList(context: Context, remoteViews: RemoteViews, pushData: PushNotificationData) {
        remoteViews.setViewVisibility(R.id.we_notification_bottom_margin, View.GONE)
        if (pushData.backgroundColor != Color.parseColor("#00000000"))
            setNativeCTAs(context, remoteViews, pushData)
        else
            setAdaptiveCTAs(context, remoteViews, pushData)
    }

    /**
     * Sets the standard template for the push notification.
     */
    fun configureRemoteView(
        context: Context,
        remoteView: RemoteViews,
        pushData: PushNotificationData,
        whenTime: Long
    ) {
        remoteView.setInt(
            R.id.we_notification_container,
            "setBackgroundColor",
            pushData.backgroundColor
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.S) {
            remoteView.setViewVisibility(R.id.push_base_container, View.GONE)
            remoteView.setViewPadding(R.id.we_notification, 0, 0, 0, 0)
        } else {
            remoteView.setViewVisibility(R.id.push_base_container, View.VISIBLE)
            remoteView.setImageViewResource(R.id.small_icon, pushData.smallIcon)
            remoteView.setTextViewText(R.id.app_name, pushData.appName)
            if (!TextUtils.isEmpty(pushData.contentSummary))
                remoteView.setTextViewText(
                    R.id.custom_summary,
                    WEHtmlParserInterface().fromHtml(pushData.contentSummary)
                )
            else
                remoteView.setViewVisibility(R.id.custom_summary, View.GONE)
            val dateFormat = DateFormat.getTimeFormat(context)
            val time = dateFormat.format(whenTime)
            remoteView.setTextViewText(
                com.webengage.sdk.android.R.id.custom_notification_time,
                time
            )
            remoteView.setTextViewText(R.id.app_name_native, pushData.appName)
            if (!TextUtils.isEmpty(pushData.contentSummary))
                remoteView.setTextViewText(
                    R.id.custom_summary_native,
                    WEHtmlParserInterface().fromHtml(pushData.contentSummary)
                )
            else
                remoteView.setViewVisibility(R.id.custom_summary_native, View.GONE)
            remoteView.setTextViewText(
                com.webengage.sdk.android.R.id.custom_notification_time_native,
                time
            )

            if (pushData.backgroundColor != Color.parseColor("#00000000")) {
                //No Background Color Set

                remoteView.setViewVisibility(R.id.app_name, View.GONE)
                remoteView.setViewVisibility(R.id.custom_notification_time, View.GONE)
                remoteView.setViewVisibility(R.id.custom_summary, View.GONE)
            } else {
                //Background Color Set
                remoteView.setViewVisibility(R.id.app_name_native, View.GONE)
                remoteView.setViewVisibility(R.id.custom_notification_time_native, View.GONE)
                remoteView.setViewVisibility(R.id.custom_summary_native, View.GONE)
            }

        }
    }

    /**
     * Sets the notification title for the push notification remote view.
     * R.id.we_notification_title should be present in the remote view.
     */
    fun setNotificationTitle(pushData: PushNotificationData, remoteViews: RemoteViews) {
        remoteViews.setTextViewText(
            R.id.we_notification_title,
            WEHtmlParserInterface().fromHtml(pushData.title)
        )
        remoteViews.setTextViewText(
            R.id.we_notification_title_native,
            WEHtmlParserInterface().fromHtml(pushData.title)
        )

        if (pushData.backgroundColor == Color.parseColor("#00000000")) {
            remoteViews.setViewVisibility(R.id.we_notification_title, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.we_notification_title_native, View.GONE)
        } else {
            remoteViews.setViewVisibility(R.id.we_notification_title, View.GONE)
            remoteViews.setViewVisibility(R.id.we_notification_title_native, View.VISIBLE)
        }
    }

    /**
     * Sets the notification description for the push notification remote view.
     * R.id.we_notification_description should be present in the remote view.
     */
    fun setNotificationDescription(
        pushData: TimerStyleData,
        remoteViews: RemoteViews
    ) {
        remoteViews.setTextViewText(
            R.id.we_notification_description,
            WEHtmlParserInterface().fromHtml(pushData.pushNotification.contentText)
        )
        remoteViews.setTextViewText(
            R.id.we_notification_description_native,
            WEHtmlParserInterface().fromHtml(pushData.pushNotification.contentText)
        )

        if (pushData.pushNotification.backgroundColor == Color.parseColor("#00000000")) {
            remoteViews.setViewVisibility(R.id.we_notification_description, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.we_notification_description_native, View.GONE)
        } else {
            remoteViews.setViewVisibility(R.id.we_notification_description, View.GONE)
            remoteViews.setViewVisibility(R.id.we_notification_description_native, View.VISIBLE)
        }
    }

    /**
     * Sets the notification details as provided in the push notification data.
     */
    fun setNotificationConfiguration(
        mBuilder: NotificationCompat.Builder,
        pushData: TimerStyleData,
        whenTime: Long
    ) {
        mBuilder.setAutoCancel(true)
        mBuilder.setOngoing(pushData.pushNotification.isSticky)
        mBuilder.setSmallIcon(pushData.pushNotification.smallIcon)
        mBuilder.priority = pushData.pushNotification.priority
        mBuilder.setContentTitle(WEHtmlParserInterface().fromHtml(pushData.pushNotification.title))
        mBuilder.setContentText(WEHtmlParserInterface().fromHtml(pushData.pushNotification.contentText))
        if (!TextUtils.isEmpty(pushData.pushNotification.contentSummary))
            mBuilder.setSubText(WEHtmlParserInterface().fromHtml(pushData.pushNotification.contentSummary))
        mBuilder.setWhen(whenTime)
    }

    /**
     * Returns the dismiss pending intent for the notification. This will be sent to the
     * PushIntentListener BroadCast Receiver.
     */
    private fun getNotificationDismissPendingIntent(
        context: Context,
        pushData: PushNotificationData,
        logDismiss: Boolean
    ): PendingIntent {
        val intent = Intent(context, PushIntentListener::class.java)
        intent.action = Constants.DELETE_ACTION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.identifier = (pushData.variationId + "_" + logDismiss)
        }
        intent.addCategory(context.packageName)
        intent.putExtra(Constants.PAYLOAD, pushData.pushPayloadJSON.toString())
        intent.putExtra(Constants.LOG_DISMISS, logDismiss)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                context,
                (pushData.variationId + "_" + logDismiss).hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                (pushData.variationId + "_" + logDismiss).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        return pendingIntent
    }

    /**
     * Sets the color for the chronometer.
     */
    fun setChronometerViewColor(
        context: Context,
        remoteViews: RemoteViews,
        pushData: PushNotificationData,
        textColor: Int?,
    ) {
        var color = textColor
        if (color == null) {
            color = context.getColor(R.color.we_black)
            if (pushData.backgroundColor != Color.parseColor("#00000000")) {
                //set the static text color
                color = context.getColor(R.color.we_hard_black)
            }
        }
        remoteViews.setInt(R.id.we_notification_timer, "setTextColor", color)
    }

}
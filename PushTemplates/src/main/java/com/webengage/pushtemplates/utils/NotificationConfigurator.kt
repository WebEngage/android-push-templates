package com.webengage.pushtemplates.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Build
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.R
import com.webengage.pushtemplates.receivers.PushIntentListener
import com.webengage.pushtemplates.receivers.PushTransparentActivity
import com.webengage.sdk.android.PendingIntentFactory
import com.webengage.sdk.android.WebEngage
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.sdk.android.utils.WebEngageConstant
import com.webengage.sdk.android.utils.htmlspanner.WEHtmlParserInterface
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


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
        remoteView.setOnClickPendingIntent(R.id.we_notification_container, clickIntent)
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
        var intent = Intent(context, PushIntentListener::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            intent = Intent(context, PushTransparentActivity::class.java)
        }
        intent.action = Constants.CLICK_ACTION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.identifier = (pushData.variationId + "_" + ctaID)
        }
        intent.addCategory(context.packageName)
        intent.putExtra(Constants.PAYLOAD, pushData.pushPayloadJSON.toString())
        intent.putExtra(Constants.CTA_ID, ctaID)
        val pendingIntent: PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(
                context,
                (pushData.variationId + "_" + ctaID).hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
        pushData: PushNotificationData,
        showDismiss: Boolean
    ) {
        val ctaButtonsList =
            listOf(R.id.action1_adaptive, R.id.action2_adaptive, R.id.action3_adaptive)

        remoteViews.setViewVisibility(R.id.actions_container, View.VISIBLE)

        if (pushData.callToActions.size > 1) {
            for (iterator in 1 until pushData.callToActions.size) {
                val ctaButton = ctaButtonsList[iterator - 1]
                remoteViews.setViewVisibility(ctaButton, View.VISIBLE)
                val cta = pushData.callToActions[iterator]
                val clickIntent =
                    getClickAndDismissPendingIntent(context, pushData, cta.id)

                remoteViews.setTextViewText(
                    ctaButton,
                    cta.text
                )
                remoteViews.setOnClickPendingIntent(ctaButton, clickIntent)
            }
        }
        if (pushData.callToActions.size - 1 < ctaButtonsList.size && showDismiss) {
            val ctaButton = ctaButtonsList[pushData.callToActions.size - 1]

            val dismissIntent =
                getNotificationDismissPendingIntent(context, pushData, true)
            remoteViews.setViewVisibility(ctaButton, View.VISIBLE)
            remoteViews.setTextViewText(ctaButton, Constants.DISMISS_CTA)
            remoteViews.setOnClickPendingIntent(ctaButton, dismissIntent)
        }
    }


    /**
     * This should be used when the background colour of the notification is set.
     * Sets the CTA for the remote views. The font color of the CTA buttons will remain static.
     */
    private fun setNativeCTAs(
        context: Context,
        remoteViews: RemoteViews,
        pushData: PushNotificationData,
        showDismiss: Boolean
    ) {
        val ctaButtonsList = listOf(R.id.action1_native, R.id.action2_native, R.id.action3_native)

        remoteViews.setViewVisibility(R.id.actions_container, View.VISIBLE)

        if (pushData.callToActions.size > 1) {
            for (iterator in 1 until pushData.callToActions.size) {
                val ctaButton = ctaButtonsList[iterator - 1]
                remoteViews.setViewVisibility(ctaButton, View.VISIBLE)
                val cta = pushData.callToActions[iterator]
                val clickIntent =
                    getClickAndDismissPendingIntent(context, pushData, cta.id)

                remoteViews.setTextViewText(
                    ctaButton,
                    cta.text
                )
                remoteViews.setOnClickPendingIntent(ctaButton, clickIntent)
            }
        }

        //todo comment
        if (pushData.callToActions.size - 1 < ctaButtonsList.size && showDismiss) {
            val ctaButton = ctaButtonsList[pushData.callToActions.size - 1]

            val dismissIntent =
                getNotificationDismissPendingIntent(context, pushData, true)
            remoteViews.setViewVisibility(ctaButton, View.VISIBLE)
            remoteViews.setTextViewText(ctaButton, Constants.DISMISS_CTA)
            remoteViews.setOnClickPendingIntent(ctaButton, dismissIntent)
        }

    }

    /**
     * Sets the CTA for the remote views.
     */
    fun setCTAList(
        context: Context,
        remoteViews: RemoteViews,
        pushData: PushNotificationData,
        showDismiss: Boolean
    ) {
        remoteViews.setViewVisibility(R.id.we_notification_bottom_margin, View.GONE)

        if (pushData.backgroundColor != context.getColor(R.color.we_transparent))
            setNativeCTAs(context, remoteViews, pushData, showDismiss)
        else
            setAdaptiveCTAs(context, remoteViews, pushData, showDismiss)

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
            var inset = 0
            if (pushData.backgroundColor != context.getColor(R.color.we_transparent)) {
                inset =
                    context.resources.getDimensionPixelSize(R.dimen.we_push_content_margin_colorbg)
            }
            remoteView.setViewPadding(R.id.we_notification, inset, 0, 0, 0)
        } else {
            remoteView.setViewVisibility(R.id.push_base_container, View.VISIBLE)
            remoteView.setImageViewResource(R.id.small_icon, pushData.smallIcon)
            if (pushData.accentColor != -1)
                remoteView.setInt(R.id.small_icon, "setColorFilter", pushData.accentColor)

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

            if (pushData.backgroundColor != context.getColor(R.color.we_transparent)) {
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
    fun setNotificationTitle(
        context: Context,
        pushData: PushNotificationData,
        remoteViews: RemoteViews
    ) {
        remoteViews.setTextViewText(
            R.id.we_notification_title,
            WEHtmlParserInterface().fromHtml(pushData.title)
        )
        remoteViews.setTextViewText(
            R.id.we_notification_title_native,
            WEHtmlParserInterface().fromHtml(pushData.title)
        )

        if (pushData.backgroundColor == context.getColor(R.color.we_transparent)) {
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
        context: Context,
        pushData: PushNotificationData,
        remoteViews: RemoteViews
    ) {
        remoteViews.setTextViewText(
            R.id.we_notification_description,
            WEHtmlParserInterface().fromHtml(pushData.contentText)
        )
        remoteViews.setTextViewText(
            R.id.we_notification_description_native,
            WEHtmlParserInterface().fromHtml(pushData.contentText)
        )

        if (pushData.backgroundColor == context.getColor(R.color.we_transparent)) {
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
        pushData: PushNotificationData,
        whenTime: Long
    ) {
        mBuilder.setAutoCancel(true)
        mBuilder.setOngoing(pushData.isSticky)
        mBuilder.setSmallIcon(pushData.smallIcon)
        mBuilder.priority = pushData.priority
        mBuilder.setContentTitle(WEHtmlParserInterface().fromHtml(pushData.title))
        mBuilder.setContentText(WEHtmlParserInterface().fromHtml(pushData.contentText))
        if (!TextUtils.isEmpty(pushData.contentSummary))
            mBuilder.setSubText(WEHtmlParserInterface().fromHtml(pushData.contentSummary))
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
        if (textColor != null) {
            remoteViews.setInt(R.id.we_notification_timer, "setTextColor", textColor)
        } else if (pushData.backgroundColor != context.getColor(R.color.we_transparent)) {
            val color = context.getColor(R.color.we_hard_black)
            remoteViews.setInt(R.id.we_notification_timer, "setTextColor", color)
        }
    }

    /**
     * Sets the color for the progressbar.
     */
    fun setProgressBarColor(
        remoteViews: RemoteViews,
        progressColor: Int?,
        backgroundColor: Int?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (progressColor != null) {
                remoteViews.setColorStateList(
                    R.id.we_notification_progressBar,
                    "setProgressTintList",
                    ColorStateList.valueOf(progressColor)
                )
            }
            if (backgroundColor != null) {
                remoteViews.setColorStateList(
                    R.id.we_notification_progressBar,
                    "setProgressBackgroundTintList",
                    ColorStateList.valueOf(backgroundColor)
                )
            }
        } else {
            if (progressColor != null) {
                setColorStateListBelowS(remoteViews, progressColor, 0)
            }
            if (backgroundColor != null) {
                setColorStateListBelowS(remoteViews, backgroundColor, 1)
            }
        }
    }

    /**
     * Sets the Images for the Notification.
     */
    fun setNotificationBanner(
        remoteViews: RemoteViews,
        pushData: PushNotificationData,
        bitmapList: ArrayList<Bitmap?>
    ) {
        if (bitmapList.size > 0) {
            if (pushData.style == WebEngageConstant.STYLE.BIG_PICTURE && !TextUtils.isEmpty(pushData.bigPictureStyleData.bigPictureUrl)) {
                val bitmap = bitmapList[0]
                if (bitmap != null) {
                    remoteViews.setViewVisibility(R.id.we_notification_image, View.VISIBLE)
                    remoteViews.setImageViewBitmap(R.id.we_notification_image, bitmap)
                } else {
                    Log.e("PushTemplates", "Bitmap returned null")
                }
            }
        }
    }

    /**
     * Below Android S, RemoteViews do not have support for setting the color state list directly
     * for the progress bar. Use private methods for this
     */
    private fun setColorStateListBelowS(remoteViews: RemoteViews, color: Int, type: Int) {
        //type = 0 -> Progress Color for progress bar
        //type = 1 -> Background Color for progress bar

        var methodName = "setProgressTintList"
        if (type == 0)
            methodName = "setProgressBackgroundTintList"

        var setTintMethod: Method? = null
        try {
            setTintMethod = RemoteViews::class.java.getMethod(
                methodName,
                Int::class.javaPrimitiveType,
                ColorStateList::class.java
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        if (setTintMethod != null) {
            try {
                setTintMethod.invoke(
                    remoteViews,
                    R.id.we_notification_progressBar,
                    ColorStateList.valueOf(color)
                )
            } catch (e: IllegalAccessException) {
            } catch (e: InvocationTargetException) {
            }
        }
    }
}
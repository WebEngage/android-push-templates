package com.webengage.pushtemplates.callbacks

import android.app.Notification
import android.content.Context
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.models.TimerStyleData
import com.webengage.pushtemplates.R
import com.webengage.pushtemplates.utils.NotificationConfigurator
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.sdk.android.callbacks.CustomPushRender
import com.webengage.sdk.android.callbacks.CustomPushRerender

class CountDownRenderer : CustomPushRender, CustomPushRerender {

    private lateinit var context: Context
    private lateinit var mBuilder: NotificationCompat.Builder
    private lateinit var pushData: TimerStyleData
    private var collapsedTimerLayoutId = R.layout.layout_timer_collapsed
    private var expandedTimerLayoutId = R.layout.layout_timer_collapsed
    private var whenTime: Long = 0

    override fun onRender(
        mContext: Context?,
        pushNotificationData: PushNotificationData?
    ): Boolean {

        this.context = mContext!!
        this.pushData = TimerStyleData(context, pushNotificationData!!)
        this.whenTime = System.currentTimeMillis()
        if (pushData.timerTime < System.currentTimeMillis())
            return false
        initRender()
        return true

    }

    override fun onRerender(
        context: Context?,
        pushNotificationData: PushNotificationData?,
        extras: Bundle?
    ): Boolean {
        //TODO("Implement when the notification is supposed to me rendered with new content")
        return false
    }

    fun initRender() {
        constructNotification(context, pushData)
        show(context)
    }


    private fun constructNotification(context: Context?, pushNotificationData: TimerStyleData?) {
        this.mBuilder =
            NotificationCompat.Builder(context!!, "Sales")
        NotificationConfigurator().setNotificationConfiguration(
            context,
            mBuilder,
            pushNotificationData!!,
            whenTime
        )
        NotificationConfigurator().setDismissIntent(context, mBuilder, pushNotificationData)
        NotificationConfigurator().setClickIntent(context, mBuilder, pushNotificationData)

        this.mBuilder.setCustomContentView(
            constructCollapsedTimerPushBase(
                context,
                pushNotificationData
            )
        )
        this.mBuilder.setCustomBigContentView(
            constructExpandedTimerPushBase(
                context,
                pushNotificationData
            )
        )
    }

    private fun constructExpandedTimerPushBase(
        context: Context,
        timerNotificationData: TimerStyleData?
    ): RemoteViews {

        val remoteView = RemoteViews(context.packageName, expandedTimerLayoutId)

        NotificationConfigurator().configureRemoteView(
            context,
            remoteView,
            timerNotificationData!!,
            whenTime
        )
        NotificationConfigurator().setNotificationDescription(
            context,
            timerNotificationData,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(context, timerNotificationData, remoteView)
        NotificationConfigurator().setCTAList(context, remoteView, pushData)
        NotificationConfigurator().setClickIntent(context, remoteView, pushData)
        val timeDiff =
            timerNotificationData.timerTime - System.currentTimeMillis() + SystemClock.elapsedRealtime()
        remoteView.setChronometer(
            R.id.we_notification_timer,
            timeDiff,
            timerNotificationData.timerFormat,
            true
        )
        return remoteView
    }

    private fun constructCollapsedTimerPushBase(
        context: Context,
        timerNotificationData: TimerStyleData?
    ): RemoteViews {
        val remoteView = RemoteViews(context.packageName, collapsedTimerLayoutId)

        NotificationConfigurator().configureRemoteView(
            context,
            remoteView,
            timerNotificationData!!,
            whenTime
        )
        NotificationConfigurator().setNotificationDescription(
            context,
            timerNotificationData,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(context, timerNotificationData, remoteView)
        NotificationConfigurator().setClickIntent(context, remoteView, timerNotificationData)

        val timeDiff =
            timerNotificationData.timerTime - System.currentTimeMillis() + SystemClock.elapsedRealtime()
        remoteView.setChronometer(
            R.id.we_notification_timer,
            timeDiff,
            timerNotificationData.timerFormat,
            true
        )

        return remoteView
    }

    private fun show(context: Context) {
        val channel = NotificationConfigurator().getDefaultNotificationChannel(
            context
        )
        mBuilder.setTimeoutAfter(pushData.timerTime - System.currentTimeMillis())
        mBuilder.setChannelId(channel.id)
        Log.d(
            "PushTemplates",
            "Showing Timer for ${pushData.timerTime - System.currentTimeMillis()}"
        )
        with(NotificationManagerCompat.from(context)) {
            notify(pushData.pushNotification.variationId.hashCode(), mBuilder.build().apply {
                this.flags = this.flags or Notification.FLAG_AUTO_CANCEL
                this.flags = this.flags or Notification.FLAG_ONLY_ALERT_ONCE
            })
        }
    }

}
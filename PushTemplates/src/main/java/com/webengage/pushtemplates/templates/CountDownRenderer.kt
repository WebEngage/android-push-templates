package com.webengage.pushtemplates.templates

import android.app.Notification
import android.content.Context
import android.os.*
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.models.TimerStyleData
import com.webengage.pushtemplates.R
import com.webengage.pushtemplates.utils.NotificationConfigurator
import com.webengage.sdk.android.actions.render.PushNotificationData

class CountDownRenderer {

    private lateinit var context: Context
    private lateinit var mBuilder: NotificationCompat.Builder
    private lateinit var pushData: TimerStyleData
    private var collapsedTimerLayoutId = R.layout.layout_timer_collapsed
    private var expandedTimerLayoutId = R.layout.layout_timer_collapsed
    private var whenTime: Long = 0

    fun onRender(
        mContext: Context?,
        pushNotificationData: PushNotificationData?
    ): Boolean {

        this.context = mContext!!
        this.pushData = TimerStyleData(context, pushNotificationData!!)
        this.whenTime = System.currentTimeMillis()
        this.mBuilder =
            NotificationCompat.Builder(
                context,
                NotificationConfigurator().getDefaultNotificationChannelID(
                    context,
                    pushNotificationData
                )
            )

        //If the provided future time is less that the system time, then do not render
        if (pushData.futureTime < System.currentTimeMillis())
            return false
        constructNotification(context, pushData)
        show(context)
        return true

    }

    private fun constructNotification(context: Context?, pushNotificationData: TimerStyleData?) {
        NotificationConfigurator().setNotificationConfiguration(
            mBuilder,
            pushNotificationData!!,
            whenTime
        )
        NotificationConfigurator().setDismissIntent(
            context!!,
            mBuilder,
            pushNotificationData.pushNotification
        )
        NotificationConfigurator().setClickIntent(
            context,
            mBuilder,
            pushNotificationData.pushNotification
        )

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


    /**
     * Create and attach the expanded layout for the notification
     */
    private fun constructExpandedTimerPushBase(
        context: Context,
        timerNotificationData: TimerStyleData?
    ): RemoteViews {

        val remoteView = RemoteViews(context.packageName, expandedTimerLayoutId)

        NotificationConfigurator().configureRemoteView(
            context,
            remoteView,
            timerNotificationData!!.pushNotification,
            whenTime
        )
        NotificationConfigurator().setNotificationDescription(
            timerNotificationData,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(
            timerNotificationData.pushNotification,
            remoteView
        )
        NotificationConfigurator().setCTAList(
            context,
            remoteView,
            timerNotificationData.pushNotification
        )
        NotificationConfigurator().setClickIntent(
            context,
            remoteView,
            timerNotificationData.pushNotification
        )
        NotificationConfigurator().setChronometerViewColor(
            context,
            remoteView,
            timerNotificationData.pushNotification,
            timerNotificationData.timerColor
        )

        val timeDiff =
            timerNotificationData.futureTime - System.currentTimeMillis() + SystemClock.elapsedRealtime()
        remoteView.setChronometer(
            R.id.we_notification_timer,
            timeDiff,
            timerNotificationData.timerFormat,
            true
        )
        return remoteView
    }

    /**
     * Create and attach the collapsed layout for the notification
     */
    private fun constructCollapsedTimerPushBase(
        context: Context,
        timerNotificationData: TimerStyleData?
    ): RemoteViews {
        val remoteView = RemoteViews(context.packageName, collapsedTimerLayoutId)

        NotificationConfigurator().configureRemoteView(
            context,
            remoteView,
            timerNotificationData!!.pushNotification,
            whenTime
        )
        NotificationConfigurator().setNotificationDescription(
            timerNotificationData,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(
            timerNotificationData.pushNotification,
            remoteView
        )
        NotificationConfigurator().setClickIntent(
            context,
            remoteView,
            timerNotificationData.pushNotification
        )
        NotificationConfigurator().setChronometerViewColor(
            context,
            remoteView,
            timerNotificationData.pushNotification,
            timerNotificationData.timerColor
        )

        val timeDiff =
            timerNotificationData.futureTime - System.currentTimeMillis() + SystemClock.elapsedRealtime()
        remoteView.setChronometer(
            R.id.we_notification_timer,
            timeDiff,
            timerNotificationData.timerFormat,
            true
        )

        return remoteView
    }

    /**
     * Show notification if the current system time is less than teh provided future time
     */
    private fun show(context: Context) {
        mBuilder.setTimeoutAfter(pushData.futureTime - System.currentTimeMillis())
        with(NotificationManagerCompat.from(context)) {
            notify(pushData.pushNotification.variationId.hashCode(), mBuilder.build().apply {
                this.flags = this.flags or Notification.FLAG_AUTO_CANCEL
                this.flags = this.flags or Notification.FLAG_ONLY_ALERT_ONCE
            })
        }
    }

}
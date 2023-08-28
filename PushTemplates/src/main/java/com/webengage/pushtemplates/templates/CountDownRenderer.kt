package com.webengage.pushtemplates.templates

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.models.TimerStyleData
import com.webengage.pushtemplates.R
import com.webengage.pushtemplates.utils.ImageUtils
import com.webengage.pushtemplates.utils.NotificationConfigurator
import com.webengage.sdk.android.actions.render.PushNotificationData
import kotlinx.coroutines.*

class CountDownRenderer {

    private lateinit var context: Context
    private lateinit var mBuilder: NotificationCompat.Builder
    private lateinit var pushData: TimerStyleData
    private var collapsedTimerLayoutId = R.layout.layout_timer_template
    private var expandedTimerLayoutId = R.layout.layout_timer_template
    private var whenTime: Long = 0
    private var bitmapList: ArrayList<Bitmap?> = ArrayList()

    fun onRender(
        mContext: Context?,
        pushNotificationData: PushNotificationData?
    ): Boolean {

        this.context = mContext!!
        this.pushData = TimerStyleData(pushNotificationData!!)
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
        if (pushData.futureTime < System.currentTimeMillis()){
            Log.d("PushTemplates","The future time provided is less than current device time")
            return false
        }
        CoroutineScope(Dispatchers.Default).launch {
            bitmapList = ImageUtils().getBitmapArrayList(pushNotificationData)
            constructNotification(context, pushData)
            show(context)
        }
        return true
    }

    private fun constructNotification(context: Context?, pushNotificationData: TimerStyleData?) {
        NotificationConfigurator().setNotificationConfiguration(
            mBuilder,
            pushNotificationData!!.pushNotification,
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

        NotificationConfigurator().setTitleMaxLines(remoteView,2)
        NotificationConfigurator().setDescriptionMaxLines(remoteView,2)

        NotificationConfigurator().setNotificationDescription(
            context,
            timerNotificationData.pushNotification,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(
            context,
            timerNotificationData.pushNotification,
            remoteView
        )

        NotificationConfigurator().setCTAList(
            context,
            remoteView,
            timerNotificationData.pushNotification,
            timerNotificationData.showDismissCTA
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

        NotificationConfigurator().setNotificationBanner(
            remoteView,
            timerNotificationData.pushNotification,
            bitmapList
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
            context,
            timerNotificationData.pushNotification,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(
            context,
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
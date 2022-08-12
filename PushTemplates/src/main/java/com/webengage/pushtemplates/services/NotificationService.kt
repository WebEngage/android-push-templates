package com.webengage.pushtemplates.services

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.utils.Constants
import com.webengage.pushtemplates.utils.NotificationConfigurator
import com.webengage.pushtemplates.models.TimerStyle
import com.webengage.pushtemplates.R
import com.webengage.pushtemplates.utils.Scheduler
import com.webengage.sdk.android.PendingIntentFactory
import com.webengage.sdk.android.actions.render.PushNotificationData
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

class NotificationService : Service() {
    private var threadRunner: AtomicBoolean = AtomicBoolean(true)
    private var context: Context? = null
    private var mBuilder: NotificationCompat.Builder? = null
    private var pushData: TimerStyle? = null
    private val SECOND = 1000;
    private val DEFAULT_SECONDS = 2;
    private var whenTime: Long = 0;
    private var collapsedLayoutId = -1
    private var expandedLayoutId = -1
    private var collapsedTimerLayoutId = R.layout.layout_progressbar_collapsed
    private var expandedTimerLayoutId = R.layout.layout_progressbar_collapsed

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PushTemplates", "Service onStartCommand")
        if (intent!!.action.equals(Constants.PROGRESSBAR_ACTION)) {
            val pushDataPayload = intent.extras!!.getString(Constants.PAYLOAD)
                ?.let { PushNotificationData(JSONObject(it), applicationContext) }
            val timerData = TimerStyle(applicationContext, pushDataPayload!!)
            this.context = applicationContext
            this.pushData = timerData
            this.mBuilder = NotificationCompat.Builder(context!!, "Sales")
            this.whenTime = (intent.extras!!.getLong("when"))
            Scheduler().cancelAlarm(context!!,NotificationConfigurator().getNotificationDismissPendingIntent(context!!,pushDataPayload,false))
            stopForeground(true)

            val notification = getNotification(timerData, context!!)
            startForeground(
                pushData!!.pushNotification.variationId.hashCode(),
                notification.build().apply {
                    this.flags = this.flags or Notification.FLAG_ONLY_ALERT_ONCE
                    this.flags = this.flags or Notification.FLAG_AUTO_CANCEL
                }
            )
        }
        else if(intent.action.equals(Constants.DELETE_ACTION)){
            stopSelf()
        }
        return START_STICKY

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startCounter(
        context: Context,
        mBuilder: NotificationCompat.Builder,
        pushData: TimerStyle,
        timeDiff: Long
    ) {

        GlobalScope.launch(Dispatchers.Main) {

            for (i in (pushData.timerTime - System.currentTimeMillis()) downTo 0 step 2000) {
                if(System.currentTimeMillis() <= pushData.timerTime && threadRunner.get()) {
                    Log.d("PushTemplates","Updating")
                    mBuilder.setProgress(
                        (pushData.timerTime - whenTime).toInt(),
                        (System.currentTimeMillis() - whenTime).toInt(),
                        false
                    )
                    constructNotification(context, pushData, timeDiff)
                    show(context)
                    delay(2000)
                }
            }
        }

        val pendingIntent =  NotificationConfigurator().getNotificationDismissPendingIntent(context,pushData.pushNotification, false)
        Scheduler().scheduleAlarm(context,pushData.timerTime,pendingIntent)

//        GlobalScope.launch(Dispatchers.Main) {
//            delay(pushData.timerTime - System.currentTimeMillis())
//            Log.d("PushTemplates","Stopping service coroutine")
//            stopSelf()
//        }
    }

    private fun getNotification(
        timerData: TimerStyle,
        mContext: Context
    ): NotificationCompat.Builder {
        val channel = NotificationConfigurator().getDefaultNotificationChannel(
            mContext,
            "Sales",
            "Sales",
            "Notifications for upcoming Sales"
        )
        this.mBuilder = NotificationCompat.Builder(mContext, channel.id)

        val timeDiff =
            timerData.timerTime - System.currentTimeMillis() + SystemClock.elapsedRealtime()

        constructNotification(mContext, timerData, timeDiff)
        startCounter(mContext, mBuilder!!, timerData, timeDiff)
        return mBuilder!!
    }


    override fun onCreate() {
        Log.d("PushTemplates", "Service Created")
        super.onCreate()
    }

    override fun onDestroy() {
        threadRunner.set(false)
        stopForeground(true)
        Scheduler().cancelAlarm(context!!,NotificationConfigurator().getNotificationDismissPendingIntent(context!!,pushData!!.pushNotification,false))
        with(NotificationManagerCompat.from(context!!)) {
            this.cancel(pushData!!.pushNotification.variationId.hashCode())
        }
        Log.d("PushTemplates", "Service Destroyed")
        super.onDestroy()
    }


    fun constructNotification(
        context: Context?,
        pushNotificationData: TimerStyle?,
        timeDiff: Long
    ) {
        this.mBuilder!!.setProgress(
            (pushData!!.timerTime - whenTime).toInt(),
            (System.currentTimeMillis() - whenTime).toInt(),
            false
        )
        NotificationConfigurator().setNotificationConfiguration(
            context!!,
            mBuilder!!,
            pushData!!,
            whenTime
        )
        this.mBuilder!!
            .setCustomContentView(
                constructCollapsedTimerPushBase(
                    context!!,
                    pushNotificationData,
                    timeDiff
                )
            )
        this.mBuilder!!.setCustomBigContentView(
            constructExpandedTimerPushBase(
                context,
                pushNotificationData,
                timeDiff
            )
        )
    }

    private fun constructExpandedTimerPushBase(
        context: Context,
        timerNotificationData: TimerStyle?,
        timeDiff: Long
    ): RemoteViews {
        val remoteView = RemoteViews(context.packageName, expandedTimerLayoutId)
        NotificationConfigurator().configureRemoteView(context, remoteView, pushData!!, whenTime)
        NotificationConfigurator().setNotificationDescription(
            context,
            timerNotificationData!!,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(context, timerNotificationData, remoteView)

        remoteView.setProgressBar(
            R.id.we_notification_progressBar,
            (pushData!!.timerTime - whenTime).toInt(),
            (System.currentTimeMillis() - whenTime).toInt(),
            false
        )
        remoteView.setChronometer(
            R.id.we_notification_timer,
            timeDiff,
            timerNotificationData!!.timerFormat,
            true
        )
        val clickIntent = PendingIntentFactory.constructPushClickPendingIntent(
            context,
            pushData!!.pushNotification,
            pushData!!.pushNotification.primeCallToAction,
            true
        )
        remoteView.setOnClickPendingIntent(R.id.we_notification_container, clickIntent)
        NotificationConfigurator().setCTAList(context, remoteView, pushData!!)
        return remoteView
    }

    private fun constructCollapsedTimerPushBase(
        context: Context,
        timerNotificationData: TimerStyle?,
        timeDiff: Long
    ): RemoteViews {
        val remoteView = RemoteViews(context.packageName, collapsedTimerLayoutId)
        val clickIntent = PendingIntentFactory.constructPushClickPendingIntent(
            context,
            pushData!!.pushNotification,
            pushData!!.pushNotification.primeCallToAction,
            true
        )
        remoteView.setOnClickPendingIntent(R.id.we_notification_container, clickIntent)

        NotificationConfigurator().configureRemoteView(context, remoteView, pushData!!, whenTime)
        NotificationConfigurator().setNotificationDescription(
            context,
            timerNotificationData!!,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(context, timerNotificationData, remoteView)

        remoteView.setProgressBar(
            R.id.we_notification_progressBar,
            (pushData!!.timerTime - whenTime).toInt(),
            (System.currentTimeMillis() - whenTime).toInt(),
            false
        )
        remoteView.setChronometer(
            R.id.we_notification_timer,
            timeDiff,
            timerNotificationData!!.timerFormat,
            true
        )

        return remoteView
    }

    private fun show(context: Context) {
        val channel = NotificationConfigurator().getDefaultNotificationChannel(
            context,
            "Sales",
            "Sales",
            "Notifications for upcoming Sales"
        )

        mBuilder!!.setChannelId(channel.id.toString())
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            if (System.currentTimeMillis() < pushData!!.timerTime && threadRunner.get())
                notify(
                    pushData!!.pushNotification.variationId.hashCode(),
                    mBuilder!!.build().apply {
                        this.flags = this.flags or Notification.FLAG_ONLY_ALERT_ONCE
                        this.flags = this.flags or Notification.FLAG_AUTO_CANCEL
                    })
        }
    }


}
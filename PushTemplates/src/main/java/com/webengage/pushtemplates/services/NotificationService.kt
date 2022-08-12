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
import com.webengage.pushtemplates.models.TimerStyleData
import com.webengage.pushtemplates.R
import com.webengage.sdk.android.PendingIntentFactory
import com.webengage.sdk.android.actions.render.PushNotificationData
import org.json.JSONObject

class NotificationService : Service() {
    private var context: Context? = null
    private var mBuilder: NotificationCompat.Builder? = null
    private var pushData: TimerStyleData? = null
    private var whenTime: Long = 0;
    private var collapsedTimerLayoutId = R.layout.layout_progressbar_collapsed
    private var expandedTimerLayoutId = R.layout.layout_progressbar_collapsed
    private var countDownTimer: CountDownTimer? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PushTemplates", "Service onStartCommand")
        if (intent!!.action.equals(Constants.PROGRESSBAR_ACTION)) {
            val pushDataPayload = intent.extras!!.getString(Constants.PAYLOAD)
                ?.let { PushNotificationData(JSONObject(it), applicationContext) }
            val timerData = TimerStyleData(applicationContext, pushDataPayload!!)
            this.context = applicationContext
            this.pushData = timerData
            val channelId =  NotificationConfigurator().getDefaultNotificationChannelID(context!!,pushDataPayload)
            this.mBuilder = NotificationCompat.Builder(context!!,channelId)
            this.whenTime = (intent.extras!!.getLong(Constants.WHEN_TIME))
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

    private fun startCounter(
        context: Context,
        mBuilder: NotificationCompat.Builder,
        pushData: TimerStyleData,
        timeDiff: Long
    ) {
        mBuilder.setTimeoutAfter(pushData.timerTime - System.currentTimeMillis())

        countDownTimer =
            object : CountDownTimer((pushData.timerTime - System.currentTimeMillis()), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if(System.currentTimeMillis() < pushData.timerTime) {
                        Log.d("PushTemplate", "updating: ")
                        mBuilder.setProgress(
                            (pushData.timerTime - whenTime).toInt(),
                            (System.currentTimeMillis() - whenTime).toInt(),
                            false
                        )
                        constructNotification(context, pushData, timeDiff)
                        show(context)
                    }
                    else{
                        Log.d("NotificationService", "timer cancelled: ")
                       stopSelf()
                    }
                }
                override fun onFinish() {
                    Log.d("NotificationService", "timer finished: ")
                    stopSelf()
                }

            }
        countDownTimer?.start()
    }

    private fun getNotification(
        timerData: TimerStyleData,
        mContext: Context
    ): NotificationCompat.Builder {

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
        val dismissIntent = PendingIntentFactory.constructPushDeletePendingIntent(context,pushData!!.pushNotification)
        dismissIntent.send()
        stopForeground(true)
        with(NotificationManagerCompat.from(context!!)) {
            this.cancel(pushData!!.pushNotification.variationId.hashCode())
        }
        Log.d("PushTemplates", "Service Destroyed")
        countDownTimer!!.cancel()
        super.onDestroy()
    }


    fun constructNotification(
        context: Context?,
        pushNotificationData: TimerStyleData?,
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
        timerNotificationData: TimerStyleData?,
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
        timerNotificationData: TimerStyleData?,
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
            context
        )
        with(NotificationManagerCompat.from(context)) {
            if (System.currentTimeMillis() < pushData!!.timerTime)
                notify(
                    pushData!!.pushNotification.variationId.hashCode(),
                    mBuilder!!.build().apply {
                        this.flags = this.flags or Notification.FLAG_ONLY_ALERT_ONCE
                        this.flags = this.flags or Notification.FLAG_AUTO_CANCEL
                    })
        }
    }


}
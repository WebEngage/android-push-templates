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
    private var whenTime: Long = 0
    private var collapsedTimerLayoutId = R.layout.layout_progressbar_collapsed
    private var expandedTimerLayoutId = R.layout.layout_progressbar_collapsed
    private var countDownTimer: CountDownTimer? = null
    private val updateFrequency: Long = 1000

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PushTemplates", "Service onStartCommand")
        if (intent!!.action.equals(Constants.PROGRESSBAR_ACTION)) {
            val pushNotificationData = intent.extras!!.getString(Constants.PAYLOAD)
                ?.let { PushNotificationData(JSONObject(it), applicationContext) }
            val timerData = TimerStyleData(applicationContext, pushNotificationData!!)
            this.context = applicationContext
            this.pushData = timerData
            val channelId = NotificationConfigurator().getDefaultNotificationChannelID(
                context!!,
                pushNotificationData
            )
            this.mBuilder = NotificationCompat.Builder(context!!, channelId)
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
        } else if (intent.action.equals(Constants.DELETE_ACTION)) {
            stopSelf()
        }
        return START_STICKY
    }


    /**
     * Start a CountDownTimer to update the notification periodically at a delay of updateFrequency.
     * The Timer will run till the System time becomes greater than the provided future time OR
     * if the service has been stopped.
     */
    private fun startCounter(
        context: Context,
        mBuilder: NotificationCompat.Builder,
        pushData: TimerStyleData,
        timeDiff: Long
    ) {
        mBuilder.setTimeoutAfter(pushData.timerTime - System.currentTimeMillis())
        val dismissIntent = PendingIntentFactory.constructPushDeletePendingIntent(
            context,
            pushData.pushNotification
        )

        countDownTimer =
            object :
                CountDownTimer((pushData.timerTime - System.currentTimeMillis()), updateFrequency) {
                override fun onTick(millisUntilFinished: Long) {
                    if (System.currentTimeMillis() < pushData.timerTime) {
                        mBuilder.setProgress(
                            (pushData.timerTime - whenTime).toInt(),
                            (System.currentTimeMillis() - whenTime).toInt(),
                            false
                        )
                        constructNotification(context, pushData, timeDiff)
                        show(context)
                    } else {
                        dismissIntent.send()
                        stopSelf()
                    }
                }

                override fun onFinish() {
                    dismissIntent.send()
                    stopSelf()
                }

            }
        countDownTimer?.start()
    }

    /**
     * Create or update the notification builder
     */
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

    /**
     * Stop the foreground service and remove notification before destroying the service
     */
    override fun onDestroy() {
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
        this.mBuilder!!.setContentIntent(
            NotificationConfigurator().getClickAndDismissPendingIntent(
                context!!,
                pushNotificationData!!.pushNotification,
                pushNotificationData.pushNotification.primeCallToAction.id
            )
        )
        NotificationConfigurator().setNotificationConfiguration(
            mBuilder!!,
            pushData!!,
            whenTime
        )
        this.mBuilder!!
            .setCustomContentView(
                constructCollapsedTimerPushBase(
                    context,
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


    /**
     * Construct remote view for expanded notification
     */
    private fun constructExpandedTimerPushBase(
        context: Context,
        timerNotificationData: TimerStyleData?,
        timeDiff: Long
    ): RemoteViews {
        val remoteView = RemoteViews(context.packageName, expandedTimerLayoutId)
        NotificationConfigurator().configureRemoteView(context, remoteView, pushData!!, whenTime)
        NotificationConfigurator().setNotificationDescription(
            timerNotificationData!!,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(timerNotificationData, remoteView)

        remoteView.setProgressBar(
            R.id.we_notification_progressBar,
            (pushData!!.timerTime - whenTime).toInt(),
            (System.currentTimeMillis() - whenTime).toInt(),
            false
        )
        remoteView.setChronometer(
            R.id.we_notification_timer,
            timeDiff,
            timerNotificationData.timerFormat,
            true
        )
        val clickIntent = NotificationConfigurator().getClickAndDismissPendingIntent(
            context,
            timerNotificationData.pushNotification,
            timerNotificationData.pushNotification.primeCallToAction.id
        )

        remoteView.setOnClickPendingIntent(R.id.we_notification_content, clickIntent)
        NotificationConfigurator().setCTAList(context, remoteView, pushData!!)
        return remoteView
    }

    /**
     * Construct remote view for collapsed notification
     */
    private fun constructCollapsedTimerPushBase(
        context: Context,
        timerNotificationData: TimerStyleData?,
        timeDiff: Long
    ): RemoteViews {
        val remoteView = RemoteViews(context.packageName, collapsedTimerLayoutId)
        val clickIntent = NotificationConfigurator().getClickAndDismissPendingIntent(
            context,
            timerNotificationData!!.pushNotification,
            timerNotificationData.pushNotification.primeCallToAction.id
        )
        remoteView.setOnClickPendingIntent(R.id.we_notification_content, clickIntent)

        NotificationConfigurator().configureRemoteView(context, remoteView, pushData!!, whenTime)
        NotificationConfigurator().setNotificationDescription(
            timerNotificationData,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(timerNotificationData, remoteView)

        remoteView.setProgressBar(
            R.id.we_notification_progressBar,
            (pushData!!.timerTime - whenTime).toInt(),
            (System.currentTimeMillis() - whenTime).toInt(),
            false
        )
        remoteView.setChronometer(
            R.id.we_notification_timer,
            timeDiff,
            timerNotificationData.timerFormat,
            true
        )

        return remoteView
    }


    /**
     * Show notification only if the current time is less than the count down time
     */
    private fun show(context: Context) {
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
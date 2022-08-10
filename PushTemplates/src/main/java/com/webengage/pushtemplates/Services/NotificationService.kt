package com.webengage.pushtemplates.Services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.Utils.Constants
import com.webengage.pushtemplates.Utils.NotificationConfigurator
import com.webengage.pushtemplates.DataTypes.TimerStyle
import com.webengage.pushtemplates.R
import com.webengage.sdk.android.Logger
import com.webengage.sdk.android.PendingIntentFactory
import com.webengage.sdk.android.actions.render.PushNotificationData
import org.json.JSONObject
import java.lang.Thread.sleep
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
    private val DEFAULT_TIME = DEFAULT_SECONDS * SECOND;
    private lateinit var thread: Thread
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PushTemplates", "Service onStartCommand")

        if (intent!!.action.equals(Constants.PROGRESSBAR_ACTION)) {
            val pushDataPayload = intent.extras!!.getString(Constants.PAYLOAD)
                ?.let { PushNotificationData(JSONObject(it), applicationContext) }
            val timerData = TimerStyle(pushDataPayload, applicationContext)
            this.context = applicationContext
            this.pushData = timerData
            this.mBuilder = NotificationCompat.Builder(context!!, "Sales")
            this.whenTime = (intent.extras!!.getLong("when"))
            val notification = getNotification(timerData, context!!)
            startForeground(
                pushData!!.pushNotification.variationId.hashCode(),
                notification.build().apply {
                    this.flags = this.flags or Notification.FLAG_ONLY_ALERT_ONCE
                    this.flags = this.flags or Notification.FLAG_AUTO_CANCEL
                }
            )
        }
        return START_STICKY

    }

    private fun startCounter(
        context: Context,
        mBuilder: NotificationCompat.Builder,
        pushData: TimerStyle,
        timeDiff: Long
    ) {

        thread = Thread() {
            run {
                while (threadRunner.get()) {
                    Log.d("PushTemplates", "Service Thread Started")
                    try {
                        for (i in (pushData.timerTime - System.currentTimeMillis()) downTo 0 step 2000) {
                            val timer: Long =
                                pushData.timerTime - System.currentTimeMillis() + SystemClock.elapsedRealtime()
                            mBuilder.setProgress(
                                (pushData.timerTime - whenTime).toInt(),
                                (System.currentTimeMillis() - whenTime).toInt(),
                                false
                            )
                            constructNotification(context, pushData, timeDiff)
                            show(context)
                            sleep(2000)
                        }
                    } catch (exception: InterruptedException) {
                        Thread.currentThread().interrupt()
                        Logger.e(
                            "PushTemplates",
                            "Service Thread interrupted"
                        )
                    }
                }
            }
        }
        thread.start()
        val handler = Handler()
        handler.postDelayed(
            Runnable {
                stopSelf()
            }, (pushData.timerTime - System.currentTimeMillis())
        )
    }

    private fun getNotification(
        timerData: TimerStyle,
        mContext: Context
    ): NotificationCompat.Builder {
        val channel = NotificationConfigurator().getDefaultNotificationChannel(
            context!!,
            "Sales",
            "Sales",
            "Notifications for upcoming Sales"
        )
        this.mBuilder = NotificationCompat.Builder(context!!, channel.id)

        val timeDiff =
            pushData!!.timerTime - System.currentTimeMillis() + SystemClock.elapsedRealtime()

        constructNotification(context, timerData, timeDiff)
        startCounter(context!!, mBuilder!!, pushData!!, timeDiff)

        return mBuilder!!
    }


    override fun onCreate() {
        Log.d("PushTemplates", "Service Created")

        super.onCreate()
    }

    override fun onDestroy() {
        threadRunner.set(false)
        thread.interrupt()
        stopForeground(true)
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
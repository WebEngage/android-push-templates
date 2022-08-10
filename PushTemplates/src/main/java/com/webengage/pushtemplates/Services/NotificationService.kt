package com.webengage.pushtemplates.Services

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.Utils.Constants
import com.webengage.pushtemplates.Utils.NotificationConfigurator
import com.webengage.pushtemplates.Receivers.PushIntentListener
import com.webengage.pushtemplates.DataTypes.TimerStyle
import com.webengage.pushtemplates.R
import com.webengage.sdk.android.Logger
import com.webengage.sdk.android.PendingIntentFactory
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.sdk.android.utils.htmlspanner.WEHtmlParserInterface
import org.json.JSONObject
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicBoolean

class NotificationService : Service() {
    private var threadRunner : AtomicBoolean = AtomicBoolean(true)
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
        Logger.d("Timer", "Service Bind")

        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d("Timer", "Service Start")

        if (intent!!.action.equals(Constants.PROGRESSBAR_ACTION)) {
            Logger.d("Timer", "Starting Service")
            val pushDataPayload = intent.extras!!.getString(Constants.PAYLOAD)
                ?.let { PushNotificationData(JSONObject(it), applicationContext) }
            val timerData = TimerStyle(pushDataPayload, applicationContext)
            this.context = applicationContext
            this.pushData = timerData
            this.mBuilder = NotificationCompat.Builder(context!!, "Sales")
            this.whenTime = (intent.extras!!.getLong("when"))
            val notification = getNotification(timerData, context!!)
            Logger.d("Timer", "Initialization Done")
            startForeground(
                pushData!!.pushNotification.experimentId.hashCode(),
                notification.build()
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
                    try {
                        for (i in (pushData.timerTime - System.currentTimeMillis()) downTo 0 step 2000) {
                            val timer: Long =
                                pushData.timerTime - System.currentTimeMillis() + SystemClock.elapsedRealtime()
                            mBuilder.setProgress((pushData.timerTime - whenTime).toInt(), (System.currentTimeMillis() - whenTime).toInt(),false)
                            constructNotification(context, pushData, timeDiff)
                            show(context)
                            Logger.d("Timer", "Thread should run -> $threadRunner")
                            sleep(2000)
                        }
                    } catch (exception: InterruptedException) {
                        Thread.currentThread().interrupt()
                        Logger.e(
                            "Timer",
                            "Thread interrupted"
                        )
                    }
                }
            }
        }
        Logger.d("Timer", "Starting Thread for service")
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

        Logger.d("Timer", "Notification Done")
        return mBuilder!!
    }


    override fun onCreate() {
        Logger.d("Timer", "Service Created")

        super.onCreate()
    }

    override fun onDestroy() {
        threadRunner.set(false)
        thread.interrupt()
        stopForeground(true)
        with(NotificationManagerCompat.from(context!!)) {
            this.cancel(pushData!!.pushNotification.experimentId.hashCode())
        }
        Logger.d("Timer", "Service Destroyed")
        super.onDestroy()
    }


    fun constructNotification(
        context: Context?,
        pushNotificationData: TimerStyle?,
        timeDiff: Long
    ) {
        this.mBuilder!!.setProgress((pushData!!.timerTime - whenTime).toInt(), (System.currentTimeMillis() - whenTime).toInt(),false)
        NotificationConfigurator().setNotificationConfiguration(context!!,mBuilder!!,pushData!!, whenTime)
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

    private fun setCTAList(remoteViews: RemoteViews, pushData: TimerStyle) {
        //created for future use
        var dismissSet = false
        var intent = Intent(context, PushIntentListener::class.java)
        intent.action = Constants.DELETE_ACTION
        intent.addCategory(context!!.packageName)
        intent.putExtra(Constants.PAYLOAD, pushData.pushNotification.pushPayloadJSON.toString())
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                context,
                pushData.pushNotification.experimentId.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                pushData.pushNotification.experimentId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        remoteViews.setViewVisibility(R.id.actions_container, View.VISIBLE)

        if (pushData.pushNotification.callToActions != null && pushData.pushNotification.callToActions.size > 1) {
            if (pushData.pushNotification.callToActions[1] != null) {
                remoteViews.setViewVisibility(R.id.action1_native, View.VISIBLE)
                val clickIntent = PendingIntentFactory.constructPushClickPendingIntent(
                    context,
                    pushData.pushNotification,
                    pushData.pushNotification.callToActions[1],
                    true
                )
                remoteViews.setTextViewText(
                    R.id.action1_native,
                    pushData.pushNotification.callToActions[1].text
                )

                remoteViews.setOnClickPendingIntent(R.id.action1_native, clickIntent)
            } else {
                remoteViews.setViewVisibility(R.id.action1_native, View.VISIBLE)
                remoteViews.setTextViewText(R.id.action1_native, "Dismiss")
                dismissSet = true
                remoteViews.setOnClickPendingIntent(R.id.action1_native, pendingIntent)
            }
            if (pushData.pushNotification.callToActions.size > 2) {
                remoteViews.setViewVisibility(R.id.action2_native, View.VISIBLE)
                remoteViews.setViewVisibility(R.id.action3_native, View.VISIBLE)
                remoteViews.setTextViewText(
                    R.id.action2_native,
                    pushData.pushNotification.callToActions[2].text
                )
                remoteViews.setTextViewText(R.id.action3_native, "Dismiss")

                val clickIntent = PendingIntentFactory.constructPushClickPendingIntent(
                    context,
                    pushData.pushNotification,
                    pushData.pushNotification.callToActions[2],
                    true
                )
                dismissSet = true

                remoteViews.setOnClickPendingIntent(R.id.action2_native, clickIntent)
                remoteViews.setOnClickPendingIntent(R.id.action3_native, pendingIntent)
            } else {
                if (!dismissSet) {

                    remoteViews.setViewVisibility(R.id.action2_native, View.VISIBLE)
                    remoteViews.setTextViewText(R.id.action2_native, "Dismiss")

                    dismissSet = true
                    remoteViews.setOnClickPendingIntent(R.id.action2_native, pendingIntent)
                }
            }
        } else {
            remoteViews.setViewVisibility(R.id.action1_native,View.VISIBLE)
            remoteViews.setTextViewText(R.id.action1_native, "Dismiss")
            remoteViews.setOnClickPendingIntent(R.id.action1_native, pendingIntent)
        }
    }

    private fun constructExpandedTimerPushBase(
        context: Context,
        timerNotificationData: TimerStyle?,
        timeDiff: Long
    ): RemoteViews {
        val remoteView = RemoteViews(context.packageName, expandedTimerLayoutId)
        NotificationConfigurator().configureRemoteView(context,remoteView,pushData!!,whenTime)
        NotificationConfigurator().setNotificationDescription(context,timerNotificationData!!,remoteView)
        NotificationConfigurator().setNotificationTitle(context,timerNotificationData,remoteView)

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

        NotificationConfigurator().configureRemoteView(context,remoteView,pushData!!,whenTime)
        NotificationConfigurator().setNotificationDescription(context,timerNotificationData!!,remoteView)
        NotificationConfigurator().setNotificationTitle(context,timerNotificationData,remoteView)

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
        Log.d("Timer", "Showing Notification")

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
                notify(pushData!!.pushNotification.variationId.hashCode(), mBuilder!!.build())
        }
    }


}
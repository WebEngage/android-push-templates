package com.webengage.pushtemplates.templates

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.R
import com.webengage.pushtemplates.models.TimerStyleData
import com.webengage.pushtemplates.utils.GifHelper
import com.webengage.pushtemplates.utils.ImageUtils
import com.webengage.pushtemplates.utils.NetworkUtils
import com.webengage.pushtemplates.utils.NotificationConfigurator
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.sdk.android.utils.http.WENetworkUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GifRenderer {



    private lateinit var context: Context
    private lateinit var mBuilder: NotificationCompat.Builder
    private var collapsedTimerLayoutId = R.layout.layout_gif_template
    private var expandedTimerLayoutId = R.layout.layout_gif_template
    private var whenTime: Long = 0
    private var bitmapList: ArrayList<Bitmap?> = ArrayList()
    private lateinit var pushData: PushNotificationData

    fun onRender(
        mContext: Context?,
        pushNotificationData: PushNotificationData?
    ): Boolean {
        this.pushData = pushNotificationData!!
        this.context = mContext!!
        this.whenTime = System.currentTimeMillis()
        this.mBuilder =
            NotificationCompat.Builder(
                context,
                NotificationConfigurator().getDefaultNotificationChannelID(
                    context,
                    pushNotificationData
                )
            )


        CoroutineScope(Dispatchers.Default).launch {
            val isString = GifHelper().downloadGif(pushNotificationData.customData.getString("gif_url")!!);
            bitmapList = GifHelper().decodeGif(context,isString!!)
            constructNotification(context, pushNotificationData)
            show(context)
        }
        return true
    }

    private fun constructNotification(context: Context?, pushNotificationData: PushNotificationData) {
        NotificationConfigurator().setNotificationConfiguration(
            mBuilder,
            pushNotificationData,
            whenTime
        )
        NotificationConfigurator().setDismissIntent(
            context!!,
            mBuilder,
            pushNotificationData
        )
        NotificationConfigurator().setClickIntent(
            context,
            mBuilder,
            pushNotificationData
        )

        this.mBuilder.setCustomContentView(
            constructCollapsedGifPushBase(
                context,
                pushNotificationData
            )
        )
        this.mBuilder.setCustomBigContentView(
            constructExpandedGifPushBase(
                context,
                pushNotificationData
            )
        )
    }


    /**
     * Create and attach the expanded layout for the notification
     */
    private fun constructExpandedGifPushBase(
        context: Context,
        pushNotificationData: PushNotificationData
    ): RemoteViews {

        val remoteView = RemoteViews(context.packageName, expandedTimerLayoutId)

        NotificationConfigurator().configureRemoteView(
            context,
            remoteView,
            pushNotificationData,
            whenTime
        )

        NotificationConfigurator().setTitleMaxLines(remoteView,2)
        NotificationConfigurator().setDescriptionMaxLines(remoteView,2)

        NotificationConfigurator().setNotificationDescription(
            context,
            pushNotificationData,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(
            context,
            pushNotificationData,
            remoteView
        )

        NotificationConfigurator().setCTAList(
            context,
            remoteView,
            pushNotificationData,
            false
        )

        NotificationConfigurator().setClickIntent(
            context,
            remoteView,
            pushNotificationData,
        )


        NotificationConfigurator().setNotificationViewFlipper(
            context,
            R.layout.gif_item,
            remoteView,
            pushNotificationData,
            bitmapList
        )


        return remoteView
    }

    /**
     * Create and attach the collapsed layout for the notification
     */
    private fun constructCollapsedGifPushBase(
        context: Context,
        pushNotificationData: PushNotificationData
    ): RemoteViews {
        val remoteView = RemoteViews(context.packageName, collapsedTimerLayoutId)

        NotificationConfigurator().configureRemoteView(
            context,
            remoteView,
            pushNotificationData,
            whenTime
        )
        NotificationConfigurator().setNotificationDescription(
            context,
            pushNotificationData,
            remoteView
        )
        NotificationConfigurator().setNotificationTitle(
            context,
            pushNotificationData,
            remoteView
        )
        NotificationConfigurator().setClickIntent(
            context,
            remoteView,
            pushNotificationData,
        )

        return remoteView
    }

    /**
     * Show notification if the current system time is less than teh provided future time
     */
    private fun show(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            notify(pushData.variationId.hashCode(), mBuilder.build().apply {
                this.flags = this.flags or Notification.FLAG_AUTO_CANCEL
                this.flags = this.flags or Notification.FLAG_ONLY_ALERT_ONCE
            })
        }
    }
}
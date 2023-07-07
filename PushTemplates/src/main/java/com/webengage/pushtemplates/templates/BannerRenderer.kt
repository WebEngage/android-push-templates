package com.webengage.pushtemplates.templates

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.R
import com.webengage.pushtemplates.models.BannerStyleData
import com.webengage.pushtemplates.utils.Constants
import com.webengage.pushtemplates.utils.ImageUtils
import com.webengage.pushtemplates.utils.NotificationConfigurator
import com.webengage.sdk.android.actions.render.PushNotificationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BannerRenderer {
    private lateinit var mContext: Context
    private lateinit var mBuilder: NotificationCompat.Builder
    private lateinit var pushData: BannerStyleData
    private var whenTime: Long = 0
    private var collapsedTimerLayoutId = R.layout.layout_banner_template
    private var expandedTimerLayoutId = R.layout.layout_banner_template
    private var bitmapList: ArrayList<Bitmap?> = ArrayList()

    fun onRender(context: Context, pushNotificationData: PushNotificationData): Boolean {
        this.mContext = context
        this.pushData = BannerStyleData(context, pushNotificationData)
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
            downloadImages(context, pushData)
            constructNotification(context, pushData)
            showNotification(context)
        }
        return true
    }

    suspend fun downloadImages(context: Context, pushNotificationData: BannerStyleData) {
        if (pushNotificationData.pushNotification.bigPictureStyleData.bigPictureUrl != null)
            bitmapList.add(
                0,
                ImageUtils().getBitmapFromURL(pushNotificationData.pushNotification.bigPictureStyleData.bigPictureUrl)
            )

        if (pushNotificationData.collapsedImageURL != null)
            bitmapList.add(
                1,
                ImageUtils().getBitmapFromURL(pushNotificationData.collapsedImageURL!!)
            )
    }

    fun showNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            notify(pushData.pushNotification.variationId.hashCode(), mBuilder.build().apply {
                this.flags = this.flags or Notification.FLAG_AUTO_CANCEL
                this.flags = this.flags or Notification.FLAG_ONLY_ALERT_ONCE
            })
        }
    }


    fun constructNotification(context: Context, pushNotificationData: BannerStyleData) {
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
            constructCollapsedBannerPushBase(
                context,
                pushNotificationData
            )
        )
        this.mBuilder.setCustomBigContentView(
            constructExpandedBannerPushBase(
                context,
                pushNotificationData
            )
        )
    }

    fun constructCollapsedBannerPushBase(
        context: Context,
        pushNotificationData: BannerStyleData
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, collapsedTimerLayoutId)
        //todo
        //set title and desc and app details

        NotificationConfigurator().configureRemoteView(
            context,
            remoteViews,
            pushNotificationData.pushNotification,
            whenTime
        )

        NotificationConfigurator().setNotificationTitle(
            context,
            pushNotificationData.pushNotification,
            remoteViews
        )
        NotificationConfigurator().setNotificationDescription(
            context,
            pushNotificationData.pushNotification,
            remoteViews
        )

        NotificationConfigurator().setClickIntent(
            context,
            remoteViews,
            pushNotificationData.pushNotification
        )

        //check mode and modify image views accordingly
        if (pushNotificationData.collapsedMode.equals(Constants.DEFAULT_MODE, true))
            configureCollapsedModeDefaultBg(context, remoteViews, pushNotificationData)
        else if (pushNotificationData.collapsedMode.equals(Constants.FULL_BACKGROUND_MODE, true))
            configureCollapsedModeFullBg(context, remoteViews, pushNotificationData)
        else if (pushNotificationData.collapsedMode.equals(Constants.HALF_BACKGROUND_MODE, true))
            configureCollapsedModeHalfBg(context, remoteViews, pushNotificationData)

        return remoteViews
    }

    fun constructExpandedBannerPushBase(
        context: Context,
        pushNotificationData: BannerStyleData
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, expandedTimerLayoutId)

        //todo
        //set title and desc and app details
        NotificationConfigurator().configureRemoteView(
            context,
            remoteViews,
            pushNotificationData.pushNotification,
            whenTime
        )

        NotificationConfigurator().setNotificationTitle(
            context,
            pushNotificationData.pushNotification,
            remoteViews
        )
        NotificationConfigurator().setNotificationDescription(
            context,
            pushNotificationData.pushNotification,
            remoteViews
        )

        NotificationConfigurator().setTitleMaxLines(remoteViews, 2)
        NotificationConfigurator().setDescriptionMaxLines(remoteViews, 2)

        //check mode and modify image views accordingly
        if (pushNotificationData.expandedMode.equals(Constants.DEFAULT_MODE, true))
            configureExpandedModeDefaultBg(context, remoteViews, pushNotificationData)
        else if (pushNotificationData.expandedMode.equals(Constants.FULL_BACKGROUND_MODE, true))
            configureExpandedModeFullBg(context, remoteViews, pushNotificationData)

        //set CTA buttons

        NotificationConfigurator().setCTAList(
            context,
            remoteViews,
            pushNotificationData.pushNotification,
            pushNotificationData.showDismissCTA
        )

        NotificationConfigurator().setClickIntent(
            context,
            remoteViews,
            pushNotificationData.pushNotification
        )
        return remoteViews
    }

    fun configureCollapsedModeHalfBg(
        context: Context,
        remoteViews: RemoteViews,
        pushNotificationData: BannerStyleData
    ) {
        //todo
        //hide R.id.we_notification_image
        remoteViews.setViewVisibility(R.id.we_notification_image, View.GONE)
        //hide R.id.we_notification_image2
        remoteViews.setViewVisibility(R.id.we_notification_image2, View.GONE)
        //hide R.id.we_notification_image3
        remoteViews.setViewVisibility(R.id.we_notification_image3, View.GONE)
        //show R.id.we_notification_image4
        remoteViews.setViewVisibility(R.id.we_notification_image4, View.VISIBLE)
        //check if collapsedImageUrl is null
        if (bitmapList.size > 1)
            remoteViews.setImageViewBitmap(R.id.we_notification_image4, bitmapList[1])
        else
            remoteViews.setImageViewBitmap(R.id.we_notification_image4, bitmapList[0])
        //if yes , use image url and render we_notification_image3
        //use adaptive title desc appName if bgColor is transparent. Override appName with fontColor if present
    }

    fun configureCollapsedModeFullBg(
        context: Context,
        remoteViews: RemoteViews,
        pushNotificationData: BannerStyleData
    ) {
        //todo
        //hide R.id.we_notification_image
        remoteViews.setViewVisibility(R.id.we_notification_image, View.GONE)
        //hide R.id.we_notification_image2
        remoteViews.setViewVisibility(R.id.we_notification_image2, View.GONE)
        //hide R.id.we_notification_image4
        remoteViews.setViewVisibility(R.id.we_notification_image4, View.GONE)
        //show R.id.we_notification_image3
        remoteViews.setViewVisibility(R.id.we_notification_image3, View.VISIBLE)
        if (bitmapList.size > 1)
            remoteViews.setImageViewBitmap(R.id.we_notification_image3, bitmapList[1])
        else
            remoteViews.setImageViewBitmap(R.id.we_notification_image3, bitmapList[0])
        //check if collapsedImageUrl is null
        //if yes , use image url and render on we_notification_image2
        //if collapsedImageUrl present , use it and render on we_notification_image2
        //use black title desc appName. Override appName with fontColor if present
    }

    fun configureCollapsedModeDefaultBg(
        context: Context,
        remoteViews: RemoteViews,
        pushNotificationData: BannerStyleData
    ) {
        //todo
        //hide R.id.we_notification_image
        remoteViews.setViewVisibility(R.id.we_notification_image, View.GONE)
        //hide R.id.we_notification_image3
        remoteViews.setViewVisibility(R.id.we_notification_image2, View.GONE)
        //hide R.id.we_notification_image2
        remoteViews.setViewVisibility(R.id.we_notification_image3, View.GONE)
        //hide R.id.we_notification_image4
        remoteViews.setViewVisibility(R.id.we_notification_image4, View.GONE)

        //use adaptive title desc appName if bgColor is transparent. Override appName with fontColor if present
    }


    fun configureExpandedModeFullBg(
        context: Context,
        remoteViews: RemoteViews,
        pushNotificationData: BannerStyleData
    ) {
        //todo
        //hide R.id.we_notification_image
        remoteViews.setViewVisibility(R.id.we_notification_image, View.GONE)
        //hide R.id.we_notification_image3
        remoteViews.setViewVisibility(R.id.we_notification_image3, View.GONE)
        //show R.id.we_notification_image2
        remoteViews.setViewVisibility(R.id.we_notification_image2, View.VISIBLE)
        //hide R.id.we_notification_image4
        remoteViews.setViewVisibility(R.id.we_notification_image4, View.GONE)

        //Use image url and render on we_notification_image2

        remoteViews.setImageViewBitmap(R.id.we_notification_image2, bitmapList[0])
        //use black title desc appName. Override appName with fontColor if present
    }

    fun configureExpandedModeDefaultBg(
        context: Context,
        remoteViews: RemoteViews,
        pushNotificationData: BannerStyleData
    ) {
        //todo
        //show R.id.we_notification_image
        remoteViews.setViewVisibility(R.id.we_notification_image, View.VISIBLE)
        //hide R.id.we_notification_image2
        remoteViews.setViewVisibility(R.id.we_notification_image2, View.GONE)
        //hide R.id.we_notification_image3
        remoteViews.setViewVisibility(R.id.we_notification_image3, View.GONE)
        //hide R.id.we_notification_image4
        remoteViews.setViewVisibility(R.id.we_notification_image4, View.GONE)
        remoteViews.setImageViewBitmap(R.id.we_notification_image, bitmapList[0])

        //Use image url and render on we_notification_image
        //use adaptive title desc appName if bgColor is transparent. Override appName with fontColor if present
    }
}
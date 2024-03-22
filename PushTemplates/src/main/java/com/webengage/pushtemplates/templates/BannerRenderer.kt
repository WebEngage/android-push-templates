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
    private var collapsedBannerLayoutId = R.layout.layout_banner_template_pt
    private var expandedBannerLayoutId = R.layout.layout_banner_template_pt
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
            downloadImages(pushData)
            constructNotification(context, pushData)
            showNotification(context)
        }
        return true
    }

    private suspend fun downloadImages(pushNotificationData: BannerStyleData) {
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

    private fun showNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            notify(pushData.pushNotification.variationId.hashCode(), mBuilder.build().apply {
                this.flags = this.flags or Notification.FLAG_AUTO_CANCEL
                this.flags = this.flags or Notification.FLAG_ONLY_ALERT_ONCE
            })
        }
    }


    private fun constructNotification(context: Context, pushNotificationData: BannerStyleData) {
        NotificationConfigurator().setNotificationConfiguration(
            mBuilder,
            pushNotificationData.pushNotification,
            whenTime
        )

        NotificationConfigurator().setDismissIntent(
            context,
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

    private fun constructCollapsedBannerPushBase(
        context: Context,
        pushNotificationData: BannerStyleData
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, collapsedBannerLayoutId)
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
            configureCollapsedModeDefaultBg(context, remoteViews)
        else if (pushNotificationData.collapsedMode.equals(Constants.FULL_BACKGROUND_MODE, true))
            configureCollapsedModeFullBg(context, remoteViews, pushNotificationData)
        else if (pushNotificationData.collapsedMode.equals(Constants.HALF_BACKGROUND_MODE, true))
            configureCollapsedModeHalfBg(remoteViews)

        return remoteViews
    }

    private fun constructExpandedBannerPushBase(
        context: Context,
        pushNotificationData: BannerStyleData
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, expandedBannerLayoutId)

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

    private fun configureCollapsedModeHalfBg(
        remoteViews: RemoteViews
    ) {
        //hide default banner image
        remoteViews.setViewVisibility(R.id.we_notification_image, View.GONE)
        //hide collapsed bg image
        remoteViews.setViewVisibility(R.id.we_notification_expanded_bg_image, View.GONE)
        //hide expanded bg image
        remoteViews.setViewVisibility(R.id.we_notification_collapsed_bg_image, View.GONE)
        //show half image
        remoteViews.setViewVisibility(R.id.we_notification_half_image, View.VISIBLE)

        remoteViews.setViewVisibility(R.id.large_icon, View.GONE)

        //check if collapsedImageUrl is null
        if (bitmapList.size > 1){
            if(bitmapList[1] != null)
                remoteViews.setImageViewBitmap(R.id.we_notification_half_image, bitmapList[1])
            else
                remoteViews.setViewVisibility(R.id.we_notification_half_image, View.GONE)
        } else if(bitmapList[0] != null) {
            remoteViews.setImageViewBitmap(R.id.we_notification_half_image, bitmapList[0])
        } else {
            remoteViews.setViewVisibility(R.id.we_notification_half_image, View.GONE)
        }

    }

    private fun configureCollapsedModeFullBg(
        context: Context,
        remoteViews: RemoteViews,
        pushNotificationData: BannerStyleData
    ) {
        //hide default banner image
        remoteViews.setViewVisibility(R.id.we_notification_image, View.GONE)
        //hide collapsed bg image
        remoteViews.setViewVisibility(R.id.we_notification_expanded_bg_image, View.GONE)
        //hide half image
        remoteViews.setViewVisibility(R.id.we_notification_half_image, View.GONE)
        //show expanded bg image
        remoteViews.setViewVisibility(R.id.we_notification_collapsed_bg_image, View.VISIBLE)

        remoteViews.setViewVisibility(R.id.large_icon, View.GONE)

        var bitmapAvailable = false
        //check if collapsedImageUrl is null
        if (bitmapList.size > 1){
            if(bitmapList[1] != null){
                bitmapAvailable = true
                remoteViews.setImageViewBitmap(R.id.we_notification_collapsed_bg_image, bitmapList[1])
            }
            else
                remoteViews.setViewVisibility(R.id.we_notification_collapsed_bg_image, View.GONE)
        } else if(bitmapList[0] != null) {
            bitmapAvailable = true
            remoteViews.setImageViewBitmap(R.id.we_notification_collapsed_bg_image, bitmapList[0])
        } else {
            remoteViews.setViewVisibility(R.id.we_notification_collapsed_bg_image, View.GONE)
        }

        if(bitmapAvailable){
            //for hiding adaptive text and showing normal text in case of background image
            NotificationConfigurator().setAdaptiveTextViewVisibility(remoteViews, pushNotificationData.pushNotification)

            NotificationConfigurator().configureCustomColorForPushBase(remoteViews, pushNotificationData.fontColor)

            NotificationConfigurator().setPaddingForFullBackground(context, remoteViews)
        }

    }

    private fun configureCollapsedModeDefaultBg(
        context: Context,
        remoteViews: RemoteViews
    ) {

        NotificationConfigurator().setBigImage(context, pushData.pushNotification, remoteViews)
        
        //hide default banner image
        remoteViews.setViewVisibility(R.id.we_notification_image, View.GONE)
        //hide expanded bg image
        remoteViews.setViewVisibility(R.id.we_notification_expanded_bg_image, View.GONE)
        //hide collapsed bg image
        remoteViews.setViewVisibility(R.id.we_notification_collapsed_bg_image, View.GONE)
        //hide half image
        remoteViews.setViewVisibility(R.id.we_notification_half_image, View.GONE)

        //use adaptive title desc appName if bgColor is transparent. Override appName with fontColor if present
    }


    private fun configureExpandedModeFullBg(
        context: Context,
        remoteViews: RemoteViews,
        pushNotificationData: BannerStyleData
    ) {

        //hide default banner image
        remoteViews.setViewVisibility(R.id.we_notification_image, View.GONE)
        //hide expanded bg image
        remoteViews.setViewVisibility(R.id.we_notification_collapsed_bg_image, View.GONE)
        //show collapsed bg image
        remoteViews.setViewVisibility(R.id.we_notification_expanded_bg_image, View.VISIBLE)
        //hide half image
        remoteViews.setViewVisibility(R.id.we_notification_half_image, View.GONE)

        remoteViews.setViewVisibility(R.id.large_icon, View.GONE)

        //Use image url and render on we_notification_expanded_bg_image
        if (bitmapList[0] != null){
            remoteViews.setImageViewBitmap(R.id.we_notification_expanded_bg_image, bitmapList[0])

            //for hiding adaptive text and showing normal text in case of background image
            NotificationConfigurator().setAdaptiveTextViewVisibility(
                remoteViews,
                pushNotificationData.pushNotification
            )

            NotificationConfigurator().configureCustomColorForPushBase(remoteViews, pushNotificationData.fontColor)

            NotificationConfigurator().setPaddingForFullBackground(context, remoteViews)

        } else {
            remoteViews.setViewVisibility(R.id.we_notification_expanded_bg_image, View.GONE)
        }


    }

    private fun configureExpandedModeDefaultBg(
        context: Context,
        remoteViews: RemoteViews,
        pushNotificationData: BannerStyleData
    ) {

        //show default banner image
        remoteViews.setViewVisibility(R.id.we_notification_image, View.VISIBLE)
        //hide collapsed bg image
        remoteViews.setViewVisibility(R.id.we_notification_expanded_bg_image, View.GONE)
        //hide expanded bg image
        remoteViews.setViewVisibility(R.id.we_notification_collapsed_bg_image, View.GONE)
        //hide half image
        remoteViews.setViewVisibility(R.id.we_notification_half_image, View.GONE)

        NotificationConfigurator().setNotificationBanner(
            remoteViews,
            pushNotificationData.pushNotification,
            bitmapList
        )

        if (bitmapList[0] != null){
            remoteViews.setImageViewBitmap(R.id.we_notification_image, bitmapList[0])
        } else {
            remoteViews.setViewVisibility(R.id.we_notification_image, View.GONE)
        }

        NotificationConfigurator().setBigImage(context, pushData.pushNotification, remoteViews)
    }
}
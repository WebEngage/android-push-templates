package com.webengage.template

import android.app.Application
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.webengage.pushtemplates.CustomCallback
import com.webengage.sdk.android.PushChannelConfiguration
import com.webengage.sdk.android.WebEngage
import com.webengage.sdk.android.WebEngageActivityLifeCycleCallbacks
import com.webengage.sdk.android.WebEngageConfig

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val mNotificationManager = applicationContext.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannelGroup(
                NotificationChannelGroup(
                    "test-group",
                    "test-group"
                )
            )
        }

        val pushChannelConfiguration =
            PushChannelConfiguration.Builder()
                .setNotificationChannelName("test")
                .setNotificationChannelGroup("test-group")
                .setNotificationChannelImportance(NotificationManager.IMPORTANCE_DEFAULT)
                .setNotificationChannelDescription("test notification channel")
                .setNotificationChannelSound("light")
                .setNotificationChannelLightColor(Color.RED)
                .setNotificationChannelVibration(true)
                .setNotificationChannelShowBadge(true)
                .build()

        val builder: WebEngageConfig.Builder =
            WebEngageConfig.Builder()
                .setWebEngageKey("LICENSE_CODE")
                .setDebugMode(true)
                .setAutoGCMRegistrationFlag(false)
                .setPushSmallIcon(R.drawable.ic_notification_small)
                .setPushLargeIcon(R.drawable.ic_notification_big)
                .setSessionDestroyTime(40)
                .setDefaultPushChannelConfiguration(pushChannelConfiguration)
                .setPushAccentColor(Color.GREEN)

        registerActivityLifecycleCallbacks(
            WebEngageActivityLifeCycleCallbacks(
                this,
                builder.build()
            )
        )
        WebEngage.registerCustomPushRenderCallback(CustomCallback())
        WebEngage.registerCustomPushRerenderCallback(CustomCallback())

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            try {
                val token: String? = task.result
                WebEngage.get().setRegistrationID(token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun onTrimMemory(level: Int) {
        Log.d("PushTemplates", "App onTrimMemory $level")
        super.onTrimMemory(level)
    }

    override fun onLowMemory() {
        Log.d("PushTemplates", "App onLowMemory")
        super.onLowMemory()
    }

    override fun onTerminate() {
        Log.d("PushTemplates", "App onTerminate")
        super.onTerminate()
    }
}
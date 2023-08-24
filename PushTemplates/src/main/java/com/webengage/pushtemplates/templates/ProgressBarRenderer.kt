package com.webengage.pushtemplates.templates

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import com.webengage.pushtemplates.utils.Constants
import com.webengage.pushtemplates.services.NotificationService
import com.webengage.sdk.android.WebEngage.startService
import com.webengage.sdk.android.actions.render.PushNotificationData

class ProgressBarRenderer {

    private lateinit var context: Context
    private lateinit var pushData: PushNotificationData
    private var whenTime: Long = 0

    fun onRender(mContext: Context?, pushNotificationData: PushNotificationData?): Boolean {
        this.context = mContext!!
        this.pushData = pushNotificationData!!
        this.whenTime = System.currentTimeMillis()

        //If the provided future time is less that the system time, then do not render notification
        if (pushData.customData.containsKey(Constants.FUTURE_TIME) &&
            pushData.customData.getString(Constants.FUTURE_TIME)!!.toLong() < System.currentTimeMillis()
        )
            return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val flag: Boolean
            var foreGroundServiceType: Int? = null
            val packageInfo: PackageInfo = mContext.packageManager.getPackageInfo(
                mContext.packageName,
                PackageManager.GET_SERVICES or PackageManager.GET_PERMISSIONS
            )
            val services = packageInfo.services
            for (serviceInfo in services) {
                if (serviceInfo.name.equals("com.webengage.pushtemplates.services.NotificationService")) {
                    foreGroundServiceType = serviceInfo.foregroundServiceType
                }
            }
            val requestedPermissions: Array<String?>? = packageInfo.requestedPermissions
            flag = when (foreGroundServiceType) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA -> {
                    requestedPermissions?.contains("android.permission.FOREGROUND_SERVICE_CAMERA") == true
                }

                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE -> {
                    requestedPermissions?.contains("android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE") == true
                }

                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC -> {
                    requestedPermissions?.contains("android.permission.FOREGROUND_SERVICE_DATA_SYNC") == true
                }

                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH -> {
                    requestedPermissions?.contains("android.permission.FOREGROUND_SERVICE_HEALTH") == true
                }

                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION -> {
                    requestedPermissions?.contains("android.permission.FOREGROUND_SERVICE_LOCATION") == true
                }

                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK -> {
                    requestedPermissions?.contains("android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK") == true
                }

                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE -> {
                    requestedPermissions?.contains("android.permission.FOREGROUND_SERVICE_SPECIAL_USE") == true
                }

                ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING -> {
                    requestedPermissions?.contains("android.permission.FOREGROUND_SERVICE_REMOTE_MESSAGING") == true
                }

                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION -> {
                    requestedPermissions?.contains("android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION") == true
                }

                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL -> {
                    requestedPermissions?.contains("android.permission.FOREGROUND_SERVICE_PHONE_CALL") == true
                }

                ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE -> {
                    requestedPermissions?.contains("android.permission.FOREGROUND_SERVICE_SHORT_SERVICE") == true
                }

                ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED -> {
                    requestedPermissions?.contains("android.permission.FOREGROUND_SERVICE_SYSTEM_EXEMPTED") == true
                }

                ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST -> {
                    requestedPermissions?.contains("android.permission.FOREGROUND_SERVICE_MANIFEST") == true
                }

                else -> {
                    false
                }
            }
            return if(flag) {
                attachToService(mContext, pushData)
                true
            } else {
                false
            }
        } else {
            attachToService(mContext, pushData)
            return true
        }
    }

    /**
    Create a foreground service to periodically update the notification progress bar at set intervals.
     */
    private fun attachToService(context: Context, pushData: PushNotificationData?) {
        val intent = Intent(context, NotificationService::class.java)
        intent.action = Constants.PROGRESS_BAR_ACTION
        intent.putExtra(Constants.PAYLOAD, pushData!!.pushPayloadJSON.toString())
        intent.putExtra(Constants.WHEN_TIME, whenTime)
        startService(intent, context)
    }

}
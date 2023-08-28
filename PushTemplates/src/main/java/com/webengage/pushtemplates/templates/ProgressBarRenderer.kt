package com.webengage.pushtemplates.templates

import android.content.Context
import android.content.Intent
import android.util.Log
import com.webengage.pushtemplates.utils.Constants
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import com.webengage.pushtemplates.services.NotificationService
import com.webengage.pushtemplates.utils.Constants
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
        ) {
            Log.d("PushTemplates", "The future time provided is less than current device time")
            return false
        }
        //If targetSdk = 34 and device SDK = 34 check added for foreground service type added or not
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && mContext.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if(isForegroundServiceTypeAdded(mContext)) {
                attachToService(mContext, pushData)
                true
            } else {
                Log.e("PushTemplates","Respective Foreground service permission not added")
                false
            }
        } else {
            attachToService(mContext, pushData)
            true
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

    /**
     * Checks if foreground service type is added or not and respective foreground service permission is present
     * return true is both present and false is any of them not present
     */
    private fun isForegroundServiceTypeAdded(mContext: Context) : Boolean{
        var foreGroundServiceType: Int? = null
        val packageInfo: PackageInfo = mContext.packageManager.getPackageInfo(
            mContext.packageName,
            PackageManager.GET_SERVICES or PackageManager.GET_PERMISSIONS
        )
        val services = packageInfo.services
        for (serviceInfo in services) {
            if (serviceInfo.name.equals(Constants.NOTIFICATION_SERVICE)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    foreGroundServiceType = serviceInfo.foregroundServiceType
                }
            }
        }
        val requestedPermissions: Array<String?>? = packageInfo.requestedPermissions
        if(foreGroundServiceType == null) {
            Log.e("PushTemplates","Foreground service type not added")
            return false
        }
        return when (foreGroundServiceType) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA -> {
                requestedPermissions?.contains(Constants.FOREGROUND_SERVICE_CAMERA_PERMISSION) == true
            }

            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE -> {
                requestedPermissions?.contains(Constants.FOREGROUND_SERVICE_CONNECTED_DEVICE_PERMISSION) == true
            }

            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC -> {
                requestedPermissions?.contains(Constants.FOREGROUND_SERVICE_DATA_SYNC_PERMISSION) == true
            }

            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH -> {
                requestedPermissions?.contains(Constants.FOREGROUND_SERVICE_HEALTH_PERMISSION) == true
            }

            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION -> {
                requestedPermissions?.contains(Constants.FOREGROUND_SERVICE_LOCATION_PERMISSION) == true
            }

            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK -> {
                requestedPermissions?.contains(Constants.FOREGROUND_SERVICE_MEDIA_PLAYBACK_PERMISSION) == true
            }

            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE -> {
                requestedPermissions?.contains(Constants.FOREGROUND_SERVICE_SPECIAL_USE_PERMISSION) == true
            }

            ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING -> {
                requestedPermissions?.contains(Constants.FOREGROUND_SERVICE_REMOTE_MESSAGING_PERMISSION) == true
            }

            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION -> {
                requestedPermissions?.contains(Constants.FOREGROUND_SERVICE_MEDIA_PROJECTION_PERMISSION) == true
            }

            ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL -> {
                requestedPermissions?.contains(Constants.FOREGROUND_SERVICE_PHONE_CALL_PERMISSION) == true
            }

            ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE -> {
                true
            }

            ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED -> {
                requestedPermissions?.contains(Constants.FOREGROUND_SERVICE_SYSTEM_EXEMPTED_PERMISSION) == true
            }

            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE -> {
                requestedPermissions?.contains(Constants.FOREGROUND_SERVICE_MICROPHONE_PERMISSION) == true
            }

            else -> {
                false
            }
        }
    }
}
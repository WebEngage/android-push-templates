package com.webengage.pushtemplates.utils

object Constants {

    const val TIMER_FORMAT = "format"

    //KEYS for Push Template Customizations
    const val FUTURE_TIME = "future_time"
    const val TEMPLATE_TYPE = "template_type"
    const val TIMER_COLOR = "timer_color"
    const val PROGRESS_BAR_COLOR = "pb_color"
    const val PROGRESS_BAR_BACKGROUND_COLOR = "pb_bg_color"
    const val SHOW_DISMISS_CTA = "show_dismiss_cta"
    const val COLLAPSED_MODE_IMAGE_URL = "we_cm_img"
    const val FONT_COLOR = "we_an_c"
    const val DURATION = "we_duration"

    //Modes for Banner Styles
    const val HALF_BACKGROUND_MODE = "half_bg"
    const val FULL_BACKGROUND_MODE = "full_bg"
    const val DEFAULT_MODE = "default"

    //Types of Custom Push Templates
    const val PROGRESS_BAR = "bar"
    const val COUNTDOWN = "timer"
    const val BANNER_1 = "banner1"
    const val BANNER_2 = "banner2"
    const val BANNER_3 = "banner3"
    const val BANNER_4 = "banner4"
    const val BANNER_5 = "banner5"

    //Intent Actions
    const val DELETE_ACTION = "com.webengage.push.delete"
    const val PROGRESS_BAR_ACTION = "com.webengage.push.PROGRESS_BAR"
    const val CLICK_ACTION = "com.webengage.push.click"

    //Internal Constants
    const val PAYLOAD = "payload"
    const val DISMISS_CTA = "Dismiss"
    const val WHEN_TIME = "whenTime"
    const val LOG_DISMISS = "logDismiss"
    const val CTA_ID = "ctaID"
    const val SECOND: Long = 1000
    const val MINUTE: Long = 60 * SECOND
    const val REMOTE_VIEW_MAX_SIZE = 4000000  //Size is in bytes

    //Permissions
    const val FOREGROUND_SERVICE_CAMERA_PERMISSION = "android.permission.FOREGROUND_SERVICE_CAMERA"
    const val FOREGROUND_SERVICE_CONNECTED_DEVICE_PERMISSION = "android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE"
    const val FOREGROUND_SERVICE_DATA_SYNC_PERMISSION = "android.permission.FOREGROUND_SERVICE_DATA_SYNC"
    const val FOREGROUND_SERVICE_HEALTH_PERMISSION = "android.permission.FOREGROUND_SERVICE_HEALTH"
    const val FOREGROUND_SERVICE_LOCATION_PERMISSION = "android.permission.FOREGROUND_SERVICE_LOCATION"
    const val FOREGROUND_SERVICE_MEDIA_PLAYBACK_PERMISSION = "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"
    const val FOREGROUND_SERVICE_SPECIAL_USE_PERMISSION = "android.permission.FOREGROUND_SERVICE_SPECIAL_USE"
    const val FOREGROUND_SERVICE_REMOTE_MESSAGING_PERMISSION = "android.permission.FOREGROUND_SERVICE_REMOTE_MESSAGING"
    const val FOREGROUND_SERVICE_MEDIA_PROJECTION_PERMISSION = "android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION"
    const val FOREGROUND_SERVICE_PHONE_CALL_PERMISSION = "android.permission.FOREGROUND_SERVICE_PHONE_CALL"
    const val FOREGROUND_SERVICE_SYSTEM_EXEMPTED_PERMISSION = "android.permission.FOREGROUND_SERVICE_SYSTEM_EXEMPTED"
    const val FOREGROUND_SERVICE_MICROPHONE_PERMISSION = "android.permission.FOREGROUND_SERVICE_MICROPHONE"


    //Service
    const val NOTIFICATION_SERVICE = "com.webengage.pushtemplates.services.NotificationService"
}
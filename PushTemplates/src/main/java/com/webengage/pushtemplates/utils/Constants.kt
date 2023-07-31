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
    const val COLLAPSED_MODE_IMAGE_URL = "we_cm_image"
    const val FONT_COLOR = "we_font_color"
    const val LAYOUT = "we_layout"
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
}
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


    //Types of Custom Push Templates
    const val PROGRESS_BAR = "bar"
    const val COUNTDOWN = "timer"

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
    const val SECOND = 1000
    const val MINUTE = 60* SECOND
    const val REMOTE_VIEW_MAX_SIZE = 4000000  //Size is in bytes
}
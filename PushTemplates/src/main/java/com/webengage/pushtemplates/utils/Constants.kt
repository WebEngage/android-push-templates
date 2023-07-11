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
    const val EXPANDED_MODE = "expanded_mode"
    const val COLLAPSED_MODE = "collapsed_mode"
    const val COLLAPSED_MODE_IMAGE_URL = "cm_image"
    const val FONT_COLOR = "font_color"
    const val LAYOUT = "layout"

    //Modes for Banner Styles
    const val HALF_BACKGROUND_MODE = "half_bg"
    const val FULL_BACKGROUND_MODE = "full_bg"
    const val DEFAULT_MODE = "default"

    //Types of Custom Push Templates
    const val PROGRESS_BAR = "bar"
    const val COUNTDOWN = "timer"
    const val BANNER = "banner"

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
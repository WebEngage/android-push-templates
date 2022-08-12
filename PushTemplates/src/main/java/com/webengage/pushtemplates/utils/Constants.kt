package com.webengage.pushtemplates.utils

object Constants {

    const val TIMER_FORMAT = "format"

    //KEYS for Push Template Customizations
    const val TIMER_DATE = "future_time"
    const val TEMPLATE_TYPE = "template_type"

    //Types of Custom Push Templates
    const val PROGRESS_BAR = "bar"
    const val COUNTDOWN = "timer"

    //Intent Actions
    const val DELETE_ACTION = "com.webengage.push.delete"
    const val PROGRESSBAR_ACTION = "com.webengage.push.PROGRESS_BAR"
    const val CLICK_ACTION = "com.webengage.push.click"

    //Internal Constants
    const val PAYLOAD = "payload"
    const val DISMISS_CTA = "Dismiss"
    const val WHEN_TIME = "whenTime"
    const val LOG_DISMISS = "logDismiss"
    const val AUTO_DISMISS = "autoDismiss"
    const val CTA_ID = "ctaID"
    const val SECOND = 1000
    const val MINUTE = 60* SECOND
}
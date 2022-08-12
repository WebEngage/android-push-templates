package com.webengage.pushtemplates.utils

object Constants {
    //Old Constants to be used for rerendered notifications
    const val TIMER_DESCRIPTION = "timer_message"
    const val TIMER_TITLE = "timer_title"
    const val TIMER_SUMMARY = "timer_summary"
    const val TIMER_RERENDER_ACTION = "TIMER_RERENDER"
    const val TIMER_TYPE = "timer_type"
    const val WHEN = "when"
    const val TIMER_FORMAT = "format"

    //KEYS for Push Template Customizations
    const val TIMER_DATE = "future_time"
    const val TYPE = "template_type"

    //Types of Custom Push Templates
    const val PROGRESS_BAR = "bar"
    const val COUNTDOWN = "timer"

    //Intent Actions
    const val DELETE_ACTION = "com.webengage.push.delete"
    const val PROGRESSBAR_ACTION = "com.webengage.push.PROGRESS_BAR"

    //Internal Constants
    const val PAYLOAD = "payload"
    const val DISMISS_CTA = "Dismiss"
    const val WHEN_TIME = "whenTime"
    const val LOG_DISMISS = "logDismiss"

    const val SECOND = 1000
    const val MINUTE = 60* SECOND
    const val HOUR = 60* MINUTE
}
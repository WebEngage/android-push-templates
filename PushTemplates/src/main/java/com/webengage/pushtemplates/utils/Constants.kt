package com.webengage.pushtemplates.utils

object Constants {
    //Old Constants to be used for rerendered notifications
    val TIMER_DESCRIPTION = "timer_message"
    val TIMER_TITLE = "timer_title"
    val TIMER_SUMMARY = "timer_summary"
    val TIMER_RERENDER_ACTION = "TIMER_RERENDER"
    val TIMER_TYPE = "timer_type"
    val WHEN = "when"
    val TIMER_FORMAT = "format"

    //KEYS for Push Template Customizations
    val TIMER_DATE = "timer_date"
    val TYPE = "type"

    //Types of Custom Push Templates
    val PROGRESS_BAR = "bar"
    val COUNTDOWN = "timer"

    //Intent Actions
    val DELETE_ACTION = "com.webengage.push.delete"
    val PROGRESSBAR_ACTION = "com.webengage.push.PROGRESS_BAR"

    //Internal Constants
    val PAYLOAD = "payload"
    val DISMISS_CTA = "Dismiss"
    val WHEN_TIME = "whenTime"
    val LOG_DISMISS = "logDismiss"

    val SECOND = 1000
    val MINUTE = 60* SECOND
    val HOUR = 60* MINUTE
}
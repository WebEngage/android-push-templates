package com.webengage.pushtemplates.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log

class Scheduler {

    fun scheduleAlarm(context: Context, timeMillis: Long, intent: PendingIntent){
        Log.d("PushTemplates","Scheduling Alarm")
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,timeMillis, intent)
    }

    fun cancelAlarm(context: Context, intent: PendingIntent){
        Log.d("PushTemplates","Cancelling Scheduled Alarm")
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmMgr.cancel(intent)
    }
}
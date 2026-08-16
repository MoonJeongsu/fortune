package com.dakbit.fortune

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import java.time.LocalDateTime
import java.time.ZoneId

object AlarmScheduler {
    const val ACTION_ALARM = "com.dakbit.fortune.ACTION_DAILY_FORTUNE_ALARM"
    private const val REQUEST_CODE = 1001

    fun scheduleNext(context: Context) {
        val settings = AlarmStore(context).load()
        if (!settings.enabled) {
            cancel(context)
            return
        }

        setBootReceiverEnabled(context, true)

        val triggerAt = nextTriggerMillis(settings.hour, settings.minute)
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = alarmPendingIntent(context)

        alarmManager.cancel(pendingIntent)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent,
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(alarmPendingIntent(context))
        setBootReceiverEnabled(context, false)
    }

    private fun alarmPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, FortuneAlarmReceiver::class.java).apply {
            action = ACTION_ALARM
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!target.isAfter(now)) {
            target = target.plusDays(1)
        }
        return target.atZone(zone).toInstant().toEpochMilli()
    }

    private fun setBootReceiverEnabled(context: Context, enabled: Boolean) {
        val component = ComponentName(context, BootReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            component,
            if (enabled) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            },
            PackageManager.DONT_KILL_APP,
        )
    }
}

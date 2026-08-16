package com.dakbit.fortune

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FortuneAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != AlarmScheduler.ACTION_ALARM) return

        try {
            NotificationHelper.showFortuneReminder(context.applicationContext)
        } finally {
            // 알림 표시가 실패해도 다음 날 예약은 유지한다.
            AlarmScheduler.scheduleNext(context.applicationContext)
        }
    }
}

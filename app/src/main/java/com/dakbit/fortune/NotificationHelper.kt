package com.dakbit.fortune

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {
    private const val CHANNEL_ID = "fortune_daily"
    private const val NOTIFICATION_ID = 2001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "오늘의 운세 알림",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "설정한 시간 무렵에 오늘의 운세를 알려드립니다."
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun ensureFcmChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            FcmConfig.CHANNEL_ID,
            "달빛운세 푸시 알림",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "매일 아침 달빛운세 안내 푸시 알림입니다."
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showFortuneReminder(context: Context): Boolean {
        ensureChannel(context)
        return notify(
            context = context,
            channelId = CHANNEL_ID,
            notificationId = NOTIFICATION_ID,
            title = "달빛 운세",
            body = "오늘의 운세가 도착했어요. 확인해 보세요.",
        )
    }

    fun showFcmFortune(context: Context, title: String, body: String): Boolean {
        ensureFcmChannel(context)
        return notify(
            context = context,
            channelId = FcmConfig.CHANNEL_ID,
            notificationId = FcmConfig.NOTIFICATION_ID,
            title = title,
            body = body,
        )
    }

    private fun notify(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        body: String,
    ): Boolean {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_TODAY, true)
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }
}
